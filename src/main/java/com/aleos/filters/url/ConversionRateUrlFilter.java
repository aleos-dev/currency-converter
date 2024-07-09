package com.aleos.filters.url;

import com.aleos.exceptions.servlets.RequestBodyParsingException;
import com.aleos.models.dtos.in.ConversionRateIdentifierPayload;
import com.aleos.models.dtos.in.ConversionRatePayload;
import com.aleos.models.dtos.out.Error;
import com.aleos.validators.ConversionRateValidator;
import com.aleos.validators.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

public class ConversionRateUrlFilter extends AbstractUrlFilter {

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
    protected ValidationResult<Error> validatePayload(HttpServletRequest req, HttpServletResponse resp) {
        var validationResult = new ValidationResult<Error>();
        if (isGetMethod(req)) {
            return conversionRateValidator.validateIdentifier(
                            getPayloadAttribute(ConversionRateIdentifierPayload.class, req).identifier())
                    .map(error -> {
                        validationResult.add(error);
                        return validationResult;
                    })
                    .orElse(validationResult);
        }

        return isPatchMethod(req)
                ? conversionRateValidator.validate(getPayloadAttribute(ConversionRatePayload.class, req))
                : validationResult;
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

    // extract rate from body for patch request
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
