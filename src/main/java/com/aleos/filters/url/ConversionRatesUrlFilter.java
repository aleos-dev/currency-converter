package com.aleos.filters.url;

import com.aleos.models.dtos.in.ConversionRatePayload;
import com.aleos.models.dtos.out.Error;
import com.aleos.util.RequestAttributeUtil;
import com.aleos.validators.ConversionRateValidator;
import com.aleos.validators.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;

public class ConversionRatesUrlFilter extends AbstractUrlFilter {

    protected ConversionRateValidator conversionRateValidator;

    @Override
    protected void initializePayload(HttpServletRequest req, HttpServletResponse resp) {
        if (isPost(req)) {
            RequestAttributeUtil.setPayload(req, extractConversionRatePayload(req));
        }
    }

    @Override
    protected ValidationResult<Error> validatePayload(HttpServletRequest req, HttpServletResponse resp) {
        return isPost(req)
                ? conversionRateValidator.validate(RequestAttributeUtil.getPayload(req, ConversionRatePayload.class))
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