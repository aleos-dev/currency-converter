package com.aleos.listener;

import com.aleos.dao.ConversionRateDao;
import com.aleos.dao.CurrencyDao;
import com.aleos.mapper.ConversionRateMapper;
import com.aleos.mapper.CurrencyMapper;
import com.aleos.service.CacheService;
import com.aleos.service.ConversionRateService;
import com.aleos.service.ConversionService;
import com.aleos.service.CurrencyService;
import com.aleos.servlet.*;
import com.aleos.util.ComponentInitializerUtil;
import com.aleos.util.PropertiesUtil;
import com.aleos.validator.ConversionRateValidator;
import com.aleos.validator.CurrencyValidator;
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
        ComponentInitializerUtil.registerServlet(event, CurrencyServlet.class, PropertiesUtil.getProperty("servlet.currency.url"));
        ComponentInitializerUtil.registerServlet(event, CurrenciesServlet.class, PropertiesUtil.getProperty("servlet.currencies.url"));
        ComponentInitializerUtil.registerServlet(event, ConversionRateServlet.class, PropertiesUtil.getProperty("servlet.conversionRate.url"));
        ComponentInitializerUtil.registerServlet(event, ConversionRatesServlet.class, PropertiesUtil.getProperty("servlet.conversionRates.url"));
        ComponentInitializerUtil.registerServlet(event, ConversionServlet.class, PropertiesUtil.getProperty("servlet.conversion.url"));
    }
}
