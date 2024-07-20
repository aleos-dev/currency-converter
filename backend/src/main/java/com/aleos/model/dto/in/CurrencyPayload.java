package com.aleos.model.dto.in;

import java.util.Locale;

public record CurrencyPayload(
        String name,
        String code,
        String sign
) {
    public CurrencyPayload {
        code = code == null ? code : code.toUpperCase(Locale.ROOT);
    }
}
