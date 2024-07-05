package com.aleos.services;

import com.aleos.daos.ConversionRateDao;
import com.aleos.mappers.ConversionRateMapper;
import com.aleos.models.dtos.in.ConversionRateIdentifierPayload;
import com.aleos.models.dtos.in.ConversionRatePayload;
import com.aleos.models.dtos.out.ConversionRateResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ConversionRateService {

    private final ConversionRateDao conversionRateDao;

    private final ConversionRateMapper mapper;

    public ConversionRateResponse save(@NonNull ConversionRatePayload payload) {
        return mapper.toDto(conversionRateDao.saveAndFetch(
                payload.baseCurrencyCode(), payload.targetCurrencyCode(), payload.rate()));
    }

    public List<ConversionRateResponse> findAll() {
        return conversionRateDao.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public Optional<ConversionRateResponse> findByCode(@NonNull ConversionRateIdentifierPayload payload) {
        return conversionRateDao.findByCode(payload.identifier()).map(mapper::toDto);
    }

    public void update(@NonNull ConversionRatePayload payload) {
        conversionRateDao.updateRate(payload.baseCurrencyCode(), payload.targetCurrencyCode(), payload.rate());
    }

    public void delete(int id) {
        conversionRateDao.delete(id);
    }
}
