package com.aleos.servlets;

import com.aleos.models.dtos.in.ConversionRatePayload;
import com.aleos.services.ConversionRateService;
import com.aleos.util.RequestAttributeUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;

@RequiredArgsConstructor
public class ConversionRatesServlet extends BaseServlet {

    private static ConversionRateService conversionRateService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        RequestAttributeUtil.setResponse(req, conversionRateService.findAll());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        var payload = RequestAttributeUtil.getPayload(req, ConversionRatePayload.class);

        var conversionRateResponse = conversionRateService.save(payload);

        RequestAttributeUtil.setResponse(req, conversionRateResponse);
        resp.setStatus(SC_CREATED);
    }
}
