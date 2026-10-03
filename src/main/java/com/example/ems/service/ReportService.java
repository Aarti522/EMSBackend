package com.example.ems.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.ems.model.Attendance;
import com.example.ems.model.Department;
import com.example.ems.model.Employee;
import com.example.ems.model.LeaveRequest;
import com.example.ems.model.Salary;
import com.example.ems.model.User;

import com.example.ems.repository.AttendanceRepository;
import com.example.ems.repository.DepartmentRepository;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.LeaveRepository;
import com.example.ems.repository.SalaryRepository;
import com.example.ems.repository.UserRepository;

@Service
public class ReportService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private SalaryRepository salaryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;


    // ==========================================
    // 1. EMPLOYEE REPORT
    // ==========================================

    public List<Employee> getEmployeeReport(
            Authentication authentication) {

        User user = getUser(authentication);

        String role = user.getRole().toUpperCase();


        // ADMIN / HR → ALL EMPLOYEES

        if (role.equals("ADMIN") ||
                role.equals("HR")) {

            return employeeRepository.findAll();
        }


        // MANAGER → OWN TEAM

        if (role.equals("MANAGER")) {

            if (user.getEmployee() == null ||
                    user.getEmployee().getDepartment() == null) {

                throw new AccessDeniedException(
                        "Manager department not assigned"
                );
            }

            Long departmentId =
                    user.getEmployee()
                            .getDepartment()
                            .getId();

            return employeeRepository
                    .findByDepartmentId(departmentId);
        }


        // EMPLOYEE → OWN PROFILE

        if (role.equals("EMPLOYEE")) {

            if (user.getEmployee() == null) {

                throw new AccessDeniedException(
                        "Employee profile not linked"
                );
            }

            return List.of(
                    user.getEmployee()
            );
        }


        throw new AccessDeniedException(
                "Invalid role"
        );
    }


    // ==========================================
    // 2. ATTENDANCE REPORT
    // ==========================================

    public List<Attendance> getAttendanceReport(
            Authentication authentication) {

        User user = getUser(authentication);

        String role = user.getRole().toUpperCase();


        // ADMIN / HR → ALL ATTENDANCE

        if (role.equals("ADMIN") ||
                role.equals("HR")) {

            return attendanceRepository.findAll();
        }


        // EMPLOYEE → OWN ATTENDANCE

        if (role.equals("EMPLOYEE")) {

            if (user.getEmployee() == null) {

                throw new AccessDeniedException(
                        "Employee profile not linked"
                );
            }

            return attendanceRepository
                    .findByEmployeeId(
                            user.getEmployee().getId()
                    );
        }


        // MANAGER → TEAM ATTENDANCE

        if (role.equals("MANAGER")) {

            List<Employee> team =
                    getManagerTeam(user);

            List<Attendance> result =
                    new ArrayList<>();

            for (Employee employee : team) {

                result.addAll(
                        attendanceRepository
                                .findByEmployeeId(
                                        employee.getId()
                                )
                );
            }

            return result;
        }


        throw new AccessDeniedException(
                "Invalid role"
        );
    }


    // ==========================================
    // 3. LEAVE REPORT
    // ==========================================

    public List<LeaveRequest> getLeaveReport(
            Authentication authentication) {

        User user = getUser(authentication);

        String role = user.getRole().toUpperCase();


        // ADMIN / HR → ALL LEAVES

        if (role.equals("ADMIN") ||
                role.equals("HR")) {

            return leaveRepository.findAll();
        }


        // EMPLOYEE → OWN LEAVES

        if (role.equals("EMPLOYEE")) {

            if (user.getEmployee() == null) {

                throw new AccessDeniedException(
                        "Employee profile not linked"
                );
            }

            return leaveRepository
                    .findByEmployeeId(
                            user.getEmployee().getId()
                    );
        }


        // MANAGER → TEAM LEAVES

        if (role.equals("MANAGER")) {

            List<Employee> team =
                    getManagerTeam(user);

            List<LeaveRequest> result =
                    new ArrayList<>();

            for (Employee employee : team) {

                result.addAll(
                        leaveRepository
                                .findByEmployeeId(
                                        employee.getId()
                                )
                );
            }

            return result;
        }


        throw new AccessDeniedException(
                "Invalid role"
        );
    }


    // ==========================================
    // 4. SALARY / PAYROLL REPORT
    // ==========================================

    public List<Salary> getSalaryReport(
            Authentication authentication) {

        User user = getUser(authentication);

        String role = user.getRole().toUpperCase();


        // ADMIN / HR → ALL SALARIES

        if (role.equals("ADMIN") ||
                role.equals("HR")) {

            return salaryRepository.findAll();
        }


        // EMPLOYEE → OWN SALARY

        if (role.equals("EMPLOYEE")) {

            if (user.getEmployee() == null) {

                throw new AccessDeniedException(
                        "Employee profile not linked"
                );
            }

            return salaryRepository
                    .findByEmployeeId(
                            user.getEmployee().getId()
                    );
        }


        // MANAGER → TEAM SALARIES

        if (role.equals("MANAGER")) {

            List<Employee> team =
                    getManagerTeam(user);

            List<Salary> result =
                    new ArrayList<>();

            for (Employee employee : team) {

                result.addAll(
                        salaryRepository
                                .findByEmployeeId(
                                        employee.getId()
                                )
                );
            }

            return result;
        }


        throw new AccessDeniedException(
                "Invalid role"
        );
    }


    // ==========================================
    // 5. DEPARTMENT REPORT
    // ==========================================

    public List<Department> getDepartmentReport(
            Authentication authentication) {

        User user = getUser(authentication);

        String role = user.getRole().toUpperCase();


        // ADMIN / HR → ALL DEPARTMENTS

        if (role.equals("ADMIN") ||
                role.equals("HR")) {

            return departmentRepository.findAll();
        }


        // MANAGER / EMPLOYEE
        // → ONLY OWN DEPARTMENT

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


    // ==========================================
    // 6. ANALYTICS
    // ==========================================

    public Map<String, Object> getAnalytics(
            Authentication authentication) {

        User user = getUser(authentication);

        String role = user.getRole().toUpperCase();

        Map<String, Object> analytics =
                new HashMap<>();


        // ==========================================
        // ADMIN / HR ANALYTICS
        // ==========================================

        if (role.equals("ADMIN") ||
                role.equals("HR")) {

            analytics.put(
                    "role",
                    role
            );

            analytics.put(
                    "totalEmployees",
                    employeeRepository.count()
            );

            analytics.put(
                    "totalDepartments",
                    departmentRepository.count()
            );

            analytics.put(
                    "totalAttendanceRecords",
                    attendanceRepository.count()
            );

            analytics.put(
                    "totalLeaveRecords",
                    leaveRepository.count()
            );

            analytics.put(
                    "totalSalaryRecords",
                    salaryRepository.count()
            );

            return analytics;
        }


        // ==========================================
        // MANAGER ANALYTICS
        // ==========================================

        if (role.equals("MANAGER")) {

            List<Employee> team =
                    getManagerTeam(user);

            long attendanceRecords = 0;
            long leaveRecords = 0;
            long salaryRecords = 0;

            for (Employee employee : team) {

                attendanceRecords +=
                        attendanceRepository
                                .findByEmployeeId(
                                        employee.getId()
                                )
                                .size();

                leaveRecords +=
                        leaveRepository
                                .findByEmployeeId(
                                        employee.getId()
                                )
                                .size();

                salaryRecords +=
                        salaryRepository
                                .findByEmployeeId(
                                        employee.getId()
                                )
                                .size();
            }


            analytics.put(
                    "role",
                    "MANAGER"
            );

            analytics.put(
                    "totalEmployees",
                    team.size()
            );

            analytics.put(
                    "totalDepartments",
                    1
            );

            analytics.put(
                    "totalAttendanceRecords",
                    attendanceRecords
            );

            analytics.put(
                    "totalLeaveRecords",
                    leaveRecords
            );

            analytics.put(
                    "totalSalaryRecords",
                    salaryRecords
            );

            analytics.put(
                    "department",
                    user.getEmployee()
                            .getDepartment()
                            .getName()
            );

            return analytics;
        }


        // ==========================================
        // EMPLOYEE ANALYTICS
        // ==========================================

        if (role.equals("EMPLOYEE")) {

            if (user.getEmployee() == null) {

                throw new AccessDeniedException(
                        "Employee profile not linked"
                );
            }

            Long employeeId =
                    user.getEmployee().getId();


            analytics.put(
                    "role",
                    "EMPLOYEE"
            );

            analytics.put(
                    "totalEmployees",
                    1
            );

            analytics.put(
                    "totalDepartments",
                    1
            );

            analytics.put(
                    "totalAttendanceRecords",
                    attendanceRepository
                            .findByEmployeeId(
                                    employeeId
                            )
                            .size()
            );

            analytics.put(
                    "totalLeaveRecords",
                    leaveRepository
                            .findByEmployeeId(
                                    employeeId
                            )
                            .size()
            );

            analytics.put(
                    "totalSalaryRecords",
                    salaryRepository
                            .findByEmployeeId(
                                    employeeId
                            )
                            .size()
            );

            return analytics;
        }


        throw new AccessDeniedException(
                "Invalid role"
        );
    }


    // ==========================================
    // MANAGER TEAM
    // ==========================================

    private List<Employee> getManagerTeam(
            User manager) {

        if (manager.getEmployee() == null ||
                manager.getEmployee().getDepartment() == null) {

            throw new AccessDeniedException(
                    "Manager department not assigned"
            );
        }

        Long departmentId =
                manager.getEmployee()
                        .getDepartment()
                        .getId();

        return employeeRepository
                .findByDepartmentId(
                        departmentId
                );
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