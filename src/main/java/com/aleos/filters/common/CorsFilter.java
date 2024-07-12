package com.aleos.filters.common;

import com.aleos.util.PropertiesUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CorsFilter extends HttpFilter {

    @Override
    public void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        String allowOrigin = PropertiesUtil.getProperty("response.cors.allowOrigin");
        resp.setHeader("Access-Control-Allow-Origin", allowOrigin);

        chain.doFilter(req, resp);
    }
}
