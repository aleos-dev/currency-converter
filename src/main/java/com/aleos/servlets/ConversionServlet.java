package com.aleos.servlets;

import com.aleos.models.dtos.in.ConversionPayload;
import com.aleos.models.dtos.out.ErrorResponse;
import com.aleos.services.ConversionService;
import com.aleos.util.AttributeNameUtil;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/exchange")
public class ConversionServlet extends HttpServlet {

    private transient ConversionService conversionService;

    @Override
    public void init(ServletConfig config) throws ServletException {

        super.init(config);
        conversionService = (ConversionService) config.getServletContext()
                .getAttribute(AttributeNameUtil.getName(ConversionService.class));

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        var conversionPayload = (ConversionPayload) req.getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);

        var conversionResponseOptional = conversionService.convert(conversionPayload);

        if (conversionResponseOptional.isPresent()) {
            req.setAttribute(AttributeNameUtil.RESPONSE_MODEL_ATTR, conversionResponseOptional.get());
            resp.setStatus(HttpServletResponse.SC_OK);

            return;
        }

        req.setAttribute(AttributeNameUtil.RESPONSE_MODEL_ATTR, new ErrorResponse("Conversion is not possible."));
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
}
