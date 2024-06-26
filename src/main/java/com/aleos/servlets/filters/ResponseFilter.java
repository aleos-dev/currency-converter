package com.aleos.servlets.filters;

import com.aleos.exceptions.servlets.HttpResponseWritingException;
import com.aleos.exceptions.servlets.WrappedJsonProcessingException;
import com.aleos.servlets.ResponseWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class ResponseFilter implements Filter {

    private static final String UTF_8 = "utf-8";

    private static final String APPLICATION_JSON = "application/json";

    private static final String ALLOW_ORIGIN = "*";

    private ObjectMapper objectMapper;

    @Override
    public void init(FilterConfig filterConfig) {
        ServletContext servletContext = filterConfig.getServletContext();
        this.objectMapper = (ObjectMapper) servletContext.getAttribute("objectMapper");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        var responseWrapper = getJsonResponseWrapper(response);
        setInitialHeaders(request, responseWrapper);

        chain.doFilter(request, responseWrapper);

        prepareResponse(responseWrapper);
    }

    private ResponseWrapper getJsonResponseWrapper(ServletResponse response) {
        return new ResponseWrapper(((HttpServletResponse) response));
    }

    private void setInitialHeaders(ServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {

        request.setCharacterEncoding(UTF_8);
        response.setHeader("Access-Control-Allow-Origin", ALLOW_ORIGIN);
        response.setCharacterEncoding(UTF_8);
    }

    private void prepareResponse(ResponseWrapper responseWrapper) {

        responseWrapper.getResponseObject()
                .map(this::mapToJson)
                .ifPresent(json -> writeJson(json, responseWrapper));

        responseWrapper.setContentType(APPLICATION_JSON);
    }

    private void writeJson(String json, ResponseWrapper response) {

        response.writeJson(json);
    }

    private String mapToJson(Object responseOutput) {
        try {
            return objectMapper.writeValueAsString(responseOutput);
        } catch (JsonProcessingException e) {
            throw new WrappedJsonProcessingException(
                    "Cannot parse object to JSON format: %s".formatted(responseOutput), e);
        }
    }
}
