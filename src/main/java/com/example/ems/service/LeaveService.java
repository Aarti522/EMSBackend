package com.example.ems.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.ems.model.Employee;
import com.example.ems.model.LeaveRequest;
import com.example.ems.model.User;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.LeaveRepository;
import com.example.ems.repository.UserRepository;

@Service
public class LeaveService {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;


 // =====================================================
 // APPLY LEAVE
 // EMPLOYEE / MANAGER / HR / ADMIN
 // =====================================================

 public LeaveRequest applyLeave(
         LeaveRequest leaveRequest,
         Authentication authentication) {

     String email = authentication.getName();

     User user = userRepository.findByEmail(email);

     if (user == null) {
         throw new AccessDeniedException("User not found");
     }

     String role = user.getRole().toUpperCase();

     // =================================================
     // EMPLOYEE
     // Own leave only
     // =================================================

     if (role.equals("EMPLOYEE")) {

         if (user.getEmployee() == null) {
             throw new AccessDeniedException(
                     "Employee profile not linked"
             );
         }

         Long ownEmployeeId =
                 user.getEmployee().getId();

         leaveRequest.setEmployeeId(ownEmployeeId);
         leaveRequest.setEmployeeName(
                 user.getEmployee().getName()
         );
     }

     // =================================================
     // MANAGER
     // Own leave
     // =================================================

     else if (role.equals("MANAGER")) {

         if (user.getEmployee() == null) {
             throw new AccessDeniedException(
                     "Manager employee profile not linked"
             );
         }

         Long ownEmployeeId =
                 user.getEmployee().getId();

         leaveRequest.setEmployeeId(ownEmployeeId);
         leaveRequest.setEmployeeName(
                 user.getEmployee().getName()
         );
     }

     // =================================================
     // ADMIN / HR
     // Can apply leave for any employee
     // =================================================

     else if (role.equals("ADMIN") ||
              role.equals("HR")) {

         if (leaveRequest.getEmployeeId() == null) {

             throw new IllegalArgumentException(
                     "Employee ID is required"
             );
         }

         Employee employee =
                 employeeRepository
                         .findById(
                                 leaveRequest.getEmployeeId()
                         )
                         .orElse(null);

         if (employee == null) {

             throw new IllegalArgumentException(
                     "Employee not found"
             );
         }

         leaveRequest.setEmployeeName(
                 employee.getName()
         );
     }

     else {

         throw new AccessDeniedException(
                 "Invalid role"
         );
     }

     // =================================================
     // DEFAULT STATUS
     // =================================================

     leaveRequest.setStatus("PENDING");

     return leaveRepository.save(leaveRequest);
 }

    // =====================================================
    // GET LEAVES
    //
    // ADMIN / HR -> ALL
    // MANAGER    -> TEAM
    // EMPLOYEE   -> OWN
    // =====================================================

    public List<LeaveRequest> getLeavesForUser(
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new AccessDeniedException(
                    "User not found"
            );
        }

        String role =
                user.getRole().toUpperCase();


        // ADMIN
        if (role.equals("ADMIN")) {

            return leaveRepository.findAll();
        }


        // HR
        if (role.equals("HR")) {

            return leaveRepository.findAll();
        }


        // EMPLOYEE
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


        // MANAGER
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

            List<Employee> team =
                    employeeRepository
                            .findByDepartmentId(
                                    departmentId
                            );

            List<LeaveRequest> teamLeaves =
                    new ArrayList<>();

            for (Employee employee : team) {

                teamLeaves.addAll(
                        leaveRepository
                                .findByEmployeeId(
                                        employee.getId()
                                )
                );
            }

            return teamLeaves;
        }


        throw new AccessDeniedException(
                "Invalid role"
        );
    }


    // =====================================================
    // GET LEAVES BY EMPLOYEE
    // =====================================================

    public List<LeaveRequest> getLeavesByEmployee(
            Long employeeId,
            Authentication authentication) {

        if (!hasAccessToEmployee(
                employeeId,
                authentication)) {

            throw new AccessDeniedException(
                    "You are not allowed to access this employee leaves"
            );
        }

        return leaveRepository
                .findByEmployeeId(employeeId);
    }


    // =====================================================
    // GET LEAVE BY ID
    // =====================================================

    public Optional<LeaveRequest> getLeaveById(
            Long id,
            Authentication authentication) {

        Optional<LeaveRequest> optionalLeave =
                leaveRepository.findById(id);

        if (optionalLeave.isEmpty()) {
            return Optional.empty();
        }

        LeaveRequest leave =
                optionalLeave.get();

        if (hasAccessToEmployee(
                leave.getEmployeeId(),
                authentication)) {

            return optionalLeave;
        }

        throw new AccessDeniedException(
                "You are not allowed to access this leave"
        );
    }


    // =====================================================
    // APPROVE LEAVE
    // ADMIN / HR / MANAGER
    // =====================================================

    public LeaveRequest approveLeave(
            Long id,
            Authentication authentication) {

        Optional<LeaveRequest> optionalLeave =
                leaveRepository.findById(id);

        if (optionalLeave.isEmpty()) {
            return null;
        }

        LeaveRequest leave =
                optionalLeave.get();


        if (!canApproveLeave(
                leave.getEmployeeId(),
                authentication)) {

            throw new AccessDeniedException(
                    "You are not allowed to approve this leave"
            );
        }


        leave.setStatus("APPROVED");

        return leaveRepository.save(leave);
    }


    // =====================================================
    // REJECT LEAVE
    // ADMIN / HR / MANAGER
    // =====================================================

    public LeaveRequest rejectLeave(
            Long id,
            Authentication authentication) {

        Optional<LeaveRequest> optionalLeave =
                leaveRepository.findById(id);

        if (optionalLeave.isEmpty()) {
            return null;
        }

        LeaveRequest leave =
                optionalLeave.get();


        if (!canApproveLeave(
                leave.getEmployeeId(),
                authentication)) {

            throw new AccessDeniedException(
                    "You are not allowed to reject this leave"
            );
        }


        leave.setStatus("REJECTED");

        return leaveRepository.save(leave);
    }


    // =====================================================
    // DELETE LEAVE
    // ADMIN / HR ONLY
    // =====================================================

    public boolean deleteLeave(
            Long id,
            Authentication authentication) {

        String email = authentication.getName();

        User user =
                userRepository.findByEmail(email);

        if (user == null) {
            throw new AccessDeniedException(
                    "User not found"
            );
        }

        String role =
                user.getRole().toUpperCase();

        if (!role.equals("ADMIN") &&
                !role.equals("HR")) {

            throw new AccessDeniedException(
                    "Only ADMIN or HR can delete leave"
            );
        }


        if (leaveRepository.existsById(id)) {

            leaveRepository.deleteById(id);

            return true;
        }

        return false;
    }


    // =====================================================
    // COMMON EMPLOYEE ACCESS CHECK
    // =====================================================

    private boolean hasAccessToEmployee(
            Long employeeId,
            Authentication authentication) {

        String email = authentication.getName();

        User user =
                userRepository.findByEmail(email);

        if (user == null) {
            return false;
        }

        String role =
                user.getRole().toUpperCase();


        // ADMIN / HR
        if (role.equals("ADMIN") ||
                role.equals("HR")) {

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

            if (user.getEmployee() == null ||
                    user.getEmployee().getDepartment() == null) {

                return false;
            }

            Employee employee =
                    employeeRepository
                            .findById(employeeId)
                            .orElse(null);

            if (employee == null ||
                    employee.getDepartment() == null) {

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


    // =====================================================
    // APPROVAL ACCESS CHECK
    // =====================================================

    private boolean canApproveLeave(
            Long employeeId,
            Authentication authentication) {

        String email = authentication.getName();

        User user =
                userRepository.findByEmail(email);

        if (user == null) {
            return false;
        }

        String role =
                user.getRole().toUpperCase();


        // ADMIN / HR
        if (role.equals("ADMIN") ||
                role.equals("HR")) {

            return true;
        }


        // MANAGER
        if (role.equals("MANAGER")) {

            return hasAccessToEmployee(
                    employeeId,
                    authentication
            );
        }


        return false;
    }
}