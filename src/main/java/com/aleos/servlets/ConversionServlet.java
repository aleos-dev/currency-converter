package com.aleos.servlets;

import com.aleos.models.dtos.in.ConversionPayload;
import com.aleos.models.dtos.out.ConversionResponse;
import com.aleos.models.dtos.out.Error;
import com.aleos.services.ConversionService;
import com.aleos.util.RequestAttributeUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public class ConversionServlet extends BaseServlet {

    private static ConversionService conversionService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        var payload = RequestAttributeUtil.getPayload(req, ConversionPayload.class);

        Optional<ConversionResponse> byPayload = conversionService.convert(payload);

        byPayload.ifPresentOrElse(
                responseModel -> RequestAttributeUtil.setResponse(req, responseModel),
                () -> {
                    RequestAttributeUtil.setResponse(req, Error.of("Conversion is not possible."));
                    resp.setStatus(SC_NOT_FOUND);
                }
        );
    }
}
