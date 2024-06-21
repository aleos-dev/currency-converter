package com.aleos.daos;

import com.aleos.models.entities.Currency;
import com.aleos.util.DbUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyDaoTest {

    private DataSource dataSource;
    private CurrencyDao currencyDao;

    @BeforeEach
    public void init() {
        dataSource = DbUtil.getTestDataSource();
        currencyDao = new CurrencyDao(dataSource);
    }

    @Test
    void testSaveCurrency() {
        Currency currency = new Currency(null, "US TEST", "UST", "$");
        currencyDao.save(currency);

        Optional<Currency> found = currencyDao.findByCode("UST");
        assertCurrency(found, "US TEST", "UST", "$");
    }

    @Test
    void testFindAllCurrencies() throws SQLException {
        int initialRows = currencyDao.findAll().size();

        insertCurrency("United States TEST", "UST", "$");
        insertCurrency("Euro NEW", "NEW", "€");

        List<Currency> currencies = currencyDao.findAll();
        assertEquals(initialRows + 2, currencies.size());
    }

    @Test
    void testFindCurrencyById() throws SQLException {
        insertCurrency("United States TEST", "UST", "$");
        var founded = currencyDao.findByCode("UST").orElseThrow();

        Optional<Currency> currency = currencyDao.findById(founded.getId());

        assertCurrency(currency, "United States TEST", "UST", "$");
    }

    @Test
    void testFindCurrencyByCode() throws SQLException {
        insertCurrency("United States TEST", "UST", "$");

        Optional<Currency> currency = currencyDao.findByCode("UST");
        assertCurrency(currency, "United States TEST", "UST", "$");
    }

    @Test
    void testUpdateCurrency() throws SQLException {
        insertCurrency("United States TEST", "UST", "$");

        Currency currency = currencyDao.findByCode("UST").orElseThrow();
        currency.setFullname("CHANGED");
        currencyDao.update(currency);

        Optional<Currency> updatedCurrency = currencyDao.findById(currency.getId());
        assertCurrency(updatedCurrency, "CHANGED", "UST", "$");
    }

    @Test
    void testDeleteCurrency() throws SQLException {
        insertCurrency("United States TEST", "UST", "$");

        int id = currencyDao.findByCode("UST").orElseThrow().getId();
        currencyDao.delete(id);

        Optional<Currency> deletedCurrency = currencyDao.findById(id);
        assertFalse(deletedCurrency.isPresent());
    }

    private void insertCurrency(String fullname, String code, String sign) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = String.format("INSERT INTO currencies (fullname, code, sign) VALUES ('%s', '%s', '%s')", fullname, code, sign);
            connection.createStatement().execute(query);
        }
    }

    private void assertCurrency(Optional<Currency> currency, String expectedFullname, String expectedCode, String expectedSign) {
        assertTrue(currency.isPresent(), "Currency should be present");
        currency.ifPresent(c -> {
            assertEquals(expectedFullname, c.getFullname(), "Fullname should match");
            assertEquals(expectedCode, c.getCode(), "Code should match");
            assertEquals(expectedSign, c.getSign(), "Sign should match");
        });
    }
}