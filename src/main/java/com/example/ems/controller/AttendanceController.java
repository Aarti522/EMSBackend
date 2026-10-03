package com.example.ems.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.ems.model.Attendance;
import com.example.ems.service.AttendanceService;

@RestController
@RequestMapping("/attendance")
@CrossOrigin(origins = "http://localhost:5173")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;


    // =====================================================
    // ADD ATTENDANCE
    // ADMIN / HR
    // =====================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Attendance> addAttendance(
            @RequestBody Attendance attendance) {

        return ResponseEntity.ok(
                attendanceService.addAttendance(attendance)
        );
    }


    // =====================================================
    // GET ATTENDANCE
    // ROLE BASED
    // =====================================================

    @GetMapping
    public ResponseEntity<List<Attendance>> getAllAttendance(
            Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService.getAttendanceForUser(
                        authentication
                )
        );
    }


    // =====================================================
    // GET ATTENDANCE BY ID
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getAttendanceById(
            @PathVariable Long id,
            Authentication authentication) {

        Optional<Attendance> attendance =
                attendanceService.getAttendanceById(
                        id,
                        authentication
                );

        if (attendance.isPresent()) {
            return ResponseEntity.ok(
                    attendance.get()
            );
        }

        return ResponseEntity.notFound().build();
    }


    // =====================================================
    // GET ATTENDANCE BY EMPLOYEE ID
    // =====================================================

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Attendance>> getAttendanceByEmployeeId(
            @PathVariable Long employeeId,
            Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService.getAttendanceByEmployeeId(
                        employeeId,
                        authentication
                )
        );
    }


    // =====================================================
    // GET ATTENDANCE BY DATE
    // =====================================================

    @GetMapping("/date/{date}")
    public ResponseEntity<List<Attendance>> getAttendanceByDate(
            @PathVariable String date,
            Authentication authentication) {

        LocalDate localDate =
                LocalDate.parse(date);

        return ResponseEntity.ok(
                attendanceService.getAttendanceByDate(
                        localDate,
                        authentication
                )
        );
    }


    // =====================================================
    // SELF CHECK-IN
    // ADMIN / HR / MANAGER / EMPLOYEE
    // =====================================================

    @PostMapping("/check-in")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')"
    )
    public ResponseEntity<?> checkIn(
            Authentication authentication) {

        try {

            Attendance attendance =
                    attendanceService.checkIn(
                            authentication
                    );

            return ResponseEntity.ok(
                    attendance
            );

        } catch (IllegalStateException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());

        }
    }


    // =====================================================
    // SELF CHECK-OUT
    // ADMIN / HR / MANAGER / EMPLOYEE
    // =====================================================

    @PostMapping("/check-out")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')"
    )
    public ResponseEntity<?> checkOut(
            Authentication authentication) {

        try {

            Attendance attendance =
                    attendanceService.checkOut(
                            authentication
                    );

            return ResponseEntity.ok(
                    attendance
            );

        } catch (IllegalStateException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());

        }
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> updateAttendance(
            @PathVariable Long id,
            @RequestBody Attendance attendance,
            Authentication authentication) {

        Attendance updatedAttendance =
                attendanceService.updateAttendance(
                        id,
                        attendance,
                        authentication
                );

        if (updatedAttendance != null) {
            return ResponseEntity.ok(updatedAttendance);
        }

        return ResponseEntity.notFound().build();
    }@DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<String> deleteAttendance(
            @PathVariable Long id,
            Authentication authentication) {

        boolean deleted =
                attendanceService.deleteAttendance(
                        id,
                        authentication
                );

        if (deleted) {
            return ResponseEntity.ok(
                    "Attendance Deleted Successfully"
            );
        }

        return ResponseEntity.notFound().build();
    }
}