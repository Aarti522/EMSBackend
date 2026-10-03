package com.example.ems.service;

import com.ems.dto.ForgotPasswordRequest;
import com.example.ems.model.User;
import com.example.ems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    private static final int OTP_EXPIRY_MINUTES = 5;

    // ===============================
    // FORGOT PASSWORD
    // ===============================

    public String sendOtp(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail());

        /*
         * Do not reveal whether an email exists or not.
         * This is safer from a security point of view.
         */
        if (user == null) {
            return "If the email is registered, an OTP has been sent.";
        }

        // Generate 6-digit OTP
        String otp = String.format("%06d", secureRandom.nextInt(1000000));

        // Save OTP
        user.setResetOtp(otp);

        // OTP valid for 5 minutes
        user.setResetOtpExpiry(
                LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)
        );

        userRepository.save(user);

        // Send OTP through email
        emailService.sendOtpEmail(user.getEmail(), otp);

        return "If the email is registered, an OTP has been sent.";
    }


    // ===============================
    // RESET PASSWORD
    // ===============================

    public String resetPassword(com.ems.dto.ResetPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail());

        if (user == null) {
            throw new RuntimeException("Invalid email or OTP.");
        }

        // Check OTP
        if (user.getResetOtp() == null ||
                !user.getResetOtp().equals(request.getOtp())) {

            throw new RuntimeException("Invalid OTP.");
        }

        // Check OTP expiry
        if (user.getResetOtpExpiry() == null ||
                LocalDateTime.now().isAfter(user.getResetOtpExpiry())) {

            throw new RuntimeException("OTP has expired. Please request a new OTP.");
        }

        // Validate new password
        if (request.getNewPassword() == null ||
                request.getNewPassword().length() < 6) {

            throw new RuntimeException(
                    "New password must be at least 6 characters."
            );
        }

        // Encode new password using BCrypt
        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        // Invalidate OTP after successful password reset
        user.setResetOtp(null);
        user.setResetOtpExpiry(null);

        userRepository.save(user);

        return "Password reset successful. Please login with your new password.";
    }
}