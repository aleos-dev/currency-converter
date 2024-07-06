package com.aleos.filters.common;

import com.aleos.util.PropertiesUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        String allowOrigin = PropertiesUtil.getProperty("response.cors.allowOrigin");
        ((HttpServletResponse) resp).setHeader("Access-Control-Allow-Origin", allowOrigin);

        chain.doFilter(req, resp);
    }
}
