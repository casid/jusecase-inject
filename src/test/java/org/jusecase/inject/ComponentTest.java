package org.jusecase.inject;

import org.junit.After;
import org.junit.Before;

import java.lang.reflect.ParameterizedType;

public class ComponentTest<Component> {
    private Component component;

    protected Component getComponent() {
        if (component == null) {
            component = createComponent();
        }
        return component;
    }

    @SuppressWarnings("unchecked")
    protected Component createComponent() {
        try {
            Class<Component> componentClass = (Class<Component>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            return componentClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance", e);
        }
    }

    @Before
    public void initInjector() {
        Injector.enableUnitTestMode();
    }

    @After
    public void resetInjector() {
        Injector.getInstance().reset();
    }

    public void givenDependency(Object dependency) {
        Injector.getInstance().add(dependency);
    }

    public void givenDependency(String name, Object dependency) {
        Injector.getInstance().add(name, dependency);
    }
}
