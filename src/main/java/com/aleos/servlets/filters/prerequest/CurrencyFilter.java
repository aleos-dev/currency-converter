package com.aleos.servlets.filters.prerequest;

import com.aleos.models.dtos.in.CurrencyIdentifierPayload;
import com.aleos.models.dtos.out.ErrorResponse;
import com.aleos.util.AttributeNameUtil;
import com.aleos.validators.CurrencyValidator;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;


public class CurrencyFilter extends BaseRequestFilter {

    private static final String CURRENCY_PATH_REGEX = "^/([a-zA-Z]{3}|\\d+)$";

    private static final Pattern CURRENCY_PATH_PATTERN = Pattern.compile(CURRENCY_PATH_REGEX);

    private CurrencyValidator currencyValidator;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        super.init(filterConfig);
        currencyValidator = (CurrencyValidator) filterConfig.getServletContext().getAttribute(
                AttributeNameUtil.getName(CurrencyValidator.class));
    }

    @Override
    protected void putPayloadToContext(HttpServletRequest request, HttpServletResponse response) {

        Optional<String> payloadOptional = parseRequest(request);

        payloadOptional.ifPresent(payload ->
                request.setAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR, new CurrencyIdentifierPayload(payload)));
    }

    @Override
    protected List<ErrorResponse> validateRequest(HttpServletRequest request, HttpServletResponse response) {

        String pathInfo = request.getPathInfo();
        if (isNull(pathInfo) || !CURRENCY_PATH_PATTERN.matcher(pathInfo).matches()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return List.of(new ErrorResponse(
                    INVALID_PATH_ERROR + " Must consist a correct identifier for the currency"));
        }

        if ("GET".equalsIgnoreCase(request.getMethod())) {
            var payload = (CurrencyIdentifierPayload) request.getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);

            return currencyValidator.validateIdentifier(payload.identifier());
        }

        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);

        return List.of(new ErrorResponse(METHOD_IS_NOT_SUPPORTED.formatted(request.getMethod())));
    }

    private Optional<String> parseRequest(ServletRequest request) {

        String pathInfo = ((HttpServletRequest) request).getPathInfo();
        var matcher = CURRENCY_PATH_PATTERN.matcher(pathInfo);

        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
