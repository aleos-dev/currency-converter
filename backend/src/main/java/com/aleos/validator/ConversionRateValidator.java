package com.aleos.validator;

import com.aleos.model.dto.in.ConversionRatePayload;
import com.aleos.model.dto.out.Error;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class ConversionRateValidator extends AbstractPayloadValidator<ConversionRatePayload> {

    private static final Pattern CONVERSION_RATE_CODE_PATTERN = Pattern.compile("^(([a-zA-Z]{6})|(\\d+))$");

    private static final Pattern CONVERSION_RATE_NUMERIC_IDENTIFIER_PATTERN = Pattern.compile("^(\\d+)$");

    @Override
    public ValidationResult validate(ConversionRatePayload payload) {
        var validationResult = new ValidationResult();
        Stream.of(
                        validateIdentifier(payload.baseCurrencyCode() + payload.targetCurrencyCode()),
                        validateRate(payload.rate()))
                .flatMap(Optional::stream)
                .forEach(validationResult::add);

        return validationResult;
    }

    public Optional<Error> validateRate(BigDecimal value) {
        if (value == null) {
            return Optional.of(Error.of("Rate is required."));
        }

        return isPositive(value)
                ? Optional.empty()
                : Optional.of(Error.of("Rate should be positive."));
    }

    public Optional<Error> validateIdentifier(String value) {
        return validatePattern("Identifier", value, CONVERSION_RATE_CODE_PATTERN);
    }

    public Optional<Error> validateNumericIdentifier(String value) {
        return validatePattern("Numeric identifier", value, CONVERSION_RATE_NUMERIC_IDENTIFIER_PATTERN);
    }

    private boolean isPositive(BigDecimal rate) {
        return rate.compareTo(BigDecimal.ZERO) > 0;
    }
}