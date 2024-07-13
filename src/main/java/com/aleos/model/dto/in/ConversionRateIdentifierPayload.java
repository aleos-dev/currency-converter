package com.aleos.model.dto.in;

import lombok.NonNull;

import java.util.Locale;

public record ConversionRateIdentifierPayload(@NonNull String identifier) {
    public ConversionRateIdentifierPayload {
        identifier = identifier.toUpperCase(Locale.ROOT);
    }
}
