package com.aleos.servlets;

import com.aleos.exceptions.servlets.PayloadNotFoundException;
import com.aleos.util.AttributeNameUtil;
import com.aleos.util.DependencyInjector;
import com.aleos.util.PropertiesUtil;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;

import java.io.IOException;
import java.util.Set;

public abstract class BaseServlet extends HttpServlet {

    private transient Set<HttpMethod> supportedMethods;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        supportedMethods = getSupportedMethods();
        DependencyInjector.inject(this, config.getServletContext());
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (supportedMethods.contains(HttpMethod.valueOf(req.getMethod().toUpperCase()))) {
            super.service(req, resp);
        } else {
            req.getRequestDispatcher(PropertiesUtil.ERROR_PAGE_404).forward(req, resp);
        }
    }

    protected abstract Set<HttpMethod> getSupportedMethods();

    protected <T> T getPayload(HttpServletRequest req, Class<T> type) {
        Object rawPayload = req.getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);

        if (rawPayload == null) {
            throw new PayloadNotFoundException("Payload is not found for %s".formatted(this.getClass().getSimpleName()));

        } else if (!type.isInstance(rawPayload)) {
            throw new PayloadNotFoundException("Payload type is incorrect for %s. Expected: %s, Found: %s".formatted(
                    this.getClass().getSimpleName(), type.getSimpleName(), rawPayload.getClass().getSimpleName()));
        } else {
            return type.cast(rawPayload);
        }
    }

    protected void setResponseModel(HttpServletRequest request, @NonNull Object responseModel) {
        request.setAttribute(AttributeNameUtil.RESPONSE_MODEL_ATTR, responseModel);
    }
}
