package org.jusecase.inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jusecase.inject.classes.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class InjectorTest implements ComponentTest {

    @BeforeEach
    void setUp() {
        // We want to test the live behavior in this test
        Injector.getInstance().setAllowMissingDependencies(false);
    }

    @Test
    void named() {
        givenDependency("host", "localhost");
        givenDependency("user", "root");

        DataSource dataSource = new DataSource();

        assertThat(dataSource.getHostName()).isEqualTo("localhost");
        assertThat(dataSource.getUserName()).isEqualTo("root");
    }

    @Test
    void named_tooMany() {
        givenDependency("host", "localhost");
        givenDependency("user1", "root1");
        givenDependency("user2", "root2");
        givenDependency("user3", "root3");

        Throwable throwable = catchThrowable(DataSource::new);

        assertThat(throwable).isInstanceOf(InjectorException.class).hasMessage("No dependency named user, got [host, user1, user2, user3]. Failed to inject java.lang.String userName in org.jusecase.inject.classes.DataSource");
    }

    @Test
    void named_none() {
        Throwable throwable = catchThrowable(DataSource::new);
        assertThat(throwable).isInstanceOf(InjectorException.class).hasMessage("No dependency named host. Failed to inject java.lang.String hostName in org.jusecase.inject.classes.DataSource");
    }

    @Test
    void service() {
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
    void serviceSubclass() {
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
    void provider() {
        Injector.getInstance().addProvider(DataSourceProvider.class);
        givenDependency("host", "localhost");
        givenDependency("user", "root");
        DataSourceUser dataSourceUser1 = new DataSourceUser();
        DataSourceUser dataSourceUser2 = new DataSourceUser();

        assertThat(dataSourceUser1.dataSource).isNotNull();
        assertThat(dataSourceUser1.dataSource).isNotSameAs(dataSourceUser2.dataSource);
    }

    @Test
    void provider_singleInstance() {
        givenDependency("host", "localhost");
        givenDependency("user", "root");
        Injector.getInstance().addProviderForSingleInstance(DataSourceProvider.class);
        DataSourceUser dataSourceUser1 = new DataSourceUser();
        DataSourceUser dataSourceUser2 = new DataSourceUser();

        assertThat(dataSourceUser1.dataSource).isNotNull();
        assertThat(dataSourceUser1.dataSource).isSameAs(dataSourceUser2.dataSource);
    }

    @Test
    void providerLookup() {
        DataSourceProvider provider = new DataSourceProvider();
        Injector.getInstance().addProvider(provider);

        DataSourceProvider resolvedProvider = Injector.getInstance().resolve(DataSourceProvider.class);

        assertThat(resolvedProvider).isSameAs(provider);
    }

    @Test
    void bean_noServiceToInject() {
        Throwable throwable = catchThrowable(() -> new Formatter("foo", "bar"));
        assertThat(throwable)
                .isInstanceOf(InjectorException.class)
                .hasMessage("No implementation found. Failed to inject org.jusecase.inject.classes.TestService service in org.jusecase.inject.classes.Formatter");
    }

    @Test
    void bean_finalField() {
        givenDependency("some string");
        Throwable throwable = catchThrowable(BeanWithFinalField::new);

        assertThat(throwable)
                .isInstanceOf(InjectorException.class)
                .hasMessage("@Inject field must not be final. Failed to inject java.lang.String something in org.jusecase.inject.classes.BeanWithFinalField");
    }

    @Test
    void bean() {
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

    @Test
    void addBeanWithName() {
        givenDependency(new TestDriverDb1());
        givenDependency(new TestDriverDb2());
        BeanWithNamedDependency bean = new BeanWithNamedDependency();

        assertThat(bean.driver1).isNotSameAs(bean.driver2);
    }

    @Test
    void addBeanWithNameAsClass() {
        givenDependency(TestDriverDb1.class);
        givenDependency(TestDriverDb2.class);

        BeanWithNamedDependency bean = new BeanWithNamedDependency();

        assertThat(bean.driver1).isNotNull();
        assertThat(bean.driver2).isNotNull();
        assertThat(bean.driver1).isNotSameAs(bean.driver2);
    }


    @Test
    void logger() {
        Injector.getInstance().addProvider(new LoggerProvider());
        LoggerUser1 loggerUser1 = new LoggerUser1();
        LoggerUser2 loggerUser2 = new LoggerUser2();

        assertThat(loggerUser1.logger.getClazz()).isEqualTo(LoggerUser1.class);
        assertThat(loggerUser2.logger.getClazz()).isEqualTo(LoggerUser2.class);
    }

    @Test
    void constructorInjection() {
        givenDependency(new TestDriver());
        Injector.getInstance().add(BeanWithConstructorInjection.class);
    }

    @Test
    void constructorInjection_missingDependencies() {
        Throwable throwable = catchThrowable(() -> Injector.getInstance().add(BeanWithConstructorInjection.class));
        assertThat(throwable).isInstanceOf(InjectorException.class).hasMessage("No implementation found. Failed to inject org.jusecase.inject.classes.Driver arg0 in org.jusecase.inject.classes.BeanWithConstructorInjection");
    }

    @Test
    void subclass() {
        givenDependency(new TestDriverSubclass());
        TestGateway testGateway = new TestGateway();
        assertThat(testGateway.getDriver()).isInstanceOf(TestDriverSubclass.class);
    }

    @Test
    void subclassMock_noInjection() {
        DataSourceMock dataSourceMock = new DataSourceMock();
        assertThat(dataSourceMock.getHostName()).isNull();
        assertThat(dataSourceMock.getUserName()).isNull();
    }
}