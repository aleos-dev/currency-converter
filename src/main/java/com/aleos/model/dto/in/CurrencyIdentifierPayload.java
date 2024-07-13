package com.aleos.model.dto.in;

import lombok.NonNull;

import java.util.Locale;

public record CurrencyIdentifierPayload(@NonNull String identifier) {

    public CurrencyIdentifierPayload {
        identifier = identifier.toUpperCase(Locale.ROOT);
    }
}
