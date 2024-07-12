package com.aleos.filters.url;

import com.aleos.models.dtos.in.ConversionPayload;
import com.aleos.models.dtos.out.Error;
import com.aleos.util.RequestAttributeUtil;
import com.aleos.validators.ConversionRateValidator;
import com.aleos.validators.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ConversionUrlFilter extends AbstractUrlFilter {

    protected ConversionRateValidator validator;

    @Override
    protected void initializePayload(HttpServletRequest req, HttpServletResponse resp) {
        if (isGet(req)) {
            RequestAttributeUtil.setPayload(req, extractConversionPayload(req));
        }
    }

    @Override
    protected ValidationResult<Error> validatePayload(HttpServletRequest req, HttpServletResponse resp) {
        var validationResult = new ValidationResult<Error>();
        if (isGet(req)) {
            var payload = RequestAttributeUtil.getPayload(req, ConversionPayload.class);
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
