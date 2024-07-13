package com.aleos.listeners;

import com.aleos.daos.ConversionRateDao;
import com.aleos.daos.CurrencyDao;
import com.aleos.mappers.ConversionRateMapper;
import com.aleos.mappers.CurrencyMapper;
import com.aleos.services.CacheService;
import com.aleos.services.ConversionRateService;
import com.aleos.services.ConversionService;
import com.aleos.services.CurrencyService;
import com.aleos.servlets.*;
import com.aleos.util.ComponentInitializerUtil;
import com.aleos.util.PropertiesUtil;
import com.aleos.validators.ConversionRateValidator;
import com.aleos.validators.CurrencyValidator;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@WebListener
public class ApplicationStartupListener implements ServletContextListener {

    private static final Set<Class<?>> declaredComponents = new LinkedHashSet<>(Arrays.asList(

            // Component classes here will be put to ServletContext
            ModelMapper.class,
            CurrencyMapper.class,
            ConversionRateMapper.class,

            CurrencyDao.class,
            ConversionRateDao.class,

            CacheService.class,
            CurrencyService.class,
            ConversionRateService.class,
            ConversionService.class,

            CurrencyValidator.class,
            ConversionRateValidator.class
    ));

    @Override
    public void contextInitialized(ServletContextEvent event) {

        ComponentInitializerUtil.initializeDataSource(event);
        ComponentInitializerUtil.initializeObjectMapper(event);
        ComponentInitializerUtil.initializeComponents(event, declaredComponents);

        // register servlets manually to dynamically load configuration (URL patterns) from property files
        registerServlets(event);
    }

    private void registerServlets(ServletContextEvent event) {
        ComponentInitializerUtil.registerServlet(event, CurrencyServlet.class, PropertiesUtil.CURRENCY_SERVICE_URL);
        ComponentInitializerUtil.registerServlet(event, CurrenciesServlet.class, PropertiesUtil.CURRENCIES_SERVICE_URL);
        ComponentInitializerUtil.registerServlet(event, ConversionRateServlet.class, PropertiesUtil.CONVERSION_RATE_SERVICE_URL);
        ComponentInitializerUtil.registerServlet(event, ConversionRatesServlet.class, PropertiesUtil.CONVERSION_RATES_SERVICE_URL);
        ComponentInitializerUtil.registerServlet(event, ConversionServlet.class, PropertiesUtil.CONVERSION_SERVICE_URL);
    }
}
