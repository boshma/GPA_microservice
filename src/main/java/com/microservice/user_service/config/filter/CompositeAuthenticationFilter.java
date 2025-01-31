package com.microservice.user_service.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;
import java.util.Set;

@Component
public class CompositeAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> PERMITTED_PATHS = Set.of(
            "/api/auth/",
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
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // Check if path is permitted
        if (isPermittedPath(request.getRequestURI())) {
            // For permitted paths, only check API key
            apiKeyAuthFilter.doFilterInternal(request, response, filterChain);
            return;
        }

        // For protected paths, check both API key and JWT
        apiKeyAuthFilter.doFilterInternal(request, response, new FilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest servletRequest,
                               jakarta.servlet.ServletResponse servletResponse)
                    throws IOException, ServletException {
                jwtAuthenticationFilter.doFilterInternal(
                        (HttpServletRequest) servletRequest,
                        (HttpServletResponse) servletResponse,
                        filterChain
                );
            }
        });
    }

    private boolean isPermittedPath(String requestUri) {
        return PERMITTED_PATHS.stream().anyMatch(requestUri::startsWith);
    }
}