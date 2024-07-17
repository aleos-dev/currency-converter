package com.aleos.filter.common;

import com.aleos.util.PropertiesUtil;
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

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

class CorsFilterTest {

    @InjectMocks
    private CorsFilter corsFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

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
    void doFilter_ShouldSetCorsHeader() throws ServletException, IOException {

        corsFilter.doFilter(request, response, chain);

        verify(response)
                .setHeader("Access-Control-Allow-Origin", PropertiesUtil.getProperty("cors.allowOrigin"));
        verify(chain).doFilter(request, response);
    }
}
