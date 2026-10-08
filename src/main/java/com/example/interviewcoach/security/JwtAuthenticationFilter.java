package com.example.interviewcoach.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

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

        System.out.println("Authorization Header: " + authHeader);

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

                System.out.println("JWT authenticated user: " + email);
                System.out.println("Role: " + role);

            } else {

                System.out.println("JWT token is NOT valid.");
            }

        } catch (Exception e) {

            System.out.println("Invalid JWT token.");
            System.out.println("Error: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}