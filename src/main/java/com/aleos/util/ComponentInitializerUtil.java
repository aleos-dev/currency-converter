package com.aleos.util;

import com.aleos.exceptions.servlets.ContextInitializationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.http.HttpServlet;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public final class ComponentInitializerUtil {

    private ComponentInitializerUtil() {
        throw new UnsupportedOperationException("ComponentInitializer can't be instantiated.");
    }

    public static void injectDependencies(ServletContext context, Object component) {
        for (Field field : component.getClass().getDeclaredFields()) {
            Class<?> clazz = field.getType();
            Object attribute = context.getAttribute(RequestAttributeUtil.getName(clazz));
            if (attribute != null) {
                field.setAccessible(true);
                try {
                    field.set(component, attribute);
                } catch (IllegalAccessException e) {
                    throw new ExceptionInInitializerError("Can't inject %s to class variable of %s"
                            .formatted(clazz.getSimpleName(), component.getClass().getSimpleName()));
                }
            }
        }
    }

    public static void initializeComponents(ServletContextEvent sce, Set<Class<?>> components) {
        components.forEach(clazz -> {
            Object instance = instantiateClass(sce, clazz);
            String attributeName = RequestAttributeUtil.getName(clazz);
            sce.getServletContext().setAttribute(attributeName, instance);
        });
    }

    public static void registerServlet(ServletContextEvent sce,
                                       Class<? extends HttpServlet> servletClass,
                                       String urlPattern) {
        String attributeName = RequestAttributeUtil.getName(servletClass);
        HttpServlet servlet = instantiateClass(sce, servletClass);

        ServletRegistration.Dynamic registration = sce.getServletContext().addServlet(attributeName, servlet);
        registration.addMapping(urlPattern);
    }

    public static void initializeDataSource(ServletContextEvent sce) {

        DataSource dataSource = DatabaseUtil.getDataSource();
        String attributeName = RequestAttributeUtil.getName(DataSource.class);
        sce.getServletContext().setAttribute(attributeName, dataSource);
    }

    public static void initializeObjectMapper(ServletContextEvent sce) {

        // Initialize ObjectMapper separately due to complex initialization requirements
        ObjectMapper objectMapper = new ObjectMapper();
        String attributeName = RequestAttributeUtil.getName(ObjectMapper.class);
        sce.getServletContext().setAttribute(attributeName, objectMapper);
    }

    @SuppressWarnings("unchecked")
    private static <T> T instantiateClass(ServletContextEvent sce, Class<T> clazz) {
        try {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            for (Constructor<?> constructor : constructors) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length == 0) {
                    return (T) constructor.newInstance();
                } else {
                    Object[] parameters = new Object[parameterTypes.length];
                    for (int i = 0; i < parameterTypes.length; i++) {
                        String attributeName = RequestAttributeUtil.getName(parameterTypes[i]);
                        parameters[i] = sce.getServletContext().getAttribute(attributeName);
                    }
                    return (T) constructor.newInstance(parameters);
                }
            }
            throw new NoSuchMethodException("No suitable constructor found for " + clazz.getName());
        } catch (InstantiationException
                 | InvocationTargetException
                 | NoSuchMethodException
                 | IllegalAccessException e) {
            throw new ContextInitializationException("Error initializing context", e);
        }
    }
}
