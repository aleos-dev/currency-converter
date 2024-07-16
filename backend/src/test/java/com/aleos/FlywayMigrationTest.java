package com.aleos;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlywayMigrationTest {

    private static DataSource dataSource;

    @BeforeAll
    public static void setUp() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:sqlite::memory:");
        dataSource = new HikariDataSource(hikariConfig);

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();

        // Run the migrations
        flyway.migrate();
    }

    @Test
    void testCurrenciesTableExists() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='currencies'")) {

            assertTrue(rs.next(), "Currencies table should exist after migration.");
        }
    }

    @Test
    void testCurrenciesRowCount() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM currencies")) {

            assertTrue(rs.next());
            int count = rs.getInt("rowcount");
            assertEquals(10, count, "Currencies table should have 10 rows after migration.");
        }
    }

    @Test
    void testConversionRatesTableExists() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='conversion_rates'")) {

            assertTrue(rs.next(), "Conversion rates table should exist after migration.");
        }
    }

    @Test
    void testConversionRatesRowCount() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM conversion_rates")) {

            assertTrue(rs.next());
            int count = rs.getInt("rowcount");
            assertEquals(12, count, "Conversion rates table should have 12 rows after migration.");
        }
    }
}
