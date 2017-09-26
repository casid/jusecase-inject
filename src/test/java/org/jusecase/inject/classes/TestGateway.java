package org.jusecase.inject.classes;

import org.jusecase.inject.Component;

import javax.inject.Inject;

@Component
public class TestGateway implements Gateway {
    @Inject
    private Driver driver;

    @Override
    public Driver getDriver() {
        return driver;
    }
}
