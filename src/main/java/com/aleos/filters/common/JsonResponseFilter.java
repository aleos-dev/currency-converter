package com.aleos.filters.common;

import com.aleos.exceptions.servlets.HttpResponseWritingException;
import com.aleos.exceptions.servlets.WrappedJsonProcessingException;
import com.aleos.filters.AbstractBaseFilter;
import com.aleos.util.AttributeNameUtil;
import com.aleos.util.PropertiesUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

public class JsonResponseFilter extends AbstractBaseFilter {

    private ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(req, resp);
        processResponse((HttpServletRequest) req, ((HttpServletResponse) resp));
    }

    private void processResponse(HttpServletRequest req, HttpServletResponse resp) {
            Optional.ofNullable(req.getAttribute(AttributeNameUtil.RESPONSE_MODEL_ATTR))
                    .map(this::toJson)
                    .ifPresent(json -> write(json, resp));
    }

    private void write(String json, HttpServletResponse resp) {
        try {
            resp.getWriter().write(json);
            resp.setContentType(PropertiesUtil.getProperty("response.contentType"));
        } catch (IOException e) {
            throw new HttpResponseWritingException("Error writing json response", e);
        }
    }

    private String toJson(Object responseObject) {
        try {
            return objectMapper.writeValueAsString(responseObject);
        } catch (JsonProcessingException e) {
            throw new WrappedJsonProcessingException(
                    "Cannot parse object to JSON format: %s".formatted(responseObject), e);
        }
    }
}
