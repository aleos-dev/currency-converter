package com.aleos.validators;

import com.aleos.models.dtos.in.CurrencyPayload;
import com.aleos.models.dtos.out.Error;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CurrencyValidator extends AbstractPayloadValidator<CurrencyPayload, Error> {

    private static final Pattern CURRENCY_CODE_PATTERN = Pattern.compile("^([a-zA-Z]{3})$");
    private static final Pattern CURRENCY_IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z]{3}$|^\\d+$");

    private static final int CURRENCY_NAME_MIN_LENGTH = 3;
    private static final int CURRENCY_NAME_MAX_LENGTH = 30;
    private static final int CURRENCY_SIGN_MIN_LENGTH = 1;
    private static final int CURRENCY_SIGN_MAX_LENGTH = 5;

    @Override
    public ValidationResult<Error> validate(CurrencyPayload payload) {
        var validationResult = new ValidationResult<Error>();
        Stream.of(
                        validateName(payload.name()),
                        validateCode(payload.code()),
                        validateSign(payload.sign()))
                .flatMap(Optional::stream)
                .forEach(validationResult::add);
        return validationResult;
    }

    public Optional<Error> validateName(String value) {
        return validateField("Name", value, CURRENCY_NAME_MIN_LENGTH, CURRENCY_NAME_MAX_LENGTH);
    }

    public Optional<Error> validateCode(String value) {
        return validatePattern("Code", value, CURRENCY_CODE_PATTERN);
    }

    public Optional<Error> validateSign(String value) {
        return validateField("Sign", value, CURRENCY_SIGN_MIN_LENGTH, CURRENCY_SIGN_MAX_LENGTH);
    }

    public Optional<Error> validateIdentifier(String value) {
        return validatePattern("Identifier", value, CURRENCY_IDENTIFIER_PATTERN);
    }

    @Override
    protected Error buildError(String message, Object... args) {
        return Error.of(String.format(message, args));
    }
}