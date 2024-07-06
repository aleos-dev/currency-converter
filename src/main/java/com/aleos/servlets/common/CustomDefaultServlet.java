package com.aleos.servlets.common;

import com.aleos.exceptions.servlets.HttpResponseWritingException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.servlets.DefaultServlet;

import java.io.IOException;

/**
 * Custom Default Servlet to override the behavior of the DefaultServlet.
 * This servlet aims to handle non-existing resources by returning a 404 Not Found response
 * instead of the strange default behavior where DefaultServlet returns 405 Method Not Allowed.
 * PATCH requests for non-existing resources will return 501 Not Implemented instead of 404.
 * This behavior for PATCH requests is left untouched and can be changed on demand in the service method.
 */
@WebServlet(name = "CustomDefaultServlet", urlPatterns = "/", loadOnStartup = 1)
public class CustomDefaultServlet extends DefaultServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        handleRequest(response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        handleRequest(response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) {
        handleRequest(response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        handleRequest(response);
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) {
        handleRequest(response);
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        handleRequest(response);
    }

    @Override
    protected void doTrace(HttpServletRequest request, HttpServletResponse response) {
        handleRequest(response);
    }

    private void handleRequest(HttpServletResponse response) {
        try {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
        } catch (IOException e) {
            throw new HttpResponseWritingException("Can't write error response", e);
        }
    }
}
