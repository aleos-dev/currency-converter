package com.aleos.service;

import com.aleos.dao.ConversionRateDao;
import com.aleos.mapper.ConversionRateMapper;
import com.aleos.model.dto.in.ConversionRateIdentifierPayload;
import com.aleos.model.dto.in.ConversionRatePayload;
import com.aleos.model.entity.ConversionRate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;


class ConversionRateServiceTest {

    @InjectMocks
    ConversionRateService service;

    @Mock
    ConversionRateDao dao;

    @Mock
    ConversionRateMapper mapper;

    AutoCloseable mocks;

    @BeforeEach
    void setup() {
        mocks = openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void save_ShouldSaveAndReturn_WhenValidRequest() {
        var payload = new ConversionRatePayload("CNY", "USD", BigDecimal.TEN);
        var stub = new ConversionRate();
        doReturn(stub).when(dao).saveAndFetch(payload.baseCurrencyCode(), payload.targetCurrencyCode(), payload.rate());

        service.save(payload);

        verify(dao).saveAndFetch(payload.baseCurrencyCode(), payload.targetCurrencyCode(), payload.rate());
        verify(mapper).toDto(stub);
    }

    @Test
    void findAll_ShouldReturnAll_WhenCalled() {
        var stub = new ConversionRate();
        doReturn(List.of(stub)).when(dao).findAll();

        service.findAll();

        verify(dao).findAll();
        verify(mapper).toDto(stub);
    }

    @Test
    void findByCode_ShouldReturnResponse_WhenFound() {
        var payload = new ConversionRateIdentifierPayload("USDEUR");
        var stub = new ConversionRate();
        doReturn(Optional.of(stub)).when(dao).find("USD", "EUR");

        service.findByCode(payload);

        verify(dao).find("USD", "EUR");
        verify(mapper).toDto(stub);
    }

    @Test
    void update_ShouldReturnTrue_WhenUpdateSucceeds() {
        var payload = new ConversionRatePayload("USD", "EUR", BigDecimal.valueOf(0.90));

        doReturn(true).when(dao).update(payload.baseCurrencyCode(), payload.targetCurrencyCode(), payload.rate());

        var result = service.update(payload);

        assertTrue(result);
        verify(dao).update(payload.baseCurrencyCode(), payload.targetCurrencyCode(), payload.rate());
    }

    @Test
    void delete_ShouldCallDaoDelete_WhenCalled() {
        var payload = new ConversionRateIdentifierPayload("1");
        doReturn(true).when(dao).delete(1);

        var result = service.delete(payload);

        assertTrue(result);
        verify(dao).delete(1);
    }
}