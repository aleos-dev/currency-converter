package com.aleos.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertiesUtil {

    private static final String RESOURCE_NAME = "application.properties";

    private static final Properties properties = new Properties();

    // DB config
    public static final String DATABASE_URL;
    public static final String DATABASE_USER;
    public static final String DATABASE_PASSWORD;
    public static final String DATABASE_DRIVER;
    public static final String DATABASE_POOL_SIZE;

    // URL patterns
    public static final String ROOT_URL;
    public static final String CURRENCY_SERVICE_URL;
    public static final String CURRENCIES_SERVICE_URL;
    public static final String CONVERSION_RATE_SERVICE_URL;
    public static final String CONVERSION_RATES_SERVICE_URL;
    public static final String CONVERSION_SERVICE_URL;

    static {
        loadProperties();

        // Load DB config
        DATABASE_URL = properties.getProperty("database.url");
        DATABASE_USER = properties.getProperty("database.user");
        DATABASE_PASSWORD = properties.getProperty("database.password");
        DATABASE_DRIVER = properties.getProperty("database.driver");
        DATABASE_POOL_SIZE = properties.getProperty("database.pool.size");

        // Load URL patterns
        ROOT_URL = properties.getProperty("servlet.root.url");
        CURRENCY_SERVICE_URL = properties.getProperty("servlet.currency.url");
        CURRENCIES_SERVICE_URL = properties.getProperty("servlet.currencies.url");
        CONVERSION_RATE_SERVICE_URL = properties.getProperty("servlet.conversionRate.url");
        CONVERSION_RATES_SERVICE_URL = properties.getProperty("servlet.conversionRates.url");
        CONVERSION_SERVICE_URL = properties.getProperty("servlet.conversion.url");
    }

    private PropertiesUtil() {
        throw new UnsupportedOperationException("PropertyUtil instance cannot be created.");
    }

    private static void loadProperties() {
        try (InputStream inputStream =
                     PropertiesUtil.class.getClassLoader().getResourceAsStream(PropertiesUtil.RESOURCE_NAME)) {

            if (inputStream == null) {
                throw new ExceptionInInitializerError(PropertiesUtil.RESOURCE_NAME + " not found in classpath");
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Error loading properties from " + PropertiesUtil.RESOURCE_NAME);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
