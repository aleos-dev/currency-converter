package com.aleos.filters.common;

import com.aleos.exceptions.daos.UniqueConstraintViolationException;
import com.aleos.exceptions.servlets.RequestBodyParsingException;
import com.aleos.exceptions.daos.DaoOperationException;
import com.aleos.exceptions.servlets.HttpResponseWritingException;
import com.aleos.exceptions.servlets.WrappedJsonProcessingException;
import com.aleos.models.dtos.out.ErrorResponse;
import com.aleos.util.AttributeNameUtil;
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
        objectMapper = (ObjectMapper) filterConfig.getServletContext()
                .getAttribute(AttributeNameUtil.getName(ObjectMapper.class));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {

        var httpResponse = ((HttpServletResponse) response);
        try {

            chain.doFilter(request, response);

        } catch (UniqueConstraintViolationException e) {
            httpResponse.setStatus(HttpServletResponse.SC_CONFLICT);
            handleException(httpResponse, e);
        } catch (NumberFormatException e) {
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            handleException((HttpServletResponse) response, e);
        } catch (HttpResponseWritingException
                 | WrappedJsonProcessingException
                 | DaoOperationException e) {
            handleException((HttpServletResponse) response, e);
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (RequestBodyParsingException e) {
            handleException((HttpServletResponse) response, e);
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            handleException((HttpServletResponse) response, new RuntimeException("Unexpected server error", e));
            ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleException(HttpServletResponse response, Exception e) throws IOException {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);

        String json = objectMapper.writeValueAsString(new ErrorResponse(e.getMessage()));

        response.resetBuffer();
        response.getWriter().write(json);
        response.setContentType("application/json");
    }
}
