package com.example.ems.controller;

import com.ems.dto.ForgotPasswordRequest;
import com.ems.dto.ResetPasswordRequest;
import com.example.ems.service.PasswordResetService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;


    // ==========================================
    // FORGOT PASSWORD - SEND OTP
    // ==========================================

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
          @Valid  @RequestBody ForgotPasswordRequest request) {

        String response = passwordResetService.sendOtp(request);

        return ResponseEntity.ok(response);
    }


    // ==========================================
    // RESET PASSWORD
    // ==========================================

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
          @Valid  @RequestBody ResetPasswordRequest request) {

        try {

            String response = passwordResetService.resetPassword(request);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}