package com.aleos.servlets;

import com.aleos.models.dtos.in.ConversionRateIdentifierPayload;
import com.aleos.models.dtos.in.ConversionRatePayload;
import com.aleos.services.ConversionRateService;
import com.aleos.util.AttributeNameUtil;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet("/exchangeRate/*")
public class ConversionRateServlet extends HttpServlet {

    private transient ConversionRateService conversionRateService;

    @Override
    public void init(ServletConfig config) throws ServletException {

        super.init(config);
        conversionRateService = (ConversionRateService) config.getServletContext()
                .getAttribute(AttributeNameUtil.getName(ConversionRateService.class));
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        var payload = (ConversionRateIdentifierPayload) req.getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);

        var byCode = conversionRateService.findByCode(payload);

        if (byCode.isPresent()) {
            req.setAttribute(AttributeNameUtil.RESPONSE_MODEL_ATTR, byCode.get());
            resp.setStatus(SC_OK);
            return;
        }

        req.setAttribute(AttributeNameUtil.RESPONSE_MODEL_ATTR,
                "Currency with identifier: %s, does not exist.".formatted(payload.code()));
        resp.setStatus(SC_NOT_FOUND);
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) {

        var payload = (ConversionRatePayload) req.getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);

        conversionRateService.update(payload);
        resp.setStatus(SC_NO_CONTENT);
    }
}
