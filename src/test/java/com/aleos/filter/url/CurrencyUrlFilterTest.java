package com.aleos.filter.url;

import com.aleos.model.dto.in.CurrencyIdentifierPayload;
import com.aleos.model.dto.out.Error;
import com.aleos.util.RequestAttributeUtil;
import com.aleos.validator.CurrencyValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static com.aleos.servlet.common.HttpMethod.GET;
import static com.aleos.servlet.common.HttpMethod.POST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

class CurrencyUrlFilterTest {

    @InjectMocks
    CurrencyUrlFilter currencyUrlFilter;

    @Mock
    CurrencyValidator validator;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    AutoCloseable mocks;

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
        final String CORRECT_PATH_INFO = "/USD";
        doReturn(GET.toString()).when(request).getMethod();
        doReturn(CORRECT_PATH_INFO).when(request).getPathInfo();

        currencyUrlFilter.initializePayload(request, response);

        ArgumentCaptor<CurrencyIdentifierPayload> responseCaptor = ArgumentCaptor.forClass(CurrencyIdentifierPayload.class);
        verify(request).setAttribute(eq(RequestAttributeUtil.PAYLOAD_MODEL), responseCaptor.capture());
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
        doReturn(GET.toString()).when(request).getMethod();
        doReturn(Optional.empty()).when(validator).validateIdentifier(any());

        var validationResult = currencyUrlFilter.validatePayload(request, response);

        verify(request).getAttribute(RequestAttributeUtil.PAYLOAD_MODEL);
        assertTrue(validationResult.isValid());
    }

    @Test
    void validatePayload_ReturnErrors_WhenIdentifierIsInvalid() {
        doReturn(GET.toString()).when(request).getMethod();
        doReturn(Optional.of(getError())).when(validator).validateIdentifier(any());

        var validationResult = currencyUrlFilter.validatePayload(request, response);

        assertEquals(1, validationResult.getErrors().size());
        assertEquals(validationResult.getErrors().get(0), getError());
        verify(validator).validateIdentifier(any());
    }

    private Error getError() {
        return Error.of("Currency test error");
    }
}
