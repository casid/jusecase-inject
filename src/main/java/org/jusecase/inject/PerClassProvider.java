package org.jusecase.inject;

public interface PerClassProvider<T> {
    T get(Class<?> classToInject);
}
