package com.aleos.validators;

import com.aleos.models.dtos.ErrorResponse;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.nonNull;

public interface PayloadValidator<T, R> {

    Predicate<String> isNonEmptyString = value -> nonNull(value) && !value.isEmpty();

    BiPredicate<String, String> matchesPattern = String::matches;

    BiFunction<String, String, ErrorResponse> createPatternMismatchErrorResponse =
            (field, regex) -> new ErrorResponse("%s should match to: %s".formatted(field, regex));

    Function<String, ErrorResponse> createRequiredFieldErrorResponse =
            field -> new ErrorResponse(field + " is required.");

    List<R> validate(T obj);

    static <T> BiFunction<String, T, Optional<ErrorResponse>> createFieldValidator(
            Predicate<T> validationPredicate,
            Function<String, ErrorResponse> responseGenerator) {

        return (field, value) -> validationPredicate.test(value)
                ? Optional.empty()
                : Optional.of(responseGenerator.apply(field));
    }

    static BiFunction<String, String, Optional<ErrorResponse>> createNonEmptyStringValidator() {
        return (field, value) -> isNonEmptyString.test(value)
                ? Optional.empty()
                : Optional.of(createRequiredFieldErrorResponse.apply(field));
    }

    static BiFunction<String, String, Optional<ErrorResponse>> createPatternValidator(String regex) {
        return (field, value) -> matchesPattern.test(value, regex)
                ? Optional.empty()
                : Optional.of(createPatternMismatchErrorResponse.apply(field, regex));
    }
}
