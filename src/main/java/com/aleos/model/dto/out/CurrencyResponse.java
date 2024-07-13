package com.aleos.model.dto.out;

public record CurrencyResponse(
        Integer id,
        String name,
        String code,
        String sign
) {
}
