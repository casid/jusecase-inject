package org.jusecase.inject.classes;

import org.jusecase.inject.PerClassProvider;

public class LoggerProvider implements PerClassProvider<Logger> {
    @Override
    public Logger get(Class<?> classToInject) {
        return new Logger(classToInject);
    }
}
