package com.microservice.user_service.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class CompositeAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> PERMITTED_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/swagger-ui/",
            "/v3/api-docs/"
    );

    private final ApiKeyAuthFilter apiKeyAuthFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public CompositeAuthenticationFilter(ApiKeyAuthFilter apiKeyAuthFilter,
                                       JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.apiKeyAuthFilter = apiKeyAuthFilter;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return isPermittedPath(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        
        if (isPermittedPath(request.getRequestURI())) {
            // For permitted paths, only apply API key filter
            apiKeyAuthFilter.doFilter(request, response, filterChain);
        } else {
            // For protected paths, apply both filters in sequence
            apiKeyAuthFilter.doFilter(request, response, new FilterChain() {
                @Override
                public void doFilter(jakarta.servlet.ServletRequest req,
                                   jakarta.servlet.ServletResponse res)
                        throws IOException, ServletException {
                    jwtAuthenticationFilter.doFilter(req, res, filterChain);
                }
            });
        }
    }

    private boolean isPermittedPath(String requestUri) {
        return PERMITTED_PATHS.stream().anyMatch(requestUri::startsWith);
    }
}