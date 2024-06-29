package com.aleos.validators;

import com.aleos.models.dtos.CurrencyIdentifierPayload;
import com.aleos.models.dtos.ErrorResponse;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class CurrencyIdentifierPayloadValidator implements PayloadValidator<CurrencyIdentifierPayload, ErrorResponse> {

    private static final int CURRENCY_CODE_LENGTH = 3;

    private static final String CURRENCY_CODE_REGEX = "^([a-zA-Z]{%d})$".formatted(CURRENCY_CODE_LENGTH);

    private static final String CURRENCY_INTEGER_REGEX = "^\\d+$";

    @Override
    public List<ErrorResponse> validate(CurrencyIdentifierPayload payload) {

        var optionalError = validateIdentifier(payload.identifier());
        return optionalError.map(Collections::singletonList).orElse(Collections.emptyList());
    }

    private Optional<ErrorResponse> validateIdentifier(String value) {

        var validator = PayloadValidator.createFieldValidator(
                validateIdentifierPredicate(),
                createIdentifierMismatchPatternResponse()
        );

        return validator.apply("Currency identifier", value);
    }

    private Predicate<String> validateIdentifierPredicate() {
        return value -> value.matches(CURRENCY_INTEGER_REGEX) || value.matches(CURRENCY_CODE_REGEX);
    }

    private Function<String, ErrorResponse> createIdentifierMismatchPatternResponse() {

        return field -> new ErrorResponse("%s should match an identifier patterns: %s or %s"
                .formatted(field, CURRENCY_INTEGER_REGEX, CURRENCY_CODE_REGEX));
    }
}

