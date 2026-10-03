package com.example.ems.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.ems.model.Department;
import com.example.ems.model.User;
import com.example.ems.repository.DepartmentRepository;
import com.example.ems.repository.UserRepository;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;


    // =========================================================
    // ADD DEPARTMENT
    // ADMIN ONLY
    // =========================================================

    public Department addDepartment(Department department) {

        return departmentRepository.save(department);
    }


    // =========================================================
    // GET DEPARTMENTS ACCORDING TO ROLE
    //
    // ADMIN -> All
    // HR -> All
    // MANAGER -> Own department
    // EMPLOYEE -> Own department
    // =========================================================

    public List<Department> getDepartmentsForUser(
            Authentication authentication) {

        User user = getUser(authentication);

        String role =
                user.getRole().toUpperCase();


        // ADMIN
        if (role.equals("ADMIN")) {
            return departmentRepository.findAll();
        }


        // HR
        if (role.equals("HR")) {
            return departmentRepository.findAll();
        }


        // MANAGER / EMPLOYEE
        if (role.equals("MANAGER") ||
                role.equals("EMPLOYEE")) {

            if (user.getEmployee() == null ||
                    user.getEmployee().getDepartment() == null) {

                throw new AccessDeniedException(
                        "Department not assigned"
                );
            }

            return List.of(
                    user.getEmployee().getDepartment()
            );
        }


        throw new AccessDeniedException(
                "Invalid role"
        );
    }


    // =========================================================
    // GET DEPARTMENT BY ID
    // ROLE BASED
    // =========================================================

    public Optional<Department> getDepartmentById(
            Long id,
            Authentication authentication) {

        Optional<Department> optionalDepartment =
                departmentRepository.findById(id);

        if (optionalDepartment.isEmpty()) {
            return Optional.empty();
        }

        Department department =
                optionalDepartment.get();

        User user = getUser(authentication);

        String role =
                user.getRole().toUpperCase();


        // ADMIN + HR
        if (role.equals("ADMIN") ||
                role.equals("HR")) {

            return optionalDepartment;
        }


        // MANAGER + EMPLOYEE
        if (role.equals("MANAGER") ||
                role.equals("EMPLOYEE")) {

            if (user.getEmployee() == null ||
                    user.getEmployee().getDepartment() == null) {

                throw new AccessDeniedException(
                        "Department not assigned"
                );
            }

            Long userDepartmentId =
                    user.getEmployee()
                            .getDepartment()
                            .getId();

            if (userDepartmentId.equals(
                    department.getId())) {

                return optionalDepartment;
            }

            throw new AccessDeniedException(
                    "You are not allowed to access this department"
            );
        }


        throw new AccessDeniedException(
                "Invalid role"
        );
    }


    // =========================================================
    // UPDATE DEPARTMENT
    // ADMIN ONLY
    // =========================================================

    public Department updateDepartment(
            Long id,
            Department department) {

        Optional<Department> existingDepartment =
                departmentRepository.findById(id);

        if (existingDepartment.isPresent()) {

            Department existing =
                    existingDepartment.get();

            existing.setName(
                    department.getName()
            );

            existing.setDescription(
                    department.getDescription()
            );

            return departmentRepository.save(existing);
        }

        return null;
    }


    // =========================================================
    // DELETE DEPARTMENT
    // ADMIN ONLY
    // =========================================================

    public boolean deleteDepartment(Long id) {

        if (departmentRepository.existsById(id)) {

            departmentRepository.deleteById(id);

            return true;
        }

        return false;
    }


    // =========================================================
    // GET USER FROM JWT
    // =========================================================

    private User getUser(
            Authentication authentication) {

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