package com.example.ems.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.ems.model.Department;
import com.example.ems.service.DepartmentService;

@RestController
@RequestMapping("/departments")
@CrossOrigin(origins = "http://localhost:5173")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;


    // =========================================================
    // ADD DEPARTMENT
    // ADMIN ONLY
    // =========================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Department> addDepartment(
            @RequestBody Department department) {

        return ResponseEntity.ok(
                departmentService.addDepartment(
                        department
                )
        );
    }


    // =========================================================
    // GET DEPARTMENTS
    // ADMIN -> All
    // HR -> All
    // MANAGER -> Own
    // EMPLOYEE -> Own
    // =========================================================

    @GetMapping
    public ResponseEntity<List<Department>>
            getDepartments(
                    Authentication authentication) {

        return ResponseEntity.ok(
                departmentService.getDepartmentsForUser(
                        authentication
                )
        );
    }


    // =========================================================
    // GET DEPARTMENT BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getDepartmentById(
            @PathVariable Long id,
            Authentication authentication) {

        Optional<Department> department =
                departmentService.getDepartmentById(
                        id,
                        authentication
                );

        if (department.isPresent()) {

            return ResponseEntity.ok(
                    department.get()
            );
        }

        return ResponseEntity
                .notFound()
                .build();
    }


    // =========================================================
    // UPDATE DEPARTMENT
    // ADMIN ONLY
    // =========================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateDepartment(
            @PathVariable Long id,
            @RequestBody Department department) {

        Department updatedDepartment =
                departmentService.updateDepartment(
                        id,
                        department
                );

        if (updatedDepartment != null) {

            return ResponseEntity.ok(
                    updatedDepartment
            );
        }

        return ResponseEntity
                .notFound()
                .build();
    }


    // =========================================================
    // DELETE DEPARTMENT
    // ADMIN ONLY
    // =========================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteDepartment(
            @PathVariable Long id) {

        boolean deleted =
                departmentService.deleteDepartment(id);

        if (deleted) {

            return ResponseEntity.ok(
                    "Department Deleted Successfully"
            );
        }

        return ResponseEntity
                .notFound()
                .build();
    }
}