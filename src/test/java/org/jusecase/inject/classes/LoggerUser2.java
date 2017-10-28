package org.jusecase.inject.classes;

import org.jusecase.inject.Component;

import javax.inject.Inject;

@Component
public class LoggerUser2 {
    @Inject
    public Logger logger;
}
