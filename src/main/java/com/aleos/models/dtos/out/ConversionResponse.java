package com.aleos.models.dtos.out;

import com.aleos.models.entities.Currency;

public record ConversionResponse (
        Currency baseCurrency,
        Currency targetCurrency,
        Double rate,
        Double amount,
        Double convertedAmount
) {
}
