package org.jusecase.inject;

import org.junit.jupiter.api.Test;
import org.jusecase.inject.classes.TestGateway;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainerDerivedTest extends TrainerTest {
    @Trainer TestGateway gateway;

    @Test
    void injection() {
        assertThat(gateway.getDriver()).isSameAs(driver);
    }
}
