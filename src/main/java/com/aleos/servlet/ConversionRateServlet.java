package com.aleos.servlet;

import com.aleos.model.dto.in.ConversionRateIdentifierPayload;
import com.aleos.model.dto.in.ConversionRatePayload;
import com.aleos.model.dto.out.ConversionRateResponse;
import com.aleos.service.ConversionRateService;
import com.aleos.util.RequestAttributeUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

import static com.aleos.servlet.common.HttpMethod.PATCH;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static jakarta.servlet.http.HttpServletResponse.SC_NO_CONTENT;

public class ConversionRateServlet extends BaseServlet {

    protected static ConversionRateService conversionRateService;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (PATCH.isMatches(req.getMethod())) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        var payload = RequestAttributeUtil.getPayload(req, ConversionRateIdentifierPayload.class);

        Optional<ConversionRateResponse> byPayload = conversionRateService.findByCode(payload);

        byPayload.ifPresentOrElse(
                responseModel -> RequestAttributeUtil.setResponse(req, responseModel),
                () -> {
                    RequestAttributeUtil.setResponse(req, "Currency with identifier: %s not found."
                            .formatted(payload.identifier()));
                    resp.setStatus(SC_NOT_FOUND);
                }
        );
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) {
        ConversionRatePayload payload = RequestAttributeUtil.getPayload(req, ConversionRatePayload.class);

        if (conversionRateService.update(payload)) {
            resp.setStatus(SC_NO_CONTENT);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
