package com.aleos.filter.common;

import com.aleos.exception.dao.NullConstraintViolationException;
import com.aleos.exception.dao.UnknownParameterTypeException;
import com.aleos.exception.dao.DaoOperationException;
import com.aleos.exception.dao.UniqueConstraintViolationException;
import com.aleos.exception.servlet.*;
import com.aleos.filter.AbstractBaseFilter;
import com.aleos.model.dto.out.Error;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExceptionHandlingFilter extends AbstractBaseFilter {

    protected ObjectMapper objectMapper;

    private static final Logger logger = Logger.getLogger(ExceptionHandlingFilter.class.getName());

    @Override
    public void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException {
        try {
            chain.doFilter(req, resp);
        } catch (RequestBodyParsingException
                 | NumberFormatException
                 | PayloadNotFoundException
                 | NullConstraintViolationException e) {
            handleException(resp, e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        } catch (UniqueConstraintViolationException e) {
            handleException(resp, e);
            resp.setStatus(HttpServletResponse.SC_CONFLICT);

        } catch (HttpResponseWritingException
                 | WrappedJsonProcessingException
                 | DaoOperationException
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
