package com.aleos.servlets.filters;

import com.aleos.models.dtos.ErrorResponse;
import com.aleos.util.AttributeNameUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

public abstract class BaseRequestProcessingFilter implements Filter {

    protected static final String METHOD_NOT_SUPPORTED = "%s is not supported.";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        extractPayloadToContext(request);

        List<ErrorResponse> errors = validateRequest(request);

        if (errors.isEmpty()) {

            chain.doFilter(request, response);
            return;
        }

        request.setAttribute(AttributeNameUtil.RESPONSE_MODEL_ATTR, errors);
        ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    protected abstract void extractPayloadToContext(ServletRequest request);

    protected abstract List<ErrorResponse> validateRequest(ServletRequest request);
}
