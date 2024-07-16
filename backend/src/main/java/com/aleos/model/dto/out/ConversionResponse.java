package com.aleos.model.dto.out;

import com.aleos.model.entity.Currency;

public record ConversionResponse (
        Currency baseCurrency,
        Currency targetCurrency,
        Double rate,
        Double amount,
        Double convertedAmount
) {
}
