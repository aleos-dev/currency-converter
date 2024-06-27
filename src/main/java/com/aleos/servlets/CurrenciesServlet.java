package com.aleos.servlets;

import com.aleos.models.dtos.CurrencyPayload;
import com.aleos.models.dtos.CurrencyResponse;
import com.aleos.models.entities.Currency;
import com.aleos.services.CurrencyService;
import com.aleos.util.AttributeNameUtil;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

@WebServlet("/currencies")
@RequiredArgsConstructor
public class CurrenciesServlet extends HttpServlet {

    private transient CurrencyService currencyService;

    @Override
    public void init(ServletConfig config) throws ServletException {

        super.init(config);

        currencyService = (CurrencyService) config.getServletContext()
                .getAttribute(AttributeNameUtil.getName(CurrencyService.class));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        List<CurrencyResponse> all = currencyService.findAll();

        req.setAttribute(AttributeNameUtil.RESPONSE_MODEL_ATTR, all);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {

        CurrencyPayload payload = (CurrencyPayload) req.getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);

        Currency saved = currencyService.save(payload);

        resp.setStatus(HttpServletResponse.SC_CREATED);
        req.setAttribute(AttributeNameUtil.RESPONSE_MODEL_ATTR, saved);
    }
}
