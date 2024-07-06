package com.aleos.filters;

import com.aleos.util.DependencyInjector;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import static com.aleos.servlets.common.HttpMethod.*;

public abstract class AbstractBaseFilter implements Filter {

    @Override
    public void init(FilterConfig config) throws ServletException {

        Filter.super.init(config);
        DependencyInjector.inject(this, config.getServletContext());
    }

    protected boolean isGetMethod(HttpServletRequest req) {
        return req.getMethod().equalsIgnoreCase(GET.toString());
    }

    protected boolean isPostMethod(HttpServletRequest req) {
        return req.getMethod().equalsIgnoreCase(POST.toString());
    }

    protected boolean isPatchMethod(HttpServletRequest req) {
        return req.getMethod().equalsIgnoreCase(PATCH.toString());
    }
}
