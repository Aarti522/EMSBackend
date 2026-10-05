package com.example.ems.controller;

import com.example.ems.model.Employee;
import com.example.ems.service.EmployeeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    @Autowired
    private EmployeeService service;

    // =========================================================
    // GET EMPLOYEES
    //
    // ADMIN -> All employees
    // HR -> All employees
    // MANAGER -> Own department/team
    // EMPLOYEE -> Own record
    // =========================================================
    @GetMapping
    public List<Employee> getEmployees(Authentication authentication) {

        return service.getEmployeesForUser(authentication);
    }


    // =========================================================
    // GET EMPLOYEE BY ID
    //
    // ADMIN / HR -> Any employee
    // MANAGER -> Only own team
    // EMPLOYEE -> Only own record
    // =========================================================
    @GetMapping("/{id}")
    public Employee getEmployeeById(
            @PathVariable Long id,
            Authentication authentication) {

        return service.getEmployeeForUser(id, authentication);
    }


    // =========================================================
    // ADD EMPLOYEE
    //
    // ADMIN / HR ONLY
    // =========================================================
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Employee addEmployee(
            @RequestBody Employee employee) {

        return service.addEmployee(employee);
    }


    // =========================================================
    // UPDATE EMPLOYEE
    //
    // ADMIN / HR ONLY
    // =========================================================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Employee updateEmployee(
            @PathVariable Long id,
            @RequestBody Employee employee) {

        return service.updateEmployee(id, employee);
    }


    // =========================================================
    // DELETE / DEACTIVATE EMPLOYEE
    //
    // ADMIN / HR ONLY
    // =========================================================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<String> deleteEmployee(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.deleteEmployee(id)
        );
    }


    // =========================================================
    // GET EMPLOYEES BY DEPARTMENT
    //
    // ADMIN / HR ONLY
    //
    // IMPORTANT:
    // Manager should NOT directly access an arbitrary
    // department through this endpoint.
    //
    // Manager's team is already handled by GET /employees.
    // =========================================================
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public List<Employee> getEmployeesByDepartment(
            @PathVariable Long departmentId) {

        return service.getEmployeesByDepartment(departmentId);
    }
}