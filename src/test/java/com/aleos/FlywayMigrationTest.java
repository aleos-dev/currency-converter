package com.aleos;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlywayMigrationTest {

    private DataSource dataSource;

    @BeforeEach
    public void setUp() {
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
    void testMigrations() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {

            // Check if the Currencies table exists
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='currencies'");
            assertTrue(rs.next());

            // Check if the table has the expected number of rows
            rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM currencies");
            rs.next();
            int count = rs.getInt("rowcount");
            assertEquals(10, count);

            // Check if the conversion_rates table exists
            rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='conversion_rates'");
            assertTrue(rs.next());

            // Check if the table has the expected number of rows
            rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM conversion_rates");
            rs.next();
            count = rs.getInt("rowcount");
            assertEquals(10, count);
        }
    }
}
