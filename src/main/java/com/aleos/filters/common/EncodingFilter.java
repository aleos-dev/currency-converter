package com.aleos.filters.common;

import com.aleos.util.PropertiesUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class EncodingFilter extends HttpFilter {

    @Override
    public void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        String charset = PropertiesUtil.getProperty("response.charset");
        req.setCharacterEncoding(charset);
        resp.setCharacterEncoding(charset);

        chain.doFilter(req, resp);
    }
}
