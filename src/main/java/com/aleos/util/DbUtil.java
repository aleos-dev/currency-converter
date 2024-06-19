package com.aleos.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;

import javax.management.RuntimeErrorException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class DbUtil {

    private static final DataSource DATA_SOURCE;

    private DbUtil() {
    }

    static {
        try {
            Properties properties = new Properties();
            properties.load(DbUtil.class.getClassLoader().getResourceAsStream("database.properties"));

            HikariConfig hikariConfig = new HikariConfig(properties);

            DATA_SOURCE = new HikariDataSource(hikariConfig);

            Flyway flyway = Flyway.configure().dataSource(DATA_SOURCE).load();
            flyway.migrate();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                    "Failed to initialize DataSource due to IOException" + e.getMessage());
        }
    }

    public static Connection getConnection() {
        try {
            return DATA_SOURCE.getConnection();
        } catch (SQLException e) {
            throw new RuntimeErrorException(new Error("Database access error was occurred, application is exit"));
        }
    }
}
