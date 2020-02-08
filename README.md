# Inject

[![Build Status](https://travis-ci.org/casid/jusecase-inject.svg?branch=master)](https://travis-ci.org/casid/jusecase)
[![Coverage Status](https://coveralls.io/repos/github/casid/jusecase-inject/badge.svg?branch=master)](https://coveralls.io/github/casid/jusecase-inject?branch=master)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://raw.githubusercontent.com/casid/jusecase-inject/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/org.jusecase/inject.svg)](http://mvnrepository.com/artifact/org.jusecase/inject)

A fast and lightweight dependency injection framework for Java, with focus on simplicity, testability and ease of use. Requires Java 11, AspectJ and JUnit 5.

## Motivation
I've written this small lib to have faster TDD cycles in my personal <a href="https://mazebert.com/">Java backend project</a>. I'm running it in production for over two years now, without looking back at Spring/Guice/Dagger or doing it all by hand.

First off, you should **NOT** use this lib, if you:
- Can't or don't want to use AspectJ
- Have more than one application context in a process (enterprise java)
- Want to have circular dependencies (well, this might actually be a benefit)

Here is why you may want to check it out:
- Create components naturally with `new Foo()`, and injection happens automatically
- First class support for unit testing
- Prepared for parallel unit test execution
- No static, hard to test loggers
- Small footprint, no dependencies except AspectJ (the JAR is about 14KB)

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
    <version>0.3.1</version>
</dependency>
<dependency>
    <groupId>org.jusecase</groupId>
    <artifactId>inject</artifactId>
    <version>0.3.1</version>
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

To see if everything works as expected, we can create a quick hello world class.

> You find the code for this example in the test source package [org.jusecase.inject.classes.example1](src/test/java/org/jusecase/inject/classes/example1)

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

## Trainers aka Custom Mocks

Let's have a look at a more interesting case than hello world. We want to write a small registration service.

> You find the code for this example in the test source package [org.jusecase.inject.classes.example2](src/test/java/org/jusecase/inject/classes/example2)

```java
@Component
public class RegisterNewsletter {
    @Inject
    private NewsletterGateway newsletterGateway;
    @Inject
    private EmailValidator emailValidator;

    public void register(String email) {
        emailValidator.validate(email);
        try {
            newsletterGateway.addRecipient(email);
        } catch (DuplicateKeyException e) {
            throw new BadRequest("This email address is already registered.");
        }
    }
}
```

We also have a entity gateway for newsletter recipients (only emails for the sake of this example).
```java
public interface NewsletterGateway {
    void addRecipient(String email) throws DuplicateKeyException;
    boolean isRecipient(String email);
}
```

It's an interface, because in our unit test we don't want to use the real thing. Let's write a custom mock for it.
```java
public class NewsletterGatewayTrainer implements NewsletterGateway {
    private final Set<String> emails = new HashSet<>();
    
    @Override
    public void addRecipient(String email) throws DuplicateKeyException {
        if (isRecipient(email)) {
            throw new DuplicateKeyException("E-Mail already registered.");
        }
        emails.add(email);
    }
    
    @Override
    public boolean isRecipient(String email) {
        return emails.contains(email);
    }
}
```

Let's have a look how we can test this class. We really would like to tell Inject that we want to use the `NewsletterGatewayTrainer` as implementation for `NewsletterGateway`. We can do this by hand, like we did it in the Hello World example:

```java
class RegisterNewsletterTest implements ComponentTest {

    RegisterNewsletter registerNewsletter;

    @BeforeEach
    void setUp() {
        givenDependency(new NewsletterGatewayTrainer());
        registerNewsletter = new RegisterNewsletter();
    }
}
```

Usually you want to do stuff with your custom mocks in your unit tests. So there is a shorthand, to inject custom mocks in unit tests, by using the `@Trainer` annotation:

```java
class RegisterNewsletterTest implements ComponentTest {

    @Trainer // ComponentTest will instantiate this field and provide it as a dependency.
    NewsletterGatewayTrainer newsletterGatewayTrainer;

    RegisterNewsletter registerNewsletter;

    @BeforeEach
    void setUp() {
        registerNewsletter = new RegisterNewsletter();
    }
}
```

There is another dependency in this class, the `EmailValidator`. This is something we don't want to mock, thus we can simply provide it to the test by saying `givenDependency(new EmailValidator());`. The final test then may look something like this:

```java
class RegisterNewsletterTest implements ComponentTest {

    @Trainer
    NewsletterGatewayTrainer newsletterGatewayTrainer;

    RegisterNewsletter registerNewsletter;

    @BeforeEach
    void setUp() {
        givenDependency(new EmailValidator());
        registerNewsletter = new RegisterNewsletter();
    }

    @Test
    void success() {
        whenEmailIsRegistered("test@example.com");
        assertThat(newsletterGatewayTrainer.isRecipient("test@example.com")).isTrue();
    }

    @Test
    void alreadyRegistered() {
        newsletterGatewayTrainer.addRecipient("test@example.com");
        Throwable throwable = catchThrowable(() -> whenEmailIsRegistered("test@example.com"));
        assertThat(throwable).isInstanceOf(BadRequest.class).hasMessage("This email address is already registered.");
    }

    @Test
    void emptyMail() {
        Throwable throwable = catchThrowable(() -> whenEmailIsRegistered(""));
        assertThat(throwable).isInstanceOf(BadRequest.class).hasMessage("Please enter an email address");
    }

    @Test
    void nullEmail() {
        Throwable throwable = catchThrowable(() -> whenEmailIsRegistered(null));
        assertThat(throwable).isInstanceOf(BadRequest.class).hasMessage("Please enter an email address");
    }

    @Test
    void invalidEmail() {
        Throwable throwable = catchThrowable(() -> whenEmailIsRegistered("email"));
        assertThat(throwable).isInstanceOf(BadRequest.class).hasMessage("email is not a valid email address");
    }

    private void whenEmailIsRegistered(String email) {
        registerNewsletter.register(email);
    }
}
```

## Nicer logging

In order to obtain a logger one usually does something like this:
```java
private static final Logger LOGGER = Logger.getLogger(MyService.class.getName());
```

With Inject, you can register per class providers. They will provide a new instance for every class they are injected into. This is exactly what we need to inject loggers.

```java
public class LoggerProvider implements PerClassProvider<Logger> {
    @Override
    public Logger get(Class<?> classToInject) {
        return Logger.getLogger(classToInject.getName());
    }
}
```

You need to register the provider like this, at the place you configure your production dependencies:

```java
injector.addProvider(new LoggerProvider());
```

In all your components, you can now simply inject a logger that will generate logs for this class:

```java
@Component
public class MyService {
    @Inject
    private Logger logger;
    
    public MyService() {
        logger.info("This service got created.");
    }
}
```

For your tests you can now write a `LoggerTrainer`, that does not log at all. This has the benefit, that your CI build logs look very clean and are not cluttered with exceptions. And finally, you can use it to verify that a certain important message was logged. For instance, your test could look like this:

```java
public class MyServiceTest {
    @Trainer
    LoggerTrainer loggerTrainer;
    
    @Test
    void logging() {
        new MyService();
        loggerTrainer.thenInfoWasLogged("This service got created.");
    }
}
```
