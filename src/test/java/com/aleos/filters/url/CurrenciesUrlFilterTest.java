package com.aleos.filters.url;

import com.aleos.models.dtos.in.CurrencyPayload;
import com.aleos.models.dtos.out.Error;
import com.aleos.util.AttributeNameUtil;
import com.aleos.validators.CurrencyValidator;
import com.aleos.validators.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.aleos.servlets.common.HttpMethod.GET;
import static com.aleos.servlets.common.HttpMethod.POST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.internal.matchers.Any.ANY;

class CurrenciesUrlFilterTest {

    @InjectMocks
    CurrenciesUrlFilter currenciesUrlFilter;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    CurrencyValidator validator;

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
        final var spy = spy(currenciesUrlFilter);
        final var payload = getUsdPayload();
        doReturn(payload.name()).when(request).getParameter("name");
        doReturn(payload.code()).when(request).getParameter("code");
        doReturn(payload.sign()).when(request).getParameter("sign");
        doReturn(POST.toString()).when(request).getMethod();

        spy.initializePayload(request, response);

        verify(spy, times(1)).setPayloadAttribute(payload, request);
        verify(request, times(1)).setAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR, payload);
    }

    @Test
    void initializePayload_ShouldDoNothing_WhenNotPostRequest() {
        final var spy = spy(currenciesUrlFilter);
        doReturn(GET.toString()).when(request).getMethod();

        spy.initializePayload(request, response);

        verify(spy, never()).setPayloadAttribute(ANY, request);
    }

    @Test
    void validatePayload_ShouldReturnNoErrors_WhenPayloadIsValid() {
        final var payload = getUsdPayload();
        final var spy = spy(currenciesUrlFilter);
        doReturn(POST.toString()).when(request).getMethod();
        doReturn(payload).when(spy).getPayloadAttribute(CurrencyPayload.class, request);
        doReturn(new ValidationResult<>()).when(validator).validate(payload);

        var validationResult = spy.validatePayload(request, response);

        verify(spy).getPayloadAttribute(CurrencyPayload.class, request);
        verify(validator).validate(payload);
        assertTrue(validationResult.isValid());
    }

    @Test
    void validatePayload_ShouldReturnErrors_WhenPayloadIsInvalid() {
        final var expectedValidationResult = new ValidationResult<Error>();
        expectedValidationResult.add(getError());
        final var payload = getInvalidPayload();
        final var spy = spy(currenciesUrlFilter);
        doReturn(POST.toString()).when(request).getMethod();
        doReturn(payload).when(spy).getPayloadAttribute(CurrencyPayload.class, request);
        doReturn(expectedValidationResult).when(validator).validate(payload);

        var actualValidationResult = spy.validatePayload(request, response);

        verify(spy).getPayloadAttribute(CurrencyPayload.class, request);
        verify(validator).validate(payload);
        assertEquals(1, actualValidationResult.getErrors().size());
        assertEquals(actualValidationResult.getErrors().get(0), expectedValidationResult.getErrors().get(0));
    }

    private Error getError() {
        return Error.of("Currencies test error");
    }

    private CurrencyPayload getUsdPayload() {
        String name = "Dollar";
        String code = "USD";
        String sign = "$";
        return new CurrencyPayload(name, code, sign);
    }

    private CurrencyPayload getInvalidPayload() {
        String name = "Invalid dollar";
        String code = "Invalid";
        String sign = "$";
        return new CurrencyPayload(name, code, sign);
    }
}