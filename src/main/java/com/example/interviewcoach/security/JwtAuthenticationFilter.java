package com.example.interviewcoach.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("===== JWT FILTER RUNNING =====");
        System.out.println("Request: " + request.getMethod() + " " + request.getRequestURI());

        String authHeader = request.getHeader("Authorization");
        if ((authHeader == null || !authHeader.startsWith("Bearer "))
                && request.getRequestURI().startsWith("/api/coding/")) {
            logger.warn("Coding execution request has no Bearer authorization header");
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            System.out.println("No Bearer token found.");

            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        System.out.println("JWT token received.");

        try {

            if (jwtService.isTokenValid(token)) {

                String email = jwtService.extractEmail(token);
                String role = jwtService.extractRole(token);

                if (email == null || email.isBlank() || role == null || role.isBlank()) {
                    filterChain.doFilter(request, response);
                    return;
                }
                role = role.trim().toUpperCase(Locale.ROOT);
                if (role.startsWith("ROLE_")) {
                    role = role.substring("ROLE_".length());
                }

                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority("ROLE_" + role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(authority)
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
                if (request.getRequestURI().startsWith("/api/coding/")) {
                    logger.info("Authenticated coding execution request with role ROLE_{}", role);
                }

            } else {

                logger.warn("JWT authentication rejected for {} {}: token invalid or expired",
                        request.getMethod(), request.getRequestURI());
            }

        } catch (Exception e) {

            logger.warn("JWT authentication failed for {} {} ({})",
                    request.getMethod(), request.getRequestURI(), e.getClass().getSimpleName());
        }

        filterChain.doFilter(request, response);
    }
}