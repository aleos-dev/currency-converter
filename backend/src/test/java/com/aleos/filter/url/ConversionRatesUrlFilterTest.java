package com.aleos.filter.url;

import com.aleos.model.dto.in.ConversionRatePayload;
import com.aleos.util.RequestAttributeUtil;
import com.aleos.validator.ConversionRateValidator;
import com.aleos.validator.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;

import static com.aleos.servlet.common.HttpMethod.GET;
import static com.aleos.servlet.common.HttpMethod.POST;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class ConversionRatesUrlFilterTest {

    @InjectMocks
    ConversionRatesUrlFilter conversionRatesUrlFilter;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    ConversionRateValidator validator;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void initializePayload_ShouldSetPayload_WhenPostRequest() {
        final var payload = getValidPayload();
        doReturn(payload.baseCurrencyCode()).when(request).getParameter("baseCurrencyCode");
        doReturn(payload.targetCurrencyCode()).when(request).getParameter("targetCurrencyCode");
        doReturn(payload.rate().toString()).when(request).getParameter("rate");
        doReturn(POST.toString()).when(request).getMethod();

        conversionRatesUrlFilter.initializePayload(request, response);

        var captor = ArgumentCaptor.forClass(ConversionRatePayload.class);
        verify(request).setAttribute(eq(RequestAttributeUtil.PAYLOAD_MODEL), captor.capture());
        assertEquals(payload, captor.getValue());
    }

    @Test
    void initializePayload_ShouldDoNothing_WhenNotPostRequest() {
        doReturn(GET.toString()).when(request).getMethod();

        conversionRatesUrlFilter.initializePayload(request, response);

        verify(request).getMethod();
        verify(request, never()).getParameter(anyString());
    }

    @Test
    void initializePayload_shouldThrowNumberFormatException_WhenRateCantBeParsed() {
        final String invalidRate = "invalid_rate";
        final var spy = spy(conversionRatesUrlFilter);
        doReturn(POST.toString()).when(request).getMethod();
        doReturn(invalidRate).when(request).getParameter("rate");

        assertThrows(NumberFormatException.class, () -> spy.initializePayload(request, response));
    }

    @Test
    void validatePayload_ShouldReturnNoErrors_WhenPayloadIsValid() {
        final var payload = getValidPayload();
        final var validationResult = new ValidationResult();
        doReturn(POST.toString()).when(request).getMethod();
        doReturn(payload).when(request).getAttribute(RequestAttributeUtil.PAYLOAD_MODEL);
        doReturn(validationResult).when(validator).validate(payload);

        var actualValidationResult = conversionRatesUrlFilter.validatePayload(request, response);

        verify(request).getAttribute(RequestAttributeUtil.PAYLOAD_MODEL);
        verify(validator).validate(payload);
        assertTrue(actualValidationResult.isValid());
    }

    private ConversionRatePayload getValidPayload() {
        String from = "USD";
        String to = "EUR";
        BigDecimal rate = BigDecimal.valueOf(2.0);
        return new ConversionRatePayload(from, to, rate);
    }
}