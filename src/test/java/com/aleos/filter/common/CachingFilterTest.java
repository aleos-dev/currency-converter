package com.aleos.filter.common;

import com.aleos.service.CacheService;
import com.aleos.util.PropertiesUtil;
import com.aleos.util.RequestAttributeUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class CachingFilterTest {

    @InjectMocks
    private CachingFilter cachingFilter;

    @Mock
    private CacheService cacheService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    private AutoCloseable mocks;

    private final String resourceUri = "test-uri";

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

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PATCH", "PUT", "DELETE"})
    void doFilter_ShouldClearCache_ForUpdatableHttpMethods(String method) throws ServletException, IOException {
        setupRequest(method, resourceUri);

        cachingFilter.doFilter(request, response, chain);

        verify(cacheService).remove(resourceUri);
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_ShouldReturnResponseFromCache_IfPresent() throws ServletException, IOException {
        setupRequest("GET", resourceUri);
        doReturn(true).when(cacheService).contains(resourceUri);
        CacheService.CacheEntry cacheEntry = new CacheService.CacheEntry(200, "cached-content");
        doReturn(cacheEntry).when(cacheService).get(resourceUri);

        cachingFilter.doFilter(request, response, chain);

        verify(request).setAttribute(RequestAttributeUtil.RESPONSE_MODEL, "cached-content");
        verify(response).setStatus(200);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_ShouldCacheResponse_ForGetRequest() throws ServletException, IOException {
        final var stub = new Object();
        final int status = 201;
        setupRequest("GET", resourceUri);
        doReturn(false).when(cacheService).contains(resourceUri);
        doReturn(stub).when(request).getAttribute(RequestAttributeUtil.RESPONSE_MODEL);
        doReturn(status).when(response).getStatus();

        cachingFilter.doFilter(request, response, chain);

        verify(request).getAttribute(RequestAttributeUtil.RESPONSE_MODEL);
        ArgumentCaptor<CacheService.CacheEntry> captor = ArgumentCaptor.forClass(CacheService.CacheEntry.class);
        verify(cacheService).put(eq(resourceUri), captor.capture());
        assertEquals(status, captor.getValue().status());
        assertEquals(stub, captor.getValue().content());
    }

    @Test
    void doFilter_ShouldPassThrough_IfNotCacheable() throws IOException, ServletException {
        setupRequest("GET", PropertiesUtil.getProperty("servlet.conversion.url"));

        cachingFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(cacheService, never()).contains(anyString());
    }

    private void setupRequest(String method, String uri) {
        doReturn(method).when(request).getMethod();
        doReturn(uri).when(request).getRequestURI();
    }
}
