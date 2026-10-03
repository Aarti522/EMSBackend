package com.example.ems.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.ems.service.AIService;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "http://localhost:5173")
public class AIController {

    @Autowired
    private AIService aiService;


    // =========================================================
    // 1. PERFORMANCE PREDICTION
    // ADMIN, HR, MANAGER, EMPLOYEE
    // =========================================================
    @PostMapping("/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<?> predictPerformance(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        return ResponseEntity.ok(
                aiService.predictPerformance(
                        request,
                        authentication
                )
        );
    }


    // =========================================================
    // 2. ATTRITION PREDICTION
    // ADMIN, HR, MANAGER
    // =========================================================
    @PostMapping("/attrition")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<?> predictAttrition(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        return ResponseEntity.ok(
                aiService.predictAttrition(
                        request,
                        authentication
                )
        );
    }


    // =========================================================
    // 3. ATTENDANCE AI INSIGHTS
    // ADMIN, HR, MANAGER, EMPLOYEE
    // =========================================================
    @PostMapping("/ai-attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<?> attendanceInsights(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        return ResponseEntity.ok(
                aiService.attendanceInsights(
                        request,
                        authentication
                )
        );
    }


    // =========================================================
    // 4. RESUME SCREENING
    // ADMIN, HR ONLY
    //
    // Request type:
    // multipart/form-data
    //
    // Fields:
    // resume
    // job_description
    // =========================================================
    @PostMapping(
            value = "/resume",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> screenResume(
            @RequestParam("resume") MultipartFile resume,
            @RequestParam("job_description") String jobDescription,
            Authentication authentication) {

        return ResponseEntity.ok(
                aiService.screenResume(
                        resume,
                        jobDescription,
                        authentication
                )
        );
    }


    // =========================================================
    // 5. HR CHATBOT
    // ADMIN, HR, MANAGER, EMPLOYEE
    // =========================================================
    @PostMapping("/chatbot")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<?> chatbot(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        return ResponseEntity.ok(
                aiService.chatbot(
                        request,
                        authentication
                )
        );
    }
}