package com.aleos.filters.url;

import com.aleos.models.dtos.in.ConversionRatePayload;
import com.aleos.models.dtos.out.Error;
import com.aleos.validators.ConversionRateValidator;
import com.aleos.validators.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;

public class ConversionRatesUrlFilter extends AbstractUrlFilter {

    protected ConversionRateValidator conversionRateValidator;

    @Override
    protected void initializePayload(HttpServletRequest req, HttpServletResponse resp) {
        if (isPostMethod(req)) {
            setPayloadAttribute(extractConversionRatePayload(req), req);
        }
    }

    @Override
    protected ValidationResult<Error> validatePayload(HttpServletRequest req, HttpServletResponse resp) {
        return isPostMethod(req)
                ? conversionRateValidator.validate(getPayloadAttribute(ConversionRatePayload.class, req))
                : new ValidationResult<>();
    }

    private ConversionRatePayload extractConversionRatePayload(HttpServletRequest req) {
        return new ConversionRatePayload(
                req.getParameter("baseCurrencyCode"),
                req.getParameter("targetCurrencyCode"),
                new BigDecimal(req.getParameter("rate"))
        );
    }
}