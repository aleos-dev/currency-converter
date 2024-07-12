package com.aleos.filters.url;

import com.aleos.filters.AbstractBaseFilter;
import com.aleos.models.dtos.out.Error;
import com.aleos.util.RequestAttributeUtil;
import com.aleos.validators.ValidationResult;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

public abstract class AbstractUrlFilter extends AbstractBaseFilter {

    protected abstract void initializePayload(HttpServletRequest req, HttpServletResponse resp);

    protected abstract ValidationResult<Error> validatePayload(HttpServletRequest req, HttpServletResponse resp);

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        var httpReq = (HttpServletRequest) req;
        var httpResp = (HttpServletResponse) resp;

        initializePayload(httpReq, httpResp);

        ValidationResult<Error> validationResult = validatePayload(httpReq, httpResp);

        if (validationResult.hasErrors()) {
            RequestAttributeUtil.setResponse(httpReq, validationResult.getErrors());
            httpResp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        chain.doFilter(req, resp);
    }

    protected Optional<String> getPathInfo(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        return pathInfo == null || pathInfo.isEmpty()
                ? Optional.empty()
                : Optional.of(pathInfo.substring(1)); // Remove the leading slash
    }
}
