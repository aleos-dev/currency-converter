package com.aleos.validators;

import com.aleos.models.dtos.ConversionRatePayload;
import com.aleos.models.dtos.ErrorResponse;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class ConversionRatePayloadValidator implements PayloadValidator<ConversionRatePayload, ErrorResponse> {

    private static final BigDecimal RATE_MIN_VALUE = BigDecimal.ZERO;

    private final CurrencyPayloadValidator currencyValidator;

    @Override
    public List<ErrorResponse> validate(ConversionRatePayload payload) {

        return Stream.of(
                        currencyValidator.validateCode("BaseCurrencyCode", payload.baseCurrencyCode()),
                        currencyValidator.validateCode("TargetCurrencyCode", payload.targetCurrencyCode()),
                        validateRate(payload.rate()))
                .flatMap(List::stream)
                .toList();
    }

    private List<ErrorResponse> validateRate(BigDecimal value) {

        return Stream.of(createNonNullBigDecimalValidator(), createRateSignValidator())
                .map(validator -> validator.apply("Rate", value))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private BiFunction<String, BigDecimal, Optional<ErrorResponse>> createNonNullBigDecimalValidator() {
        return PayloadValidator.createFieldValidator(
                Objects::nonNull,
                fieldName -> new ErrorResponse(String.format("%s should be positive.", fieldName))
        );
    }

    private BiFunction<String, BigDecimal, Optional<ErrorResponse>> createRateSignValidator() {
        return PayloadValidator.createFieldValidator(
                value -> value.compareTo(RATE_MIN_VALUE) > 0,
                fieldName -> new ErrorResponse(String.format("%s should be positive.", fieldName))
        );
    }
}
