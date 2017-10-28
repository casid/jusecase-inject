package org.jusecase.inject.classes;

import org.jusecase.inject.Component;

import javax.inject.Inject;
import javax.inject.Named;

@Component
public class DataSource {
    @Inject
    @Named(value = "host")
    private String hostName;
    @Inject
    @Named(value = "user")
    private String userName;

    public String getHostName() {
        return hostName;
    }

    public String getUserName() {
        return userName;
    }
}
