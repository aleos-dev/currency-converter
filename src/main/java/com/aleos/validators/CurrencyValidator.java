package com.aleos.validators;

import com.aleos.models.dtos.in.CurrencyPayload;
import com.aleos.models.dtos.out.ErrorResponse;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.aleos.validators.PayloadValidator.createNonEmptyStringValidator;
import static com.aleos.validators.PayloadValidator.createPatternValidator;

public class CurrencyValidator implements PayloadValidator<CurrencyPayload, ErrorResponse> {

    private static final int CURRENCY_NAME_MIN_LENGTH = 3;

    private static final int CURRENCY_NAME_MAX_LENGTH = 30;

    private static final String CURRENCY_CODE_REGEX = "^([a-zA-Z]{3})$";

    private static final String CURRENCY_ID_REGEX = "^\\d+$";

    private static final int CURRENCY_SIGN_MIN_LENGTH = 1;

    private static final int CURRENCY_SIGN_MAX_LENGTH = 5;

    @Override
    public List<ErrorResponse> validate(CurrencyPayload payload) {

        return Stream.of(validateName("Name", payload.name()),
                        validateCode("Code", payload.code()),
                        validateSign("Sign", payload.sign()))
                .flatMap(List::stream)
                .toList();
    }

    public List<ErrorResponse> validateName(String field, String value) {
        return validateStringByLength(field, value, CURRENCY_NAME_MIN_LENGTH, CURRENCY_NAME_MAX_LENGTH);
    }


    public List<ErrorResponse> validateCode(String field, String value) {

        return Stream.of(
                        createNonEmptyStringValidator(),
                        createPatternValidator(CURRENCY_CODE_REGEX))
                .map(validator -> validator.apply(field, value))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public List<ErrorResponse> validateSign(String field, String value) {
        return validateStringByLength(field, value, CURRENCY_SIGN_MIN_LENGTH, CURRENCY_SIGN_MAX_LENGTH);
    }

    public List<ErrorResponse> validateIdentifier(String value) {

        var validator = PayloadValidator.createFieldValidator(
                validateIdentifierPredicate(),
                createIdentifierMismatchPatternResponse()
        );

        return validator.apply("Currency identifier", value).stream().toList();
    }

    private List<ErrorResponse> validateStringByLength(String field, String value, int min, int max) {

        return Stream.of(
                        createNonEmptyStringValidator(),
                        createStringLengthValidator(min, max))
                .map(validator -> validator.apply(field, value))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private BiFunction<String, String, Optional<ErrorResponse>> createStringLengthValidator(int min, int max) {

        return (field, value) -> PayloadValidator.createFieldValidator(
                s -> value.length() >= min && value.length() <= max,
                f -> new ErrorResponse(String.format("%s must be between %d and %d", f, min, max))
        ).apply(field, value);
    }

    private Predicate<String> validateIdentifierPredicate() {
        return value -> value.matches(CURRENCY_ID_REGEX) || value.matches(CURRENCY_CODE_REGEX);
    }

    private Function<String, ErrorResponse> createIdentifierMismatchPatternResponse() {

        return field -> new ErrorResponse("%s should match any of follow regex: %s or %s"
                .formatted(field, CURRENCY_ID_REGEX, CURRENCY_CODE_REGEX));
    }
}