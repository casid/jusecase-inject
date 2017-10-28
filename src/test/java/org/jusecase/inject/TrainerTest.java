package org.jusecase.inject;

import org.junit.jupiter.api.Test;
import org.jusecase.inject.classes.TestDriver;
import org.jusecase.inject.classes.TestGateway;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainerTest implements ComponentTest {
    @Trainer TestDriver driver;

    @Test
    void injection() {
        TestGateway testGateway = new TestGateway();

        assertThat(testGateway.getDriver()).isSameAs(driver);
    }
}
