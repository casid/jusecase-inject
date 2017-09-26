package org.jusecase.inject.classes;

import org.jusecase.inject.Component;

import javax.inject.Inject;


@Component
public class TestService {

    @Inject
    private Gateway gateway;
    @Inject
    private Gateway gateway2;

    public TestService() {
        if (gateway == null || gateway2 == null) {
            throw new RuntimeException("Dependencies must be injected before constructor execution");
        }
    }

    public Gateway getGateway() {
        return gateway;
    }

    public Gateway getGateway2() {
        return gateway;
    }

    public String add(String a, String b) {
        return a + b;
    }
}
