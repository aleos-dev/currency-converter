package com.aleos.servlets;

import com.aleos.models.dtos.CurrencyCodePayload;
import com.aleos.models.dtos.ConversionRateResponse;
import com.aleos.models.dtos.ErrorResponse;
import com.aleos.services.CurrencyService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {

    private transient CurrencyService currencyService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        currencyService = (CurrencyService) config.getServletContext().getAttribute("currencyService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

        var currencyIdentifierPayload = (CurrencyCodePayload) request.getAttribute("currencyIdentifierPayload");

        Optional<ConversionRateResponse> currency = currencyService.findByIdentifier(currencyIdentifierPayload.code());

        ResponseWrapper responseWrapper = (ResponseWrapper) response;
        if (currency.isPresent()) {
            responseWrapper.composeResponse(HttpServletResponse.SC_OK, currency.get());
        } else {
            responseWrapper.composeResponse(HttpServletResponse.SC_NOT_FOUND, new ErrorResponse("Currency not found"));
        }
    }
}
