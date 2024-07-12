package com.aleos.filters.common;

import com.aleos.exceptions.PayloadCastException;
import com.aleos.exceptions.UnknownParameterTypeException;
import com.aleos.exceptions.daos.DaoOperationException;
import com.aleos.exceptions.daos.UniqueConstraintViolationException;
import com.aleos.exceptions.servlets.*;
import com.aleos.filters.AbstractBaseFilter;
import com.aleos.models.dtos.out.Error;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExceptionHandlingFilter extends AbstractBaseFilter {

    private ObjectMapper objectMapper;

    private static final Logger logger = Logger.getLogger(ExceptionHandlingFilter.class.getName());

    @Override
    public void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException {
        try {
            chain.doFilter(req, resp);
        } catch (RequestBodyParsingException
                 | NumberFormatException
                 | PayloadNotFoundException e) {
            handleException(resp, e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        } catch (UniqueConstraintViolationException e) {
            handleException(resp, e);
            resp.setStatus(HttpServletResponse.SC_CONFLICT);

        } catch (HttpResponseWritingException
                 | WrappedJsonProcessingException
                 | DaoOperationException
                 | PayloadCastException
                 | ContextInitializationException
                 | UnknownParameterTypeException e) {
            handleException(resp, e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            handleException(resp, new RuntimeException("Unexpected server error", e));
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleException(HttpServletResponse response, Exception e) throws IOException {
        logger.log(Level.SEVERE, e.getMessage(), e);

        String json = objectMapper.writeValueAsString(Error.of(e.getMessage()));
        response.resetBuffer();
        response.getWriter().write(json);
        response.setContentType("application/json");
    }
}
