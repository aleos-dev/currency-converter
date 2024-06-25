package com.aleos.services;

import com.aleos.daos.CurrencyDao;
import com.aleos.mappers.CurrencyMapper;
import com.aleos.models.dtos.CurrencyCodePayload;
import com.aleos.models.dtos.CurrencyPayload;
import com.aleos.models.dtos.CurrencyResponse;
import com.aleos.models.entities.Currency;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyDao currencyDao;

    private final CurrencyMapper mapper;

    public Currency save(CurrencyPayload payload) {

        Currency newCurrency = mapper.toEntity(payload);
        int id =  currencyDao.save(newCurrency);
        newCurrency.setId(id);

        return newCurrency;
    }

    public List<CurrencyResponse> findAll() {

        return currencyDao.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public Optional<CurrencyResponse> findByIdentifier(CurrencyCodePayload payload) {
        return currencyDao.findByCode(payload.code()).map(mapper::toDto);
    }

    public void update(int id, CurrencyPayload payload) {

        Currency toUpdate = mapper.toEntity(payload);
        toUpdate.setId(id);

        currencyDao.update(toUpdate);
    }

    public void delete(int id) {

        currencyDao.delete(id);
    }
}
