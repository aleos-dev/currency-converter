package com.aleos.filter.common;

import com.aleos.util.PropertiesUtil;
import com.aleos.util.RequestAttributeUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Mockito.*;

class JsonResponseFilterTest {

    @InjectMocks
    JsonResponseFilter jsonResponseFilter;

    @Mock
    ObjectMapper objectMapper;

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
    void doFilter_WriteResponseInJson_IfPresent() throws ServletException, IOException {
        final var stub = new Object();
        final var json = "{}";
        final var printWriter = mock(PrintWriter.class);
        doReturn(stub).when(request).getAttribute(RequestAttributeUtil.RESPONSE_MODEL);
        doReturn(json).when(objectMapper).writeValueAsString(stub);
        doReturn(printWriter).when(response).getWriter();

        jsonResponseFilter.doFilter(request, response, chain);

        verify(objectMapper).writeValueAsString(stub);
        verify(response).getWriter();
        verify(response).setContentType(PropertiesUtil.getProperty("response.contentType"));
    }

    @Test
    void doFilter_DoNothing_IfResponseObjectIsNotPresent() throws ServletException, IOException {
        doReturn(null).when(request).getAttribute(RequestAttributeUtil.RESPONSE_MODEL);

        jsonResponseFilter.doFilter(request, response, chain);

        verify(objectMapper, never()).writeValueAsString(any());
        verify(response, never()).getWriter();
        verify(response, never()).setContentType(anyString());
    }
}