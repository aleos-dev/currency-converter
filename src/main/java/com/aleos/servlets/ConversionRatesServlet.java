package com.aleos.servlets;

import com.aleos.models.dtos.in.ConversionRatePayload;
import com.aleos.services.ConversionRateService;
import com.aleos.util.AttributeNameUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import static com.aleos.servlets.HttpMethod.*;
import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;

@RequiredArgsConstructor
public class ConversionRatesServlet extends BaseServlet {

    private static ConversionRateService conversionRateService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        req.setAttribute(AttributeNameUtil.RESPONSE_MODEL_ATTR, conversionRateService.findAll());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        var conversionRateResponse = conversionRateService.save(getPayload(req, ConversionRatePayload.class));
        setResponseModel(req, conversionRateResponse);
        resp.setStatus(SC_CREATED);
    }

    @Override
    protected Set<HttpMethod> getSupportedMethods() {
        return Set.of(GET, POST);
    }
}
