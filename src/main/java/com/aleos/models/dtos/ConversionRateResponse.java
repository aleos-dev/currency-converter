package com.aleos.models.dtos;

import com.aleos.models.entities.Currency;

import java.math.BigDecimal;

public record ConversionRateResponse(
        Integer id,
        Currency baseCurrency,
        Currency targetCurrency,
        BigDecimal rate
) {
}
