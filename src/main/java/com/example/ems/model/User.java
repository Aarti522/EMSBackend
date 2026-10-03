package com.example.ems.model;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    /*
     * Allowed roles:
     *
     * ADMIN
     * HR
     * MANAGER
     * EMPLOYEE
     */
    @Column(nullable = false)
    private String role;

    /*
     * Every User can be linked with one Employee.
     *
     * This relation is important because:
     *
     * JWT
     *   ↓
     * User
     *   ↓
     * Employee
     *   ↓
     * employeeId
     *
     * Manager:
     *   Employee → Department → Team
     *
     * Employee:
     *   Employee → Own Data
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    /*
     * Used to identify whether the user is logging in
     * for the first time.
     */
    @Column(name = "first_login")
    private boolean firstLogin = true;

    @Column(name = "reset_otp")
    private String resetOtp;

    @Column(name = "reset_otp_expiry")
    private LocalDateTime resetOtpExpiry;
    // =========================
    // Constructors
    // =========================

    public User() {
    }


    // =========================
    // Getters and Setters
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }


    public boolean isFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(boolean firstLogin) {
        this.firstLogin = firstLogin;
    }
    
    public String getResetOtp() {
        return resetOtp;
    }

    public void setResetOtp(String resetOtp) {
        this.resetOtp = resetOtp;
    }

    public LocalDateTime getResetOtpExpiry() {
        return resetOtpExpiry;
    }

    public void setResetOtpExpiry(LocalDateTime resetOtpExpiry) {
        this.resetOtpExpiry = resetOtpExpiry;
    }
    
}