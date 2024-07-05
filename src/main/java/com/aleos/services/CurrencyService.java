package com.aleos.services;

import com.aleos.daos.CurrencyDao;
import com.aleos.mappers.CurrencyMapper;
import com.aleos.models.dtos.in.CurrencyIdentifierPayload;
import com.aleos.models.dtos.in.CurrencyPayload;
import com.aleos.models.dtos.out.CurrencyResponse;
import com.aleos.models.entities.Currency;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyDao currencyDao;

    private final CurrencyMapper mapper;

    public CurrencyResponse save(@NonNull CurrencyPayload payload) {
        Currency newCurrency = mapper.toEntity(payload);
        int id = currencyDao.save(newCurrency);
        newCurrency.setId(id);

        return mapper.toDto(newCurrency);
    }

    public List<CurrencyResponse> findAll() {
        return currencyDao.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public Optional<CurrencyResponse> findByIdentifier(@NonNull CurrencyIdentifierPayload payload) {
        String isDigitsRegex = "\\d+";

        return payload.identifier().matches(isDigitsRegex)
                ? currencyDao.findById(Integer.parseInt(payload.identifier())).map(mapper::toDto)
                : currencyDao.findByCode(payload.identifier()).map(mapper::toDto);
    }

    public void update(int id, @NonNull CurrencyPayload payload) {
        Currency toUpdate = mapper.toEntity(payload);
        toUpdate.setId(id);

        currencyDao.update(toUpdate);
    }

    public void delete(int id) {
        currencyDao.delete(id);
    }
}
