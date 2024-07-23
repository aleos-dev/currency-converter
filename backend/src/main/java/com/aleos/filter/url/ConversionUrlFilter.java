package com.aleos.filter.url;

import com.aleos.exception.servlet.BadParameterException;
import com.aleos.model.dto.in.ConversionPayload;
import com.aleos.util.RequestAttributeUtil;
import com.aleos.validator.ConversionRateValidator;
import com.aleos.validator.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ConversionUrlFilter extends AbstractUrlFilter {

    protected transient ConversionRateValidator validator;

    @Override
    protected void initializePayload(HttpServletRequest req, HttpServletResponse resp) {
        if (isGet(req)) {
            RequestAttributeUtil.setPayload(req, extractConversionPayload(req));
        }
    }

    @Override
    protected ValidationResult validatePayload(HttpServletRequest req, HttpServletResponse resp) {
        var validationResult = new ValidationResult();
        if (isGet(req)) {
            var payload = RequestAttributeUtil.getPayload(req, ConversionPayload.class);
            validator.validateIdentifier(payload.baseCurrencyCode() + payload.targetCurrencyCode())
                    .ifPresent(validationResult::add);
        }

        return validationResult;
    }

    private ConversionPayload extractConversionPayload(HttpServletRequest req) {
        try {
            return new ConversionPayload(
                    req.getParameter("from"),
                    req.getParameter("to"),
                    Double.parseDouble(req.getParameter("amount")));
        } catch (NullPointerException | NumberFormatException e) {
            throw new BadParameterException("Amount has an invalid format.");
        }
    }
}
