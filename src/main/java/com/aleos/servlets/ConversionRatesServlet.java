package com.aleos.servlets;

import com.aleos.models.dtos.in.ConversionRatePayload;
import com.aleos.models.dtos.out.ConversionRateResponse;
import com.aleos.services.ConversionRateService;
import com.aleos.util.AttributeNameUtil;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

@WebServlet("/exchangeRates")
@RequiredArgsConstructor
public class ConversionRatesServlet extends HttpServlet {

    private transient ConversionRateService conversionRateService;

    @Override
    public void init(ServletConfig config) throws ServletException {

        super.init(config);
        conversionRateService = (ConversionRateService) config.getServletContext()
                .getAttribute(AttributeNameUtil.getName(ConversionRateService.class));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        List<ConversionRateResponse> all = conversionRateService.findAll();

        req.setAttribute(AttributeNameUtil.RESPONSE_MODEL_ATTR, all);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {

        ConversionRatePayload payload =
                (ConversionRatePayload) req.getServletContext().getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);

        conversionRateService.save(payload);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
