package org.jusecase.inject.classes;

import org.jusecase.inject.Component;

import javax.inject.Inject;

@Component
public class Datasource {
    @Inject
    private String host;
    @Inject
    private String user;

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }
}
