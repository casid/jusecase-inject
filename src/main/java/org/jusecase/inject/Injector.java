package org.jusecase.inject;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class Injector {
    private static final Injector instance = new Injector();
    private static boolean unitTestMode;

    private final Map<Class<?>, Object> implementations = new HashMap<>();
    private final Map<Class<?>, Map<String, Object>> implementationsByName = new HashMap<>();
    private final Map<Class<?>, List<Field>> injectableFields = new HashMap<>();
    private boolean resolveUnitTestDependencies;

    public static Injector getInstance() {
        if (unitTestMode) {
            return UnitTestInstanceHolder.unitTestInstance.get();
        }
        return instance;
    }

    public void add(Object implementation) {
        add(implementation.getClass(), implementation);
    }

    public void add(String name, Object implementation) {
        add(name, implementation.getClass(), implementation);
    }

    public <T> void add(Class<T> implementationClass) {
        add(implementationClass, newInstance(implementationClass));
    }

    public <T> void add(String name, Class<T> implementationClass) {
        add(name, implementationClass, newInstance(implementationClass));
    }

    public <T extends Provider<?>> void addProvider(Class<T> providerClass) {
        addProvider((Provider<?>) newInstance(providerClass));
    }

    public <T extends Provider<?>> void addProviderForSingleInstance(Class<T> providerClass) {
        addProviderForSingleInstance((Provider<?>) newInstance(providerClass));
    }

    @SuppressWarnings("unchecked")
    private <T> T newInstance(Class<T> clazz) {

        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                Object[] arguments = new Object[constructor.getParameterCount()];
                Parameter[] parameters = constructor.getParameters();
                for (int i = 0; i < parameters.length; ++i) {
                    arguments[i] = resolveImplementation(parameters[i].getType(), clazz);
                    if (arguments[i] == null) {
                        throw new InjectorException(createInjectErrorMessage("No implementation found.", clazz, parameters[i]));
                    }
                }
                try {
                    return (T)constructor.newInstance(arguments);
                } catch (Throwable e) {
                    throw new InjectorException("Failed to create instance of " + clazz , e);
                }
            }
        }

        try {
            return clazz.getConstructor().newInstance();
        } catch (Throwable e) {
            throw new InjectorException("Failed to create instance of " + clazz , e);
        }
    }

    public <T> void addProvider(Provider<T> provider) {
        Class<?> providedClass = GenericTypeResolver.resolve(Provider.class, provider.getClass(), 0);
        add(providedClass, provider);
        add(provider.getClass(), provider);
    }

    public <T> void addProviderForSingleInstance(Provider<T> provider) {
        Class<?> providedClass = GenericTypeResolver.resolve(Provider.class, provider.getClass(), 0);
        add(providedClass, provider.get());
        add(provider.getClass(), provider);
    }

    public <T> void addProvider(PerClassProvider<T> provider) {
        Class<?> providedClass = GenericTypeResolver.resolve(PerClassProvider.class, provider.getClass(), 0);
        add(providedClass, provider);
        add(provider.getClass(), provider);
    }

    private void add(Class<?> clazz, Object implementationOrProvider) {
        if (clazz.isAnnotationPresent((Named.class))) {
            Named named = clazz.getAnnotation(Named.class);
            if (named.value().isEmpty()) {
                add(clazz, implementationOrProvider, implementations::put);
            } else {
                add(named.value(), clazz);
            }
        } else {
            add(clazz, implementationOrProvider, implementations::put);
        }
    }

    private void add(String name, Class<?> clazz, Object implementationOrProvider) {
        add(clazz, implementationOrProvider, (c, i) -> {
            Map<String, Object> implementationByName = implementationsByName.computeIfAbsent(c, (key) -> new HashMap<>());
            implementationByName.put(name, implementationOrProvider);
        });
    }

    private void add(Class<?> clazz, Object implementationOrProvider, BiConsumer<Class<?>, Object> consumer) {
        consumer.accept(clazz, implementationOrProvider);

        for (Class<?> interfaceClass : clazz.getInterfaces()) {
            consumer.accept(interfaceClass, implementationOrProvider);
        }

        if (clazz.getSuperclass() != null) {
            add(clazz.getSuperclass(), implementationOrProvider, consumer);
        }
    }

    public <T> T resolve(Class<T> clazz) {
        return resolveImplementation(clazz, null);
    }

    public void inject(Object instance, Class<?> declaringType) {
        for (Field field : getInjectableFields(declaringType)) {
            Object implementation = resolveImplementation(field, declaringType);
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

    public Stream<Object> getImplementations() {
        return implementations.values().stream().distinct();
    }

    private String createInjectErrorMessage(String reason, Class<?> type, Field field) {
        return reason + " Failed to inject " + field.getType().getName() + " " + field.getName() + " in " + type.getName();
    }

    private String createInjectErrorMessage(String reason, Class<?> type, Parameter parameter) {
        return reason + " Failed to inject " + parameter.getType().getName() + " " + parameter.getName() + " in " + type.getName();
    }

    @SuppressWarnings("unchecked")
    private <T> T resolveImplementation(Class<T> clazz, Class<?> toBeInjectedIn) {
        return (T)resolveImplementation(implementations.get(clazz), clazz, toBeInjectedIn);
    }

    private Object resolveImplementation(Field field, Class<?> toBeInjectedIn) {
        if (field.isAnnotationPresent(Named.class)) {
            String name = field.getAnnotation(Named.class).value();
            return resolveImplementationByName(field, toBeInjectedIn, name, true);
        }

        Object implementation = resolveImplementation(field.getType(), toBeInjectedIn);
        if (implementation == null && unitTestMode && resolveUnitTestDependencies) {
            implementation = resolveImplementationForUnitTest(field, toBeInjectedIn);
        }

        return implementation;
    }

    private Object resolveImplementationForUnitTest(Field field, Class<?> toBeInjectedIn) {
        if (field.getType().isInterface()) {
            return resolveImplementationForInterfaceInUnitTest(field, toBeInjectedIn);
        } else {
            return resolveImplementationForClassInUnitTest(field, toBeInjectedIn);
        }
    }

    private Object resolveImplementationForInterfaceInUnitTest(Field field, Class<?> toBeInjectedIn) {
        try {
            Class<?> trainerClass = getClass().getClassLoader().loadClass(field.getType().getName() + "Trainer");
            try {
                Object implementation = trainerClass.getConstructor().newInstance();
                add(implementation);
                return implementation;
            } catch (Exception e) {
                throw new InjectorException(createInjectErrorMessage("Failed to instantiate trainer " + trainerClass.getName(), toBeInjectedIn, field));
            }
        } catch (ClassNotFoundException e) {
            throw new InjectorException(createInjectErrorMessage("No trainer found for interface " + field.getType().getName(), toBeInjectedIn, field));
        }
    }

    private Object resolveImplementationForClassInUnitTest(Field field, Class<?> toBeInjectedIn) {
        try {
            Object implementation = field.getType().getConstructor().newInstance();
            add(implementation);
            return implementation;
        } catch (Exception e) {
            throw new InjectorException(createInjectErrorMessage("Failed to instantiate test dependency, you probably need to call givenDependency() manually.", toBeInjectedIn, field));
        }
    }

    private Object resolveImplementationByName(Field field, Class<?> toBeInjectedIn, String name, boolean failIfMissing) {
        Map<String, Object> implementationByName = implementationsByName.get(field.getType());
        if (implementationByName == null) {
            if (failIfMissing) {
                throw new InjectorException(createInjectErrorMessage("No dependency named " + name + ".", toBeInjectedIn, field));
            } else {
                return null;
            }
        }

        Object implementation = implementationByName.get(name);
        if (implementation == null) {
            if (failIfMissing) {
                TreeSet<String> available = new TreeSet<>(implementationByName.keySet());
                throw new InjectorException(createInjectErrorMessage("No dependency named " + name + ", got " + available + ".", toBeInjectedIn, field));
            } else {
                return null;
            }
        }
        return implementation;
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

    private Object resolveImplementation(Object implementation, Class<?> requestedClass, Class<?> toBeInjectedIn) {
        if (toBeInjectedIn != null && implementation instanceof PerClassProvider && !PerClassProvider.class.isAssignableFrom(requestedClass)) {
            return ((PerClassProvider<?>)implementation).get(toBeInjectedIn);
        }
        if (implementation instanceof Provider && !Provider.class.isAssignableFrom(requestedClass)) {
            return ((Provider<?>)implementation).get();
        }
        return implementation;
    }

    public void reset() {
        implementations.clear();
        implementationsByName.clear();
    }

    public static void enableUnitTestMode() {
        enableUnitTestMode(true);
    }

    public static void enableUnitTestMode(boolean resolveUnitTestDependencies) {
        unitTestMode = true;
        getInstance().resolveUnitTestDependencies = resolveUnitTestDependencies;
    }

    private static class UnitTestInstanceHolder {
        static final ThreadLocal<Injector> unitTestInstance = ThreadLocal.withInitial(Injector::new);
    }
}
