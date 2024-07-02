package com.aleos.models.dtos.in;

public record CurrencyPayload(
        String name,
        String code,
        String sign
) {
}
