package com.aleos.filters.url;

import com.aleos.models.dtos.in.ConversionPayload;
import com.aleos.models.dtos.out.Error;
import com.aleos.validators.ConversionRateValidator;
import com.aleos.validators.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ConversionUrlFilter extends AbstractUrlFilter {

    protected ConversionRateValidator validator;

    @Override
    protected void initializePayload(HttpServletRequest req, HttpServletResponse resp) {
        setPayloadAttribute(extractConversionPayload(req), req);
    }

    @Override
    protected ValidationResult<Error> validatePayload(HttpServletRequest req, HttpServletResponse resp) {
        var validationResult = new ValidationResult<Error>();
        if (isGetMethod(req)) {
            var payload = getPayloadAttribute(ConversionPayload.class, req);
            validator.validateIdentifier(payload.baseCurrencyCode() + payload.targetCurrencyCode())
                    .ifPresent(validationResult::add);
        }

        return validationResult;
    }

    private ConversionPayload extractConversionPayload(HttpServletRequest req) {
        return new ConversionPayload(
                req.getParameter("from"),
                req.getParameter("to"),
                Double.parseDouble(req.getParameter("amount"))
        );
    }
}
