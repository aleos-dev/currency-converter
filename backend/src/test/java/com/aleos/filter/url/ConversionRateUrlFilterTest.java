package com.aleos.filter.url;

import com.aleos.model.dto.in.ConversionRateIdentifierPayload;
import com.aleos.model.dto.in.ConversionRatePayload;
import com.aleos.model.dto.out.Error;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import static com.aleos.servlet.common.HttpMethod.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class ConversionRateUrlFilterTest {

    @InjectMocks
    ConversionRateUrlFilter filter;

    @Mock
    ConversionRateValidator validator;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    BufferedReader reader;

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
    void initializePayload_ShouldSetPayload_WhenGetRequestType() {
        final String CORRECT_PATH_INFO = "/" + getValidIdentifier().identifier();
        doReturn(GET.toString()).when(request).getMethod();
        doReturn(CORRECT_PATH_INFO).when(request).getPathInfo();

        filter.initializePayload(request, response);

        var captor = ArgumentCaptor.forClass(ConversionRateIdentifierPayload.class);
        verify(request).setAttribute(eq(RequestAttributeUtil.PAYLOAD_MODEL), captor.capture());
        assertEquals(getValidIdentifier().identifier(), captor.getValue().identifier());
    }

    @Test
    void initializePayload_ShouldSetPayload_WhenPatchRequestType() throws IOException {
        final var payload = getValidPayload();
        final String CORRECT_PATH_INFO = "/" + payload.baseCurrencyCode() + payload.targetCurrencyCode();
        doReturn(PATCH.toString()).when(request).getMethod();
        doReturn(CORRECT_PATH_INFO).when(request).getPathInfo();
        doReturn(reader).when(request).getReader();
        doReturn(Stream.of("rate=" + payload.rate())).when(reader).lines();

        filter.initializePayload(request, response);

        var captor = ArgumentCaptor.forClass(ConversionRatePayload.class);
        verify(request).setAttribute(eq(RequestAttributeUtil.PAYLOAD_MODEL), captor.capture());
        assertEquals(payload, captor.getValue());
    }

    @Test
    void initializePayload_shouldDoNothing_WhenRequestMethodIsUnsupported() {
        doReturn(POST.toString()).when(request).getMethod();

        filter.initializePayload(request, response);

        verify(request, times(2)).getMethod();
        verify(request, never()).getParameter(anyString());
    }

    @Test
    void validatePayload_ShouldReturnNoErrors_WhenGetValidRequest() {
        final var payload = getValidIdentifier();
        doReturn(GET.toString()).when(request).getMethod();
        doReturn(payload).when(request).getAttribute(RequestAttributeUtil.PAYLOAD_MODEL);
        doReturn(Optional.empty()).when(validator).validateIdentifier(payload.identifier());

        var validationResult = filter.validatePayload(request, response);

        verify(request).getAttribute(RequestAttributeUtil.PAYLOAD_MODEL);
        verify(validator).validateIdentifier(payload.identifier());
        assertTrue(validationResult.isValid());
    }

    @Test
    void validatePayload_ShouldReturnErrors_WhenGetInvalidRequest() {
        var payload = getInvalidIdentifier();
        doReturn(GET.toString()).when(request).getMethod();
        doReturn(payload).when(request).getAttribute(RequestAttributeUtil.PAYLOAD_MODEL);
        doReturn(Optional.of(getError())).when(validator).validateIdentifier(payload.identifier());

        var validationResult = filter.validatePayload(request, response);

        assertEquals(1, validationResult.getErrors().size());
        assertEquals(validationResult.getErrors().get(0), getError());
        verify(validator, times(1)).validateIdentifier(payload.identifier());
    }

    @Test
    void validatePayload_ShouldReturnNoErrors_WhenPatchValidRequest() {
        final var payload = getValidPayload();
        doReturn(PATCH.toString()).when(request).getMethod();
        doReturn(payload).when(request).getAttribute(RequestAttributeUtil.PAYLOAD_MODEL);
        doReturn(new ValidationResult()).when(validator).validate(payload);

        var validationResult = filter.validatePayload(request, response);

        verify(validator).validate(payload);
        assertTrue(validationResult.isValid());
    }

    @Test
    void validatePayload_ShouldReturnErrors_WhenPatchInvalidRequest() {
        final var expectedValidationResult = new ValidationResult();
        final var payload = getInvalidPayloadWithBadRate();
        expectedValidationResult.add(getError());
        doReturn(PATCH.toString()).when(request).getMethod();
        doReturn(payload).when(request).getAttribute(RequestAttributeUtil.PAYLOAD_MODEL);
        doReturn(expectedValidationResult).when(validator).validate(payload);

        var actualValidationResult = filter.validatePayload(request, response);

        assertEquals(1, actualValidationResult.getErrors().size());
        assertEquals(actualValidationResult.getErrors().get(0), expectedValidationResult.getErrors().get(0));
        verify(validator, times(1)).validate(payload);
    }

    private Error getError() {
        return Error.of("ConversionRate test error");
    }

    private ConversionRateIdentifierPayload getValidIdentifier() {
        return new ConversionRateIdentifierPayload("USDEUR");
    }

    private ConversionRateIdentifierPayload getInvalidIdentifier() {
        return new ConversionRateIdentifierPayload("Invalid");
    }

    private ConversionRatePayload getValidPayload() {
        String from = "USD";
        String to = "EUR";
        BigDecimal rate = BigDecimal.valueOf(2.0);
        return new ConversionRatePayload(from, to, rate);
    }

    private ConversionRatePayload getInvalidPayloadWithBadRate() {
        String from = "USE";
        String to = "EUR";
        return new ConversionRatePayload(from, to, BigDecimal.valueOf(-100));
    }
}