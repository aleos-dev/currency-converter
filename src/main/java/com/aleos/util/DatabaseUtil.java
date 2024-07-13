package com.aleos.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.util.Properties;

public final class DatabaseUtil {

    private static final DataSource DATA_SOURCE;

    private DatabaseUtil() {
        throw new UnsupportedOperationException("DbUtil instance cannot be created.");
    }

    static {
        DATA_SOURCE = createDataSource();
        runFlywayMigration(DATA_SOURCE);
    }

    public static DataSource getDataSource() {
        return DATA_SOURCE;
    }

    public static DataSource getTestDataSource() {
        var dataSource = createDataSource();
        runFlywayMigration(dataSource);
        return dataSource;
    }

    private static DataSource createDataSource() {
            Properties properties = new Properties();
            properties.setProperty("jdbcUrl", PropertiesUtil.getProperty("database.url"));
            properties.setProperty("username", PropertiesUtil.getProperty("database.user"));
            properties.setProperty("password", PropertiesUtil.getProperty(("database.password")));
            properties.setProperty("driverClassName", PropertiesUtil.getProperty("database.driver"));
            properties.setProperty("maximumPoolSize", PropertiesUtil.getProperty("database.pool.size"));

            HikariConfig hikariConfig = new HikariConfig(properties);
            return new HikariDataSource(hikariConfig);
    }

    private static void runFlywayMigration(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .load();
        flyway.migrate();
    }
}
