package com.example.ems.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.ems.model.Employee;
import com.example.ems.model.Salary;
import com.example.ems.model.User;
import com.example.ems.repository.SalaryRepository;
import com.example.ems.repository.UserRepository;

@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SalaryRepository salaryRepository;


    // ==========================================
    // GET LOGGED-IN USER PROFILE
    // ==========================================

    public Map<String, Object> getMyProfile(
            Authentication authentication) {

        User user = getUser(authentication);

        Map<String, Object> profile = new HashMap<>();


        // ==========================================
        // USER INFORMATION
        // ==========================================

        profile.put("userId", user.getId());
        profile.put("email", user.getEmail());
        profile.put("role", user.getRole());
        profile.put("firstLogin", user.isFirstLogin());


        // ==========================================
        // EMPLOYEE INFORMATION
        // ==========================================

        Employee employee = user.getEmployee();

        if (employee != null) {

            profile.put("employeeId", employee.getId());
            profile.put("name", employee.getName());
            profile.put("phone", employee.getPhone());
            profile.put("address", employee.getAddress());
            profile.put("designation", employee.getDesignation());
            profile.put("status", employee.getStatus());


            // ==========================================
            // DEPARTMENT
            // ==========================================

            if (employee.getDepartment() != null) {

                profile.put(
                        "departmentId",
                        employee.getDepartment().getId()
                );

                profile.put(
                        "department",
                        employee.getDepartment().getName()
                );
            }


            // ==========================================
            // SALARY FROM PAYROLL TABLE
            // ==========================================

            List<Salary> salaries =
                    salaryRepository.findByEmployeeId(
                            employee.getId()
                    );

            if (!salaries.isEmpty()) {

                // Latest salary record
                Salary latestSalary =
                        salaries.get(salaries.size() - 1);

                profile.put(
                        "salary",
                        latestSalary.getNetSalary()
                );

            } else {

                profile.put(
                        "salary",
                        0
                );
            }
        }


        return profile;
    }


    // ==========================================
    // UPDATE MY PROFILE
    // ==========================================

    public Map<String, Object> updateMyProfile(
            Authentication authentication,
            Employee updatedEmployee) {

        User user = getUser(authentication);

        Employee employee = user.getEmployee();


        if (employee == null) {

            throw new AccessDeniedException(
                    "Employee profile not linked"
            );
        }


        // ==========================================
        // ALLOWED PROFILE FIELDS
        // ==========================================

        if (updatedEmployee.getName() != null) {

            employee.setName(
                    updatedEmployee.getName()
            );
        }


        if (updatedEmployee.getPhone() != null) {

            employee.setPhone(
                    updatedEmployee.getPhone()
            );
        }


        if (updatedEmployee.getAddress() != null) {

            employee.setAddress(
                    updatedEmployee.getAddress()
            );
        }


        /*
         * These fields cannot be changed by the user:
         *
         * Email
         * Salary
         * Designation
         * Department
         * Status
         * Employee ID
         *
         * They are controlled by ADMIN / HR.
         */


        user.setEmployee(employee);

        userRepository.save(user);


        return getMyProfile(authentication);
    }


    // ==========================================
    // COMPLETE FIRST LOGIN
    // ==========================================

    public Map<String, Object> completeFirstLogin(
            Authentication authentication) {

        User user = getUser(authentication);

        user.setFirstLogin(false);

        userRepository.save(user);

        return getMyProfile(authentication);
    }


    // ==========================================
    // GET LOGGED-IN USER
    // ==========================================

    private User getUser(
            Authentication authentication) {

        if (authentication == null) {

            throw new AccessDeniedException(
                    "Authentication required"
            );
        }


        String email =
                authentication.getName();


        User user =
                userRepository.findByEmail(email);


        if (user == null) {

            throw new AccessDeniedException(
                    "User not found"
            );
        }


        return user;
    }
}