package com.aleos.filter.common;

import com.aleos.exception.servlet.HttpResponseWritingException;
import com.aleos.exception.servlet.WrappedJsonProcessingException;
import com.aleos.filter.AbstractBaseFilter;
import com.aleos.util.PropertiesUtil;
import com.aleos.util.RequestAttributeUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class JsonResponseFilter extends AbstractBaseFilter {

    private ObjectMapper objectMapper;

    @Override
    public void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(req, resp);
        processResponse(req, resp);
    }

    private void processResponse(HttpServletRequest req, HttpServletResponse resp) {
        RequestAttributeUtil.getResponse(req)
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
