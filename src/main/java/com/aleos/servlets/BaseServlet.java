package com.aleos.servlets;

import com.aleos.util.ComponentInitializerUtil;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;

public abstract class BaseServlet extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ComponentInitializerUtil.injectDependencies(config.getServletContext(), this);
    }
}
