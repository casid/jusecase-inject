package org.jusecase.inject.classes;

import org.jusecase.inject.Component;

import javax.inject.Inject;

@Component
public class BeanWithConstructorInjection {
    private final Driver driver;

    @Inject
    public BeanWithConstructorInjection(Driver driver) {
        this.driver = driver;
    }

    public Driver getDriver() {
        return driver;
    }
}
