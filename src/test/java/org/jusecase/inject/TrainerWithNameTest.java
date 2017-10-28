package org.jusecase.inject;

import org.junit.jupiter.api.Test;
import org.jusecase.inject.classes.BeanWithNamedDependency;
import org.jusecase.inject.classes.TestDriver;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainerWithNameTest implements ComponentTest {
    @Trainer(named = "db1") TestDriver driver1;
    @Trainer(named = "db2") TestDriver driver2;

    @Test
    void injection() {
        BeanWithNamedDependency beanWithNamedDependency = new BeanWithNamedDependency();

        assertThat(beanWithNamedDependency.driver1).isSameAs(driver1);
        assertThat(beanWithNamedDependency.driver2).isSameAs(driver2);
    }
}
