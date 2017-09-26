package org.jusecase.inject;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class Injector {
    private static final Injector instance = new Injector();
    private static boolean unitTestMode;

    private Map<Class<?>, Object> implementations = new HashMap<>();
    private Map<Class<?>, Map<String, Object>> implementationsByName = new HashMap<>();
    private Map<Class<?>, List<Field>> injectableFields = new HashMap<>();

    public static Injector getInstance() {
        if (unitTestMode) {
            return UnitTestInstanceHolder.unitTestInstance.get();
        }
        return instance;
    }

    public void add(Object implementation) {
        implementations.put(implementation.getClass(), implementation);

        for (Class<?> interfaceClass : implementation.getClass().getInterfaces()) {
            implementations.put(interfaceClass, implementation);
        }
    }

    public void add(String name, Object implementation) {
        Map<String, Object> implementationByName = implementationsByName.computeIfAbsent(implementation.getClass(), (key) -> new HashMap<>());
        implementationByName.put(name, implementation);
    }

    public void inject(Object instance, Class declaringType) {
        for(Field field : getInjectableFields(declaringType)) {
            Object implementation = resolveImplementation(field);
            if (implementation == null) {
                throw new InjectorException(createInjectErrorMessage("No implementation found.", declaringType, field));
            }

            if (Modifier.isFinal(field.getModifiers())) {
                throw new InjectorException(createInjectErrorMessage("@Inject field must not be final.", declaringType, field));
            }

            try {
                field.setAccessible(true);
                field.set(instance, implementation);
            } catch (IllegalAccessException e) {
                throw new InjectorException(createInjectErrorMessage("Failed to access field.", declaringType, field), e);
            }
        }
    }

    private String createInjectErrorMessage(String reason, Class type, Field field) {
        return reason + " Failed to inject " + field.getType().getName() + " " + field.getName() + " in " + type.getName();
    }

    private Object resolveImplementation(Field field) {
        Map<String, Object> implementationByName = implementationsByName.get(field.getType());
        if (implementationByName != null) {
            Object implementation = implementationByName.get(field.getName());
            if (implementation != null) {
                return implementation;
            }
        }

        return implementations.get(field.getType());
    }

    private List<Field> getInjectableFields(Class<?> type) {
        List<Field> fields = injectableFields.get(type);
        if (fields == null) {
            fields = calculateInjectableFields(type);
            injectableFields.put(type, fields);
        }
        return fields;
    }

    private List<Field> calculateInjectableFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                fields.add(field);
            }
        }
        return fields;
    }

    public void reset() {
        implementations.clear();
    }

    public static void enableUnitTestMode() {
        unitTestMode = true;
    }

    private static class UnitTestInstanceHolder {
        static final ThreadLocal<Injector> unitTestInstance = ThreadLocal.withInitial(Injector::new);
    }
}
