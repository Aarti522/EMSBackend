package com.example.ems.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.ems.service.DashboardService;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;


    // ==========================================
    // ROLE-BASED DASHBOARD
    // ==========================================

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboard(
            Authentication authentication) {

        return ResponseEntity.ok(
                dashboardService.getDashboard(
                        authentication
                )
        );
    }
}