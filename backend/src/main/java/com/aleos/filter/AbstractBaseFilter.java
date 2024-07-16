package com.aleos.filter;

import com.aleos.util.ComponentInitializerUtil;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;

import static com.aleos.servlet.common.HttpMethod.*;

public abstract class AbstractBaseFilter extends HttpFilter {

    @Override
    public void init(FilterConfig config) throws ServletException {
        super.init(config);
        ComponentInitializerUtil.injectDependencies(config.getServletContext(), this);
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
