package com.aleos.servlets.filters;

import com.aleos.models.dtos.CurrencyPayload;
import com.aleos.models.dtos.ErrorResponse;
import com.aleos.util.AttributeNameUtil;
import com.aleos.validators.CurrencyPayloadValidator;
import com.aleos.validators.PayloadValidator;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;

public class CurrenciesRequestProcessingFilter extends BaseRequestProcessingFilter {

    private static final List<String> SUPPORTED_METHODS = List.of("GET", "POST");

    private static final String URL_ERROR_MESSAGE = "URL is incorrect, doesn't expect any path info: %s";

    private PayloadValidator<CurrencyPayload, ErrorResponse> currencyPayloadValidator;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        super.init(filterConfig);
        currencyPayloadValidator = (CurrencyPayloadValidator) filterConfig.getServletContext()
                .getAttribute(AttributeNameUtil.getName(CurrencyPayloadValidator.class));
    }

    @Override
    protected void extractPayloadToContext(ServletRequest request) {

        var payload = new CurrencyPayload(
                request.getParameter("name"),
                request.getParameter("code"),
                request.getParameter("sign"));

        request.setAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR, payload);
    }

    @Override
    protected List<ErrorResponse> validateRequest(ServletRequest request) {

        var httpRequest = (HttpServletRequest) request;

        Optional<String> method = SUPPORTED_METHODS.stream()
                .filter(httpRequest.getMethod()::equalsIgnoreCase)
                .findAny();

        if (method.isEmpty()) {
            return List.of(new ErrorResponse(METHOD_NOT_SUPPORTED.formatted(httpRequest.getMethod())));
        }

        String pathInfo = (httpRequest).getPathInfo();
        if (nonNull(pathInfo)) {
            return List.of(new ErrorResponse(URL_ERROR_MESSAGE.formatted(pathInfo)));
        }

        if (method.get().equals("GET")) {
            return Collections.emptyList();
        }

        var payload = (CurrencyPayload) request.getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);

        return currencyPayloadValidator.validate(payload);
    }
}
