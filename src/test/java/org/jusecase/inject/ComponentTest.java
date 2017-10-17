package org.jusecase.inject;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import javax.inject.Inject;
import java.lang.reflect.Field;

public interface ComponentTest {

    @BeforeEach
    default void initInjector() {
        Injector.enableUnitTestMode();
        injectFieldsDeclaredInTestClassAndSuperClasses(getClass());
    }

    default void injectFieldsDeclaredInTestClassAndSuperClasses(Class<?> testClass) {
        if (testClass != Object.class) {
            injectFieldsDeclaredInTestClassAndSuperClasses(testClass.getSuperclass());
        }
        injectFieldsDeclaredInTestClass(testClass);
    }

    default void injectFieldsDeclaredInTestClass(Class<?> testClass) {
        for (Field field : testClass.getDeclaredFields()) {
            if (field.getAnnotation(Inject.class) != null) {
                try {
                    Object instance = field.getType().newInstance();
                    field.set(this, instance);
                    givenDependency(instance);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to inject field " + field.getName() + " in test class " + testClass.getSimpleName(), e);
                }
            }
        }
    }

    @AfterEach
    default void resetInjector() {
        Injector.getInstance().reset();
    }

    default void givenDependency(Object dependency) {
        Injector.getInstance().add(dependency);
    }

    default void givenDependency(String name, Object dependency) {
        Injector.getInstance().add(name, dependency);
    }
}
