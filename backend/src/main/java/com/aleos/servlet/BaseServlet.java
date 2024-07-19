package com.aleos.servlet;

import com.aleos.util.ComponentInitializerUtil;
import com.aleos.util.RequestAttributeUtil;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public abstract class BaseServlet extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ComponentInitializerUtil.injectDependencies(config.getServletContext(), this);
    }


    protected void setNotFoundResponse(HttpServletRequest req, HttpServletResponse resp, String message) {
        RequestAttributeUtil.setResponse(req, message);
        resp.setStatus(SC_NOT_FOUND);
    }
}
