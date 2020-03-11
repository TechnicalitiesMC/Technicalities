package com.technicalitiesmc.lib.block;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class Util {

    private static <O, T extends Throwable> void visitComponents(O obj, Class<O> upTo, UnsafeConsumer<Field, T> visitor) throws T {
        Class<?> type = obj.getClass();
        while (type != null && upTo.isAssignableFrom(type)) {
            for (Field field : type.getDeclaredFields()) {
                if (field.getAnnotation(Component.class) == null) continue;
                if (!TKBlockComponent.class.isAssignableFrom(field.getType())) {
                    throw new IllegalStateException("Marked a field that is not a component with @Component: " + field);
                }
                if (getComponentType(field.getType()) == null) {
                    throw new IllegalStateException("Component class is not marked with @Component: " + field.getType());
                }

                visitor.accept(field);
            }
            type = type.getSuperclass();
        }
    }

    static Class<? extends TKBlockComponent> getComponentType(Class<?> type) {
        Class<?> t = type;
        while (t != null && t.getAnnotation(Component.class) == null) {
            t = t.getSuperclass();
        }
        if (t == null) return null;
        if (!TKBlockComponent.class.isAssignableFrom(t)) {
            throw new IllegalStateException("A class that is not a component is marked as one: " + t);
        }
        return (Class<? extends TKBlockComponent>) t;
    }

    static Set<ComponentInfo> findComponents(TKBlock block) throws IllegalAccessException {
        Map<Class<? extends TKBlockComponent>, ComponentInfo> componentMap = getComponents(block);
        injectDependencies(componentMap);
        return new HashSet<>(componentMap.values());
    }

    private static Map<Class<? extends TKBlockComponent>, ComponentInfo> getComponents(TKBlock block) throws IllegalAccessException {
        Map<Class<? extends TKBlockComponent>, ComponentInfo> components = new HashMap<>();

        visitComponents(block, TKBlock.class, field -> {
            field.setAccessible(true);

            boolean isStatic = Modifier.isStatic(field.getModifiers());
            TKBlockComponent component = (TKBlockComponent) field.get(isStatic ? null : block);
            ComponentInfo info = new ComponentInfo(component);

            ComponentInfo previous = components.put(info.type, info);
            if (previous != null) {
                throw new IllegalStateException("Found multiple components of type: " + info.type);
            }
        });

        return components;
    }

    private static void injectDependencies(Map<Class<? extends TKBlockComponent>, ComponentInfo> components) throws IllegalAccessException {
        for (ComponentInfo info : components.values()) {
            visitComponents(info.instance, TKBlockComponent.class, field -> {
                if (Modifier.isStatic(field.getModifiers())) {
                    throw new IllegalStateException("Found static field marked with @Component in another component: " + field);
                }

                field.setAccessible(true);

                Class<? extends TKBlockComponent> type = getComponentType(field.getType());
                if (type == null) {
                    throw new IllegalStateException("Component class is not marked with @Component: " + field.getType());
                }
                ComponentInfo i = components.get(type);

                if (i != null) {
                    field.set(info.instance, i.instance);
                } else if (!field.getAnnotation(Component.class).optional()) {
                    throw new IllegalStateException("Required component not found: " + type + " in " + info.instance.getClass());
                }
            });
        }
    }

    static class ComponentInfo {

        final TKBlockComponent instance;
        final Class<? extends TKBlockComponent> type;

        ComponentInfo(TKBlockComponent instance) {
            this.instance = instance;
            this.type = getComponentType(instance.getClass());
            if (this.type == null) {
                throw new IllegalStateException("Component class is not marked with @Component: " + instance.getClass());
            }
        }
    }

    interface UnsafeConsumer<V, T extends Throwable> {
        void accept(V value) throws T;
    }

}
