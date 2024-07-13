package com.aleos.dao;

import com.aleos.model.entity.Currency;
import com.aleos.util.DatabaseUtil;
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

        dataSource = DatabaseUtil.getTestDataSource();
        currencyDao = new CurrencyDao(dataSource);
    }

    @Test
    void testSaveCurrency() {

        Currency currency = new Currency(null, "US TEST", "UST", "$");
        currencyDao.save(currency);

        Optional<Currency> found = currencyDao.find("UST");

        assertTrue(found.isPresent(), "Currency should be present");
        assertCurrency(found.get(), "US TEST", "UST", "$");
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
        var founded = currencyDao.find("UST").orElseThrow();

        Optional<Currency> currency = currencyDao.find(founded.getId());

        assertTrue(currency.isPresent(), "Currency should be present");
        assertCurrency(currency.get(), "United States TEST", "UST", "$");
    }

    @Test
    void testFindCurrencyByCode() throws SQLException {

        insertCurrency("United States TEST", "USV", "#");

        Optional<Currency> currency = currencyDao.find("USV");

        assertTrue(currency.isPresent(), "Currency should be present");
        assertCurrency(currency.get(), "United States TEST", "USV", "#");
    }

    @Test
    void testUpdateCurrency() throws SQLException {
        insertCurrency("United States TEST", "UST", "$");

        Currency currency = currencyDao.find("UST").orElseThrow();
        currency.setFullname("CHANGED");
        currencyDao.update(currency);

        Optional<Currency> updatedCurrency = currencyDao.find(currency.getId());
        assertTrue(updatedCurrency.isPresent(), "Currency should be present");
        assertCurrency(updatedCurrency.get(), "CHANGED", "UST", "$");
    }

    @Test
    void testDeleteCurrency() throws SQLException {
        insertCurrency("United States TEST", "UST", "$");

        int id = currencyDao.find("UST").orElseThrow().getId();
        currencyDao.delete(id);

        Optional<Currency> deletedCurrency = currencyDao.find(id);
        assertFalse(deletedCurrency.isPresent());
    }

    private void insertCurrency(String fullname, String code, String sign) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = String.format("INSERT INTO currencies (fullname, code, sign) VALUES ('%s', '%s', '%s')", fullname, code, sign);
            connection.createStatement().execute(query);
        }
    }

    private void assertCurrency(Currency currency, String expectedFullname, String expectedCode, String expectedSign) {
        assertEquals(expectedFullname, currency.getFullname(), "Fullname should match");
        assertEquals(expectedCode, currency.getCode(), "Code should match");
        assertEquals(expectedSign, currency.getSign(), "Sign should match");
    }
}