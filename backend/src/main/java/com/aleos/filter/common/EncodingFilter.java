package com.aleos.filter.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EncodingFilter extends HttpFilter {

    @Override
    public void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        String charset = StandardCharsets.UTF_8.name();
        req.setCharacterEncoding(charset);
        resp.setCharacterEncoding(charset);

        chain.doFilter(req, resp);
    }
}
