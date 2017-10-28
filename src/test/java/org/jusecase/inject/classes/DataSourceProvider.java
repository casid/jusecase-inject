package org.jusecase.inject.classes;

import javax.inject.Provider;

public class DataSourceProvider implements Provider<DataSource> {
    @Override
    public DataSource get() {
        return new DataSource();
    }
}
