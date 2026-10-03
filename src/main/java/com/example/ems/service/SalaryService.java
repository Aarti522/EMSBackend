package com.example.ems.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.ems.model.Employee;
import com.example.ems.model.Salary;
import com.example.ems.model.User;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.SalaryRepository;
import com.example.ems.repository.UserRepository;

@Service
public class SalaryService {

    @Autowired
    private SalaryRepository salaryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;


    // =========================================================
    // ADD SALARY
    // ADMIN + HR
    //
    // ADMIN -> Can add salary for anyone
    // HR    -> Can add salary for anyone except own salary
    // =========================================================

    public Salary addSalary(
            Salary salary,
            Authentication authentication) {

        User user = getUser(authentication);

        String role = user.getRole().toUpperCase();

        if (!role.equals("ADMIN") && !role.equals("HR")) {
            throw new AccessDeniedException(
                    "You are not allowed to add salary"
            );
        }

        // HR cannot add own salary
        if (role.equals("HR")
                && user.getEmployee() != null
                && user.getEmployee().getId()
                        .equals(salary.getEmployeeId())) {

            throw new AccessDeniedException(
                    "HR cannot manage their own salary"
            );
        }

        double netSalary =
                salary.getBasicSalary()
                + salary.getAllowance()
                - salary.getDeduction();

        salary.setNetSalary(netSalary);

        return salaryRepository.save(salary);
    }


    // =========================================================
    // GET SALARIES
    //
    // ADMIN   -> All
    // HR      -> All
    // MANAGER -> Team
    // EMPLOYEE-> Own
    // =========================================================

    public List<Salary> getSalariesForUser(
            Authentication authentication) {

        User user = getUser(authentication);

        String role =
                user.getRole().toUpperCase();


        // ADMIN
        if (role.equals("ADMIN")) {

            return salaryRepository.findAll();
        }


        // HR
        if (role.equals("HR")) {

            return salaryRepository.findAll();
        }


        // EMPLOYEE
        if (role.equals("EMPLOYEE")) {

            if (user.getEmployee() == null) {

                throw new AccessDeniedException(
                        "Employee profile not linked"
                );
            }

            Long employeeId =
                    user.getEmployee().getId();

            return salaryRepository
                    .findByEmployeeId(employeeId);
        }


        // MANAGER
        if (role.equals("MANAGER")) {

            if (user.getEmployee() == null
                    || user.getEmployee().getDepartment() == null) {

                throw new AccessDeniedException(
                        "Manager department not assigned"
                );
            }

            Long departmentId =
                    user.getEmployee()
                            .getDepartment()
                            .getId();

            List<Employee> team =
                    employeeRepository
                            .findByDepartmentId(departmentId);

            List<Salary> teamSalaries =
                    new ArrayList<>();

            for (Employee employee : team) {

                teamSalaries.addAll(
                        salaryRepository
                                .findByEmployeeId(
                                        employee.getId()
                                )
                );
            }

            return teamSalaries;
        }


        throw new AccessDeniedException(
                "Invalid role"
        );
    }


    // =========================================================
    // GET SALARY BY ID
    // =========================================================

    public Optional<Salary> getSalaryById(
            Long id,
            Authentication authentication) {

        Optional<Salary> optionalSalary =
                salaryRepository.findById(id);

        if (optionalSalary.isEmpty()) {
            return Optional.empty();
        }

        Salary salary =
                optionalSalary.get();

        if (hasAccessToEmployee(
                salary.getEmployeeId(),
                authentication)) {

            return optionalSalary;
        }

        throw new AccessDeniedException(
                "You are not allowed to access this salary"
        );
    }


    // =========================================================
    // GET SALARY BY EMPLOYEE ID
    // =========================================================

    public List<Salary> getSalaryByEmployeeId(
            Long employeeId,
            Authentication authentication) {

        if (!hasAccessToEmployee(
                employeeId,
                authentication)) {

            throw new AccessDeniedException(
                    "You are not allowed to access this employee salary"
            );
        }

        return salaryRepository
                .findByEmployeeId(employeeId);
    }


    // =========================================================
    // UPDATE SALARY
    //
    // ADMIN -> Can update anyone
    // HR    -> Can update anyone except own salary
    // =========================================================

    public Salary updateSalary(
            Long id,
            Salary salary,
            Authentication authentication) {

        Optional<Salary> existingSalary =
                salaryRepository.findById(id);

        if (existingSalary.isEmpty()) {
            return null;
        }

        Salary existing =
                existingSalary.get();

        User user = getUser(authentication);

        String role =
                user.getRole().toUpperCase();


        // Only ADMIN and HR can update
        if (!role.equals("ADMIN")
                && !role.equals("HR")) {

            throw new AccessDeniedException(
                    "You are not allowed to update salary"
            );
        }


        // HR cannot update own salary
        if (role.equals("HR")
                && user.getEmployee() != null) {

            Long hrEmployeeId =
                    user.getEmployee().getId();

            if (hrEmployeeId.equals(
                    existing.getEmployeeId())) {

                throw new AccessDeniedException(
                        "HR cannot manage their own salary"
                );
            }
        }


        existing.setEmployeeId(
                salary.getEmployeeId()
        );

        existing.setBasicSalary(
                salary.getBasicSalary()
        );

        existing.setAllowance(
                salary.getAllowance()
        );

        existing.setDeduction(
                salary.getDeduction()
        );

        existing.setMonth(
                salary.getMonth()
        );


        double netSalary =
                salary.getBasicSalary()
                + salary.getAllowance()
                - salary.getDeduction();

        existing.setNetSalary(netSalary);

        return salaryRepository.save(existing);
    }


    // =========================================================
    // DELETE SALARY
    //
    // ADMIN -> Can delete anyone
    // HR    -> Can delete anyone except own salary
    // =========================================================

    public boolean deleteSalary(
            Long id,
            Authentication authentication) {

        Optional<Salary> optionalSalary =
                salaryRepository.findById(id);

        if (optionalSalary.isEmpty()) {
            return false;
        }

        Salary salary =
                optionalSalary.get();

        User user =
                getUser(authentication);

        String role =
                user.getRole().toUpperCase();


        // Only ADMIN and HR can delete
        if (!role.equals("ADMIN")
                && !role.equals("HR")) {

            throw new AccessDeniedException(
                    "You are not allowed to delete salary"
            );
        }


        // HR cannot delete own salary
        if (role.equals("HR")
                && user.getEmployee() != null) {

            Long hrEmployeeId =
                    user.getEmployee().getId();

            if (hrEmployeeId.equals(
                    salary.getEmployeeId())) {

                throw new AccessDeniedException(
                        "HR cannot manage their own salary"
                );
            }
        }


        salaryRepository.deleteById(id);

        return true;
    }


    // =========================================================
    // CHECK WHETHER USER CAN ACCESS EMPLOYEE SALARY
    // =========================================================

    private boolean hasAccessToEmployee(
            Long employeeId,
            Authentication authentication) {

        User user =
                getUser(authentication);

        String role =
                user.getRole().toUpperCase();


        // ADMIN
        if (role.equals("ADMIN")) {
            return true;
        }


        // HR
        if (role.equals("HR")) {
            return true;
        }


        // EMPLOYEE
        if (role.equals("EMPLOYEE")) {

            if (user.getEmployee() == null) {
                return false;
            }

            return user.getEmployee()
                    .getId()
                    .equals(employeeId);
        }


        // MANAGER
        if (role.equals("MANAGER")) {

            if (user.getEmployee() == null
                    || user.getEmployee().getDepartment() == null) {

                return false;
            }

            Employee employee =
                    employeeRepository
                            .findById(employeeId)
                            .orElse(null);

            if (employee == null
                    || employee.getDepartment() == null) {

                return false;
            }

            Long managerDepartmentId =
                    user.getEmployee()
                            .getDepartment()
                            .getId();

            Long employeeDepartmentId =
                    employee.getDepartment()
                            .getId();

            return managerDepartmentId.equals(
                    employeeDepartmentId
            );
        }


        return false;
    }


    // =========================================================
    // GET USER FROM JWT AUTHENTICATION
    // =========================================================

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