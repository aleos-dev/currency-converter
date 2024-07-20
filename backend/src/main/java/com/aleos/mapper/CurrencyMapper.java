package com.aleos.mapper;

import com.aleos.model.dto.in.CurrencyPayload;
import com.aleos.model.dto.out.CurrencyResponse;
import com.aleos.model.entity.Currency;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;

public class CurrencyMapper {

    private final ModelMapper modelMapper;

    public CurrencyMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        configureMappings();
    }

    private void configureMappings() {
        Converter<Currency, CurrencyResponse> toDtoConverter = context -> {
            Currency source = context.getSource();
            return new CurrencyResponse(
                    source.getId(),
                    source.getFullname(),
                    source.getCode(),
                    source.getSign()
            );
        };
        Converter<CurrencyPayload, Currency> toEntityConverter = context -> {
            CurrencyPayload source = context.getSource();
            var currency = new Currency();
            currency.setFullname(source.name());
            currency.setCode(source.code());
            currency.setSign(source.sign());

            return currency;
        };
        TypeMap<Currency, CurrencyResponse> toDtoTypeMap =
                modelMapper.createTypeMap(Currency.class, CurrencyResponse.class);
        toDtoTypeMap.setConverter(toDtoConverter);

        TypeMap<CurrencyPayload, Currency> toEntityTypeMap =
                modelMapper.createTypeMap(CurrencyPayload.class, Currency.class);
        toEntityTypeMap.setConverter(toEntityConverter);
    }

    public CurrencyResponse toDto(Currency currency) {
        return modelMapper.map(currency, CurrencyResponse.class);
    }

    public Currency toEntity(CurrencyPayload currencyPayload) {
        return modelMapper.map(currencyPayload, Currency.class);
    }
}
