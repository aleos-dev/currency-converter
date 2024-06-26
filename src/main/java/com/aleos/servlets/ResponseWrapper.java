package com.aleos.servlets;

import com.aleos.exceptions.servlets.HttpResponseWritingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Optional;

@Getter
@Setter
public class ResponseWrapper extends HttpServletResponseWrapper {

    private Object responseObject;

    public ResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    public void composeResponse(int status, Object responseObject) {

        this.setStatus(status);
        this.responseObject = responseObject;
    }

    public Optional<Object> getResponseObject() {
        return Optional.ofNullable(responseObject);
    }

    public void writeJson(String json) {

        try {
            getWriter().write(json);
            setContentType("application/json");
        } catch (IOException e) {
            throw new HttpResponseWritingException("Error writing json response", e);
        }
    }
}
