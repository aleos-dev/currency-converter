package com.aleos.models.entities;

import lombok.*;

import java.math.BigDecimal;


@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversionRate implements Entity<Integer> {

    Integer id;

    Currency baseCurrency;

    Currency targetCurrency;

    BigDecimal rate;
}
