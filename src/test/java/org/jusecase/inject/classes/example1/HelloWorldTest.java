package org.jusecase.inject.classes.example1;

import org.junit.jupiter.api.Test;
import org.jusecase.inject.ComponentTest;

import static org.assertj.core.api.Assertions.assertThat;

class HelloWorldTest implements ComponentTest {
    @Test
    void test() {
        givenDependency("hello", "Hello");
        givenDependency("world", "World");

        HelloWorld helloWorld = new HelloWorld();

        assertThat(helloWorld.getHello()).isEqualTo("Hello");
        assertThat(helloWorld.getWorld()).isEqualTo("World");
    }
}