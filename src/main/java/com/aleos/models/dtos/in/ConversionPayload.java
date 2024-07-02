package com.aleos.models.dtos.in;

public record ConversionPayload (
        String baseCurrencyCode,
        String targetCurrencyCode,
        Double amount
) {
}
