package com.aleos.models.dtos;

public record CurrencyResponse(
        Integer id,
        String name,
        String code,
        String sign
) {
}
