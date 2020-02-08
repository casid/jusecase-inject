package org.jusecase.inject.classes;

import org.jusecase.inject.Component;

import javax.inject.Inject;

@Component
public class HelloWorld {
    @Inject
    private String hello;
    @Inject
    private String world;

    public HelloWorld() {
        System.out.println(hello + " " + world);
    }

    public String getHello() {
        return hello;
    }

    public String getWorld() {
        return world;
    }
}
