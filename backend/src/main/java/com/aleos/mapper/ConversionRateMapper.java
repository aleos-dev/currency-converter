package com.aleos.mapper;

import com.aleos.model.dto.out.ConversionRateResponse;
import com.aleos.model.entity.ConversionRate;
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
}
