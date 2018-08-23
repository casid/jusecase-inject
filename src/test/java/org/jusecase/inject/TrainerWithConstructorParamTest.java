package org.jusecase.inject;

import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainerWithConstructorParamTest implements ComponentTest {
    @Trainer Gateway trainer = new Gateway("hello");

    @Test
    void injection() {
        Service service = new Service();
        assertThat(service.gateway).isSameAs(trainer);
        assertThat(trainer.source).isEqualTo("hello");
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
