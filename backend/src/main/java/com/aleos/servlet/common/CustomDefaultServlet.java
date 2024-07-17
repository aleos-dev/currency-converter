package com.aleos.servlet.common;

import com.aleos.exception.servlet.HttpResponseWritingException;
import com.aleos.model.dto.out.Error;
import com.aleos.util.RequestAttributeUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.servlets.DefaultServlet;

import java.io.IOException;

import static com.aleos.servlet.common.HttpMethod.GET;
import static com.aleos.servlet.common.HttpMethod.HEAD;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * This servlet customizes the standard behavior of DefaultServlet, which is intended to serve static resources.
 * By default, requests for non-existing resources can result in a 405 Method Not Allowed or
 * 501 Not Implemented response, which is not the expected 404 Not Found.
 * This servlet overrides the standard behavior to return a 404 Not Found response.
 */

@WebServlet(name = "CustomDefaultServlet", urlPatterns = "/", loadOnStartup = 1)
public class CustomDefaultServlet extends DefaultServlet {

    // doGet() and doHead() methods are specifically designed to serve static resources without side effects.
    // The other HTTP methods—doPost, doPut, doDelete, doOptions, and doTrace—typically aren't used in read-only mode.
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var method = req.getMethod();

        if (GET.isMatches(method) || HEAD.isMatches(method)) {
            serveResource(req, resp, true, this.fileEncoding);
        } else {
            generateResponse(resp);
        }
    }

    @Override
    protected void serveResource(HttpServletRequest request,
                                 HttpServletResponse response,
                                 boolean content,
                                 String inputEncoding) {

        var message = """
                The requested resource '%s' cannot be found. This application does not directly serve static resources.
                """.formatted(request.getRequestURI());

        RequestAttributeUtil.setResponse(request, Error.of(message));
        response.setStatus(SC_NOT_FOUND);
    }

    private void generateResponse(HttpServletResponse response) {
        try {
            // Example of an error page mechanism: this error will be caught and processed by the corresponding error handler.
            response.sendError(SC_NOT_FOUND, "Resource not found");
        } catch (IOException e) {
            throw new HttpResponseWritingException("Can't write error response", e);
        }
    }
}
