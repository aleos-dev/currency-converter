package com.aleos.servlets;

import com.aleos.models.dtos.in.ConversionRateIdentifierPayload;
import com.aleos.models.dtos.in.ConversionRatePayload;
import com.aleos.models.dtos.out.ConversionRateResponse;
import com.aleos.services.ConversionRateService;
import com.aleos.servlets.common.HttpMethod;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static jakarta.servlet.http.HttpServletResponse.SC_NO_CONTENT;

public class ConversionRateServlet extends BaseServlet {

    private static ConversionRateService conversionRateService;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getMethod().equalsIgnoreCase(HttpMethod.PATCH.toString())) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        var payload = getPayload(req, ConversionRateIdentifierPayload.class);

        Optional<ConversionRateResponse> byPayload = conversionRateService.findByCode(payload);

        byPayload.ifPresentOrElse(
                responseModel -> setResponseModel(req, responseModel),
                () -> {
                    setResponseModel(req, "Currency with identifier: %s not found.".formatted(payload.identifier()));
                    resp.setStatus(SC_NOT_FOUND);
                }
        );
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) {
        if (conversionRateService.update(getPayload(req, ConversionRatePayload.class))) {
            resp.setStatus(SC_NO_CONTENT);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
