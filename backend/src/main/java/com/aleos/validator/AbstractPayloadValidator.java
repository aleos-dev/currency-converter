package com.aleos.validator;

import com.aleos.model.dto.out.Error;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public abstract class AbstractPayloadValidator<T> {

    protected abstract ValidationResult validate(T payload);

    protected Optional<Error> validatePattern(String field, String value, Pattern pattern) {
        if (value == null) {
            return Optional.of(Error.of(field + " is required."));
        }

        return pattern.matcher(value).matches()
                ? Optional.empty()
                : Optional.of(Error.of("%s should match to %s regex.".formatted(field, pattern.pattern())));
    }

    protected Optional<Error> validateField(String field, String value, int minLength, int maxLength) {
        if (value == null) {
            return Optional.of(Error.of(field + " is required."));
        }

        return isInBounds(minLength, maxLength).test(value)
                ? Optional.empty()
                : Optional.of(Error.of("%s should be between %d and %d.".formatted(field, minLength, maxLength)));
    }

    protected Predicate<String> isInBounds(int min, int max) {
        return value -> value.length() >= min && value.length() <= max;
    }
}
