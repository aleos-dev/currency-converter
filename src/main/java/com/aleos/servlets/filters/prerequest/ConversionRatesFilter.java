package com.aleos.servlets.filters.prerequest;

import com.aleos.exceptions.servlets.RequestBodyParsingException;
import com.aleos.models.dtos.in.ConversionRatePayload;
import com.aleos.models.dtos.out.ErrorResponse;
import com.aleos.util.AttributeNameUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;

public class ConversionRatesFilter extends ConversionRateFilter {

    @Override
    protected void putPayloadToContext(HttpServletRequest request, HttpServletResponse response) {

        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return;
        }

        Optional<String> codeOptional = extractCodeFromPath(request);
        if ("POST".equalsIgnoreCase(request.getMethod()) && codeOptional.isPresent()) {
            var conversionRatePayload = getConversionRatePayload(request, response);
            request.setAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR, conversionRatePayload);
        }
    }

    @Override
    protected List<ErrorResponse> validateRequest(HttpServletRequest request, HttpServletResponse response) {

        String methodType = request.getMethod();

        if ("GET".equalsIgnoreCase(methodType)) {
            return Collections.emptyList();
        }

        String pathInfo = request.getPathInfo();
        if ("POST".equalsIgnoreCase(methodType)
            && nonNull(pathInfo)
            && CONVERSION_RATE_REQUEST_PATTERN.matcher(pathInfo).matches()) {
            var conversionRatePayload = (ConversionRatePayload) request.getAttribute(
                    AttributeNameUtil.PAYLOAD_MODEL_ATTR);

            return conversionRateValidator.validate(conversionRatePayload);

        }

        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);

        return List.of(new ErrorResponse(INVALID_PATH_ERROR + "ConversionRate code must be present and correct."));
    }

    private ConversionRatePayload getConversionRatePayload(HttpServletRequest request, HttpServletResponse response) {

        try {
            return new ConversionRatePayload(
                    request.getParameter("baseCurrencyCode"),
                    request.getParameter("targetCurrencyCode"),
                    toBigDecimal(request.getParameter("rate"))
            );
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            throw new RequestBodyParsingException("Cannot convert passed value to BigDecimal.", e);
        }
    }
}

