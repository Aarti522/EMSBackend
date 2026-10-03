package com.example.ems.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.ems.service.UserService;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserService userService;


    // =========================
    // GET ALL USERS
    // =========================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {

        return ResponseEntity.ok(
                userService.getAllUsers()
        );
    }


    // =========================
    // GET USER BY ID
    // =========================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }


    // =========================
    // CREATE HR / MANAGER
    // =========================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createUser(

            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String role,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) Double salary,
            @RequestParam(required = false) Long departmentId) {

        return ResponseEntity.ok(
                userService.createUser(
                        name,
                        email,
                        password,
                        role,
                        phone,
                        address,
                        designation,
                        salary,
                        departmentId
                )
        );
    }


    // =========================
    // UPDATE USER ROLE / DEPARTMENT
    // =========================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUser(

            @PathVariable Long id,

            @RequestParam String role,

            @RequestParam(required = false)
            Long departmentId) {

        return ResponseEntity.ok(
                userService.updateUser(
                        id,
                        role,
                        departmentId
                )
        );
    }


    // =========================
    // DELETE USER
    // =========================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.deleteUser(id)
        );
    }
}