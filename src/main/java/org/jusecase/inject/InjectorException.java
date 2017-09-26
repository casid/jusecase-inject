package org.jusecase.inject;

public class InjectorException extends RuntimeException {
    public InjectorException(String message) {
        super(message);
    }

    public InjectorException(String message, Throwable cause) {
        super(message, cause);
    }

}
