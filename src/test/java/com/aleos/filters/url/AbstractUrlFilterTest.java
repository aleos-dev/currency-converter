package com.aleos.filters.url;

import com.aleos.models.dtos.out.Error;
import com.aleos.util.AttributeNameUtil;
import com.aleos.validators.ValidationResult;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.IOException;

import static org.mockito.Mockito.*;

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
        mocks = MockitoAnnotations.openMocks(this);
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
        doReturn(new ValidationResult<>()).when(spy).validatePayload(request, response);

        spy.doFilter(request, response, chain);

        verify(spy, times(1)).initializePayload(request, response);
        verify(spy, times(1)).validatePayload(request, response);
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_neverCalledNextFilter_WhenErrorsOccur() throws IOException, ServletException {
        final var validationResult = new ValidationResult<Error>();
        final var spy = spy(currencyUrlFilter);
        validationResult.add(Error.of("Abstract test error"));
        doNothing().when(spy).initializePayload(request, response);
        doReturn(validationResult).when(spy).validatePayload(request, response);

        spy.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(request).setAttribute(AttributeNameUtil.RESPONSE_MODEL_ATTR, validationResult.getErrors());
        verify(chain, never()).doFilter(request, response);
    }
}