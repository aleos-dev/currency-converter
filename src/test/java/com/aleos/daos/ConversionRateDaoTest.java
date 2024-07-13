package com.aleos.daos;

import com.aleos.models.entities.ConversionRate;
import com.aleos.models.entities.Currency;
import com.aleos.util.DatabaseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConversionRateDaoTest {

    private ConversionRateDao conversionRateDao;
    private CurrencyDao currencyDao;

    @BeforeEach
    public void init() {
        DataSource dataSource = DatabaseUtil.getTestDataSource();
        conversionRateDao = new ConversionRateDao(dataSource);
        currencyDao = new CurrencyDao(dataSource);
    }

    @Test
    void testSaveConversionRate() {
        Currency jpy = currencyDao.find("JPY").orElseThrow();
        Currency rub = currencyDao.find("RUB").orElseThrow();
        var newEntity = new ConversionRate(null, jpy, rub, BigDecimal.valueOf(0.85));

        conversionRateDao.save(newEntity);

        var founded = conversionRateDao.find("JPY", "RUB").orElseThrow();
        assertNotNull(founded.getId());
        assertEquals(founded.getBaseCurrency(), newEntity.getBaseCurrency());
        assertEquals(founded.getTargetCurrency(), newEntity.getTargetCurrency());
        assertEquals(founded.getRate(), newEntity.getRate());
    }


    @Test
    void testFindAllConversionRates() {
        var initSize = conversionRateDao.findAll();
        var newConversionRate = ConversionRate.builder()
                .baseCurrency(currencyDao.find(5).orElseThrow())
                .targetCurrency(currencyDao.find(6).orElseThrow())
                .rate(BigDecimal.TEN)
                .build();

        conversionRateDao.save(newConversionRate);

        var actualSize = conversionRateDao.findAll();
        assertEquals(initSize.size() + 1, actualSize.size());
        assertNotNull(newConversionRate.getId());
    }

    @Test
    void testFindConversionRateById() {

        var optional = conversionRateDao.find(1);

        var result = optional.orElseThrow();
        assertEquals("USD", result.getBaseCurrency().getCode());
        assertEquals("EUR", result.getTargetCurrency().getCode());
        assertEquals(result.getRate(), BigDecimal.valueOf(0.93));
    }

    @Test
    void testUpdateConversionRate() {

        var initial = conversionRateDao.find(1).orElseThrow();
        var newRate = BigDecimal.valueOf(0.90);
        initial.setRate(newRate);

        conversionRateDao.update(initial);

        var actual = conversionRateDao.find(initial.getId()).orElseThrow();
        assertEquals(actual.getId(), initial.getId());
        assertEquals(actual.getRate(), newRate);
    }

    @Test
    void testDeleteConversionRate() {
        int id = conversionRateDao.find(1).orElseThrow().getId();

        conversionRateDao.delete(id);

        Optional<ConversionRate> deletedConversionRate = conversionRateDao.find(id);
        assertFalse(deletedConversionRate.isPresent());
    }
}
