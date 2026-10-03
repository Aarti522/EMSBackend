package com.example.ems.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ems.model.Attendance;
import com.example.ems.model.Department;
import com.example.ems.model.Employee;
import com.example.ems.model.LeaveRequest;
import com.example.ems.model.Salary;
import com.example.ems.service.ReportService;

@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = "http://localhost:5173")
public class ReportController {

    @Autowired
    private ReportService reportService;


    // =========================================================
    // 1. EMPLOYEE REPORT
    // =========================================================
    // ADMIN / HR  -> All employees
    // MANAGER     -> Own department/team
    // EMPLOYEE    -> Own profile
    @GetMapping("/employees")
    public ResponseEntity<List<Employee>> getEmployeeReport(
            Authentication authentication) {

        return ResponseEntity.ok(
                reportService.getEmployeeReport(authentication)
        );
    }


    // =========================================================
    // 2. ATTENDANCE REPORT
    // =========================================================
    // ADMIN / HR  -> All attendance
    // MANAGER     -> Team attendance
    // EMPLOYEE    -> Own attendance
    @GetMapping("/attendance")
    public ResponseEntity<List<Attendance>> getAttendanceReport(
            Authentication authentication) {

        return ResponseEntity.ok(
                reportService.getAttendanceReport(authentication)
        );
    }


    // =========================================================
    // 3. LEAVE REPORT
    // =========================================================
    // ADMIN / HR  -> All leaves
    // MANAGER     -> Team leaves
    // EMPLOYEE    -> Own leaves
    @GetMapping("/leaves")
    public ResponseEntity<List<LeaveRequest>> getLeaveReport(
            Authentication authentication) {

        return ResponseEntity.ok(
                reportService.getLeaveReport(authentication)
        );
    }


    // =========================================================
    // 4. SALARY / PAYROLL REPORT
    // =========================================================
    // ADMIN / HR  -> All salary records
    // MANAGER     -> Team salary records
    // EMPLOYEE    -> Own salary records
    @GetMapping("/salary")
    public ResponseEntity<List<Salary>> getSalaryReport(
            Authentication authentication) {

        return ResponseEntity.ok(
                reportService.getSalaryReport(authentication)
        );
    }


    // =========================================================
    // 5. DEPARTMENT REPORT
    // =========================================================
    // ADMIN / HR  -> All departments
    // MANAGER     -> Own department
    // EMPLOYEE    -> Own department
    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getDepartmentReport(
            Authentication authentication) {

        return ResponseEntity.ok(
                reportService.getDepartmentReport(authentication)
        );
    }


    // =========================================================
    // 6. LIVE REPORT ANALYTICS
    // =========================================================
    // This is the main endpoint for the Reports Dashboard.
    //
    // ADMIN / HR:
    //   - Total Employees
    //   - Present Today
    //   - Absent Today
    //   - Half Day Today
    //   - On Leave Today
    //   - Pending Leaves
    //   - Approved Leaves
    //   - Rejected Leaves
    //   - Salary Records
    //
    // MANAGER:
    //   - Own team/department statistics
    //
    // EMPLOYEE:
    //   - Own attendance, leave and salary statistics
    //
    // Data is fetched directly from the database,
    // so today's report changes automatically when
    // attendance/leave/payroll data changes.
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics(
            Authentication authentication) {

        return ResponseEntity.ok(
                reportService.getAnalytics(authentication)
        );
    }
}
