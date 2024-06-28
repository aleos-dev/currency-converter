package com.aleos.daos;

import com.aleos.models.entities.ConversionRate;
import com.aleos.models.entities.Currency;
import com.aleos.util.DbUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ConversionRateDaoTest {

    private ConversionRateDao conversionRateDao;
    private CurrencyDao currencyDao;

    @BeforeEach
    public void init() {
        DataSource dataSource = DbUtil.getTestDataSource();
        conversionRateDao = new ConversionRateDao(dataSource);
        currencyDao = new CurrencyDao(dataSource);
    }

    @Test
    void testSaveConversionRate() {
        Currency jpy = currencyDao.findByCode("JPY").orElseThrow();
        Currency rub = currencyDao.findByCode("RUB").orElseThrow();

        ConversionRate conversionRate = new ConversionRate(null, jpy, rub, BigDecimal.valueOf(0.85));
        conversionRateDao.save(conversionRate);

        Optional<ConversionRate> found = conversionRateDao.findByCode("JPYRUB");
        assertConversionRate(found.orElseThrow(), jpy.getId(), rub.getId(), BigDecimal.valueOf(0.85));
    }

    @Test
    void testFindAllConversionRates() {

        int initialRows = conversionRateDao.findAll().size();

        Currency a = insertCurrency("AAA name", "AAA", "$");
        Currency b = insertCurrency("BBB name", "BBB", "€");
        Currency c = insertCurrency("CCC name", "CCC", "£");

        insertConversionRate(a, b, BigDecimal.valueOf(0.85));
        insertConversionRate(a, c, BigDecimal.valueOf(0.75));

        List<ConversionRate> conversionRates = conversionRateDao.findAll();
        assertEquals(initialRows + 2, conversionRates.size());
    }

    @Test
    void testFindConversionRateById() {

        Currency ust = insertCurrency("UST name", "UST", "$");
        Currency eut = insertCurrency("EUT name", "EUT", "€");

        insertConversionRate(ust, eut, BigDecimal.valueOf(0.85));

        Optional<ConversionRate> conversionRate = conversionRateDao.findByCode("USTEUT");
        assertConversionRate(conversionRate.orElseThrow(), ust.getId(), eut.getId(), BigDecimal.valueOf(0.85));
    }

    @Test
    void testUpdateConversionRate() {

        var conversionRate = conversionRateDao.findByCode("USDEUR").orElseThrow();

        conversionRate.setRate(BigDecimal.valueOf(0.90));
        conversionRateDao.update(conversionRate);

        Optional<ConversionRate> updatedConversionRate = conversionRateDao.findById(conversionRate.getId());
        assertConversionRate(
                updatedConversionRate.orElseThrow(),
                conversionRate.getBaseCurrency().getId(),
                conversionRate.getTargetCurrency().getId(),
                BigDecimal.valueOf(0.90));
    }

    @Test
    void testDeleteConversionRate() {

        int id = conversionRateDao.findByCode("USDEUR").orElseThrow().getId();

        conversionRateDao.delete(id);

        Optional<ConversionRate> deletedConversionRate = conversionRateDao.findById(id);
        assertFalse(deletedConversionRate.isPresent());
    }

    private Currency insertCurrency(String fullname, String code, String sign) {

        Currency currency = new Currency(null, fullname, code, sign);
        currencyDao.save(currency);
        return currencyDao.findByCode(code).orElseThrow();
    }

    private void insertConversionRate(Currency baseCurrency, Currency targetCurrency, BigDecimal rate) {

        ConversionRate conversionRate = new ConversionRate(null, baseCurrency, targetCurrency, rate);
        conversionRateDao.save(conversionRate);
    }

    private void assertConversionRate(
            ConversionRate cr,
            Integer baseCurrencyId,
            Integer targetCurrencyId,
            BigDecimal rate
    ) {

        assertEquals(baseCurrencyId, cr.getBaseCurrency().getId(), "Base currency should match");
        assertEquals(targetCurrencyId, cr.getTargetCurrency().getId(), "Target currency should match");
        assertEquals(rate, cr.getRate(), "Rate should match");
    }
}
