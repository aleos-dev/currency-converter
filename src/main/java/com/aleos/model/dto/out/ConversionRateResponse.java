package com.aleos.model.dto.out;

import com.aleos.model.entity.Currency;

import java.math.BigDecimal;

public record ConversionRateResponse(
        Integer id,
        Currency baseCurrency,
        Currency targetCurrency,
        BigDecimal rate
) {
}
