package com.aleos.validator;

import com.aleos.model.dto.out.Error;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ValidationResult {

    List<Error> errors = new ArrayList<>();

    public void add(Error error) {
        errors.add(error);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean isValid() {
        return errors.isEmpty();
    }
}