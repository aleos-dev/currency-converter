package com.aleos.models.dtos.out;

import lombok.Value;

@Value(staticConstructor = "of")
public class Error {
    String message;
}
