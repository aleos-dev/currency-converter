package com.aleos.servlets.filters;

import com.aleos.exceptions.servlets.HttpResponseWritingException;
import com.aleos.exceptions.servlets.WrappedJsonProcessingException;
import com.aleos.util.AttributeNameUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static java.util.Objects.nonNull;

public class ResponseFilter implements Filter {

    private static final String UTF_8 = "utf-8";

    private static final String APPLICATION_JSON = "application/json";

    private static final String ALLOW_ORIGIN = "*";

    private ObjectMapper objectMapper;

    @Override
    public void init(FilterConfig filterConfig) {

        ServletContext servletContext = filterConfig.getServletContext();
        this.objectMapper = (ObjectMapper) servletContext.getAttribute(AttributeNameUtil.getName(ObjectMapper.class));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        var httpResponse = ((HttpServletResponse) response);
        setInitialHeaders(request, httpResponse);

        chain.doFilter(request, response);

        prepareResponse((HttpServletRequest) request, httpResponse);
    }

    private void setInitialHeaders(ServletRequest request, HttpServletResponse response)
            throws UnsupportedEncodingException {

        request.setCharacterEncoding(UTF_8);
        response.setHeader("Access-Control-Allow-Origin", ALLOW_ORIGIN);
        response.setCharacterEncoding(UTF_8);
    }

    private void prepareResponse(HttpServletRequest request, HttpServletResponse response) {

        var responseObject = request.getAttribute(AttributeNameUtil.RESPONSE_MODEL_ATTR);

        if (nonNull(responseObject)) {
            var json = mapToJson(responseObject);
            writeJson(json, response);
        }
    }

    private void writeJson(String json, HttpServletResponse response) {

        try {
            response.getWriter().write(json);
            response.setContentType(APPLICATION_JSON);

        } catch (IOException e) {
            throw new HttpResponseWritingException("Error writing json response", e);
        }
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
