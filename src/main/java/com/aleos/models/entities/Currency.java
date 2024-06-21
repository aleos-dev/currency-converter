package com.aleos.models.entities;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Currency implements Entity<Integer> {

    Integer id;

    String fullname;

    String code;

    String sign;
}
