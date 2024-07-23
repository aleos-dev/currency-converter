package com.aleos.filter.url;

import com.aleos.model.dto.out.Error;
import com.aleos.util.RequestAttributeUtil;
import com.aleos.validator.ValidationResult;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class AbstractUrlFilterTest {

    @InjectMocks
    CurrencyUrlFilter currencyUrlFilter;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    FilterChain chain;

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
    void doFilter_CallNextFilter_WhenNoErrorsAreFound() throws IOException, ServletException {
        final var spy = spy(currencyUrlFilter);
        doNothing().when(spy).initializePayload(request, response);
        doReturn(new ValidationResult()).when(spy).validatePayload(request, response);

        spy.doFilter(request, response, chain);

        verify(spy, times(1)).initializePayload(request, response);
        verify(spy, times(1)).validatePayload(request, response);
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_neverCalledNextFilter_WhenErrorsOccur() throws IOException, ServletException {
        final var validationResult = new ValidationResult();
        final var spy = spy(currencyUrlFilter);
        validationResult.add(Error.of("Abstract test error"));
        doNothing().when(spy).initializePayload(request, response);
        doReturn(validationResult).when(spy).validatePayload(request, response);

        spy.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(request).setAttribute(eq(RequestAttributeUtil.RESPONSE_MODEL), any());
        verify(chain, never()).doFilter(request, response);
    }
}