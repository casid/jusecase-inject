package org.jusecase.inject.classes;

import org.jusecase.inject.Component;

import javax.inject.Inject;

@Component
public class Formatter {
    @Inject
    private TestService service;

    private final String a;
    private final String b;

    public Formatter(String a, String b) {
        this.a = a;
        this.b = b;
    }

    public String getResult() {
        return "service said: " + service.add(a, b);
    }
}
