package com.example.ems.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.ems.model.Employee;
import com.example.ems.service.ManagerService;

@RestController
@RequestMapping("/manager")
@CrossOrigin(origins = "http://localhost:5173")
public class ManagerController {

    @Autowired
    private ManagerService managerService;

    // =========================
    // GET MY TEAM
    // =========================

    @GetMapping("/my-team")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<Employee>> getMyTeam(
            Authentication authentication) {

        return ResponseEntity.ok(
                managerService.getMyTeam(authentication)
        );
    }
}