package com.example.ems.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.ems.model.Attendance;
import com.example.ems.model.Employee;
import com.example.ems.model.User;
import com.example.ems.repository.AttendanceRepository;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.UserRepository;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;


    // =====================================================
    // ADD ATTENDANCE
    // ADMIN / HR
    // =====================================================

    public Attendance addAttendance(Attendance attendance) {

        if (attendance.getDate() == null) {
            attendance.setDate(LocalDate.now());
        }

        if (attendance.getStatus() == null ||
                attendance.getStatus().isBlank()) {

            attendance.setStatus("PRESENT");
        }

        return attendanceRepository.save(attendance);
    }


    // =====================================================
    // GET ATTENDANCE
    // ROLE BASED
    // =====================================================

    public List<Attendance> getAttendanceForUser(
            Authentication authentication) {

        if (authentication == null) {
            throw new AccessDeniedException(
                    "Authentication required"
            );
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new AccessDeniedException(
                    "User not found"
            );
        }

        String role = user.getRole().toUpperCase();


        // =================================================
        // ADMIN / HR
        // Can see all attendance
        // =================================================

        if (role.equals("ADMIN") ||
                role.equals("HR")) {

            return attendanceRepository.findAll();
        }


        // =================================================
        // EMPLOYEE
        // Can see own attendance
        // =================================================

        if (role.equals("EMPLOYEE")) {

            if (user.getEmployee() == null) {
                throw new AccessDeniedException(
                        "Employee profile not linked"
                );
            }

            Long employeeId =
                    user.getEmployee().getId();

            return attendanceRepository
                    .findByEmployeeId(employeeId);
        }


        // =================================================
        // MANAGER
        // Can see attendance of same department
        // =================================================

        if (role.equals("MANAGER")) {

            if (user.getEmployee() == null) {
                throw new AccessDeniedException(
                        "Manager employee profile not linked"
                );
            }

            if (user.getEmployee().getDepartment() == null) {
                throw new AccessDeniedException(
                        "Manager department not assigned"
                );
            }

            Long departmentId =
                    user.getEmployee()
                            .getDepartment()
                            .getId();

            List<Employee> employees =
                    employeeRepository
                            .findByDepartmentIdAndStatusIgnoreCase(
                                    departmentId,
                                    "ACTIVE"
                            );

            return employees.stream()
                    .flatMap(employee ->
                            attendanceRepository
                                    .findByEmployeeId(
                                            employee.getId()
                                    )
                                    .stream()
                    )
                    .toList();
        }


        throw new AccessDeniedException(
                "Invalid role"
        );
    }


    // =====================================================
    // GET ATTENDANCE BY ID
    // =====================================================

    public Optional<Attendance> getAttendanceById(
            Long id,
            Authentication authentication) {

        Optional<Attendance> attendance =
                attendanceRepository.findById(id);

        if (attendance.isEmpty()) {
            return Optional.empty();
        }

        Attendance record = attendance.get();

        if (!hasAccessToEmployee(
                record.getEmployeeId(),
                authentication)) {

            throw new AccessDeniedException(
                    "You do not have access to this attendance"
            );
        }

        return attendance;
    }


    // =====================================================
    // GET ATTENDANCE BY EMPLOYEE ID
    // =====================================================

    public List<Attendance> getAttendanceByEmployeeId(
            Long employeeId,
            Authentication authentication) {

        if (!hasAccessToEmployee(
                employeeId,
                authentication)) {

            throw new AccessDeniedException(
                    "You do not have access to this employee's attendance"
            );
        }

        return attendanceRepository
                .findByEmployeeId(employeeId);
    }


    // =====================================================
    // GET ATTENDANCE BY DATE
    // =====================================================

    public List<Attendance> getAttendanceByDate(
            LocalDate date,
            Authentication authentication) {

        if (authentication == null) {
            throw new AccessDeniedException(
                    "Authentication required"
            );
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new AccessDeniedException(
                    "User not found"
            );
        }

        String role = user.getRole().toUpperCase();


        // ADMIN / HR
        if (role.equals("ADMIN") ||
                role.equals("HR")) {

            return attendanceRepository
                    .findByDate(date);
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

            return attendanceRepository
                    .findByDate(date)
                    .stream()
                    .filter(attendance ->
                            employeeId.equals(
                                    attendance.getEmployeeId()
                            )
                    )
                    .toList();
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

            return attendanceRepository
                    .findByDate(date)
                    .stream()
                    .filter(attendance ->
                            hasAccessToEmployee(
                                    attendance.getEmployeeId(),
                                    authentication
                            )
                    )
                    .toList();
        }


        throw new AccessDeniedException(
                "Invalid role"
        );
    }public Attendance updateAttendance(
            Long id,
            Attendance attendance,
            Authentication authentication) {

        if (authentication == null) {
            throw new AccessDeniedException(
                    "Authentication required"
            );
        }

        User user =
                userRepository.findByEmail(
                        authentication.getName()
                );

        if (user == null) {
            throw new AccessDeniedException(
                    "User not found"
            );
        }

        String role = user.getRole().toUpperCase();

        // Only ADMIN and HR can update attendance
        if (!role.equals("ADMIN") && !role.equals("HR")) {
            throw new AccessDeniedException(
                    "You do not have permission to update attendance"
            );
        }

        Optional<Attendance> existing =
                attendanceRepository.findById(id);

        if (existing.isEmpty()) {
            return null;
        }

        Attendance existingAttendance =
                existing.get();

        // =====================================================
        // HR CANNOT EDIT OWN ATTENDANCE
        // =====================================================

        if (role.equals("HR")) {

            if (user.getEmployee() == null) {
                throw new AccessDeniedException(
                        "HR employee profile not linked"
                );
            }

            Long loggedInEmployeeId =
                    user.getEmployee().getId();

            // Existing attendance belongs to logged-in HR
            if (loggedInEmployeeId.equals(
                    existingAttendance.getEmployeeId())) {

                throw new AccessDeniedException(
                        "You cannot edit your own attendance"
                );
            }

            // HR cannot change another employee's record
            // to their own employee ID
            if (loggedInEmployeeId.equals(
                    attendance.getEmployeeId())) {

                throw new AccessDeniedException(
                        "You cannot assign your attendance ID to another record"
                );
            }
        }

        // =====================================================
        // UPDATE
        // =====================================================

        existingAttendance.setEmployeeId(
                attendance.getEmployeeId()
        );

        existingAttendance.setDate(
                attendance.getDate()
        );

        existingAttendance.setCheckIn(
                attendance.getCheckIn()
        );

        existingAttendance.setCheckOut(
                attendance.getCheckOut()
        );

        existingAttendance.setStatus(
                attendance.getStatus()
        );

        return attendanceRepository.save(
                existingAttendance
        );
    }
    public boolean deleteAttendance(
            Long id,
            Authentication authentication) {

        if (authentication == null) {
            throw new AccessDeniedException(
                    "Authentication required"
            );
        }

        User user =
                userRepository.findByEmail(
                        authentication.getName()
                );

        if (user == null) {
            throw new AccessDeniedException(
                    "User not found"
            );
        }

        String role = user.getRole().toUpperCase();

        // Only ADMIN and HR can delete
        if (!role.equals("ADMIN") && !role.equals("HR")) {
            throw new AccessDeniedException(
                    "You do not have permission to delete attendance"
            );
        }

        Optional<Attendance> existing =
                attendanceRepository.findById(id);

        if (existing.isEmpty()) {
            return false;
        }

        Attendance attendance =
                existing.get();

        // =====================================================
        // HR CANNOT DELETE OWN ATTENDANCE
        // =====================================================

        if (role.equals("HR")) {

            if (user.getEmployee() == null) {
                throw new AccessDeniedException(
                        "HR employee profile not linked"
                );
            }

            Long loggedInEmployeeId =
                    user.getEmployee().getId();

            if (loggedInEmployeeId.equals(
                    attendance.getEmployeeId())) {

                throw new AccessDeniedException(
                        "You cannot delete your own attendance"
                );
            }
        }

        attendanceRepository.deleteById(id);

        return true;
    }
    // =====================================================
    // SELF CHECK-IN
    // ADMIN / HR / MANAGER / EMPLOYEE
    // =====================================================

    public Attendance checkIn(
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

        String role =
                user.getRole().toUpperCase();


        // Allow all four roles
        if (!role.equals("ADMIN") &&
                !role.equals("HR") &&
                !role.equals("MANAGER") &&
                !role.equals("EMPLOYEE")) {

            throw new AccessDeniedException(
                    "Invalid role"
            );
        }


        // User must be linked with Employee
        if (user.getEmployee() == null) {
            throw new AccessDeniedException(
                    "Employee profile not linked"
            );
        }

        Long employeeId =
                user.getEmployee().getId();

        LocalDate today =
                LocalDate.now();


        // Check today's attendance
        List<Attendance> existingAttendance =
                attendanceRepository
                        .findByEmployeeId(employeeId);

        boolean alreadyCheckedIn =
                existingAttendance.stream()
                        .anyMatch(attendance ->
                                today.equals(
                                        attendance.getDate()
                                )
                        );


        if (alreadyCheckedIn) {
            throw new IllegalStateException(
                    "Attendance already marked for today"
            );
        }


        // Create attendance
        Attendance attendance =
                new Attendance();

        attendance.setEmployeeId(employeeId);

        attendance.setDate(today);

        attendance.setCheckIn(
                LocalTime.now()
        );

        attendance.setStatus(
                "PRESENT"
        );

        return attendanceRepository.save(
                attendance
        );
    }


    // =====================================================
    // SELF CHECK-OUT
    // ADMIN / HR / MANAGER / EMPLOYEE
    // =====================================================

    public Attendance checkOut(
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

        String role =
                user.getRole().toUpperCase();


        // Allow all four roles
        if (!role.equals("ADMIN") &&
                !role.equals("HR") &&
                !role.equals("MANAGER") &&
                !role.equals("EMPLOYEE")) {

            throw new AccessDeniedException(
                    "Invalid role"
            );
        }


        // User must be linked with Employee
        if (user.getEmployee() == null) {
            throw new AccessDeniedException(
                    "Employee profile not linked"
            );
        }

        Long employeeId =
                user.getEmployee().getId();

        LocalDate today =
                LocalDate.now();


        // Find today's attendance
        List<Attendance> existingAttendance =
                attendanceRepository
                        .findByEmployeeId(employeeId);

        Optional<Attendance> todayAttendance =
                existingAttendance.stream()
                        .filter(attendance ->
                                today.equals(
                                        attendance.getDate()
                                )
                        )
                        .findFirst();


        // Check-in is required first
        if (todayAttendance.isEmpty()) {

            throw new IllegalStateException(
                    "Please check in first"
            );
        }


        Attendance attendance =
                todayAttendance.get();


        // Prevent duplicate checkout
        if (attendance.getCheckOut() != null) {

            throw new IllegalStateException(
                    "You have already checked out today"
            );
        }


        // Set checkout time
        attendance.setCheckOut(
                LocalTime.now()
        );

        return attendanceRepository.save(
                attendance
        );
    }


    // =====================================================
    // COMMON ACCESS CHECK
    // =====================================================

    private boolean hasAccessToEmployee(
            Long employeeId,
            Authentication authentication) {

        if (authentication == null) {
            return false;
        }

        String email =
                authentication.getName();

        User user =
                userRepository.findByEmail(email);

        if (user == null) {
            return false;
        }

        String role =
                user.getRole().toUpperCase();


        // =================================================
        // ADMIN / HR
        // Access to everyone
        // =================================================

        if (role.equals("ADMIN") ||
                role.equals("HR")) {

            return true;
        }


        // =================================================
        // EMPLOYEE
        // Access only own attendance
        // =================================================

        if (role.equals("EMPLOYEE")) {

            if (user.getEmployee() == null) {
                return false;
            }

            return user.getEmployee()
                    .getId()
                    .equals(employeeId);
        }


        // =================================================
        // MANAGER
        // Access employees in same department
        // =================================================

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

            return managerDepartmentId
                    .equals(employeeDepartmentId);
        }


        return false;
    }
}