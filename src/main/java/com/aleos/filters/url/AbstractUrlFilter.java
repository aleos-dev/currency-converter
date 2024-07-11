package com.aleos.filters.url;

import com.aleos.exceptions.PayloadCastException;
import com.aleos.exceptions.servlets.PayloadNotFoundException;
import com.aleos.filters.AbstractBaseFilter;
import com.aleos.models.dtos.out.Error;
import com.aleos.util.AttributeNameUtil;
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
            setResponseAttribute(validationResult.getErrors(), httpReq);
            httpResp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        chain.doFilter(req, resp);
    }

    protected <T> T getPayloadAttribute(Class<T> clazz, HttpServletRequest req) {
        Object rawPayload = req.getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);

        if (rawPayload == null) {
            throw new PayloadNotFoundException("Payload is not found for %s".formatted(clazz.getSimpleName()));

        } else if (!clazz.isInstance(rawPayload)) {
            throw new PayloadCastException("Payload type is incorrect for %s. Expected: %s, Found: %s".formatted(
                    this.getClass().getSimpleName(), clazz.getSimpleName(), rawPayload.getClass().getSimpleName()));
        } else {
            return clazz.cast(rawPayload);
        }
    }

    protected void setPayloadAttribute(Object payload, HttpServletRequest req) {
        req.setAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR, payload);
    }

    protected void setResponseAttribute(Object response, HttpServletRequest req) {
        req.setAttribute(AttributeNameUtil.RESPONSE_MODEL_ATTR, response);
    }

    protected Optional<String> getPathInfo(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        return pathInfo == null || pathInfo.isEmpty()
                ? Optional.empty()
                : Optional.of(pathInfo.substring(1)); // Remove the leading slash
    }
}
