package org.jusecase.inject;

import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Named;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainerWithConstructorParamTest implements ComponentTest {
    @Trainer private Gateway trainer = new Gateway("hello");
    @Trainer(named = "name") private Gateway namedTrainer = new Gateway("hello named");

    @Test
    void injection() {
        Service service = new Service();
        assertThat(service.gateway).isSameAs(trainer);
        assertThat(trainer.source).isEqualTo("hello");

        assertThat(service.namedGateway).isSameAs(namedTrainer);
        assertThat(namedTrainer.source).isEqualTo("hello named");
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

        @Inject
        @Named("name")
        public Gateway namedGateway;
    }
}
