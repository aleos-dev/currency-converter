package com.aleos.servlets.common;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/error404")
public class Error404Servlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(Error404Servlet.class.getName());

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        sendJsonResponse(resp);
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    private void sendJsonResponse(HttpServletResponse resp) {
        try {
            PrintWriter out = resp.getWriter();
            out.print(
                    """
                            {
                               "error": "404 - Not Found",
                               "joke": "Why do Java developers wear glasses? Because they don't C#! Xa-xa-xa!"
                            }
                            """);
            out.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
