package com.aleos.model.dto.in;

import java.math.BigDecimal;

public record ConversionRatePayload(
        String baseCurrencyCode,
        String targetCurrencyCode,
        BigDecimal rate
) {
}
