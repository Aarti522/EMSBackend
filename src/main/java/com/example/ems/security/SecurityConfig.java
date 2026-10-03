package com.example.ems.security;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================================================
    // AUTHENTICATION MANAGER
    // =========================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {

        return config.getAuthenticationManager();
    }

    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http

                // -------------------------------------------------
                // CSRF
                // -------------------------------------------------

                .csrf(csrf -> csrf.disable())

                // -------------------------------------------------
                // CORS
                // -------------------------------------------------

                .cors(Customizer.withDefaults())

                // -------------------------------------------------
                // STATELESS JWT AUTHENTICATION
                // -------------------------------------------------

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // -------------------------------------------------
                // EXCEPTION HANDLING
                // -------------------------------------------------

                .exceptionHandling(exception ->
                        exception
                                // 401 - Missing/invalid JWT
                                .authenticationEntryPoint(
                                        jwtAuthenticationEntryPoint
                                )

                                // 403 - Valid JWT but insufficient role
                                .accessDeniedHandler(
                                        jwtAccessDeniedHandler
                                )
                )

                // =================================================
                // AUTHORIZATION
                // =================================================

                .authorizeHttpRequests(auth -> auth

                        // -------------------------------------------------
                        // PUBLIC AUTHENTICATION APIs
                        // -------------------------------------------------

                        .requestMatchers("/auth/**")
                        .permitAll()

                        // -------------------------------------------------
                        // SWAGGER / OPENAPI
                        // -------------------------------------------------

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/swagger-resources/**",
                                "/swagger-resources",
                                "/webjars/**"
                        )
                        .permitAll()

                        // =================================================
                        // AI APIs
                        // =================================================

                        // -------------------------------------------------
                        // Performance Prediction
                        // ADMIN, HR, MANAGER, EMPLOYEE
                        // -------------------------------------------------

                        .requestMatchers("/ai/performance")
                        .hasAnyRole(
                                "ADMIN",
                                "HR",
                                "MANAGER",
                                "EMPLOYEE"
                        )

                        // -------------------------------------------------
                        // Attrition Prediction
                        // ADMIN, HR, MANAGER ONLY
                        // -------------------------------------------------

                        .requestMatchers("/ai/attrition")
                        .hasAnyRole(
                                "ADMIN",
                                "HR",
                                "MANAGER"
                        )

                        // -------------------------------------------------
                        // Attendance AI
                        // ADMIN, HR, MANAGER, EMPLOYEE
                        // -------------------------------------------------

                        .requestMatchers("/ai/ai-attendance")
                        .hasAnyRole(
                                "ADMIN",
                                "HR",
                                "MANAGER",
                                "EMPLOYEE"
                        )

                        // -------------------------------------------------
                        // Resume Screening
                        // ADMIN, HR ONLY
                        // -------------------------------------------------

                        .requestMatchers("/ai/resume")
                        .hasAnyRole(
                                "ADMIN",
                                "HR"
                        )

                        // -------------------------------------------------
                        // HR Chatbot
                        // ADMIN, HR, MANAGER, EMPLOYEE
                        // -------------------------------------------------

                        .requestMatchers("/ai/chatbot")
                        .hasAnyRole(
                                "ADMIN",
                                "HR",
                                "MANAGER",
                                "EMPLOYEE"
                        )

                        // -------------------------------------------------
                        // ALL OTHER APIs
                        // AUTHENTICATED USERS ONLY
                        // -------------------------------------------------

                        .anyRequest()
                        .authenticated()
                )

                // =================================================
                // JWT FILTER
                // =================================================
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}