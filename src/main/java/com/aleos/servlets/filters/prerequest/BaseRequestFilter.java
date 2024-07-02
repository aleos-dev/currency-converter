package com.aleos.servlets.filters.prerequest;

import com.aleos.models.dtos.out.ErrorResponse;
import com.aleos.util.AttributeNameUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

public abstract class BaseRequestFilter implements Filter {

    protected static final String METHOD_IS_NOT_SUPPORTED = "%s is not supported.";

    protected static final String INVALID_PATH_ERROR = "Invalid path info for current URL.";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        var httpRequest = (HttpServletRequest) request;
        var httpResponse = ((HttpServletResponse) response);

        putPayloadToContext(httpRequest, httpResponse);

        List<ErrorResponse> errors = validateRequest(httpRequest, httpResponse);

        if (errors.isEmpty()) {

            chain.doFilter(request, response);
            return;
        }

        httpRequest.setAttribute(AttributeNameUtil.RESPONSE_MODEL_ATTR, errors);
        httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    protected abstract void putPayloadToContext(HttpServletRequest request, HttpServletResponse response);

    protected abstract List<ErrorResponse> validateRequest(HttpServletRequest request, HttpServletResponse response);
}
