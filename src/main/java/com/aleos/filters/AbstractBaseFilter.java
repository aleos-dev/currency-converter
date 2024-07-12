package com.aleos.filters;

import com.aleos.util.DependencyInjector;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;

import static com.aleos.servlets.common.HttpMethod.*;

public abstract class AbstractBaseFilter extends HttpFilter {

    @Override
    public void init(FilterConfig config) throws ServletException {
        super.init(config);
        DependencyInjector.inject(config.getServletContext(), this);
    }

    protected boolean isGet(HttpServletRequest req) {
        return GET.isMatches(req.getMethod());
    }

    protected boolean isPost(HttpServletRequest req) {
        return POST.isMatches(req.getMethod());
    }

    protected boolean isPatch(HttpServletRequest req) {
        return PATCH.isMatches(req.getMethod());
    }
}
