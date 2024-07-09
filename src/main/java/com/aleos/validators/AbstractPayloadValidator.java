package com.aleos.validators;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public abstract class AbstractPayloadValidator<T, R> {

    protected final Predicate<Object> isNull = Objects::isNull;

    protected final Predicate<String> isBlank = String::isBlank;

    protected final BiFunction<Integer, Integer, Predicate<String>> isInBounds = (min, max) ->
            val -> val.length() >= min && val.length() <= max;

    protected abstract ValidationResult<R> validate(T payload);

    protected abstract R buildError(String message, Object... args);

    protected Optional<R> validatePattern(String field, String value, Pattern pattern) {
        R error = null;
        if (isNull.test(value)) {
            error = buildError(field + " is required.");
        } else if (isBlank.test(value)) {
            error = buildError(field + " should not be blank.");
        } else if (!pattern.asPredicate().test(value)) {
            error = buildError("%s should match to %s regex.", field, pattern.pattern());
        }
        return Optional.ofNullable(error);
    }

    protected Optional<R> validateField(String field, String value, int minLength, int maxLength) {
        R error = null;
        if (isNull.test(value)) {
            error = buildError(field + " is required.");
        } else if (isBlank.test(value)) {
            error = buildError(field + " should not be blank.");
        } else if (!isInBounds.apply(minLength, maxLength).test(value)) {
            error = buildError("%s should be between %d and %d.", field, minLength, maxLength);
        }
        return Optional.ofNullable(error);
    }
}
