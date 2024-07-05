package com.aleos.servlets;

import com.aleos.models.dtos.in.CurrencyIdentifierPayload;
import com.aleos.models.dtos.out.CurrencyResponse;
import com.aleos.services.CurrencyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;
import java.util.Set;

import static com.aleos.servlets.HttpMethod.GET;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public class CurrencyServlet extends BaseServlet {

    private static CurrencyService currencyService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        var payload = getPayload(req, CurrencyIdentifierPayload.class);

        Optional<CurrencyResponse> byPayload = currencyService.findByIdentifier(payload);

        byPayload.ifPresentOrElse(
                responseModel -> setResponseModel(req, responseModel),
                () -> {
                    setResponseModel(req, "Currency with identifier: %s not found.".formatted(payload.identifier()));
                    resp.setStatus(SC_NOT_FOUND);
                }
        );
    }

    @Override
    protected Set<HttpMethod> getSupportedMethods() {
        return Set.of(GET);
    }
}
