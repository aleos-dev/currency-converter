package com.aleos.filters.common;

import com.aleos.util.PropertiesUtil;
import jakarta.servlet.*;

import java.io.IOException;

public class EncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        String charset = PropertiesUtil.getProperty("response.charset");
        req.setCharacterEncoding(charset);
        resp.setCharacterEncoding(charset);

        chain.doFilter(req, resp);
    }
}
