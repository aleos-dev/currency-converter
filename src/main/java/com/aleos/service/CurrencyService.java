package com.aleos.service;

import com.aleos.dao.CurrencyDao;
import com.aleos.mapper.CurrencyMapper;
import com.aleos.model.dto.in.CurrencyIdentifierPayload;
import com.aleos.model.dto.in.CurrencyPayload;
import com.aleos.model.dto.out.CurrencyResponse;
import com.aleos.model.entity.Currency;
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
        currencyDao.save(newCurrency);
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
                ? currencyDao.find(Integer.parseInt(payload.identifier())).map(mapper::toDto)
                : currencyDao.find(payload.identifier()).map(mapper::toDto);
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
