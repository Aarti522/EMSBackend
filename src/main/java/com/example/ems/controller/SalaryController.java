package com.example.ems.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.ems.model.Salary;
import com.example.ems.service.SalaryService;

@RestController
@RequestMapping("/salary")
@CrossOrigin(origins = "http://localhost:5173")
public class SalaryController {

    @Autowired
    private SalaryService salaryService;


    // =========================================================
    // ADD SALARY
    // ADMIN + HR
    // =========================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Salary> addSalary(
            @RequestBody Salary salary,
            Authentication authentication) {

        return ResponseEntity.ok(
                salaryService.addSalary(
                        salary,
                        authentication
                )
        );
    }


    // =========================================================
    // GET SALARIES
    // =========================================================

    @GetMapping
    public ResponseEntity<List<Salary>> getSalaries(
            Authentication authentication) {

        return ResponseEntity.ok(
                salaryService.getSalariesForUser(
                        authentication
                )
        );
    }


    // =========================================================
    // GET SALARY BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getSalaryById(
            @PathVariable Long id,
            Authentication authentication) {

        Optional<Salary> salary =
                salaryService.getSalaryById(
                        id,
                        authentication
                );

        if (salary.isPresent()) {
            return ResponseEntity.ok(
                    salary.get()
            );
        }

        return ResponseEntity.notFound().build();
    }


    // =========================================================
    // GET SALARY BY EMPLOYEE ID
    // =========================================================

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Salary>>
            getSalaryByEmployeeId(
                    @PathVariable Long employeeId,
                    Authentication authentication) {

        return ResponseEntity.ok(
                salaryService.getSalaryByEmployeeId(
                        employeeId,
                        authentication
                )
        );
    }


    // =========================================================
    // UPDATE SALARY
    // ADMIN + HR
    // =========================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> updateSalary(
            @PathVariable Long id,
            @RequestBody Salary salary,
            Authentication authentication) {

        Salary updatedSalary =
                salaryService.updateSalary(
                        id,
                        salary,
                        authentication
                );

        if (updatedSalary != null) {
            return ResponseEntity.ok(
                    updatedSalary
            );
        }

        return ResponseEntity.notFound().build();
    }


    // =========================================================
    // DELETE SALARY
    // ADMIN + HR
    // =========================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<String> deleteSalary(
            @PathVariable Long id,
            Authentication authentication) {

        boolean deleted =
                salaryService.deleteSalary(
                        id,
                        authentication
                );

        if (deleted) {
            return ResponseEntity.ok(
                    "Salary Deleted Successfully"
            );
        }

        return ResponseEntity.notFound().build();
    }
}