package com.aleos.filters;

import com.aleos.models.dtos.in.CurrencyIdentifierPayload;
import com.aleos.models.dtos.out.ErrorResponse;
import com.aleos.validators.CurrencyValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CurrencyFilter extends AbstractPreprocessingFilter {

    private CurrencyValidator currencyValidator;

    @Override
    protected void initializePayload(HttpServletRequest req, HttpServletResponse resp) {
        if (isGetMethod(req)) {
            extractCurrencyIdentifierPayload(req)
                    .ifPresent(payload -> setPayloadAttribute(payload, req));
        }
    }

    @Override
    protected List<ErrorResponse> validatePayload(HttpServletRequest req, HttpServletResponse resp) {
        if (!isGetMethod(req)) {
            return Collections.emptyList();
        }
        CurrencyIdentifierPayload payload = getPayloadAttribute(CurrencyIdentifierPayload.class, req);

        return currencyValidator.validateIdentifier(payload.identifier())
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }

    private Optional<CurrencyIdentifierPayload> extractCurrencyIdentifierPayload(HttpServletRequest req) {
        return getPathInfo(req).map(CurrencyIdentifierPayload::new);
    }
}
