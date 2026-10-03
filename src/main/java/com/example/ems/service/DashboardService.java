package com.example.ems.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.ems.model.Attendance;
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
public class DashboardService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private SalaryRepository salaryRepository;

    @Autowired
    private UserRepository userRepository;


    // ==========================================
    // MAIN ROLE-BASED DASHBOARD
    // ==========================================

    public Map<String, Object> getDashboard(
            Authentication authentication) {

        User user = getUser(authentication);

        String role = user.getRole().toUpperCase();

        if (role.equals("ADMIN")) {
            return getAdminDashboard();
        }

        if (role.equals("HR")) {
            return getHRDashboard();
        }

        if (role.equals("MANAGER")) {
            return getManagerDashboard(user);
        }

        if (role.equals("EMPLOYEE")) {

            if (user.getEmployee() == null) {
                throw new AccessDeniedException(
                        "Employee profile not linked"
                );
            }

            return getEmployeeDashboard(
                    user.getEmployee().getId()
            );
        }

        throw new AccessDeniedException(
                "Invalid role"
        );
    }


    // ==========================================
    // ADMIN DASHBOARD
    // ==========================================

    private Map<String, Object> getAdminDashboard() {

        Map<String, Object> dashboard =
                new HashMap<>();

        // Total Employees
        long totalEmployees =
                employeeRepository.count();

        // Total Departments
        long totalDepartments =
                departmentRepository.count();

        // Today's Date
        LocalDate today =
                LocalDate.now();

        // Today's Attendance
        List<Attendance> todayAttendance =
                attendanceRepository.findByDate(today);

        long presentToday =
                todayAttendance.stream()
                        .filter(a ->
                                "PRESENT".equalsIgnoreCase(
                                        a.getStatus()))
                        .count();

        long absentToday =
                todayAttendance.stream()
                        .filter(a ->
                                "ABSENT".equalsIgnoreCase(
                                        a.getStatus()))
                        .count();


        // Employees On Leave
        List<LeaveRequest> leaveRequests =
                leaveRepository.findAll();

        long onLeave =
                leaveRequests.stream()
                        .filter(l ->
                                "APPROVED".equalsIgnoreCase(
                                        l.getStatus()))
                        .count();


        // Total Payroll
        List<Salary> salaries =
                salaryRepository.findAll();

        double totalPayroll = 0;

        for (Salary salary : salaries) {
            totalPayroll += salary.getNetSalary();
        }


        // Dashboard Response

        dashboard.put(
                "role",
                "ADMIN"
        );

        dashboard.put(
                "totalEmployees",
                totalEmployees
        );

        dashboard.put(
                "totalDepartments",
                totalDepartments
        );

        dashboard.put(
                "presentToday",
                presentToday
        );

        dashboard.put(
                "absentToday",
                absentToday
        );

        dashboard.put(
                "onLeave",
                onLeave
        );

        dashboard.put(
                "totalPayroll",
                totalPayroll
        );

        return dashboard;
    }


    // ==========================================
    // HR DASHBOARD
    // ==========================================

    private Map<String, Object> getHRDashboard() {

        /*
         * HR has organization-wide HR access.
         *
         * Therefore HR receives the same
         * overall statistics as ADMIN.
         */

        Map<String, Object> dashboard =
                getAdminDashboard();

        dashboard.put(
                "role",
                "HR"
        );

        return dashboard;
    }


    // ==========================================
    // MANAGER DASHBOARD
    // ==========================================

    private Map<String, Object> getManagerDashboard(
            User manager) {

        if (manager.getEmployee() == null) {

            throw new AccessDeniedException(
                    "Manager employee profile not linked"
            );
        }

        if (manager.getEmployee().getDepartment() == null) {

            throw new AccessDeniedException(
                    "Manager department not assigned"
            );
        }


        // Manager's Department

        Long departmentId =
                manager.getEmployee()
                        .getDepartment()
                        .getId();


        // Manager's Team

        List<Employee> team =
                employeeRepository
                        .findByDepartmentId(
                                departmentId
                        );


        Map<String, Object> dashboard =
                new HashMap<>();


        LocalDate today =
                LocalDate.now();


        long presentToday = 0;

        long absentToday = 0;

        long onLeave = 0;

        double totalPayroll = 0;


        // ==========================================
        // TEAM ATTENDANCE
        // ==========================================

        for (Employee employee : team) {

            List<Attendance> attendanceList =
                    attendanceRepository
                            .findByEmployeeId(
                                    employee.getId()
                            );

            for (Attendance attendance :
                    attendanceList) {

                if (today.equals(
                        attendance.getDate())) {

                    if ("PRESENT".equalsIgnoreCase(
                            attendance.getStatus())) {

                        presentToday++;

                    } else if ("ABSENT".equalsIgnoreCase(
                            attendance.getStatus())) {

                        absentToday++;
                    }
                }
            }
        }


        // ==========================================
        // TEAM LEAVE
        // ==========================================

        for (Employee employee : team) {

            List<LeaveRequest> leaves =
                    leaveRepository
                            .findByEmployeeId(
                                    employee.getId()
                            );

            for (LeaveRequest leave : leaves) {

                if ("APPROVED".equalsIgnoreCase(
                        leave.getStatus())) {

                    onLeave++;
                }
            }
        }


        // ==========================================
        // TEAM PAYROLL
        // ==========================================

        for (Employee employee : team) {

            List<Salary> salaries =
                    salaryRepository
                            .findByEmployeeId(
                                    employee.getId()
                            );

            for (Salary salary : salaries) {

                totalPayroll +=
                        salary.getNetSalary();
            }
        }


        // ==========================================
        // MANAGER DASHBOARD RESPONSE
        // ==========================================

        dashboard.put(
                "role",
                "MANAGER"
        );

        dashboard.put(
                "totalEmployees",
                team.size()
        );

        dashboard.put(
                "totalDepartments",
                1
        );

        dashboard.put(
                "presentToday",
                presentToday
        );

        dashboard.put(
                "absentToday",
                absentToday
        );

        dashboard.put(
                "onLeave",
                onLeave
        );

        dashboard.put(
                "totalPayroll",
                totalPayroll
        );

        dashboard.put(
                "department",
                manager.getEmployee()
                        .getDepartment()
                        .getName()
        );

        return dashboard;
    }


    // ==========================================
    // EMPLOYEE DASHBOARD
    // ==========================================

    private Map<String, Object> getEmployeeDashboard(
            Long employeeId) {

        Map<String, Object> dashboard =
                new HashMap<>();


        // Find Employee

        Employee employee =
                employeeRepository
                        .findById(employeeId)
                        .orElse(null);


        if (employee == null) {

            dashboard.put(
                    "message",
                    "Employee Not Found"
            );

            return dashboard;
        }


        // ==========================================
        // EMPLOYEE BASIC DETAILS
        // ==========================================

        dashboard.put(
                "role",
                "EMPLOYEE"
        );

        dashboard.put(
                "employeeId",
                employee.getId()
        );

        dashboard.put(
                "employeeName",
                employee.getName()
        );

        dashboard.put(
                "email",
                employee.getEmail()
        );

        dashboard.put(
                "department",
                employee.getDepartment()
        );

        dashboard.put(
                "designation",
                employee.getDesignation()
        );


        // ==========================================
        // ATTENDANCE
        // ==========================================

        LocalDate today =
                LocalDate.now();


        List<Attendance> attendanceList =
                attendanceRepository
                        .findByEmployeeId(
                                employeeId
                        );


        String todayAttendanceStatus =
                "NOT MARKED";


        long totalPresent = 0;

        long totalAbsent = 0;


        for (Attendance attendance :
                attendanceList) {


            if ("PRESENT".equalsIgnoreCase(
                    attendance.getStatus())) {

                totalPresent++;

            } else if ("ABSENT".equalsIgnoreCase(
                    attendance.getStatus())) {

                totalAbsent++;
            }


            if (today.equals(
                    attendance.getDate())) {

                if ("PRESENT".equalsIgnoreCase(
                        attendance.getStatus())) {

                    todayAttendanceStatus =
                            "PRESENT";

                } else if ("ABSENT".equalsIgnoreCase(
                        attendance.getStatus())) {

                    todayAttendanceStatus =
                            "ABSENT";
                }
            }
        }


        // ==========================================
        // LEAVE
        // ==========================================

        List<LeaveRequest> leaveRequests =
                leaveRepository
                        .findByEmployeeId(
                                employeeId
                        );


        String leaveStatus =
                "NO LEAVE";


        if (!leaveRequests.isEmpty()) {

            LeaveRequest latestLeave =
                    leaveRequests.get(
                            leaveRequests.size() - 1
                    );

            leaveStatus =
                    latestLeave.getStatus();
        }


        // ==========================================
        // SALARY
        // ==========================================

        double salaryAmount = 0;


        List<Salary> salaries =
                salaryRepository
                        .findByEmployeeId(
                                employeeId
                        );


        if (!salaries.isEmpty()) {

            Salary latestSalary =
                    salaries.get(
                            salaries.size() - 1
                    );

            salaryAmount =
                    latestSalary.getNetSalary();
        }


        // ==========================================
        // FINAL EMPLOYEE DASHBOARD
        // ==========================================

        dashboard.put(
                "todayAttendance",
                todayAttendanceStatus
        );

        dashboard.put(
                "totalPresent",
                totalPresent
        );

        dashboard.put(
                "totalAbsent",
                totalAbsent
        );

        dashboard.put(
                "leaveStatus",
                leaveStatus
        );

        dashboard.put(
                "salary",
                salaryAmount
        );


        return dashboard;
    }


    // ==========================================
    // GET LOGGED-IN USER
    // ==========================================

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