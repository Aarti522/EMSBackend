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

                .csrf(csrf -> csrf.disable())

                // Uses CorsConfig.java
                .cors(Customizer.withDefaults())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        jwtAuthenticationEntryPoint
                                )
                                .accessDeniedHandler(
                                        jwtAccessDeniedHandler
                                )
                )

                .authorizeHttpRequests(auth -> auth

                        // PUBLIC AUTH APIs
                        .requestMatchers("/auth/**")
                        .permitAll()

                        // SWAGGER / OPENAPI
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

                        // PERFORMANCE AI
                        .requestMatchers("/ai/performance")
                        .hasAnyRole(
                                "ADMIN",
                                "HR",
                                "MANAGER",
                                "EMPLOYEE"
                        )

                        // ATTRITION AI
                        .requestMatchers("/ai/attrition")
                        .hasAnyRole(
                                "ADMIN",
                                "HR",
                                "MANAGER"
                        )

                        // ATTENDANCE AI
                        .requestMatchers("/ai/ai-attendance")
                        .hasAnyRole(
                                "ADMIN",
                                "HR",
                                "MANAGER",
                                "EMPLOYEE"
                        )

                        // RESUME AI
                        .requestMatchers("/ai/resume")
                        .hasAnyRole(
                                "ADMIN",
                                "HR"
                        )

                        // CHATBOT AI
                        .requestMatchers("/ai/chatbot")
                        .hasAnyRole(
                                "ADMIN",
                                "HR",
                                "MANAGER",
                                "EMPLOYEE"
                        )

                        // EVERYTHING ELSE
                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}