package com.aleos.validator;

import com.aleos.model.dto.in.ConversionRatePayload;
import com.aleos.model.dto.out.Error;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class ConversionRateValidator extends AbstractPayloadValidator<ConversionRatePayload, Error> {

    private static final Pattern CONVERSION_RATE_CODE_PATTERN = Pattern.compile("^([a-zA-Z]{6})$");

    Predicate<BigDecimal> isRatePositive = rate -> rate.compareTo(BigDecimal.ZERO) > 0;

    @Override
    public ValidationResult<Error> validate(ConversionRatePayload payload) {
        var validationResult = new ValidationResult<Error>();
        Stream.of(
                        validateIdentifier(payload.baseCurrencyCode() + payload.targetCurrencyCode()),
                        validateRate(payload.rate()))
                .flatMap(Optional::stream)
                .forEach(validationResult::add);
        return validationResult;
    }

    public Optional<Error> validateRate(BigDecimal value) {
        Error error = null;
        if (isNull.test(value)) {
            error = buildError("Rate is required.");
        } else if (!isRatePositive.test(value)) {
            error = buildError("Rate should be positive.");
        }
        return Optional.ofNullable(error);
    }


    public Optional<Error> validateIdentifier(String value) {
        return validatePattern("Identifier", value, CONVERSION_RATE_CODE_PATTERN);
    }

    @Override
    protected Error buildError(String message, Object... args) {
        return Error.of(String.format(message, args));
    }
}