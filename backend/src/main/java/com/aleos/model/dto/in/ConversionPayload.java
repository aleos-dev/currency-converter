package com.aleos.model.dto.in;

public record ConversionPayload(
        String baseCurrencyCode,
        String targetCurrencyCode,
        Double amount
) {
}
