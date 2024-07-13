package com.aleos.servlet;

import com.aleos.model.dto.in.ConversionRatePayload;
import com.aleos.service.ConversionRateService;
import com.aleos.util.RequestAttributeUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;

@RequiredArgsConstructor
public class ConversionRatesServlet extends BaseServlet {

    protected static ConversionRateService conversionRateService;

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
