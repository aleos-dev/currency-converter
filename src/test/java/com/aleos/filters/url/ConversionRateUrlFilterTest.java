package com.aleos.filters.url;

import com.aleos.models.dtos.in.ConversionRateIdentifierPayload;
import com.aleos.models.dtos.in.ConversionRatePayload;
import com.aleos.models.dtos.out.Error;
import com.aleos.util.AttributeNameUtil;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import static com.aleos.servlets.common.HttpMethod.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
        mocks = MockitoAnnotations.openMocks(this);
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
        verify(request).setAttribute(eq(AttributeNameUtil.PAYLOAD_MODEL_ATTR), captor.capture());
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
        verify(request).setAttribute(eq(AttributeNameUtil.PAYLOAD_MODEL_ATTR), captor.capture());
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
        doReturn(payload).when(request).getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);
        doReturn(Optional.empty()).when(validator).validateIdentifier(payload.identifier());

        var validationResult = filter.validatePayload(request, response);

        verify(request).getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);
        verify(validator).validateIdentifier(payload.identifier());
        assertTrue(validationResult.isValid());
    }

    @Test
    void validatePayload_ShouldReturnErrors_WhenGetInvalidRequest() {
        var payload = getInvalidIdentifier();
        doReturn(GET.toString()).when(request).getMethod();
        doReturn(payload).when(request).getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);
        doReturn(Optional.of(getError())).when(validator).validateIdentifier(payload.identifier());

        var validationResult = filter.validatePayload(request, response);

        assertEquals(1, validationResult.getErrors().size());
        assertEquals(validationResult.getErrors().get(0), getError());
        verify(validator, times(1)).validateIdentifier(payload.identifier());
    }

    @Test
    void validatePayload_ShouldReturnNoErrors_WhenPatchValidRequest() {
        final var payload = getValidPayload();
        final var spy = spy(filter);
        doReturn(PATCH.toString()).when(request).getMethod();
        doReturn(payload).when(spy).getPayloadAttribute(ConversionRatePayload.class, request);
        doReturn(new ValidationResult<>()).when(validator).validate(payload);

        var validationResult = spy.validatePayload(request, response);

        verify(spy).getPayloadAttribute(ConversionRatePayload.class, request);
        verify(validator).validate(payload);
        assertTrue(validationResult.isValid());
    }

    @Test
    void validatePayload_ShouldReturnErrors_WhenPatchInvalidRequest() {
        final var expectedValidationResult = new ValidationResult<Error>();
        expectedValidationResult.add(getError());
        final var payload = getInvalidPayloadWithBadRate();
        final var spy = spy(filter);
        doReturn(PATCH.toString()).when(request).getMethod();
        doReturn(payload).when(spy).getPayloadAttribute(ConversionRatePayload.class, request);
        doReturn(expectedValidationResult).when(validator).validate(payload);

        var actualValidationResult = spy.validatePayload(request, response);

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
        return new ConversionRatePayload(from, to, null);
    }
}