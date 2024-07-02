package com.aleos.servlets;

import com.aleos.models.dtos.in.CurrencyIdentifierPayload;
import com.aleos.models.dtos.out.CurrencyResponse;
import com.aleos.services.CurrencyService;
import com.aleos.util.AttributeNameUtil;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {

    private transient CurrencyService currencyService;


    @Override
    public void init(ServletConfig config) throws ServletException {

        super.init(config);
        currencyService = (CurrencyService) config.getServletContext()
                .getAttribute(AttributeNameUtil.getName(CurrencyService.class));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

        var payload = (CurrencyIdentifierPayload) request.getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);

        Optional<CurrencyResponse> byIdentifier = currencyService.findByIdentifier(payload);

        if (byIdentifier.isPresent()) {
            request.setAttribute(AttributeNameUtil.RESPONSE_MODEL_ATTR, byIdentifier.get());
            response.setStatus(SC_OK);

            return;
        }

        request.setAttribute(AttributeNameUtil.RESPONSE_MODEL_ATTR,
                "Currency with identifier: %s, does not exist.".formatted(payload.identifier()));
        response.setStatus(SC_NOT_FOUND);
    }
}
