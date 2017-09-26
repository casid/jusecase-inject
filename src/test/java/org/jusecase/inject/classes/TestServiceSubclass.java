package org.jusecase.inject.classes;

import org.jusecase.inject.Component;

import javax.inject.Inject;

@Component
public class TestServiceSubclass extends TestService {
    @Inject
    private Driver driver;

    public Driver getDriver() {
        return driver;
    }
}
