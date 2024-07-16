package com.aleos.model.dto.in;

import lombok.NonNull;

import java.math.BigDecimal;
import java.util.Locale;

public record ConversionRatePayload(
        @NonNull String baseCurrencyCode,
        @NonNull String targetCurrencyCode,
        @NonNull BigDecimal rate
) {
    public ConversionRatePayload {
        baseCurrencyCode = baseCurrencyCode.toUpperCase(Locale.ROOT);
        targetCurrencyCode = targetCurrencyCode.toUpperCase(Locale.ROOT);
    }
}
