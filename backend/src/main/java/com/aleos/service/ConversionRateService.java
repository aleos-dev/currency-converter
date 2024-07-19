package com.aleos.service;

import com.aleos.dao.ConversionRateDao;
import com.aleos.mapper.ConversionRateMapper;
import com.aleos.model.dto.in.ConversionRateIdentifierPayload;
import com.aleos.model.dto.in.ConversionRatePayload;
import com.aleos.model.dto.out.ConversionRateResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ConversionRateService {

    private final ConversionRateDao conversionRateDao;

    private final ConversionRateMapper mapper;

    public ConversionRateResponse save(@NonNull ConversionRatePayload payload) {
        var conversionRate = conversionRateDao.saveAndFetch(
                payload.baseCurrencyCode(),
                payload.targetCurrencyCode(),
                payload.rate());

        return mapper.toDto(conversionRate);
    }

    public List<ConversionRateResponse> findAll() {
        return conversionRateDao.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public Optional<ConversionRateResponse> findByCode(@NonNull ConversionRateIdentifierPayload payload) {

        String identifier = payload.identifier();
        return conversionRateDao.find(identifier.substring(0, 3), identifier.substring(3)).map(mapper::toDto);
    }

    public boolean update(@NonNull ConversionRatePayload payload) {
        return conversionRateDao.update(payload.baseCurrencyCode(), payload.targetCurrencyCode(), payload.rate());
    }

    public boolean delete(@NonNull ConversionRateIdentifierPayload payload) {
        return conversionRateDao.delete(Integer.parseInt(payload.identifier()));
    }
}
