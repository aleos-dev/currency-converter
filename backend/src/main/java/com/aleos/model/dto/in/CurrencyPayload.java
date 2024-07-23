package com.aleos.model.dto.in;

public record CurrencyPayload(
        String name,
        String code,
        String sign
) {
}
