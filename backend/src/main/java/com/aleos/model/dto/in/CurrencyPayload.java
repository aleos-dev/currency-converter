package com.aleos.model.dto.in;

import lombok.NonNull;

import java.util.Locale;

public record CurrencyPayload(
        @NonNull String name,
        @NonNull String code,
        @NonNull String sign
) {
    public CurrencyPayload {
        code = code.toUpperCase(Locale.ROOT);
    }
}
