package org.jusecase.inject;

import net.jodah.typetools.TypeResolver;
import org.junit.After;
import org.junit.Before;

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
            return (Component) TypeResolver.resolveRawArguments(ComponentTest.class, getClass())[0].newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance", e);
        }
    }

    @Before
    public void initInjector() {
        // TODO bring injector to test aka "thread local" mode
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
