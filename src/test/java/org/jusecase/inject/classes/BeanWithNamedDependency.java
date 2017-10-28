package org.jusecase.inject.classes;

import org.jusecase.inject.Component;

import javax.inject.Inject;
import javax.inject.Named;

@Component
public class BeanWithNamedDependency {
    @Inject
    @Named(value = "db1")
    public Driver driver1;

    @Inject
    @Named(value = "db2")
    public Driver driver2;
}
