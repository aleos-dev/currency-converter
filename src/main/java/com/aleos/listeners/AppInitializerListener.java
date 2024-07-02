package com.aleos.listeners;

import com.aleos.daos.ConversionRateDao;
import com.aleos.daos.CurrencyDao;
import com.aleos.exceptions.servlets.ContextInitializationException;
import com.aleos.mappers.ConversionRateMapper;
import com.aleos.mappers.CurrencyMapper;
import com.aleos.services.CacheService;
import com.aleos.services.ConversionRateService;
import com.aleos.services.ConversionService;
import com.aleos.services.CurrencyService;
import com.aleos.util.AttributeNameUtil;
import com.aleos.util.DbUtil;
import com.aleos.validators.ConversionRateValidator;
import com.aleos.validators.CurrencyValidator;
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
        initializerSet.add(ConversionRateMapper.class);

        initializerSet.add(CurrencyDao.class);
        initializerSet.add(ConversionRateDao.class);

        initializerSet.add(CacheService.class);
        initializerSet.add(CurrencyService.class);
        initializerSet.add(ConversionRateService.class);
        initializerSet.add(ConversionService.class);

        initializerSet.add(CurrencyValidator.class);
        initializerSet.add(ConversionRateValidator.class);

        for (var clazz : initializerSet) {
            var instance = initializeClass(clazz, sce);
            var attributeName = AttributeNameUtil.getName(clazz);
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
                        String attributeName = AttributeNameUtil.getName(parameterTypes[i]);
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

    private void initDataSource(ServletContextEvent sce) {

        // Initialize DataSource separately using the utility class
        DataSource dataSource = DbUtil.getDataSource();
        String attributeName = AttributeNameUtil.getName(DataSource.class);
        sce.getServletContext().setAttribute(attributeName, dataSource);
    }

    private void initObjectMapper(ServletContextEvent sce) {

        // Initialize ObjectMapper separately due to complex initialization requirements
        ObjectMapper objectMapper = new ObjectMapper();
        String attributeName = AttributeNameUtil.getName(ObjectMapper.class);
        sce.getServletContext().setAttribute(attributeName, objectMapper);
    }
}
