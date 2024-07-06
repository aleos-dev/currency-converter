package com.aleos.filters;

import com.aleos.models.dtos.in.CurrencyPayload;
import com.aleos.models.dtos.out.ErrorResponse;
import com.aleos.validators.CurrencyValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collections;
import java.util.List;

public class CurrenciesFilter extends AbstractPreprocessingFilter {

    private CurrencyValidator currencyValidator;

    @Override
    protected void initializePayload(HttpServletRequest req, HttpServletResponse resp) {
        if (isPostMethod(req)) {
            setPayloadAttribute(extractCurrencyPayload(req), req);
        }
    }

    @Override
    protected List<ErrorResponse> validatePayload(HttpServletRequest req, HttpServletResponse resp) {
        return isPostMethod(req)
                ? currencyValidator.validate(getPayloadAttribute(CurrencyPayload.class, req))
                : Collections.emptyList();
    }

    private CurrencyPayload extractCurrencyPayload(HttpServletRequest req) {
        return new CurrencyPayload(
                req.getParameter("name"),
                req.getParameter("code"),
                req.getParameter("sign"));
    }
}
