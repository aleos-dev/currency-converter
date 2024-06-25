package com.aleos.services;

import com.aleos.daos.ConversionRateDao;
import com.aleos.mappers.ConversionRateMapper;
import com.aleos.models.dtos.ConversionRateIdentifierPayload;
import com.aleos.models.dtos.ConversionRatePayload;
import com.aleos.models.dtos.ConversionRateResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ConversionRateService {

    private final ConversionRateDao conversionRateDao;

    private final ConversionRateMapper mapper;

    public ConversionRateResponse save(ConversionRatePayload payload) {

        var conversionRate = conversionRateDao
                .saveAndReturn(payload.baseCurrencyCode(), payload.targetCurrencyCode(), payload.rate());

        return mapper.toDto(conversionRate);
    }

    public List<ConversionRateResponse> findAll() {

        return conversionRateDao.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public Optional<ConversionRateResponse> findByCode(ConversionRateIdentifierPayload payload) {
        return conversionRateDao.findByCode(payload.code()).map(mapper::toDto);
    }

    public void update(ConversionRatePayload payload) {

        conversionRateDao.updateRateByCurrencyCodes(
                payload.baseCurrencyCode(), payload.targetCurrencyCode(), payload.rate());
    }

    public void delete(int id) {
        conversionRateDao.delete(id);
    }
}
