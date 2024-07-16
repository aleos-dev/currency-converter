package com.aleos.filter.url;

import com.aleos.model.dto.in.ConversionRatePayload;
import com.aleos.util.RequestAttributeUtil;
import com.aleos.validator.ConversionRateValidator;
import com.aleos.validator.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;

public class ConversionRatesUrlFilter extends AbstractUrlFilter {

    protected transient ConversionRateValidator conversionRateValidator;

    @Override
    protected void initializePayload(HttpServletRequest req, HttpServletResponse resp) {
        if (isPost(req)) {
            RequestAttributeUtil.setPayload(req, extractConversionRatePayload(req));
        }
    }

    @Override
    protected ValidationResult validatePayload(HttpServletRequest req, HttpServletResponse resp) {
        return isPost(req)
                ? conversionRateValidator.validate(RequestAttributeUtil.getPayload(req, ConversionRatePayload.class))
                : new ValidationResult();
    }

    private ConversionRatePayload extractConversionRatePayload(HttpServletRequest req) {
        return new ConversionRatePayload(
                req.getParameter("baseCurrencyCode"),
                req.getParameter("targetCurrencyCode"),
                new BigDecimal(req.getParameter("rate"))
        );
    }
}