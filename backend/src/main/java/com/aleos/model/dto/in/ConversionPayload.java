package com.aleos.model.dto.in;

import lombok.NonNull;

import java.util.Locale;

public record ConversionPayload (
        @NonNull String baseCurrencyCode,
        @NonNull String targetCurrencyCode,
        @NonNull Double amount
) {
    public ConversionPayload {
        baseCurrencyCode = baseCurrencyCode.toUpperCase(Locale.ROOT);
        targetCurrencyCode = targetCurrencyCode.toUpperCase(Locale.ROOT);
    }
}
