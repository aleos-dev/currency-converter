package com.aleos.servlets;

import com.aleos.models.dtos.in.CurrencyPayload;
import com.aleos.models.dtos.out.CurrencyResponse;
import com.aleos.services.CurrencyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CurrenciesServlet extends BaseServlet {

    private static CurrencyService currencyService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        setResponseModel(req, currencyService.findAll());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        CurrencyResponse responseModel = currencyService.save(getPayload(req, CurrencyPayload.class));

        setResponseModel(req, responseModel);
        resp.setStatus(HttpServletResponse.SC_CREATED);
    }
}
