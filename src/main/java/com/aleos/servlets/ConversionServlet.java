package com.aleos.servlets;

import com.aleos.models.dtos.in.ConversionPayload;
import com.aleos.models.dtos.out.ConversionResponse;
import com.aleos.models.dtos.out.Error;
import com.aleos.services.ConversionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public class ConversionServlet extends BaseServlet {

    private static ConversionService conversionService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        Optional<ConversionResponse> byPayload = conversionService.convert(getPayload(req, ConversionPayload.class));

        byPayload.ifPresentOrElse(
                responseModel -> setResponseModel(req, responseModel),
                () -> {
                    setResponseModel(req, Error.of("Conversion is not possible."));
                    resp.setStatus(SC_NOT_FOUND);
                }
        );
    }
}
