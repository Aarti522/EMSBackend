package com.example.ems.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.ems.model.Employee;
import com.example.ems.model.User;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.UserRepository;

@Service
public class ManagerService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;


    // =========================
    // GET MANAGER'S TEAM
    // =========================

    public List<Employee> getMyTeam(
            Authentication authentication) {

        if (authentication == null) {
            throw new AccessDeniedException(
                    "Authentication required"
            );
        }

        String email = authentication.getName();

        User manager =
                userRepository.findByEmail(email);

        if (manager == null) {
            throw new AccessDeniedException(
                    "User not found"
            );
        }

        // Only MANAGER can use this service
        if (!"MANAGER".equalsIgnoreCase(
                manager.getRole())) {

            throw new AccessDeniedException(
                    "Only managers can access team data"
            );
        }

        // Manager must be linked with Employee
        if (manager.getEmployee() == null) {
            throw new AccessDeniedException(
                    "Manager employee profile not linked"
            );
        }

        // Manager must have a department
        if (manager.getEmployee().getDepartment() == null) {
            throw new AccessDeniedException(
                    "Manager department not assigned"
            );
        }

        Long departmentId =
                manager.getEmployee()
                        .getDepartment()
                        .getId();

        // Get all employees belonging
        // to manager's department
        return employeeRepository
                .findByDepartmentIdAndStatusIgnoreCase(
                        departmentId,
                        "ACTIVE"
                )
                .stream()
                .filter(employee ->
                        !employee.getId().equals(
                                manager.getEmployee().getId()
                        )
                )
                .toList();
    }
}