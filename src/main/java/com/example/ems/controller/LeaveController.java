package com.example.ems.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.ems.model.LeaveRequest;
import com.example.ems.service.LeaveService;

@RestController
@RequestMapping("/leaves")
@CrossOrigin(origins = "http://localhost:5173")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;


    // =====================================================
    // APPLY LEAVE
    //
    // EMPLOYEE / MANAGER / HR / ADMIN
    // =====================================================

    @PostMapping
    public ResponseEntity<LeaveRequest> applyLeave(
            @RequestBody LeaveRequest leaveRequest,
            Authentication authentication) {

        return ResponseEntity.ok(
                leaveService.applyLeave(
                        leaveRequest,
                        authentication
                )
        );
    }


    // =====================================================
    // GET LEAVES
    //
    // ADMIN     -> ALL
    // HR        -> ALL
    // MANAGER   -> TEAM + OWN
    // EMPLOYEE  -> OWN
    // =====================================================

    @GetMapping
    public ResponseEntity<List<LeaveRequest>> getAllLeaves(
            Authentication authentication) {

        return ResponseEntity.ok(
                leaveService.getLeavesForUser(
                        authentication
                )
        );
    }


    // =====================================================
    // GET LEAVES BY EMPLOYEE
    // =====================================================

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveRequest>> getLeavesByEmployee(
            @PathVariable Long employeeId,
            Authentication authentication) {

        return ResponseEntity.ok(
                leaveService.getLeavesByEmployee(
                        employeeId,
                        authentication
                )
        );
    }


    // =====================================================
    // GET LEAVE BY ID
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getLeaveById(
            @PathVariable Long id,
            Authentication authentication) {

        Optional<LeaveRequest> leave =
                leaveService.getLeaveById(
                        id,
                        authentication
                );

        if (leave.isPresent()) {
            return ResponseEntity.ok(leave.get());
        }

        return ResponseEntity.notFound().build();
    }


    // =====================================================
    // APPROVE LEAVE
    //
    // ADMIN     -> ALL
    // HR        -> MANAGER + EMPLOYEE
    // MANAGER   -> TEAM EMPLOYEES ONLY
    // EMPLOYEE  -> NO
    //
    // SPECIAL:
    // MANAGER's own leave -> HR
    // HR's own leave      -> ADMIN
    // =====================================================

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveLeave(
            @PathVariable Long id,
            Authentication authentication) {

        LeaveRequest leave =
                leaveService.approveLeave(
                        id,
                        authentication
                );

        if (leave != null) {
            return ResponseEntity.ok(leave);
        }

        return ResponseEntity.notFound().build();
    }


    // =====================================================
    // REJECT LEAVE
    //
    // ADMIN     -> ALL
    // HR        -> MANAGER + EMPLOYEE
    // MANAGER   -> TEAM EMPLOYEES ONLY
    // EMPLOYEE  -> NO
    //
    // SPECIAL:
    // MANAGER's own leave -> HR
    // HR's own leave      -> ADMIN
    // =====================================================

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectLeave(
            @PathVariable Long id,
            Authentication authentication) {

        LeaveRequest leave =
                leaveService.rejectLeave(
                        id,
                        authentication
                );

        if (leave != null) {
            return ResponseEntity.ok(leave);
        }

        return ResponseEntity.notFound().build();
    }


    // =====================================================
    // DELETE LEAVE
    //
    // ADMIN     -> ALL
    // HR        -> MANAGER + EMPLOYEE
    // MANAGER   -> TEAM EMPLOYEES ONLY
    // EMPLOYEE  -> NO
    //
    // SPECIAL:
    // MANAGER cannot delete own leave
    // HR cannot delete own leave
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLeave(
            @PathVariable Long id,
            Authentication authentication) {

        boolean deleted =
                leaveService.deleteLeave(
                        id,
                        authentication
                );

        if (deleted) {
            return ResponseEntity.ok(
                    "Leave Deleted Successfully"
            );
        }

        return ResponseEntity.notFound().build();
    }

}
