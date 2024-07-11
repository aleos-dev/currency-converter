package com.aleos.filters.url;

import com.aleos.models.dtos.in.ConversionPayload;
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

import java.util.Optional;

import static com.aleos.servlets.common.HttpMethod.GET;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ConversionUrlFilterTest {

    private static final String FROM = "USD";
    private static final String TO = "EUR";
    private static final String AMOUNT = "100.0";
    private static final String METHOD_GET = "GET";

    @InjectMocks
    private ConversionUrlFilter conversionUrlFilter;

    @Mock
    private ConversionRateValidator validator;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

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
    void initializePayload_shouldSetPayloadAttributeSuccessfully() {
        doReturn(FROM).when(request).getParameter("from");
        doReturn(TO).when(request).getParameter("to");
        doReturn(AMOUNT).when(request).getParameter("amount");

        conversionUrlFilter.initializePayload(request, response);

        var payloadCaptor = ArgumentCaptor.forClass(ConversionPayload.class);
        verify(request).setAttribute(eq(AttributeNameUtil.PAYLOAD_MODEL_ATTR), payloadCaptor.capture());
        var payload = payloadCaptor.getValue();
        assertEquals(FROM, payload.baseCurrencyCode());
        assertEquals(TO, payload.targetCurrencyCode());
        assertEquals(Double.parseDouble(AMOUNT), payload.amount());
    }

    @Test
    void initializePayload_shouldThrowNumberFormatException_WhenAmountIsNotDouble() {
        final String INVALID_AMOUNT = "one";
        final var spy = spy(conversionUrlFilter);
        doReturn(GET.toString()).when(request).getMethod();
        doReturn(INVALID_AMOUNT).when(request).getParameter("amount");

        assertThrows(NumberFormatException.class, () -> spy.initializePayload(request, response));
    }

    @Test
    void validatePayload_shouldReturnEmptyList_WhenRequestIsGetTypeAndValid() {
        final ConversionPayload payload = new ConversionPayload(FROM, TO, Double.parseDouble(AMOUNT));
        final var identifier = FROM + TO;
        var spy = spy(conversionUrlFilter);
        doReturn(METHOD_GET).when(request).getMethod();
        doReturn(payload).when(spy).getPayloadAttribute(ConversionPayload.class, request);
        doReturn(Optional.empty()).when(validator).validateIdentifier(identifier);

        ValidationResult<Error> validationResult = spy.validatePayload(request, response);

        verify(spy, times(1)).getPayloadAttribute(ConversionPayload.class, request);
        verify(validator, times(1)).validateIdentifier(identifier);
        assertTrue(validationResult.isValid());
    }

    @Test
    void validatePayload_shouldReturnErrorsForInvalidCode() {
        final var payload = new ConversionPayload(FROM, TO, Double.parseDouble(AMOUNT));
        final var identifier = FROM + TO;
        final var message = "Invalid currency code.";
        final var errorResponse = Error.of(message);
        doReturn(METHOD_GET).when(request).getMethod();
        doReturn(payload).when(request).getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);
        doReturn(Optional.of(errorResponse)).when(validator).validateIdentifier(identifier);

        ValidationResult<Error> validationResult = conversionUrlFilter.validatePayload(request, response);

        verify(request, times(1)).getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);
        verify(validator, times(1)).validateIdentifier(identifier);
        assertEquals(message, validationResult.getErrors().get(0).getMessage());
    }

    @Test
    void validatePayload_shouldReturnEmptyList_WhenHttpMethodIsNotSupported() {
        final String METHOD_POST = "POST";
        doReturn(METHOD_POST).when(request).getMethod();

        ValidationResult<Error> validationResult = conversionUrlFilter.validatePayload(request, response);

        assertTrue(validationResult.isValid());
    }
}