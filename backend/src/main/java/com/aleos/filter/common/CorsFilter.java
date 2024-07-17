package com.aleos.filter.common;

import com.aleos.util.PropertiesUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CorsFilter extends HttpFilter {

    private static final String ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
    private static final String ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";

    private static final String ALLOW_ORIGIN_PROPERTY = "cors.allowOrigin";
    private static final String ALLOW_METHODS_PROPERTY = "cors.allowMethods";

    private final String allowOrigin;
    private final String allowedMethods;

    public CorsFilter() {
        allowOrigin = PropertiesUtil.getProperty(ALLOW_ORIGIN_PROPERTY);
        allowedMethods = PropertiesUtil.getProperty(ALLOW_METHODS_PROPERTY);
    }

    @Override
    public void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        resp.setHeader(ALLOW_ORIGIN_HEADER, allowOrigin);
        resp.setHeader(ALLOW_METHODS_HEADER, allowedMethods);

        chain.doFilter(req, resp);
    }
}
