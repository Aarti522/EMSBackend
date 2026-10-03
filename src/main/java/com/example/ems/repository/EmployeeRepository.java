package com.example.ems.repository;

import com.example.ems.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Check whether an employee already exists with this email
    boolean existsByEmail(String email);

    // Find logged-in employee using JWT email
    Employee findByEmail(String email);

    // Find all employees belonging to a department
    List<Employee> findByDepartmentId(Long departmentId);
    
    List<Employee> findByDepartmentIdAndStatusIgnoreCase(
            Long departmentId,
            String status
    );
}