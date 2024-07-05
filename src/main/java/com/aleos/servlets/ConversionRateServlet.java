package com.aleos.servlets;

import com.aleos.models.dtos.in.ConversionRateIdentifierPayload;
import com.aleos.models.dtos.in.ConversionRatePayload;
import com.aleos.models.dtos.out.ConversionRateResponse;
import com.aleos.services.ConversionRateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;
import java.util.Set;

import static com.aleos.servlets.HttpMethod.GET;
import static com.aleos.servlets.HttpMethod.PATCH;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static jakarta.servlet.http.HttpServletResponse.SC_NO_CONTENT;

public class ConversionRateServlet extends BaseServlet {

    private static ConversionRateService conversionRateService;

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
        conversionRateService.update(getPayload(req, ConversionRatePayload.class));
        resp.setStatus(SC_NO_CONTENT);
    }

    @Override
    protected Set<HttpMethod> getSupportedMethods() {
        return Set.of(GET, PATCH);
    }
}
