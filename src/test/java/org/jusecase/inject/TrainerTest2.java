package org.jusecase.inject;

import org.junit.jupiter.api.Test;
import org.jusecase.inject.classes.TestGateway;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainerTest2 implements ComponentTest {
    @Trainer Gateway trainer = new Gateway("hello");

    @Test
    void injection() {
        Service service = new Service();
        assertThat(service.gateway).isSameAs(trainer);
    }

    public static class Gateway {
        private final String source;

        public Gateway(String source) {
            this.source = source;
        }
    }

    @Component
    public static class Service {
        @Inject
        public Gateway gateway;
    }
}
