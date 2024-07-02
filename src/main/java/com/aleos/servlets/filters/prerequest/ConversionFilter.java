package com.aleos.servlets.filters.prerequest;

import com.aleos.exceptions.servlets.RequestBodyParsingException;
import com.aleos.models.dtos.in.ConversionPayload;
import com.aleos.models.dtos.out.ErrorResponse;
import com.aleos.util.AttributeNameUtil;
import com.aleos.validators.CurrencyValidator;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static java.util.Objects.isNull;

public class ConversionFilter extends BaseRequestFilter {

    protected static final String CONVERSION_PATH_REGEX = "^from=[a-zA-Z]{3}&to=[a-zA-Z]{3}&amount=\\d+(\\.\\d+)?$";

    protected static final Pattern CONVERSION_REQUEST_PATTERN = Pattern.compile(CONVERSION_PATH_REGEX);

    protected CurrencyValidator currencyValidator;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        super.init(filterConfig);
        currencyValidator = (CurrencyValidator) filterConfig.getServletContext().getAttribute(
                AttributeNameUtil.getName(CurrencyValidator.class));
    }

    @Override
    protected void putPayloadToContext(HttpServletRequest request, HttpServletResponse response) {

        try {
            var conversionPayload = new ConversionPayload(
                    request.getParameter("from"),
                    request.getParameter("to"),
                    toDouble(request.getParameter("amount"))
            );

            request.setAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR, conversionPayload);
        } catch (NumberFormatException e) {
            response.setStatus(SC_BAD_REQUEST);
            throw new RequestBodyParsingException("Cannot parse passed the amount value to Double.", e);
        }
    }

    @Override
    protected List<ErrorResponse> validateRequest(HttpServletRequest request, HttpServletResponse response) {

        String queryString = request.getQueryString();
        if (isNull(queryString) || !CONVERSION_REQUEST_PATTERN.matcher(queryString).matches()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            return List.of(new ErrorResponse(INVALID_PATH_ERROR + "Conversion request is incorrect. Pattern is: %s"
                    .formatted(CONVERSION_PATH_REGEX)));
        }

        String methodType = request.getMethod();
        if ("GET".equalsIgnoreCase(methodType)) {

            var conversionPayload = (ConversionPayload) request.getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);

            return Stream.of(
                            currencyValidator.validateCode("BaseCurrencyCode", conversionPayload.baseCurrencyCode()),
                            currencyValidator.validateCode("TargetCurrencyCode", conversionPayload.targetCurrencyCode())
                    )
                    .flatMap(List::stream)
                    .toList();
        }

        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return List.of(new ErrorResponse(METHOD_IS_NOT_SUPPORTED.formatted(methodType)));
    }

    private Double toDouble(String amount) {
        return Double.parseDouble(amount);
    }
}
