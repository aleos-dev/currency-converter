package com.aleos.filter.url;

import com.aleos.exception.servlet.RequestBodyParsingException;
import com.aleos.model.dto.in.ConversionRateIdentifierPayload;
import com.aleos.model.dto.in.ConversionRatePayload;
import com.aleos.util.RequestAttributeUtil;
import com.aleos.validator.ConversionRateValidator;
import com.aleos.validator.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

public class ConversionRateUrlFilter extends AbstractUrlFilter {

    private static final int CURRENCY_CODE_LENGTH = 3;

    protected transient ConversionRateValidator conversionRateValidator;

    @Override
    protected void initializePayload(HttpServletRequest req, HttpServletResponse resp) {
        if (isGet(req) || isDelete(req)) {
            extractConversionRateIdentifierPayload(req)
                    .ifPresent(payload -> RequestAttributeUtil.setPayload(req, payload));

        } else if (isPatch(req)) {
            extractConversionRatePayload(req)
                    .ifPresent(payload -> RequestAttributeUtil.setPayload(req, payload));
        }
    }

    @Override
    protected ValidationResult validatePayload(HttpServletRequest req, HttpServletResponse resp) {
        var validationResult = new ValidationResult();

        if (isGet(req)) {

            conversionRateValidator.validateIdentifier(
                    RequestAttributeUtil.getPayload(req, ConversionRateIdentifierPayload.class).identifier()
            ).ifPresent(validationResult::add);
        } else if (isDelete(req)) {
            conversionRateValidator.validateNumericIdentifier(
                    RequestAttributeUtil.getPayload(req, ConversionRateIdentifierPayload.class).identifier()
            ).ifPresent(validationResult::add);
        } else if (isPatch(req)) {
            validationResult = conversionRateValidator.validate(
                    RequestAttributeUtil.getPayload(req, ConversionRatePayload.class));
        }

        return validationResult;
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
    private BigDecimal extractRate(HttpServletRequest req) {
        String contentType = req.getContentType();
        String supportedContentType = "application/x-www-form-urlencoded";
        if (supportedContentType.equals(contentType)) {
            try {
                return req.getReader().lines()
                        .filter(row -> row.startsWith("rate="))
                        .map(row -> row.split("=")[1])
                        .findFirst()
                        .map(BigDecimal::new)
                        .orElseThrow(() -> new RequestBodyParsingException("Payload cannot be parsed."));
            } catch (IOException e) {
                throw new RequestBodyParsingException("Error reading request body.", e);
            }
        }
        throw new RequestBodyParsingException("Unsupported Content-Type: " + contentType);
    }
}
