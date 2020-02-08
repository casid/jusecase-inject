# JUsecase Inject

[![Build Status](https://travis-ci.org/casid/jusecase-inject.svg?branch=master)](https://travis-ci.org/casid/jusecase)
[![Coverage Status](https://coveralls.io/repos/github/casid/jusecase-inject/badge.svg?branch=master)](https://coveralls.io/github/casid/jusecase-inject?branch=master)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://raw.githubusercontent.com/casid/jusecase-inject/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/org.jusecase/inject.svg)](http://mvnrepository.com/artifact/org.jusecase/inject)

A fast and lightweight dependency injection framework for Java, with focus on simplicity, testability and ease of use. Requires Java 11, AspectJ and JUnit 5.

## Motivation
I've written this small lib to have faster TDD cycles in my personal <a href="https://mazebert.com/">Java backend project</a>. I'm running it in production for over two years now, without looking back at Spring/Guice/Dagger or doing it all by hand.

First of, you should NOT use this lib, if you:
- Can't or don't want to use AspectJ
- Have more than one application context
- Want to have circular dependencies (well, this might actually be a benefit)

Here is why you may want to check it out:
- Create components naturally with new Foo(), and injection happens automatically
- First class, built in support for unit testing
- Prepared for parallel unit test execution
- No static, hard to test loggers

But see for yourself. Here's a small component:
```java
import org.jusecase.inject.Component;
import javax.inject.Inject;

@Component
public class CoffeeMachine {
  @Inject
  private BeansRepository beansRepository;
  @Inject
  private WaterRepository waterRepository;
}
```
At some place at startup, we init the dependencies:
```java
Injector.getInstance().add(new BeansRepository());
Injector.getInstance().add(new WaterRepository());
```

Here is how you create the CoffeeMachine:
```java
CoffeeMachine coffeeMachine = new CoffeeMachine();
```
All dependencies are injected. If dependencies are missing you will get an exception telling you what's exactly missing.

## Getting started

JUsecase Inject is available on maven central repository:
```xml
<dependency>
    <groupId>org.jusecase</groupId>
    <artifactId>inject</artifactId>
    <version>0.3.0</version>
</dependency>
<dependency>
    <groupId>org.jusecase</groupId>
    <artifactId>inject</artifactId>
    <version>0.3.0</version>
    <type>test-jar</type>
    <scope>test</scope>
</dependency>
```

You should add JUnit 5 testing dependencies if you haven't already.

And AspectJ (for Java 11 we unfortunately can't use the official plugin):
```xml
<plugin>
    <groupId>com.nickwongdev</groupId>
    <artifactId>aspectj-maven-plugin</artifactId>
    <version>1.12.1</version>
    <configuration>
        <complianceLevel>${maven.compiler.release}</complianceLevel>
        <source>${maven.compiler.source}</source>
        <target>${maven.compiler.target}</target>
        <encoding>UTF-8 </encoding>
        <aspectLibraries>
            <aspectLibrary>
                <groupId>org.jusecase</groupId>
                <artifactId>inject</artifactId>
            </aspectLibrary>
        </aspectLibraries>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>compile</goal>
                <goal>test-compile</goal>
            </goals>
        </execution>
    </executions>
    <dependencies>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjtools</artifactId>
            <version>1.9.4</version>
        </dependency>
    </dependencies>
</plugin>
```

To see if everything works as expected, we can create a quick hello world class:
```java
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
}
```

Let's create a unit test to see if everything is working:
```java
import org.junit.jupiter.api.Test;

class HelloWorldTest {
    @Test
    void test() {
        new HelloWorld();
    }
}
```

Run the test. It fails with this error message: 
`org.jusecase.inject.InjectorException: No implementation found. Failed to inject java.lang.String hello in org.jusecase.inject.classes.HelloWorld`

Well, that makes sense. We haven't told Inject, what dependencies to use. There is a `ComponentTest` interface that helps with writing unit tests. By implementing it, we get some BDD style default methods, to setup test dependencies:
```java
import org.junit.jupiter.api.Test;
import org.jusecase.inject.ComponentTest;

class HelloWorldTest implements ComponentTest {
    @Test
    void test() {
        givenDependency("hello", "Hello");
        givenDependency("world", "World");
        new HelloWorld();
    }
}
```

You should now see this output: `"Hello World"`

This works, because the fields are named like the names of our dependencies. If you do not want to rely on field names you can use the `@Named` annotation:
```java
import org.jusecase.inject.Component;
import javax.inject.Inject;
import javax.inject.Named;

@Component
public class HelloWorld {
    @Inject
    @Named(value = "hello")
    private String _hello;
    @Inject
    @Named(value = "world")
    private String _world;

    public HelloWorld() {
        System.out.println(_hello + " " + _world);
    }
}
```
