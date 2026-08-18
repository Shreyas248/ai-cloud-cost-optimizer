package com.cloudoptimizer.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
    }


    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    // =========================================================
    // CORS CONFIGURATION
    // =========================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();


        // -----------------------------------------------------
        // FRONTEND
        // -----------------------------------------------------

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:5173"
                )
        );


        // -----------------------------------------------------
        // HTTP METHODS
        // -----------------------------------------------------

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );


        // -----------------------------------------------------
        // HEADERS
        // -----------------------------------------------------

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept",
                        "Origin",
                        "X-Requested-With"
                )
        );


        // -----------------------------------------------------
        // EXPOSED HEADERS
        // -----------------------------------------------------

        configuration.setExposedHeaders(
                List.of(
                        "Authorization"
                )
        );


        // -----------------------------------------------------
        // CREDENTIALS
        // -----------------------------------------------------

        configuration.setAllowCredentials(true);


        // -----------------------------------------------------
        // REGISTER CORS
        // -----------------------------------------------------

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();


        source.registerCorsConfiguration(
                "/**",
                configuration
        );


        return source;
    }


    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {


        http

                // =================================================
                // CORS
                // =================================================

                .cors(
                        cors ->
                                cors.configurationSource(
                                        corsConfigurationSource()
                                )
                )


                // =================================================
                // CSRF
                //
                // REST API + JWT = CSRF disabled
                // =================================================

                .csrf(
                        csrf ->
                                csrf.disable()
                )


                // =================================================
                // SESSION
                //
                // JWT authentication is stateless.
                // =================================================

                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )


                // =================================================
                // AUTHORIZATION
                // =================================================

                .authorizeHttpRequests(
                        auth -> auth

                                // ---------------------------------
                                // CORS PREFLIGHT
                                // ---------------------------------

                                .requestMatchers(
                                        HttpMethod.OPTIONS,
                                        "/**"
                                )
                                .permitAll()


                                // ---------------------------------
                                // AUTHENTICATION APIs
                                // ---------------------------------

                                .requestMatchers(
                                        "/api/auth/**"
                                )
                                .permitAll()


                                // ---------------------------------
                                // HEALTH
                                // ---------------------------------

                                .requestMatchers(
                                        "/api/health",
                                        "/actuator",
                                        "/actuator/**"
                                )
                                .permitAll()


                                // ---------------------------------
                                // DOCUMENT APIs
                                //
                                // MUST BE AUTHENTICATED
                                // ---------------------------------

                                .requestMatchers(
                                        "/api/documents/**"
                                )
                                .authenticated()


                                // ---------------------------------
                                // EVERYTHING ELSE
                                // ---------------------------------

                                .anyRequest()
                                .authenticated()
                )


                // =================================================
                // JWT FILTER
                // =================================================

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }
}