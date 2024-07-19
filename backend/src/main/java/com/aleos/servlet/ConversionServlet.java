package com.aleos.servlet;

import com.aleos.model.dto.in.ConversionPayload;
import com.aleos.model.dto.out.ConversionResponse;
import com.aleos.service.ConversionService;
import com.aleos.util.RequestAttributeUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

public class ConversionServlet extends BaseServlet {

    protected static ConversionService conversionService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        var payload = RequestAttributeUtil.getPayload(req, ConversionPayload.class);

        Optional<ConversionResponse> byPayload = conversionService.convert(payload);

        byPayload.ifPresentOrElse(
                responseModel -> RequestAttributeUtil.setResponse(req, responseModel),
                () -> setNotFoundResponse(req, resp, "Conversion is not found and can't be calculated."));
    }
}
