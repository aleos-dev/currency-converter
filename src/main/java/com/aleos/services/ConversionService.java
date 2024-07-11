package com.aleos.services;

import com.aleos.daos.ConversionRateDao;
import com.aleos.models.dtos.in.ConversionPayload;
import com.aleos.models.dtos.out.ConversionResponse;
import com.aleos.models.entities.ConversionRate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Optional;

@RequiredArgsConstructor
public class ConversionService {

    private final ConversionRateDao conversionRateDao;

    public Optional<ConversionResponse> convert(@NonNull ConversionPayload payload) {
        var from = payload.baseCurrencyCode();
        var to = payload.targetCurrencyCode();

        return conversionRateDao
                .find(from, to)
                .or(() -> findReverseConversion(from, to))
                .or(() -> findCrossRateConversion(from, to))
                .map(entity -> composeConversionResponse(entity, payload.amount()));
    }

    private ConversionResponse composeConversionResponse(ConversionRate conversionRate,
                                                         Double amount) {
        return new ConversionResponse(
                conversionRate.getBaseCurrency(),
                conversionRate.getTargetCurrency(),
                conversionRate.getRate().setScale(2, RoundingMode.HALF_DOWN).doubleValue(),
                amount,
                convert(amount, conversionRate.getRate())
        );
    }

    private Optional<ConversionRate> findCrossRateConversion(String from, String to) {
        return conversionRateDao.findCrossRate(from, to);
    }

    private Double convert(Double amount, BigDecimal rate) {
        return rate
                .multiply(BigDecimal.valueOf(amount))
                .setScale(2, RoundingMode.HALF_DOWN)
                .doubleValue();
    }

    private Optional<ConversionRate> findReverseConversion(String from, String to) {
        Optional<ConversionRate> byCode = conversionRateDao.find(to, from);

        return byCode
                .map(cr -> new ConversionRate(
                        0,
                        cr.getBaseCurrency(),
                        cr.getTargetCurrency(),
                        BigDecimal.ONE.divide(cr.getRate(), MathContext.DECIMAL64)
                ));
    }
}
