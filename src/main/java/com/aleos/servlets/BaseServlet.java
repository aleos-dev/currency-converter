package com.aleos.servlets;

import com.aleos.exceptions.servlets.PayloadNotFoundException;
import com.aleos.util.AttributeNameUtil;
import com.aleos.util.DependencyInjector;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;

public abstract class BaseServlet extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        DependencyInjector.inject(this, config.getServletContext());
    }

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
