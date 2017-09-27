package org.jusecase.inject;

import org.junit.jupiter.api.Test;
import org.jusecase.inject.classes.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class InjectorTest implements ComponentTest {

    @Test
    public void fieldNames() {
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
                .hasMessage("No implementation found. Failed to inject org.jusecase.inject.classes.TestService service in org.jusecase.inject.classes.Formatter");
    }

    @Test
    public void bean_finalField() {
        givenDependency("some string");
        Throwable throwable = catchThrowable(BeanWithFinalField::new);

        assertThat(throwable)
                .isInstanceOf(InjectorException.class)
                .hasMessage("@Inject field must not be final. Failed to inject java.lang.String something in org.jusecase.inject.classes.BeanWithFinalField");
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