package com.aleos.util;

import jakarta.servlet.ServletContext;

import java.lang.reflect.Field;

public final class DependencyInjector {

    private DependencyInjector() {
        throw new UnsupportedOperationException("DependencyInjector can't be instantiated.");
    }

    public static void inject(Object target, ServletContext context) {
        for (Field field : target.getClass().getDeclaredFields()) {
            Class<?> clazz = field.getType();
            Object attribute = context.getAttribute(AttributeNameUtil.getName(clazz));
            if (attribute != null) {
                field.setAccessible(true);
                try {
                    field.set(target, attribute);
                } catch (IllegalAccessException e) {
                    throw new ExceptionInInitializerError("Can't inject %s to class variable of %s"
                            .formatted(clazz.getSimpleName(), target.getClass().getSimpleName()));
                }
            }
        }
    }
}
