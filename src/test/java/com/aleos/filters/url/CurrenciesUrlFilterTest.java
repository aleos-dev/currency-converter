package com.aleos.filters.url;

import com.aleos.models.dtos.in.CurrencyPayload;
import com.aleos.models.dtos.out.Error;
import com.aleos.util.RequestAttributeUtil;
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
        final var payload = getUsdPayload();
        doReturn(payload.name()).when(request).getParameter("name");
        doReturn(payload.code()).when(request).getParameter("code");
        doReturn(payload.sign()).when(request).getParameter("sign");
        doReturn(POST.toString()).when(request).getMethod();

        currenciesUrlFilter.initializePayload(request, response);

        verify(request, times(1)).setAttribute(RequestAttributeUtil.PAYLOAD_MODEL, payload);
    }

    @Test
    void initializePayload_ShouldDoNothing_WhenNotPostRequest() {
        doReturn(GET.toString()).when(request).getMethod();

        currenciesUrlFilter.initializePayload(request, response);

        verify(request, never()).getParameter(any());
    }

    @Test
    void validatePayload_ShouldReturnNoErrors_WhenPayloadIsValid() {
        final var payload = getUsdPayload();
        doReturn(POST.toString()).when(request).getMethod();
        doReturn(new ValidationResult<>()).when(validator).validate(any());
        doReturn(payload).when(request).getAttribute(RequestAttributeUtil.PAYLOAD_MODEL);

        var validationResult = currenciesUrlFilter.validatePayload(request, response);

        verify(validator).validate(payload);
        assertTrue(validationResult.isValid());
    }

    @Test
    void validatePayload_ShouldReturnErrors_WhenPayloadIsInvalid() {
        final var expectedValidationResult = new ValidationResult<Error>();
        expectedValidationResult.add(getError());
        final var payload = getInvalidPayload();
        doReturn(POST.toString()).when(request).getMethod();
        doReturn(expectedValidationResult).when(validator).validate(payload);
        doReturn(payload).when(request).getAttribute(RequestAttributeUtil.PAYLOAD_MODEL);

        var actualValidationResult = currenciesUrlFilter.validatePayload(request, response);

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