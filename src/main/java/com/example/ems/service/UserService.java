package com.example.ems.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.ems.model.Department;
import com.example.ems.model.Employee;
import com.example.ems.model.User;
import com.example.ems.repository.DepartmentRepository;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    // =========================
    // GET ALL USERS
    // =========================

    public List<Map<String, Object>> getAllUsers() {

        List<User> users = userRepository.findAll();

        List<Map<String, Object>> result = new ArrayList<>();

        for (User user : users) {

            Map<String, Object> data = new HashMap<>();

            data.put("id", user.getId());
            data.put("email", user.getEmail());
            data.put("role", user.getRole());
            data.put("firstLogin", user.isFirstLogin());

            if (user.getEmployee() != null) {

                Employee employee = user.getEmployee();

                data.put("employeeId", employee.getId());
                data.put("name", employee.getName());
                data.put("phone", employee.getPhone());
                data.put("designation", employee.getDesignation());
                data.put("status", employee.getStatus());

                if (employee.getDepartment() != null) {

                    data.put(
                            "departmentId",
                            employee.getDepartment().getId()
                    );

                    data.put(
                            "department",
                            employee.getDepartment().getName()
                    );
                }
            }

            // IMPORTANT:
            // Password intentionally NOT returned.

            result.add(data);
        }

        return result;
    }


    // =========================
    // GET USER BY ID
    // =========================

    public Map<String, Object> getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        Map<String, Object> data = new HashMap<>();

        data.put("id", user.getId());
        data.put("email", user.getEmail());
        data.put("role", user.getRole());
        data.put("firstLogin", user.isFirstLogin());

        if (user.getEmployee() != null) {

            Employee employee = user.getEmployee();

            data.put("employeeId", employee.getId());
            data.put("name", employee.getName());
            data.put("phone", employee.getPhone());
            data.put("address", employee.getAddress());
            data.put("designation", employee.getDesignation());
            data.put("salary", employee.getSalary());
            data.put("status", employee.getStatus());

            if (employee.getDepartment() != null) {

                data.put(
                        "departmentId",
                        employee.getDepartment().getId()
                );

                data.put(
                        "department",
                        employee.getDepartment().getName()
                );
            }
        }

        return data;
    }


    // =========================
    // CREATE HR / MANAGER
    // =========================

    public Map<String, Object> createUser(
            String name,
            String email,
            String password,
            String role,
            String phone,
            String address,
            String designation,
            Double salary,
            Long departmentId) {

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException(
                    "User with this email already exists"
            );
        }

        if (employeeRepository.existsByEmail(email)) {
            throw new RuntimeException(
                    "Employee with this email already exists"
            );
        }

        role = role.toUpperCase();

        if (!role.equals("HR") &&
                !role.equals("MANAGER")) {

            throw new AccessDeniedException(
                    "Only HR and MANAGER users can be created here"
            );
        }

        Department department = null;

        if (departmentId != null) {

            department =
                    departmentRepository
                            .findById(departmentId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Department not found"
                                    )
                            );
        }

        // Manager MUST have a department
        if (role.equals("MANAGER") &&
                department == null) {

            throw new RuntimeException(
                    "Manager must be assigned to a department"
            );
        }

        Employee employee = new Employee();

        employee.setName(name);
        employee.setEmail(email);
        employee.setPhone(phone);
        employee.setAddress(address);
        employee.setDesignation(designation);
        employee.setSalary(
                salary != null ? salary : 0
        );
        employee.setDepartment(department);
        employee.setStatus("ACTIVE");

        Employee savedEmployee =
                employeeRepository.save(employee);


        User user = new User();

        user.setEmail(email);

        user.setPassword(
                passwordEncoder.encode(password)
        );

        user.setRole(role);

        user.setEmployee(savedEmployee);

        // HR / Manager will be considered
        // first-time users.
        user.setFirstLogin(true);

        User savedUser =
                userRepository.save(user);

        return getUserById(savedUser.getId());
    }


    // =========================
    // UPDATE USER ROLE
    // =========================

    public Map<String, Object> updateUser(
            Long id,
            String role,
            Long departmentId) {

        User user =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );

        role = role.toUpperCase();

        if (!role.equals("HR") &&
                !role.equals("MANAGER") &&
                !role.equals("EMPLOYEE")) {

            throw new RuntimeException(
                    "Invalid role"
            );
        }

        Department department = null;

        if (departmentId != null) {

            department =
                    departmentRepository
                            .findById(departmentId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Department not found"
                                    )
                            );
        }

        if (role.equals("MANAGER") &&
                department == null) {

            throw new RuntimeException(
                    "Manager must have a department"
            );
        }

        user.setRole(role);

        if (user.getEmployee() != null) {

            user.getEmployee()
                    .setDepartment(department);

            employeeRepository.save(
                    user.getEmployee()
            );
        }

        userRepository.save(user);

        return getUserById(id);
    }


    // =========================
    // DELETE USER
    // =========================

    public String deleteUser(Long id) {

        User user =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );

        /*
         * Delete User first.
         * Employee is then deleted separately.
         */

        Employee employee =
                user.getEmployee();

        userRepository.delete(user);

        if (employee != null) {
            employeeRepository.delete(employee);
        }

        return "User deleted successfully";
    }
}