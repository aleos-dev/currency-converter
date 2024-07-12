package com.aleos.filters.url;

import com.aleos.models.dtos.in.CurrencyPayload;
import com.aleos.models.dtos.out.Error;
import com.aleos.util.RequestAttributeUtil;
import com.aleos.validators.CurrencyValidator;
import com.aleos.validators.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CurrenciesUrlFilter extends AbstractUrlFilter {

    private CurrencyValidator currencyValidator;

    @Override
    protected void initializePayload(HttpServletRequest req, HttpServletResponse resp) {
        if (isPost(req)) {
            RequestAttributeUtil.setPayload(req, extractCurrencyPayload(req));
        }
    }

    @Override
    protected ValidationResult<Error> validatePayload(HttpServletRequest req, HttpServletResponse resp) {
        return isPost(req)
                ? currencyValidator.validate(RequestAttributeUtil.getPayload(req, CurrencyPayload.class))
                : new ValidationResult<>();
    }

    private CurrencyPayload extractCurrencyPayload(HttpServletRequest req) {
        return new CurrencyPayload(
                req.getParameter("name"),
                req.getParameter("code"),
                req.getParameter("sign"));
    }
}
