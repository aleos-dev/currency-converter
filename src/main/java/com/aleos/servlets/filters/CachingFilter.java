package com.aleos.servlets.filters;

import com.aleos.services.CacheService;
import com.aleos.services.CacheService.CacheEntry;
import com.aleos.util.AttributeNameUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.aleos.util.AttributeNameUtil.RESPONSE_MODEL_ATTR;
import static java.util.Objects.nonNull;

public class CachingFilter implements Filter {

    private static final String GET_METHOD = "GET";

    private CacheService cacheService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        Filter.super.init(filterConfig);
        cacheService = (CacheService) filterConfig.getServletContext()
                .getAttribute(AttributeNameUtil.getName(CacheService.class));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        var httpRequest = (HttpServletRequest) request;
        String cacheKey = generateCacheKey(httpRequest);

        if (!GET_METHOD.equalsIgnoreCase(httpRequest.getMethod())) {

            // Invalidate the cache before processing the request
            cacheService.remove(cacheKey);

            chain.doFilter(request, response);
            return;
        }


        var httpResponse = ((HttpServletResponse) response);

        if (cacheService.contains(cacheKey)) {
            prepareResponseFromCache(httpRequest, httpResponse, cacheKey);
            return;
        }

        chain.doFilter(request, response);

        putResponseObjectToCache(httpRequest, httpResponse, cacheKey);

    }
    
    private String generateCacheKey(HttpServletRequest request) {
        
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();

        return requestUri + (queryString != null ? "?" + queryString : "");
    }

    private void putResponseObjectToCache(HttpServletRequest request, HttpServletResponse response, String pathInfo) {

        Object responseObject = request.getAttribute(RESPONSE_MODEL_ATTR);
        if (nonNull(responseObject)) {
            cacheService.put(pathInfo, new CacheEntry(response.getStatus(), responseObject));
        }
    }

    private void prepareResponseFromCache(HttpServletRequest request, HttpServletResponse response, String pathInfo) {

        CacheEntry cache = cacheService.get(pathInfo);

        request.setAttribute(RESPONSE_MODEL_ATTR, cache.content());
        response.setStatus(cache.status());
    }
}

