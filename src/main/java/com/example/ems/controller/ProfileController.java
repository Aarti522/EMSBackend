package com.example.ems.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.ems.model.Employee;
import com.example.ems.service.ProfileService;

@RestController
@RequestMapping("/profile")
@CrossOrigin(origins = "http://localhost:5173")
public class ProfileController {

    @Autowired
    private ProfileService profileService;


    // ==========================================
    // GET MY PROFILE
    // ==========================================

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyProfile(
            Authentication authentication) {

        return ResponseEntity.ok(
                profileService.getMyProfile(
                        authentication
                )
        );
    }


    // ==========================================
    // UPDATE MY PROFILE
    // ==========================================

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateMyProfile(
            Authentication authentication,
            @RequestBody Employee employee) {

        return ResponseEntity.ok(
                profileService.updateMyProfile(
                        authentication,
                        employee
                )
        );
    }


    // ==========================================
    // COMPLETE FIRST LOGIN
    // ==========================================

    @PutMapping("/complete-first-login")
    public ResponseEntity<Map<String, Object>> completeFirstLogin(
            Authentication authentication) {

        return ResponseEntity.ok(
                profileService.completeFirstLogin(
                        authentication
                )
        );
    }
}