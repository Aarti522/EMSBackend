package com.example.ems.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.ems.dto.LoginRequest;
import com.ems.dto.LoginResponse;
import com.ems.dto.RegisterRequest;

import com.example.ems.model.Department;
import com.example.ems.model.Employee;
import com.example.ems.model.User;

import com.example.ems.repository.DepartmentRepository;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.UserRepository;

import com.example.ems.security.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;


    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        User user =
                userRepository.findByEmail(request.getEmail());

        if (user == null) {
            return new LoginResponse(
                    null,
                    null,
                    "User Not Found"
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            return new LoginResponse(
                    null,
                    null,
                    "Invalid Password"
            );
        }

        String token =
                jwtUtil.generateToken(user.getEmail());

        return new LoginResponse(
                token,
                user.getRole(),
                "Login Successful"
        );
    }


    // =========================================================
    // REGISTER
    // =========================================================

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request) {

        // -----------------------------------------------------
        // 1. CHECK USER EMAIL
        // -----------------------------------------------------

        if (userRepository.existsByEmail(request.getEmail())) {

            return ResponseEntity
                    .badRequest()
                    .body("Email already exists");
        }


        // -----------------------------------------------------
        // 2. CHECK EMPLOYEE EMAIL
        // -----------------------------------------------------

        if (employeeRepository.existsByEmail(request.getEmail())) {

            return ResponseEntity
                    .badRequest()
                    .body("Email already exists");
        }


        // -----------------------------------------------------
        // 3. FIND DEPARTMENT
        // -----------------------------------------------------

        Department department =
                departmentRepository
                        .findByName(request.getDepartment())
                        .orElse(null);

        if (department == null) {

            return ResponseEntity
                    .badRequest()
                    .body("Department not found");
        }


        // -----------------------------------------------------
        // 4. CREATE EMPLOYEE
        // -----------------------------------------------------

        Employee employee = new Employee();

        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());

        // SAVE ADDRESS
        employee.setAddress(request.getAddress());

        // SAVE DEPARTMENT RELATION
        employee.setDepartment(department);

        employee.setStatus("ACTIVE");

        Employee savedEmployee =
                employeeRepository.save(employee);


        // -----------------------------------------------------
        // 5. CREATE USER
        // -----------------------------------------------------

        User user = new User();

        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        // Public registration creates Employee only
        user.setRole("EMPLOYEE");

        // Connect user with employee
        user.setEmployee(savedEmployee);

        user.setFirstLogin(true);


        // -----------------------------------------------------
        // 6. SAVE USER
        // -----------------------------------------------------

        userRepository.save(user);


        // -----------------------------------------------------
        // 7. RESPONSE
        // -----------------------------------------------------

        return ResponseEntity.ok(
                "Registration Successful"
        );
    }
}