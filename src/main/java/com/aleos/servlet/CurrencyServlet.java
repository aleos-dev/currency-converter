package com.aleos.servlet;

import com.aleos.model.dto.in.CurrencyIdentifierPayload;
import com.aleos.model.dto.out.CurrencyResponse;
import com.aleos.service.CurrencyService;
import com.aleos.util.RequestAttributeUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public class CurrencyServlet extends BaseServlet {

    private static CurrencyService currencyService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        var payload = RequestAttributeUtil.getPayload(req, CurrencyIdentifierPayload.class);

        Optional<CurrencyResponse> byPayload = currencyService.findByIdentifier(payload);

        byPayload.ifPresentOrElse(
                responseModel -> RequestAttributeUtil.setResponse(req, responseModel),
                () -> {
                    RequestAttributeUtil
                            .setResponse(req, "Currency with identifier: %s not found".formatted(payload.identifier()));
                    resp.setStatus(SC_NOT_FOUND);
                }
        );
    }
}
