package com.aleos.validators;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ValidationResult<T> {

    List<T> errors = new ArrayList<>();

    public void add(T error) {
        errors.add(error);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean isValid() {
        return errors.isEmpty();
    }
}