package com.aleos.servlet;

import com.aleos.model.dto.in.CurrencyPayload;
import com.aleos.model.dto.out.CurrencyResponse;
import com.aleos.service.CurrencyService;
import com.aleos.util.RequestAttributeUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CurrenciesServlet extends BaseServlet {

    private static CurrencyService currencyService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        RequestAttributeUtil.setResponse(req, currencyService.findAll());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        var payload = RequestAttributeUtil.getPayload(req, CurrencyPayload.class);

        CurrencyResponse responseModel = currencyService.save(payload);

        RequestAttributeUtil.setResponse(req, responseModel);
        resp.setStatus(HttpServletResponse.SC_CREATED);
    }
}
