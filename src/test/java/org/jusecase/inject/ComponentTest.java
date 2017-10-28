package org.jusecase.inject;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

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
            Trainer trainer = field.getAnnotation(Trainer.class);
            if (trainer != null) {
                try {
                    Object instance = field.getType().getConstructor().newInstance();
                    field.setAccessible(true);
                    field.set(this, instance);
                    if (trainer.named().isEmpty()) {
                        givenDependency(instance);
                    } else {
                        givenDependency(trainer.named(), instance);
                    }
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
