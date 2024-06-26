package com.aleos.servlets.filters;

import com.aleos.models.dtos.CurrencyCodePayload;
import com.aleos.models.dtos.ErrorResponse;
import com.aleos.servlets.ResponseWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;


public class CurrencyRequestFilter implements Filter {

    private static final String CURRENCY_IDENTIFIER_PATTERN = "^/([a-zA-Z]{3}|\\d+)$";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        Optional<String> optionalId = parseRequestForCurrencyIdentifier(request);

        if (optionalId.isEmpty()) {
            setErrorResponse(response);
            return;
        }
        request.setAttribute("currencyIdentifierPayload", new CurrencyCodePayload(optionalId.get()));

        chain.doFilter(request, response);
    }

    private Optional<String> parseRequestForCurrencyIdentifier(ServletRequest request) {

        String pathInfo = ((HttpServletRequest) request).getPathInfo();

        if (isNull(pathInfo)) {
            return Optional.empty();
        }

        Pattern currencyCodePattern = Pattern.compile(CURRENCY_IDENTIFIER_PATTERN);
        Matcher matcher = currencyCodePattern.matcher(pathInfo);
        if (!matcher.find()) {
            return Optional.empty();
        }

        return Optional.of(matcher.group(1));
    }

    private void setErrorResponse(ServletResponse response) {

        var responseWrapper = (ResponseWrapper) response;

        responseWrapper.composeResponse(HttpServletResponse.SC_BAD_REQUEST, new ErrorResponse(
                        "Invalid currency code format. Expected pattern: %s".formatted(CURRENCY_IDENTIFIER_PATTERN)));
    }
}
