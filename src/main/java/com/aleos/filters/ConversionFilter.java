package com.aleos.filters;

import com.aleos.models.dtos.in.ConversionPayload;
import com.aleos.models.dtos.out.ErrorResponse;
import com.aleos.validators.ConversionRateValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collections;
import java.util.List;

public class ConversionFilter extends AbstractPreprocessingFilter {

    protected ConversionRateValidator validator;

    @Override
    protected void initializePayload(HttpServletRequest req, HttpServletResponse resp) {
        setPayloadAttribute(extractConversionPayload(req), req);
    }

    @Override
    protected List<ErrorResponse> validatePayload(HttpServletRequest req, HttpServletResponse resp) {
        if (!isGetMethod(req)) {
            return Collections.emptyList();
        }
        var payload = getPayloadAttribute(ConversionPayload.class, req);
        return validator.validateCode(payload.baseCurrencyCode() + payload.targetCurrencyCode())
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }

    private ConversionPayload extractConversionPayload(HttpServletRequest req) {
        return new ConversionPayload(
                req.getParameter("from"),
                req.getParameter("to"),
                Double.parseDouble(req.getParameter("amount"))
        );
    }
}
