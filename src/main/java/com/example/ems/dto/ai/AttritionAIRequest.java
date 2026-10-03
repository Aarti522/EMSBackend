package com.example.ems.dto.ai;

public class AttritionAIRequest {

    private Long employeeId;

    private String role;

    public AttritionAIRequest() {
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}