package com.aleos.filter.url;

import com.aleos.model.dto.in.CurrencyIdentifierPayload;
import com.aleos.util.RequestAttributeUtil;
import com.aleos.validator.CurrencyValidator;
import com.aleos.validator.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

public class CurrencyUrlFilter extends AbstractUrlFilter {

    protected transient CurrencyValidator currencyValidator;

    @Override
    protected void initializePayload(HttpServletRequest req, HttpServletResponse resp) {
        if (isGet(req)) {
            extractCurrencyIdentifierPayload(req)
                    .ifPresent(payload -> RequestAttributeUtil.setPayload(req, payload));
        }
    }

    @Override
    protected ValidationResult validatePayload(HttpServletRequest req, HttpServletResponse resp) {
        var validationResult = new ValidationResult();

        if (isGet(req)) {
            CurrencyIdentifierPayload payload = RequestAttributeUtil.getPayload(req, CurrencyIdentifierPayload.class);
            currencyValidator.validateIdentifier(payload.identifier())
                    .ifPresent(validationResult::add);
        }
        return validationResult;
    }

    private Optional<CurrencyIdentifierPayload> extractCurrencyIdentifierPayload(HttpServletRequest req) {
        return getPathInfo(req).map(CurrencyIdentifierPayload::new);
    }
}
