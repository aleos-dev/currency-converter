package com.aleos.model.dto.out;

import lombok.Value;

@Value(staticConstructor = "of")
public class Error {
    String message;
}
