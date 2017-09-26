package org.jusecase.inject.classes;

import org.jusecase.inject.Component;

import javax.inject.Inject;

@Component
public class BeanWithFinalField {

    @SuppressWarnings("unused")
    @Inject
    private final String something = null;
}
