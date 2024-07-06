package com.aleos.filters;

import com.aleos.models.dtos.in.ConversionRatePayload;
import com.aleos.models.dtos.out.ErrorResponse;
import com.aleos.validators.ConversionRateValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class ConversionRatesFilter extends AbstractPreprocessingFilter {

    protected ConversionRateValidator conversionRateValidator;

    @Override
    protected void initializePayload(HttpServletRequest req, HttpServletResponse resp) {
        if (isPostMethod(req)) {
            setPayloadAttribute(extractConversionRatePayload(req), req);
        }
    }

    @Override
    protected List<ErrorResponse> validatePayload(HttpServletRequest req, HttpServletResponse resp) {
        return isPostMethod(req)
                ? conversionRateValidator.validate(getPayloadAttribute(ConversionRatePayload.class, req))
                : Collections.emptyList();
    }

    private ConversionRatePayload extractConversionRatePayload(HttpServletRequest req) {
            return new ConversionRatePayload(
                    req.getParameter("baseCurrencyCode"),
                    req.getParameter("targetCurrencyCode"),
                    new BigDecimal(req.getParameter("rate"))
            );
    }
}