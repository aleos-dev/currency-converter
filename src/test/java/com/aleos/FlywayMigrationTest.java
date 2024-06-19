package com.aleos;

import org.flywaydb.core.Flyway;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FlywayMigrationTest {

    private DataSource dataSource;

    @Before
    public void setUp() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:sqlite::memory:");
        dataSource = new HikariDataSource(hikariConfig);

        // Configure Flyway
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();

        // Run the migrations
        flyway.migrate();
    }

    @Test
    public void testMigrations() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {

            // Check if the Currencies table exists
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='currencies'");
            assertTrue(rs.next());

            // Check if the table has the expected number of rows
            rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM Currencies");
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
