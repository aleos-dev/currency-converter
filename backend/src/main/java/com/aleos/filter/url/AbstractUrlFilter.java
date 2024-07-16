package com.aleos.filter.url;

import com.aleos.filter.AbstractBaseFilter;
import com.aleos.util.RequestAttributeUtil;
import com.aleos.validator.ValidationResult;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

public abstract class AbstractUrlFilter extends AbstractBaseFilter {

    protected abstract void initializePayload(HttpServletRequest req, HttpServletResponse resp);

    protected abstract ValidationResult validatePayload(HttpServletRequest req, HttpServletResponse resp);

    @Override
    public void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {


        initializePayload(req, resp);

        ValidationResult validationResult = validatePayload(req, resp);

        if (validationResult.hasErrors()) {
            RequestAttributeUtil.setResponse(req, validationResult.getErrors());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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
