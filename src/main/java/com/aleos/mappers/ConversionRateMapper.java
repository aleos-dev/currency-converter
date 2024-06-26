package com.aleos.mappers;

import com.aleos.models.dtos.ConversionRateResponse;
import com.aleos.models.dtos.ConversionRatePayload;
import com.aleos.models.entities.ConversionRate;
import com.aleos.models.entities.Currency;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;

public class ConversionRateMapper {

    private final ModelMapper modelMapper;

    public ConversionRateMapper(ModelMapper modelMapper) {

        this.modelMapper = modelMapper;
        configureMappings();
    }

    private void configureMappings() {

        Converter<ConversionRate, ConversionRateResponse> toDtoConverter = context -> {
            ConversionRate source = context.getSource();

            return new ConversionRateResponse(
                    source.getId(),
                    source.getBaseCurrency(),
                    source.getTargetCurrency(),
                    source.getRate()
            );
        };

        TypeMap<ConversionRate, ConversionRateResponse> toDtoTypeMap =
                modelMapper.createTypeMap(ConversionRate.class, ConversionRateResponse.class);
        toDtoTypeMap.setConverter(toDtoConverter);

    }

    public ConversionRateResponse toDto(ConversionRate conversionRate) {
        return modelMapper.map(conversionRate, ConversionRateResponse.class);
    }

    public ConversionRate toEntity(ConversionRatePayload payload, Currency baseCurrency, Currency targetCurrency) {

        var conversionRate = new ConversionRate();
        conversionRate.setBaseCurrency(baseCurrency);
        conversionRate.setTargetCurrency(targetCurrency);
        conversionRate.setRate(payload.rate());

        return conversionRate;
    }

}
