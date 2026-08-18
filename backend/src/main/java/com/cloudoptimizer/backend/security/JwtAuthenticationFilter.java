package com.cloudoptimizer.backend.security;

import com.cloudoptimizer.backend.model.User;
import com.cloudoptimizer.backend.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        System.out.println("========================================");
        System.out.println("JWT FILTER");
        System.out.println(
                "Request: "
                        + request.getMethod()
                        + " "
                        + request.getRequestURI()
        );

        String authorizationHeader =
                request.getHeader("Authorization");

        System.out.println(
                "Authorization header exists: "
                        + (authorizationHeader != null)
        );

        // =====================================================
        // NO AUTHORIZATION HEADER
        // =====================================================

        if (
                authorizationHeader == null ||
                authorizationHeader.isBlank()
        ) {

            System.out.println("No Authorization header");
            System.out.println("========================================");

            filterChain.doFilter(request, response);
            return;
        }

        // =====================================================
        // CHECK BEARER
        // =====================================================

        if (!authorizationHeader.startsWith("Bearer ")) {

            System.out.println(
                    "Authorization header is not Bearer"
            );

            System.out.println("========================================");

            filterChain.doFilter(request, response);
            return;
        }

        // =====================================================
        // EXTRACT TOKEN
        // =====================================================

        String token =
                authorizationHeader
                        .substring(7)
                        .trim();

        if (token.isBlank()) {

            System.out.println("Bearer token is empty");
            System.out.println("========================================");

            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("JWT token received");
        System.out.println(
                "JWT token length: "
                        + token.length()
        );

        try {

            // =================================================
            // EXTRACT EMAIL
            // =================================================

            String email =
                    jwtService.extractEmail(token);

            System.out.println(
                    "Email extracted from JWT: "
                            + email
            );

            if (
                    email == null ||
                    email.isBlank()
            ) {

                System.out.println(
                        "JWT does not contain a valid email"
                );

                System.out.println("========================================");

                filterChain.doFilter(request, response);
                return;
            }

            email =
                    email
                            .trim()
                            .toLowerCase();

            // =================================================
            // CHECK EXISTING AUTHENTICATION
            // =================================================

            if (
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication()
                            != null
            ) {

                System.out.println(
                        "SecurityContext already contains authentication"
                );

                System.out.println("========================================");

                filterChain.doFilter(request, response);
                return;
            }

            // =================================================
            // FIND USER
            // =================================================

            User user =
                    userRepository
                            .findByEmail(email)
                            .orElse(null);

            if (user == null) {

                System.out.println(
                        "USER NOT FOUND"
                );

                System.out.println(
                        "Email: "
                                + email
                );

                System.out.println("========================================");

                filterChain.doFilter(request, response);
                return;
            }

            System.out.println("User found");
            System.out.println(
                    "User ID: "
                            + user.getId()
            );

            System.out.println(
                    "User email: "
                            + user.getEmail()
            );

            // =================================================
            // VALIDATE JWT
            // =================================================

            boolean tokenValid =
                    jwtService.isTokenValid(
                            token,
                            user
                    );

            System.out.println(
                    "JWT valid: "
                            + tokenValid
            );

            if (!tokenValid) {

                System.out.println(
                        "JWT VALIDATION FAILED"
                );

                System.out.println("========================================");

                filterChain.doFilter(request, response);
                return;
            }

            // =================================================
            // CREATE AUTHENTICATION
            //
            // IMPORTANT:
            //
            // Use EMAIL as principal instead of User object.
            //
            // This makes:
            //
            // authentication.getName()
            //
            // return the user's email.
            // =================================================

            UsernamePasswordAuthenticationToken
                    authenticationToken =

                    new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        Collections.emptyList()
                );

            // =================================================
            // REQUEST DETAILS
            // =================================================

            authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            // =================================================
            // SET SECURITY CONTEXT
            // =================================================

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authenticationToken
                    );

            System.out.println("========================================");
            System.out.println(
                    "JWT AUTHENTICATION SUCCESSFUL"
            );

            System.out.println(
                    "User ID: "
                            + user.getId()
            );

            System.out.println(
                    "Email: "
                            + user.getEmail()
            );

            System.out.println(
                    "Authentication principal: "
                            + authenticationToken.getPrincipal()
            );

            System.out.println("========================================");

        } catch (Exception e) {

            System.err.println("========================================");
            System.err.println(
                    "JWT AUTHENTICATION FAILED"
            );

            System.err.println(
                    "Request: "
                            + request.getRequestURI()
            );

            System.err.println(
                    "Error type: "
                            + e.getClass().getName()
            );

            System.err.println(
                    "Error message: "
                            + e.getMessage()
            );

            e.printStackTrace();

            System.err.println("========================================");

            SecurityContextHolder
                    .clearContext();
        }

        filterChain.doFilter(request, response);
    }
}