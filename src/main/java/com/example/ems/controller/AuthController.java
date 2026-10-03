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
@CrossOrigin(origins = "*")
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

        // Find user using email
        User user =
                userRepository.findByEmail(request.getEmail());

        // User not found
        if (user == null) {
            return new LoginResponse(
                    null,
                    null,
                    "User Not Found"
            );
        }

        // Check password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            return new LoginResponse(
                    null,
                    null,
                    "Invalid Password"
            );
        }

        // Generate JWT
        String token =
                jwtUtil.generateToken(user.getEmail());

        // Return token + role
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
        // 1. Validate User email
        // -----------------------------------------------------

        if (userRepository.existsByEmail(
                request.getEmail())) {

            return ResponseEntity
                    .badRequest()
                    .body("Email already exists");
        }


        // -----------------------------------------------------
        // 2. Validate Employee email
        // -----------------------------------------------------

        if (employeeRepository.existsByEmail(
                request.getEmail())) {

            return ResponseEntity
                    .badRequest()
                    .body("Email already exists");
        }


        // -----------------------------------------------------
        // 3. Find Department
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
        // 4. Create Employee
        // -----------------------------------------------------

        Employee employee = new Employee();

        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setDepartment(department);

        // New employee is active
        employee.setStatus("ACTIVE");

        Employee savedEmployee =
                employeeRepository.save(employee);


        // -----------------------------------------------------
        // 5. Create User
        // -----------------------------------------------------

        User user = new User();

        user.setEmail(request.getEmail());

        // Password must always be encrypted
        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );


        // -----------------------------------------------------
        // IMPORTANT
        // Public registration can create ONLY EMPLOYEE
        // -----------------------------------------------------

        user.setRole("EMPLOYEE");


        // Connect User with Employee
        user.setEmployee(savedEmployee);


        // Mark as first login
        user.setFirstLogin(true);


        // -----------------------------------------------------
        // 6. Save User
        // -----------------------------------------------------

        userRepository.save(user);


        // -----------------------------------------------------
        // 7. Registration response
        // -----------------------------------------------------

        return ResponseEntity.ok(
                "Registration Successful"
        );
    }
}
