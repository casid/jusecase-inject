package org.jusecase.inject.classes.example1;

import org.jusecase.inject.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;

@Component
public class HelloWorld {
    @Inject
    @Named("hello")
    private String hello;
    @Inject
    @Named("world")
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
