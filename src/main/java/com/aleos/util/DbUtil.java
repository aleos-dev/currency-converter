package com.aleos.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

public final class DbUtil {

    private static final DataSource DATA_SOURCE;

    private DbUtil() {
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

        try {
            Properties properties = new Properties();
            properties.load(DbUtil.class.getClassLoader().getResourceAsStream("database.properties"));

            HikariConfig hikariConfig = new HikariConfig(properties);

            return new HikariDataSource(hikariConfig);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Failed to initialize DataSource" + e.getMessage());
        }
    }

    private static void runFlywayMigration(DataSource dataSource) {

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .load();

        flyway.migrate();
    }
}
