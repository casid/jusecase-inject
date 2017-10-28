package org.jusecase.inject.classes;

import org.jusecase.inject.Component;

import javax.inject.Inject;

@Component
public class LoggerUser1 {
    @Inject
    public Logger logger;
}
