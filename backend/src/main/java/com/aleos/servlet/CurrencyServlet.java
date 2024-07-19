package com.aleos.servlet;

import com.aleos.model.dto.in.CurrencyIdentifierPayload;
import com.aleos.model.dto.in.CurrencyPayload;
import com.aleos.model.dto.out.CurrencyResponse;
import com.aleos.service.CurrencyService;
import com.aleos.servlet.common.HttpMethod;
import com.aleos.util.RequestAttributeUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.*;

public class CurrencyServlet extends BaseServlet {

    protected static CurrencyService currencyService;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (HttpMethod.PATCH.isMatches(req.getMethod())) {
            doPatch(req, resp);

        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        var payload = RequestAttributeUtil.getPayload(req, CurrencyIdentifierPayload.class);

        Optional<CurrencyResponse> byPayload = currencyService.findByIdentifier(payload);

        byPayload.ifPresentOrElse(
                responseModel -> RequestAttributeUtil.setResponse(req, responseModel),
                () -> setNotFoundResponse(req, resp, payload.identifier())
        );
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) {
        int id = getIdentifier(req.getPathInfo());
        var payload = RequestAttributeUtil.getPayload(req, CurrencyPayload.class);

        if (currencyService.update(id, payload)) {
            resp.setStatus(SC_NO_CONTENT);
        } else {
            setNotFoundResponse(req, resp, String.valueOf(id));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        var payload = RequestAttributeUtil.getPayload(req, CurrencyIdentifierPayload.class);

        if (currencyService.delete(payload)) {
            resp.setStatus(SC_NO_CONTENT);

        } else {
            setNotFoundResponse(req, resp,
                    "Currency with identifier: %s not found.".formatted(payload.identifier()));
        }
    }

    private int getIdentifier(String pathInfo) {
        var pathPrefix = "/";
        return Integer.parseInt(pathInfo.split(pathPrefix)[1]);
    }
}
