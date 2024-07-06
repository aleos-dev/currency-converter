package com.aleos.validators;

import java.util.List;
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

    protected abstract List<R> validate(T payload);

    protected abstract R buildErrorResponse(String message, Object... args);


    protected Optional<R> validatePattern(String field, String value, Pattern pattern) {
        if (isNull.test(value)) {
            return Optional.of(buildErrorResponse(field + " is required."));
        }

        if (isBlank.test(value)) {
            return Optional.of(buildErrorResponse(field + " should not be blank."));
        }

        if (!pattern.asPredicate().test(value)) {
            return Optional.of(buildErrorResponse("%s should match to %s regex.", field, pattern.pattern()));
        }

        return Optional.empty();
    }

    protected Optional<R> validateField(String field, String value, int minLength, int maxLength) {
        if (isNull.test(value)) {
            return Optional.of(buildErrorResponse(field + " is required."));
        }

        if (isBlank.test(value)) {
            return Optional.of(buildErrorResponse(field + " should not be blank."));
        }

        if (!isInBounds.apply(minLength, maxLength).test(value)) {
            return Optional.of(buildErrorResponse("%s should be between %d and %d.", field, minLength, maxLength));
        }

        return Optional.empty();
    }
}
