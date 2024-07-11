package com.aleos.filters.url;

import com.aleos.models.dtos.in.CurrencyIdentifierPayload;
import com.aleos.models.dtos.out.Error;
import com.aleos.util.AttributeNameUtil;
import com.aleos.validators.CurrencyValidator;
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
import static com.aleos.servlets.common.HttpMethod.POST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class CurrencyUrlFilterTest {

    @InjectMocks
    CurrencyUrlFilter currencyUrlFilter;

    @Mock
    CurrencyValidator validator;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

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
        final String CORRECT_PATH_INFO = "/USD";
        doReturn(GET.toString()).when(request).getMethod();
        doReturn(CORRECT_PATH_INFO).when(request).getPathInfo();

        currencyUrlFilter.initializePayload(request, response);

        ArgumentCaptor<CurrencyIdentifierPayload> responseCaptor = ArgumentCaptor.forClass(CurrencyIdentifierPayload.class);
        verify(request).setAttribute(eq(AttributeNameUtil.PAYLOAD_MODEL_ATTR), responseCaptor.capture());
        CurrencyIdentifierPayload payload = responseCaptor.getValue();
        assertEquals(CORRECT_PATH_INFO.substring(1), payload.identifier());
    }

    @Test
    void initializePayload_shouldDoNothing_WhenRequestMethodIsUnsupported() {
        doReturn(POST.toString()).when(request).getMethod();

        currencyUrlFilter.initializePayload(request, response);

        verify(request).getMethod();
        verify(request, never()).getParameter(anyString());
    }

    @Test
    void validatePayload_ShouldReturnNoErrors_WhenPayloadIsValid() {
        final var payload = getValidPayload();
        doReturn(GET.toString()).when(request).getMethod();
        doReturn(payload).when(request).getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);
        doReturn(Optional.empty()).when(validator).validateIdentifier(payload.identifier());

        var validationResult = currencyUrlFilter.validatePayload(request, response);

        verify(request).getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);
        assertTrue(validationResult.isValid());
    }

    @Test
    void validatePayload_ReturnErrors_WhenIdentifierIsInvalid() {
        CurrencyIdentifierPayload payload = getInvalidPayload();
        doReturn(GET.toString()).when(request).getMethod();
        doReturn(payload).when(request).getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);
        doReturn(Optional.of(getError())).when(validator).validateIdentifier(payload.identifier());

        var validationResult = currencyUrlFilter.validatePayload(request, response);

        assertEquals(1, validationResult.getErrors().size());
        assertEquals(validationResult.getErrors().get(0), getError());
        verify(validator, times(1)).validateIdentifier(payload.identifier());
    }

    private Error getError() {
        return Error.of("Currency test error");
    }

    private CurrencyIdentifierPayload getValidPayload() {
        return new CurrencyIdentifierPayload("EUR");
    }

    private CurrencyIdentifierPayload getInvalidPayload() {
        return new CurrencyIdentifierPayload("invalid");
    }
}
