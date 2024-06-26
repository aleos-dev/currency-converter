package com.aleos.servlets.filters;

import com.aleos.exceptions.servlets.HttpResponseWritingException;
import com.aleos.exceptions.servlets.WrappedJsonProcessingException;
import com.aleos.models.dtos.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExceptionHandlingFilter implements Filter {

    private ObjectMapper objectMapper;

    private static final Logger LOGGER = Logger.getLogger(ExceptionHandlingFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        Filter.super.init(filterConfig);

        objectMapper = (ObjectMapper) filterConfig.getServletContext().getAttribute("objectMapper");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {

        try {

            chain.doFilter(request, response);

        } catch (HttpResponseWritingException
                 | WrappedJsonProcessingException e) {
            handleException((HttpServletResponse) response, e);
        } catch (Exception e) {
            handleException((HttpServletResponse) response, new RuntimeException("Unexpected server error", e));
        }
    }

    private void handleException(HttpServletResponse response, RuntimeException e) throws IOException {

        LOGGER.log(Level.SEVERE, e.getMessage(), e);

        String json = objectMapper.writeValueAsString(new ErrorResponse(e.getMessage()));

        response.resetBuffer();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(json);
        response.setContentType("application/json");
    }
}
