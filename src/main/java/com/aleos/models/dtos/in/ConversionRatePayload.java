package com.aleos.models.dtos.in;

import java.math.BigDecimal;

public record ConversionRatePayload(
        String baseCurrencyCode,
        String targetCurrencyCode,
        BigDecimal rate
) {
}
