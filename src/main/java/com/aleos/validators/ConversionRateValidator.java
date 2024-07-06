package com.aleos.validators;

import com.aleos.models.dtos.in.ConversionRatePayload;
import com.aleos.models.dtos.out.ErrorResponse;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class ConversionRateValidator extends AbstractPayloadValidator<ConversionRatePayload, ErrorResponse> {

    private static final Pattern CONVERSION_RATE_CODE_PATTERN = Pattern.compile("^([a-zA-Z]{6})$");

    private final CurrencyValidator currencyValidator;

    Predicate<BigDecimal> isRatePositive = rate -> rate.compareTo(BigDecimal.ZERO) > 0;

    @Override
    public List<ErrorResponse> validate(ConversionRatePayload payload) {
        return Stream.of(
                        validateCode(payload.baseCurrencyCode() + payload.targetCurrencyCode()),
                        validateRate(payload.rate()))
                .flatMap(Optional::stream)
                .toList();
    }

    public Optional<ErrorResponse> validateRate(BigDecimal value) {
        if (isNull.test(value)) {
            return Optional.of(new ErrorResponse("Rate is required."));
        }
        if (!isRatePositive.test(value)) {
            return Optional.of(new ErrorResponse("Rate should be positive."));
        }
        return Optional.empty();
    }


    public Optional<ErrorResponse> validateCode(String value) {
        return validatePattern("Code", value, CONVERSION_RATE_CODE_PATTERN);
    }

    @Override
    protected ErrorResponse buildErrorResponse(String message, Object... args) {
        return new ErrorResponse(String.format(message, args));
    }
}