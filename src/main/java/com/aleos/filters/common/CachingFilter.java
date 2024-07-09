package com.aleos.filters.common;

import com.aleos.filters.AbstractBaseFilter;
import com.aleos.services.CacheService;
import com.aleos.services.CacheService.CacheEntry;
import com.aleos.servlets.common.HttpMethod;
import com.aleos.util.AttributeNameUtil;
import com.aleos.util.PropertiesUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CachingFilter extends AbstractBaseFilter {

    private static final Logger LOGGER = Logger.getLogger(CachingFilter.class.getName());

    private CacheService cacheService;

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        var httpReq = (HttpServletRequest) req;

        if (isInvalidatingMethod(httpReq)) {
            // Invalidate the cache before processing the request
            clearInvalidatingCache(httpReq);

            chain.doFilter(req, resp);
            return;
        }
        if (isCacheableMethod(httpReq) && !httpReq.getRequestURI().contains(PropertiesUtil.CONVERSION_SERVICE_URL)) {
            var cacheKey = getKey(httpReq);
            var httpResp = ((HttpServletResponse) resp);
            if (cacheService.contains(cacheKey)) {
                prepareResponseFromCache(httpReq, httpResp, cacheKey);
                return;
            }

            chain.doFilter(req, resp);
            putResponseObjectToCache(httpReq, httpResp, cacheKey);
        } else {
            chain.doFilter(req, resp);
        }
    }

    private String getKey(HttpServletRequest req) {
        return req.getRequestURI();
    }

    private void putResponseObjectToCache(HttpServletRequest req, HttpServletResponse resp, String key) {
        Optional.ofNullable(req.getAttribute(AttributeNameUtil.RESPONSE_MODEL_ATTR))
                .ifPresent(toCache -> {
                    cacheService.put(key, new CacheEntry(resp.getStatus(), toCache));
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info(String.format("Cached response for key: %s", key));
                    }
                });
    }

    private void prepareResponseFromCache(HttpServletRequest req, HttpServletResponse resp, String key) {
        CacheEntry cache = cacheService.get(key);
        req.setAttribute(AttributeNameUtil.RESPONSE_MODEL_ATTR, cache.content());
        resp.setStatus(cache.status());
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Prepared response from cache for key: %s".formatted(key));
        }
    }

    private boolean isCacheableMethod(HttpServletRequest req) {
        return HttpMethod.GET.name().equalsIgnoreCase(req.getMethod());
    }

    private boolean isInvalidatingMethod(HttpServletRequest req) {
        return Set.of(
                        HttpMethod.POST.name(),
                        HttpMethod.PATCH.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name())
                .contains(req.getMethod().toUpperCase());
    }

    private void clearInvalidatingCache(HttpServletRequest req) {
        cacheService.remove(getKey(req));
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Cache invalidated due to modifying method: %s, key: %s "
                    .formatted(req.getMethod(), getKey(req)));
        }
    }
}

