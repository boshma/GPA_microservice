package com.microservice.user_service.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

public abstract class BaseAuthenticationFilter extends OncePerRequestFilter {

    protected static final Set<String> PERMITTED_PATHS = Set.of(
            "/api/auth/",
            "/swagger-ui/",
            "/v3/api-docs/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        
        if (isPermittedPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        doAuthenticate(request, response, filterChain);
    }

    protected abstract void doAuthenticate(HttpServletRequest request,
                                         HttpServletResponse response,
                                         FilterChain filterChain) throws ServletException, IOException;

    protected boolean isPermittedPath(String requestUri) {
        return PERMITTED_PATHS.stream().anyMatch(requestUri::startsWith);
    }
}
