package com.aleos.servlets.filters;

import com.aleos.services.CacheService;
import com.aleos.services.CacheService.CacheEntry;
import com.aleos.servlets.ResponseWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

import static java.util.Objects.isNull;

public class CachingFilter implements Filter {

    private CacheService cacheService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        Filter.super.init(filterConfig);
        cacheService = (CacheService) filterConfig.getServletContext().getAttribute("cacheService");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String pathInfo = ((HttpServletRequest) request).getPathInfo();

        pathInfo = isNull(pathInfo) ? "" : pathInfo;

        var responseWrapper = ((ResponseWrapper) response);

        if (cacheService.contains(pathInfo)) {
            prepareResponseFromCache(responseWrapper, pathInfo);
            return;
        }

        chain.doFilter(request, response);

        putResponseObjectToCache(responseWrapper, pathInfo);
    }

    private void putResponseObjectToCache(ResponseWrapper responseWrapper, String pathInfo) {

        if (responseWrapper.getResponseObject().isPresent()) {
            cacheService.put(pathInfo,
                    new CacheEntry(responseWrapper.getStatus(), responseWrapper.getResponseObject().get()));
        }
    }

    private void prepareResponseFromCache(ResponseWrapper responseWrapper, String pathInfo) {

        CacheEntry cache = cacheService.get(pathInfo);

        responseWrapper.setResponseObject(cache.content());
        responseWrapper.setStatus(cache.status());
    }
}

