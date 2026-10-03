package com.example.ems.service;

import com.example.ems.model.Department;
import com.example.ems.model.Employee;
import com.example.ems.model.User;
import com.example.ems.repository.DepartmentRepository;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;


    // =========================================================
    // ADD EMPLOYEE
    // ADMIN / HR will be allowed from Controller
    // =========================================================
    public Employee addEmployee(Employee employee) {

        if (employee.getDepartment() != null &&
                employee.getDepartment().getId() != null) {

            Long departmentId = employee.getDepartment().getId();

            Department department = departmentRepository
                    .findById(departmentId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Department not found with ID: " + departmentId));

            employee.setDepartment(department);
        }

        return employeeRepository.save(employee);
    }


    // =========================================================
    // GET EMPLOYEES ACCORDING TO LOGGED-IN USER
    //
    // ADMIN / HR  -> ALL
    // MANAGER     -> OWN DEPARTMENT
    // EMPLOYEE    -> OWN RECORD
    // =========================================================
    public List<Employee> getEmployeesForUser(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("Logged-in user not found");
        }

        String role = user.getRole();


        // ADMIN
        if ("ADMIN".equalsIgnoreCase(role)) {
            return employeeRepository.findAll();
        }


        // HR
        if ("HR".equalsIgnoreCase(role)) {
            return employeeRepository.findAll();
        }


        // MANAGER
        if ("MANAGER".equalsIgnoreCase(role)) {

            Employee manager = user.getEmployee();

            if (manager == null) {
                throw new RuntimeException(
                        "Manager is not linked with an employee record");
            }

            if (manager.getDepartment() == null ||
                    manager.getDepartment().getId() == null) {

                throw new RuntimeException(
                        "Manager department is not assigned");
            }

            Long departmentId = manager.getDepartment().getId();

            return employeeRepository.findByDepartmentId(departmentId);
        }


        // EMPLOYEE
        if ("EMPLOYEE".equalsIgnoreCase(role)) {

            Employee employee = user.getEmployee();

            if (employee == null) {
                throw new RuntimeException(
                        "Employee is not linked with a user account");
            }

            return List.of(employee);
        }


        throw new RuntimeException("Invalid user role: " + role);
    }


    // =========================================================
    // GET EMPLOYEE BY ID WITH ROLE VALIDATION
    // =========================================================
    public Employee getEmployeeForUser(
            Long id,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("Logged-in user not found");
        }

        String role = user.getRole();

        Employee targetEmployee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Employee not found with ID: " + id));


        // ADMIN
        if ("ADMIN".equalsIgnoreCase(role)) {
            return targetEmployee;
        }


        // HR
        if ("HR".equalsIgnoreCase(role)) {
            return targetEmployee;
        }


        // MANAGER
        if ("MANAGER".equalsIgnoreCase(role)) {

            Employee manager = user.getEmployee();

            if (manager == null ||
                    manager.getDepartment() == null) {

                throw new RuntimeException(
                        "Manager department is not assigned");
            }

            if (targetEmployee.getDepartment() == null) {

                throw new RuntimeException(
                        "Employee department is not assigned");
            }

            Long managerDepartmentId =
                    manager.getDepartment().getId();

            Long employeeDepartmentId =
                    targetEmployee.getDepartment().getId();

            if (!managerDepartmentId.equals(employeeDepartmentId)) {

                throw new RuntimeException(
                        "Access denied: Employee is outside your team");
            }

            return targetEmployee;
        }


        // EMPLOYEE
        if ("EMPLOYEE".equalsIgnoreCase(role)) {

            Employee loggedInEmployee = user.getEmployee();

            if (loggedInEmployee == null) {
                throw new RuntimeException(
                        "Employee is not linked with user account");
            }

            if (!loggedInEmployee.getId().equals(id)) {

                throw new RuntimeException(
                        "Access denied: You can access only your own data");
            }

            return targetEmployee;
        }


        throw new RuntimeException("Invalid user role: " + role);
    }


    // =========================================================
    // OLD METHOD
    // Used internally if required
    // =========================================================
    public Employee getEmployeeById(Long id) {

        return employeeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Employee not found with ID: " + id));
    }


    // =========================================================
    // UPDATE EMPLOYEE
    // ADMIN / HR ONLY
    // Controller will enforce role
    // =========================================================
    public Employee updateEmployee(
            Long id,
            Employee employee) {

        Employee existingEmployee =
                employeeRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee not found with ID: " + id));

        existingEmployee.setName(employee.getName());
        existingEmployee.setEmail(employee.getEmail());
        existingEmployee.setSalary(employee.getSalary());
        existingEmployee.setPhone(employee.getPhone());
        existingEmployee.setAddress(employee.getAddress());
        existingEmployee.setDesignation(employee.getDesignation());
        existingEmployee.setStatus(employee.getStatus());


        if (employee.getDepartment() != null &&
                employee.getDepartment().getId() != null) {

            Long departmentId =
                    employee.getDepartment().getId();

            Department department =
                    departmentRepository.findById(departmentId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Department not found with ID: "
                                                    + departmentId));

            existingEmployee.setDepartment(department);
        }

        return employeeRepository.save(existingEmployee);
    }


    // =========================================================
    // DELETE / DEACTIVATE EMPLOYEE
    // ADMIN / HR ONLY
    // =========================================================
    public String deleteEmployee(Long id) {

        Employee employee =
                employeeRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee not found with ID: " + id));

        employee.setStatus("INACTIVE");

        employeeRepository.save(employee);

        return "Employee deactivated successfully";
    }


    // =========================================================
    // GET EMPLOYEES BY DEPARTMENT
    // This method should NOT be publicly exposed
    // without role validation.
    // =========================================================
    public List<Employee> getEmployeesByDepartment(
            Long departmentId) {

        return employeeRepository.findByDepartmentId(departmentId);
    }
}