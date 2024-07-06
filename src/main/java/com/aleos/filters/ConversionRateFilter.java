package com.aleos.filters;

import com.aleos.exceptions.servlets.RequestBodyParsingException;
import com.aleos.models.dtos.in.ConversionRateIdentifierPayload;
import com.aleos.models.dtos.in.ConversionRatePayload;
import com.aleos.models.dtos.out.ErrorResponse;
import com.aleos.validators.ConversionRateValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ConversionRateFilter extends AbstractPreprocessingFilter {

    private static final int CURRENCY_CODE_LENGTH = 3;

    private ConversionRateValidator conversionRateValidator;

    @Override
    protected void initializePayload(HttpServletRequest req, HttpServletResponse resp) {
        if (isGetMethod(req)) {
            extractConversionRateIdentifierPayload(req).ifPresent(payload -> setPayloadAttribute(payload, req));

        } else if (isPatchMethod(req)) {
            extractConversionRatePayload(req).ifPresent(payload -> setPayloadAttribute(payload, req));
        }
    }

    @Override
    protected List<ErrorResponse> validatePayload(HttpServletRequest req, HttpServletResponse resp) {
        if (isGetMethod(req)) {
            return conversionRateValidator.validateCode(
                    getPayloadAttribute(ConversionRateIdentifierPayload.class, req).identifier())
                    .map(Collections::singletonList)
                    .orElseGet(Collections::emptyList);

        } else if (isPatchMethod(req)) {
            return conversionRateValidator.validate(getPayloadAttribute(ConversionRatePayload.class, req));
        }

        return Collections.emptyList();
    }

    private Optional<ConversionRatePayload> extractConversionRatePayload(HttpServletRequest req) {
        return getPathInfo(req).map(requestCode -> new ConversionRatePayload(
                requestCode.substring(0, CURRENCY_CODE_LENGTH),
                requestCode.substring(CURRENCY_CODE_LENGTH),
                extractRate(req)
        ));
    }

    private Optional<ConversionRateIdentifierPayload> extractConversionRateIdentifierPayload(HttpServletRequest req) {
        return getPathInfo(req).map(ConversionRateIdentifierPayload::new);
    }

    // patch request
    private BigDecimal extractRate(HttpServletRequest request) {
        try {
            return request.getReader().lines()
                    .filter(row -> row.startsWith("rate="))
                    .map(row -> row.split("=")[1])
                    .findFirst()
                    .map(BigDecimal::new)
                    .orElseThrow(() -> new RequestBodyParsingException("Payload cannot be parsed."));
        } catch (IOException e) {
            throw new RequestBodyParsingException("Error reading request body.", e);
        }
    }
}
