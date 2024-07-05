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
        String code = String.join("", payload.baseCurrencyCode(), payload.targetCurrencyCode());

        return conversionRateDao.findByCode(code)
                .or(() -> reverseConversion(code))
                .or(() -> crossConversion(code))
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

    private Optional<ConversionRate> crossConversion(String code) {
        return conversionRateDao.findCrossRateByCode(code);
    }

    private Optional<ConversionRate> reverseConversion(String code) {
        Optional<ConversionRate> byCode = conversionRateDao.findByCode(reverseCurrencyCode(code));

        return byCode
                .map(cr -> new ConversionRate(
                        0,
                        cr.getBaseCurrency(),
                        cr.getTargetCurrency(),
                        BigDecimal.ONE.divide(cr.getRate(), MathContext.DECIMAL64)
                ));
    }

    private Double convert(Double amount, BigDecimal rate) {
        return rate
                .multiply(BigDecimal.valueOf(amount))
                .setScale(2, RoundingMode.HALF_DOWN)
                .doubleValue();
    }

    private String reverseCurrencyCode(String code) {
        int codeLength = 3;

        return code.substring(codeLength) + code.substring(0, codeLength);
    }
}
