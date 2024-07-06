package com.aleos.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.util.Properties;

public final class DbUtil {

    private static final DataSource DATA_SOURCE;

    private DbUtil() {
        throw new UnsupportedOperationException("DbUtil instance cannot be created.");
    }

    static {
        DATA_SOURCE = initDataSource();
        runFlywayMigration(DATA_SOURCE);
    }

    public static DataSource getDataSource() {
        return DATA_SOURCE;
    }

    public static DataSource getTestDataSource() {
        var dataSource = initDataSource();
        runFlywayMigration(dataSource);
        return dataSource;
    }

    private static DataSource initDataSource() {
            Properties properties = new Properties();
            properties.setProperty("jdbcUrl", PropertiesUtil.DATABASE_URL);
            properties.setProperty("username", PropertiesUtil.DATABASE_USER);
            properties.setProperty("password", PropertiesUtil.DATABASE_PASSWORD);
            properties.setProperty("driverClassName", PropertiesUtil.DATABASE_DRIVER);
            properties.setProperty("maximumPoolSize", PropertiesUtil.DATABASE_POOL_SIZE);

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
