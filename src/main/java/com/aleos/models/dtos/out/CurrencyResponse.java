package com.aleos.models.dtos.out;

public record CurrencyResponse(
        Integer id,
        String name,
        String code,
        String sign
) {
}
