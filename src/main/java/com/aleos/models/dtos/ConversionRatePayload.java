package com.aleos.models.dtos;

import java.math.BigDecimal;

public record ConversionRatePayload(
        String baseCurrencyCode,
        String targetCurrencyCode,
        BigDecimal rate
) {
}
