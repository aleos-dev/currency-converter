package com.aleos.filters.url;

import com.aleos.models.dtos.in.CurrencyIdentifierPayload;
import com.aleos.models.dtos.out.Error;
import com.aleos.validators.CurrencyValidator;
import com.aleos.validators.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

public class CurrencyUrlFilter extends AbstractUrlFilter {

    private CurrencyValidator currencyValidator;

    @Override
    protected void initializePayload(HttpServletRequest req, HttpServletResponse resp) {
        if (isGetMethod(req)) {
            extractCurrencyIdentifierPayload(req)
                    .ifPresent(payload -> setPayloadAttribute(payload, req));
        }
    }

    @Override
    protected ValidationResult<Error> validatePayload(HttpServletRequest req, HttpServletResponse resp) {
        var validationResult = new ValidationResult<Error>();

        if (isGetMethod(req)) {
            CurrencyIdentifierPayload payload = getPayloadAttribute(CurrencyIdentifierPayload.class, req);
            currencyValidator.validateIdentifier(payload.identifier())
                    .ifPresent(validationResult::add);
        }
        return validationResult;
    }

    private Optional<CurrencyIdentifierPayload> extractCurrencyIdentifierPayload(HttpServletRequest req) {
        return getPathInfo(req).map(CurrencyIdentifierPayload::new);
    }
}
