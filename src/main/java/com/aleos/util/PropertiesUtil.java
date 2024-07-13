package com.aleos.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertiesUtil {

    private static final String RESOURCE_NAME = "application.properties";

    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }


    public static String getProperty(String key) {
        return properties.getProperty(key);
    }


    private PropertiesUtil() {
        throw new UnsupportedOperationException("PropertiesUtil instance cannot be created.");
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
}
