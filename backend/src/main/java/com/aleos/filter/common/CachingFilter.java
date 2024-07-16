package com.aleos.filter.common;

import com.aleos.filter.AbstractBaseFilter;
import com.aleos.service.CacheService;
import com.aleos.service.CacheService.CacheEntry;
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

import static com.aleos.servlet.common.HttpMethod.*;

public class CachingFilter extends AbstractBaseFilter {

    private static final Logger LOGGER = Logger.getLogger(CachingFilter.class.getName());

    private static final String NOT_CACHING_REGEX_FORMAT = ".*%s$";

    private static final Set<String> INVALIDATING_METHODS = Set.of(
            POST.name(),
            PATCH.name(),
            PUT.name(),
            DELETE.name());

    private final boolean isEnabled;

    public CachingFilter() {
        isEnabled = Boolean.parseBoolean(PropertiesUtil.getProperty("service.cache.enable"));
    }

    protected transient CacheService cacheService;

    @Override
    public void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        if (!isEnabled) {
            chain.doFilter(req, resp);
            return;
        }

        String method = req.getMethod();

        if (isInvalidatingMethod(method)) {
            // Invalidate the cache before processing the request
            clearInvalidatingCache(req);

            chain.doFilter(req, resp);
            return;
        }

        var currentUri = req.getRequestURI();
        var notCachingRegex = NOT_CACHING_REGEX_FORMAT
                .formatted(PropertiesUtil.getProperty("servlet.conversion.url"));

        if (currentUri.matches(notCachingRegex) || !isCacheableMethod(method)) {
            chain.doFilter(req, resp);
            return;
        }

        var cacheKey = getKey(req);
        if (cacheService.contains(cacheKey)) {
            prepareResponseFromCache(req, resp, cacheKey);
            return;
        }

        chain.doFilter(req, resp);
        putResponseObjectToCache(req, resp, cacheKey);
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

    private void clearInvalidatingCache(HttpServletRequest req) {
        cacheService.remove(getKey(req));

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Cache invalidated due to modifying method: %s, key: %s "
                    .formatted(req.getMethod(), getKey(req)));
        }
    }

    private boolean isInvalidatingMethod(String method) {
        return INVALIDATING_METHODS.contains(method.toUpperCase());
    }

    private boolean isCacheableMethod(String method) {
        return GET.toString().equalsIgnoreCase(method);
    }
}

