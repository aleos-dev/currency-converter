package com.aleos.filters.url;

import com.aleos.models.dtos.in.ConversionRatePayload;
import com.aleos.models.dtos.out.Error;
import com.aleos.util.RequestAttributeUtil;
import com.aleos.validators.ConversionRateValidator;
import com.aleos.validators.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static com.aleos.servlets.common.HttpMethod.GET;
import static com.aleos.servlets.common.HttpMethod.POST;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        mocks = MockitoAnnotations.openMocks(this);
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
        final var validationResult = new ValidationResult<Error>();
        doReturn(POST.toString()).when(request).getMethod();
        doReturn(payload).when(request).getAttribute(RequestAttributeUtil.PAYLOAD_MODEL);
        doReturn(validationResult).when(validator).validate(payload);

        var actualValidationResult = conversionRatesUrlFilter.validatePayload(request, response);

        verify(request).getAttribute(RequestAttributeUtil.PAYLOAD_MODEL);
        verify(validator).validate(payload);
        assertTrue(actualValidationResult.isValid());
    }

    @Test
    void validatePayload_ShouldReturnErrors_WhenPayloadHasNullField() {
        final var expectedValidationResult = new ValidationResult<Error>();
        final var payload = getInvalidPayloadWithNullField();
        expectedValidationResult.add(Error.of("Test error"));
        doReturn("POST").when(request).getMethod();
        doReturn(payload).when(request).getAttribute(RequestAttributeUtil.PAYLOAD_MODEL);
        doReturn(expectedValidationResult).when(validator).validate(payload);

        var actualValidationResult = conversionRatesUrlFilter.validatePayload(request, response);

        verify(request).getAttribute(RequestAttributeUtil.PAYLOAD_MODEL);
        verify(validator).validate(payload);
        assertEquals(1, actualValidationResult.getErrors().size());
        assertEquals(actualValidationResult.getErrors().get(0), expectedValidationResult.getErrors().get(0));
    }


    private ConversionRatePayload getValidPayload() {
        String from = "USD";
        String to = "EUR";
        BigDecimal rate = BigDecimal.valueOf(2.0);
        return new ConversionRatePayload(from, to, rate);
    }

    private ConversionRatePayload getInvalidPayloadWithNullField() {
        String to = "EUR";
        BigDecimal rate = BigDecimal.valueOf(2.0);
        return new ConversionRatePayload(null, to, rate);
    }
}