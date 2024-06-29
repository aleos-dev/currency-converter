package com.aleos.servlets.filters;

import com.aleos.models.dtos.CurrencyIdentifierPayload;
import com.aleos.models.dtos.ErrorResponse;
import com.aleos.util.AttributeNameUtil;
import com.aleos.validators.CurrencyIdentifierPayloadValidator;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;


public class CurrencyRequestProcessingFilter extends BaseRequestProcessingFilter {

    private static final String SUPPORTED_METHOD = "GET";

    private static final String CURRENCY_REQUEST_REGEX = "^/([a-zA-Z]{3}|\\d+)$";

    private static final Pattern CURRENCY_REQUEST_PATTERN = Pattern.compile(CURRENCY_REQUEST_REGEX);

    private static final String ERROR_MESSAGE = "Invalid path info, it must contains of a valid currency identifier. " +
                                                "Expected pattern: " + CURRENCY_REQUEST_REGEX;

    private CurrencyIdentifierPayloadValidator identifierValidator;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        super.init(filterConfig);
        identifierValidator = (CurrencyIdentifierPayloadValidator) filterConfig.getServletContext().getAttribute(
                AttributeNameUtil.getName(CurrencyIdentifierPayloadValidator.class));
    }

    @Override
    protected void extractPayloadToContext(ServletRequest request) {

        Optional<String> payloadOptional = parseRequest(request);

        payloadOptional.ifPresent(payload ->
                request.setAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR, new CurrencyIdentifierPayload(payload)));
    }

    @Override
    protected List<ErrorResponse> validateRequest(ServletRequest request) {

        var httpRequest = (HttpServletRequest) request;

        if (!SUPPORTED_METHOD.equalsIgnoreCase(httpRequest.getMethod())) {
            return List.of(new ErrorResponse(METHOD_NOT_SUPPORTED.formatted(httpRequest.getMethod())));
        }

        String pathInfo = httpRequest.getPathInfo();

        if (isNull(pathInfo) || !CURRENCY_REQUEST_PATTERN.matcher(pathInfo).matches()) {
            return List.of(new ErrorResponse(ERROR_MESSAGE));
        }

        var payload = (CurrencyIdentifierPayload) request.getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);

        return identifierValidator.validate(payload);
    }

    private Optional<String> parseRequest(ServletRequest request) {

        String pathInfo = ((HttpServletRequest) request).getPathInfo();
        var matcher = CURRENCY_REQUEST_PATTERN.matcher(pathInfo);

        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
