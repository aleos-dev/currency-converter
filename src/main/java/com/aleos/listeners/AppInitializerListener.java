package com.aleos.listeners;

import com.aleos.daos.CurrencyDao;
import com.aleos.exceptions.servlets.ContextInitializationException;
import com.aleos.mappers.CurrencyMapper;
import com.aleos.services.CacheService;
import com.aleos.services.CurrencyService;
import com.aleos.util.DbUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.modelmapper.ModelMapper;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.Set;

@WebListener
public class AppInitializerListener implements ServletContextListener {

    private final Set<Class<?>> initializerSet = new LinkedHashSet<>();

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        initDataSource(sce);
        initObjectMapper(sce);

        initializerSet.add(ModelMapper.class);
        initializerSet.add(CurrencyMapper.class);
        initializerSet.add(CurrencyDao.class);
        initializerSet.add(CacheService.class);
        initializerSet.add(CurrencyService.class);

        for (var clazz : initializerSet) {
            var instance = initializeClass(clazz, sce);
            var attributeName = getAttributeName(clazz);
            sce.getServletContext().setAttribute(attributeName, instance);
        }
    }

    private Object initializeClass(Class<?> clazz, ServletContextEvent sce) {

        try {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();

            for (Constructor<?> constructor : constructors) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();

                if (parameterTypes.length == 0) {

                    return constructor.newInstance();
                } else {

                    Object[] parameters = new Object[parameterTypes.length];

                    for (int i = 0; i < parameterTypes.length; i++) {
                        String attributeName = getAttributeName(parameterTypes[i]);
                        parameters[i] = sce.getServletContext().getAttribute(attributeName);
                    }

                    return constructor.newInstance(parameters);
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

    private String getAttributeName(Class<?> clazz) {

        String simpleName = clazz.getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    private void initDataSource(ServletContextEvent sce) {

        // Initialize DataSource separately using the utility class
        DataSource dataSource = DbUtil.getDataSource();
        String attributeName = getAttributeName(DataSource.class);
        sce.getServletContext().setAttribute(attributeName, dataSource);
    }

    private void initObjectMapper(ServletContextEvent sce) {

        // Initialize ObjectMapper separately due to complex initialization requirements
        ObjectMapper objectMapper = new ObjectMapper();
        sce.getServletContext().setAttribute("objectMapper", objectMapper);
    }
}
