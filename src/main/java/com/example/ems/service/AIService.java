package com.example.ems.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.ems.client.PythonAIClient;
import com.example.ems.model.User;
import com.example.ems.repository.UserRepository;

@Service
public class AIService {

    @Autowired
    private PythonAIClient pythonAIClient;

    @Autowired
    private UserRepository userRepository;


    // =========================================================
    // AI PERFORMANCE PREDICTION
    // Access: ADMIN, HR, MANAGER, EMPLOYEE
    // =========================================================

    public Object predictPerformance(
            Map<String, Object> request,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);
        String role = getRole(user);

        // Role-level access control
        checkAccess(
                role,
                "ADMIN",
                "HR",
                "MANAGER",
                "EMPLOYEE"
        );

        Map<String, Object> aiRequest =
                addUserContext(request, user);

        return pythonAIClient.callPerformancePrediction(
                aiRequest
        );
    }


    // =========================================================
    // AI ATTRITION PREDICTION
    // Access: ADMIN, HR, MANAGER
    // EMPLOYEE: NO ACCESS
    // =========================================================

    public Object predictAttrition(
            Map<String, Object> request,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);
        String role = getRole(user);

        // Employee is not allowed
        checkAccess(
                role,
                "ADMIN",
                "HR",
                "MANAGER"
        );

        Map<String, Object> aiRequest =
                addUserContext(request, user);

        return pythonAIClient.callAttritionPrediction(
                aiRequest
        );
    }


    // =========================================================
    // ATTENDANCE AI INSIGHTS
    // Access: ADMIN, HR, MANAGER, EMPLOYEE
    // =========================================================

    public Object attendanceInsights(
            Map<String, Object> request,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);
        String role = getRole(user);

        checkAccess(
                role,
                "ADMIN",
                "HR",
                "MANAGER",
                "EMPLOYEE"
        );

        if (request == null) {
            throw new RuntimeException(
                    "Attendance request data is required."
            );
        }

        Map<String, Object> aiRequest =
                new HashMap<>();

        aiRequest.putAll(request);

        aiRequest.put(
                "role",
                role
        );

        Object employeeName =
                aiRequest.get("employeeName");

        if (employeeName == null
                || String.valueOf(employeeName)
                        .trim()
                        .isEmpty()) {

            throw new RuntimeException(
                    "employeeName is required for attendance analysis."
            );
        }

        return pythonAIClient.callAttendanceInsights(
                aiRequest
        );
    }


    // =========================================================
    // AI RESUME SCREENING
    // Access: ADMIN, HR
    // MANAGER: NO ACCESS
    // EMPLOYEE: NO ACCESS
    // =========================================================

    public Object screenResume(
            MultipartFile resume,
            String jobDescription,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);
        String role = getRole(user);

        // Only ADMIN and HR
        checkAccess(
                role,
                "ADMIN",
                "HR"
        );

        if (resume == null || resume.isEmpty()) {
            throw new RuntimeException(
                    "Resume PDF is required."
            );
        }

        if (jobDescription == null
                || jobDescription.trim().isEmpty()) {

            throw new RuntimeException(
                    "Job description is required."
            );
        }

        return pythonAIClient.callResumeScreening(
                resume,
                jobDescription,
                role
        );
    }


    // =========================================================
    // AI HR CHATBOT
    // Access: ADMIN, HR, MANAGER, EMPLOYEE
    // =========================================================

    public Object chatbot(
            Map<String, Object> request,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);
        String role = getRole(user);

        checkAccess(
                role,
                "ADMIN",
                "HR",
                "MANAGER",
                "EMPLOYEE"
        );

        Map<String, Object> aiRequest =
                new HashMap<>();

        if (request != null) {
            aiRequest.putAll(request);
        }

        aiRequest.put(
                "userEmail",
                user.getEmail()
        );

        aiRequest.put(
                "role",
                role
        );

        return pythonAIClient.callChatbot(
                aiRequest
        );
    }


    // =========================================================
    // GET AUTHENTICATED USER
    // =========================================================

    private User getAuthenticatedUser(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {

            throw new RuntimeException(
                    "User is not authenticated."
            );
        }

        User user =
                userRepository.findByEmail(
                        authentication.getName()
                );

        if (user == null) {
            throw new RuntimeException(
                    "Authenticated user was not found."
            );
        }

        return user;
    }


    // =========================================================
    // GET USER ROLE
    // =========================================================

    private String getRole(User user) {

        if (user.getRole() == null) {
            throw new RuntimeException(
                    "User role is not configured."
            );
        }

        return user.getRole()
                .trim()
                .toUpperCase();
    }


    // =========================================================
    // ADD USER CONTEXT
    // Existing Python integration preserved
    // =========================================================

    private Map<String, Object> addUserContext(
            Map<String, Object> request,
            User user) {

        Map<String, Object> aiRequest =
                new HashMap<>();

        if (request != null) {
            aiRequest.putAll(request);
        }

        aiRequest.put(
                "userEmail",
                user.getEmail()
        );

        aiRequest.put(
                "userRole",
                getRole(user)
        );

        return aiRequest;
    }


    // =========================================================
    // COMMON ACCESS CONTROL
    // =========================================================

    private void checkAccess(
            String userRole,
            String... allowedRoles) {

        for (String allowedRole : allowedRoles) {

            if (allowedRole.equals(userRole)) {
                return;
            }
        }

        throw new RuntimeException(
                "You are not authorized to access this AI feature."
        );
    }
}