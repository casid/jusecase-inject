package org.jusecase.inject;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public interface ComponentTest {

    @BeforeEach
    default void initInjector() {
        Injector.enableUnitTestMode();
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
