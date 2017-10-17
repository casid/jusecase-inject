package org.jusecase.inject;

import org.junit.jupiter.api.Test;
import org.jusecase.inject.classes.TestDriver;
import org.jusecase.inject.classes.TestGateway;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

public class InjectDependenciesDerivedTest extends InjectDependenciesTest {
    @Inject
    TestGateway gateway;

    @Test
    void injection() {
        assertThat(gateway.getDriver()).isSameAs(driver);
    }
}
