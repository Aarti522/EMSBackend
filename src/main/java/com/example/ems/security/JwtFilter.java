package com.example.ems.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        // JWT is not required for authentication endpoints
        return path.startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        String authHeader =
                request.getHeader("Authorization");

        String token = null;
        String username = null;

        System.out.println();
        System.out.println("==========================================");
        System.out.println("JWT FILTER");
        System.out.println("Request URI : " + requestUri);
        System.out.println("Method      : " + request.getMethod());
        System.out.println("==========================================");

        /*
         * --------------------------------------------------
         * 1. Check Authorization header
         * --------------------------------------------------
         */

        if (authHeader == null || authHeader.isBlank()) {

            System.out.println(
                    "JWT STATUS  : NO AUTHORIZATION HEADER"
            );

        } else if (!authHeader.startsWith("Bearer ")) {

            System.out.println(
                    "JWT STATUS  : INVALID AUTHORIZATION FORMAT"
            );

            System.out.println(
                    "Expected    : Bearer <token>"
            );

        } else {

            /*
             * --------------------------------------------------
             * 2. Extract token
             * --------------------------------------------------
             */

            token = authHeader.substring(7).trim();

            if (token.isEmpty()) {

                System.out.println(
                        "JWT STATUS  : EMPTY BEARER TOKEN"
                );

            } else {

                /*
                 * --------------------------------------------------
                 * 3. Extract username from JWT
                 * --------------------------------------------------
                 */

                try {

                    username =
                            jwtUtil.extractUsername(token);

                    System.out.println(
                            "JWT USERNAME: " + username
                    );

                } catch (Exception e) {

                    System.out.println(
                            "JWT STATUS  : TOKEN EXTRACTION FAILED"
                    );

                    System.out.println(
                            "JWT ERROR   : " + e.getMessage()
                    );

                    SecurityContextHolder.clearContext();
                }
            }
        }

        /*
         * --------------------------------------------------
         * 4. Authenticate user
         * --------------------------------------------------
         */

        if (username != null
                && SecurityContextHolder
                        .getContext()
                        .getAuthentication() == null) {

            try {

                UserDetails userDetails =
                        customUserDetailsService
                                .loadUserByUsername(username);

                System.out.println(
                        "DB USER     : "
                                + userDetails.getUsername()
                );

                System.out.println(
                        "DB AUTHORITIES: "
                                + userDetails.getAuthorities()
                );

                /*
                 * --------------------------------------------------
                 * 5. Validate JWT
                 * --------------------------------------------------
                 */

                boolean validToken =
                        jwtUtil.validateToken(
                                token,
                                userDetails.getUsername()
                        );

                if (validToken) {

                    /*
                     * --------------------------------------------------
                     * 6. Create Spring Security authentication
                     * --------------------------------------------------
                     */

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);

                    System.out.println(
                            "=========================================="
                    );

                    System.out.println(
                            "JWT AUTHENTICATION SUCCESS"
                    );

                    System.out.println(
                            "Username    : "
                                    + userDetails.getUsername()
                    );

                    System.out.println(
                            "Authorities  : "
                                    + userDetails.getAuthorities()
                    );

                    System.out.println(
                            "Request URI  : " + requestUri
                    );

                    System.out.println(
                            "=========================================="
                    );

                } else {

                    System.out.println(
                            "=========================================="
                    );

                    System.out.println(
                            "JWT VALIDATION FAILED"
                    );

                    System.out.println(
                            "Username    : " + username
                    );

                    System.out.println(
                            "Request URI : " + requestUri
                    );

                    System.out.println(
                            "=========================================="
                    );

                    SecurityContextHolder.clearContext();
                }

            } catch (Exception e) {

                System.out.println(
                        "=========================================="
                );

                System.out.println(
                        "JWT AUTHENTICATION FAILED"
                );

                System.out.println(
                        "Username    : " + username
                );

                System.out.println(
                        "Request URI : " + requestUri
                );

                System.out.println(
                        "Error       : " + e.getMessage()
                );

                e.printStackTrace();

                System.out.println(
                        "=========================================="
                );

                SecurityContextHolder.clearContext();
            }
        }

        /*
         * --------------------------------------------------
         * 7. Continue request
         * --------------------------------------------------
         */

        filterChain.doFilter(request, response);
    }
}