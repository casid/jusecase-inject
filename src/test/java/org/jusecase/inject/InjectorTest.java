package org.jusecase.inject;

import org.junit.Before;
import org.junit.Test;
import org.jusecase.inject.classes.*;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class InjectorTest extends ComponentTest<Injector> {

    @Test
    public void sameTypeTwice() {
        givenDependency("host", "localhost");
        givenDependency("user", "root");

        Datasource datasource = new Datasource();

        assertThat(datasource.getHost()).isEqualTo("localhost");
        assertThat(datasource.getUser()).isEqualTo("root");
    }

    @Test
    public void service() {
        TestDriver testDriver = new TestDriver();
        givenDependency(testDriver);

        TestGateway testGateway = new TestGateway();
        givenDependency(testGateway);

        TestService testService = new TestService();

        assertThat(testService.getGateway()).isSameAs(testGateway);
        assertThat(testService.getGateway2()).isSameAs(testGateway);
        assertThat(testService.getGateway().getDriver()).isSameAs(testDriver);
    }

    @Test
    public void serviceSubclass() {
        TestDriver testDriver = new TestDriver();
        givenDependency(testDriver);

        TestGateway testGateway = new TestGateway();
        givenDependency(testGateway);

        TestServiceSubclass testService = new TestServiceSubclass();

        assertThat(testService.getGateway()).isSameAs(testGateway);
        assertThat(testService.getGateway2()).isSameAs(testGateway);
        assertThat(testService.getGateway().getDriver()).isSameAs(testDriver);
        assertThat(testService.getDriver()).isSameAs(testDriver);
    }

    @Test
    public void bean_noServiceToInject() {
        Throwable throwable = catchThrowable(() -> new Formatter("foo", "bar"));
        assertThat(throwable)
                .isInstanceOf(InjectorException.class)
                .hasMessage("Failed to inject org.jusecase.inject.classes.TestService into org.jusecase.inject.classes.Formatter");
    }

    @Test
    public void bean() {
        TestDriver testDriver = new TestDriver();
        givenDependency(testDriver);

        TestGateway testGateway = new TestGateway();
        givenDependency(testGateway);

        TestService testService = new TestService();
        givenDependency(testService);

        Formatter formatter = new Formatter("foo", "bar");
        String result = formatter.getResult();

        assertThat(result).isEqualTo("service said: foobar");
    }
}