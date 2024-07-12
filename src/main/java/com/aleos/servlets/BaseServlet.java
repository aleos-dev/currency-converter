package com.aleos.servlets;

import com.aleos.util.DependencyInjector;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;

public abstract class BaseServlet extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        DependencyInjector.inject(this, config.getServletContext());
    }
}
