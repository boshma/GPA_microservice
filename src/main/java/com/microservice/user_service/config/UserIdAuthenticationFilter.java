package com.microservice.user_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

public class UserIdAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(UserIdAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) 
            throws ServletException, IOException {

        String userId = request.getHeader("X-User-ID");
        logger.debug("Processing request with X-User-ID: {}", userId);

        if (userId != null && !userId.isEmpty()) {
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("User authenticated successfully with ID: {}", userId);
        } else {
            logger.warn("Unauthorized request - Missing or empty X-User-ID");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("User ID is required");
            return;
        }

        filterChain.doFilter(request, response);
    }
}