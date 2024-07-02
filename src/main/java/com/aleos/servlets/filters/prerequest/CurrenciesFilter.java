package com.aleos.servlets.filters.prerequest;

import com.aleos.models.dtos.in.CurrencyPayload;
import com.aleos.models.dtos.out.ErrorResponse;
import com.aleos.util.AttributeNameUtil;
import com.aleos.validators.CurrencyValidator;
import com.aleos.validators.PayloadValidator;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collections;
import java.util.List;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static java.util.Objects.nonNull;

public class CurrenciesFilter extends BaseRequestFilter {

    private PayloadValidator<CurrencyPayload, ErrorResponse> currencyValidator;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        super.init(filterConfig);
        currencyValidator = (CurrencyValidator) filterConfig.getServletContext()
                .getAttribute(AttributeNameUtil.getName(CurrencyValidator.class));
    }

    @Override
    protected void putPayloadToContext(HttpServletRequest request, HttpServletResponse response) {

        var payload = new CurrencyPayload(
                request.getParameter("name"),
                request.getParameter("code"),
                request.getParameter("sign"));

        request.setAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR, payload);
    }

    @Override
    protected List<ErrorResponse> validateRequest(HttpServletRequest request, HttpServletResponse response) {

        String pathInfo = (request).getPathInfo();
        if (nonNull(pathInfo)) {
            response.setStatus(SC_BAD_REQUEST);
            return List.of(new ErrorResponse(INVALID_PATH_ERROR));
        }

        String methodType = request.getMethod();

        if ("GET".equals(methodType)) {
            return Collections.emptyList();
        }

        if ("POST".equals(methodType)) {
            var payload = (CurrencyPayload) request.getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);

            return currencyValidator.validate(payload);
        }

        response.setStatus(SC_METHOD_NOT_ALLOWED);
        return List.of(new ErrorResponse(METHOD_IS_NOT_SUPPORTED.formatted(methodType)));
    }
}
