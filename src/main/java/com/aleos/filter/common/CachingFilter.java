package com.aleos.filter.common;

import com.aleos.filter.AbstractBaseFilter;
import com.aleos.service.CacheService;
import com.aleos.service.CacheService.CacheEntry;
import com.aleos.servlet.common.HttpMethod;
import com.aleos.util.PropertiesUtil;
import com.aleos.util.RequestAttributeUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CachingFilter extends AbstractBaseFilter {

    private static final Logger LOGGER = Logger.getLogger(CachingFilter.class.getName());

    private transient CacheService cacheService;

    @Override
    public void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        if (isInvalidatingMethod(req)) {
            // Invalidate the cache before processing the request
            clearInvalidatingCache(req);

            chain.doFilter(req, resp);
            return;
        }
        if (isCacheableMethod(req)
                && !req.getRequestURI().contains(PropertiesUtil.getProperty("servlet.conversion.url"))) {
            var cacheKey = getKey(req);

            if (cacheService.contains(cacheKey)) {
                prepareResponseFromCache(req, resp, cacheKey);
                return;
            }

            chain.doFilter(req, resp);
            putResponseObjectToCache(req, resp, cacheKey);
        } else {
            chain.doFilter(req, resp);
        }
    }

    private String getKey(HttpServletRequest req) {
        return req.getRequestURI();
    }

    private void putResponseObjectToCache(HttpServletRequest req, HttpServletResponse resp, String key) {
        RequestAttributeUtil.getResponse(req)
                .ifPresent(toCache -> {
                    cacheService.put(key, new CacheEntry(resp.getStatus(), toCache));

                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info(String.format("Cached response for key: %s", key));
                    }
                });
    }

    private void prepareResponseFromCache(HttpServletRequest req, HttpServletResponse resp, String key) {
        CacheEntry cache = cacheService.get(key);

        RequestAttributeUtil.setResponse(req, cache.content());
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

