package org.jusecase.inject;

import org.junit.jupiter.api.Test;
import org.jusecase.inject.classes.TestDriver;
import org.jusecase.inject.classes.TestGateway;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

public class InjectDependenciesTest implements ComponentTest {
    @Inject
    TestDriver driver;

    @Test
    void injection() {
        TestGateway testGateway = new TestGateway();

        assertThat(testGateway.getDriver()).isSameAs(driver);
    }
}
