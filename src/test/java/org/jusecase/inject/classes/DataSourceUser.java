package org.jusecase.inject.classes;

import org.jusecase.inject.Component;

import javax.inject.Inject;

@Component
public class DataSourceUser {
    @Inject
    public DataSource dataSource;
}
