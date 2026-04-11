package com.example.emotiondebugging.model.response;

public class UserResponse {
    private int userId;
    private String name;
    private String email;
    private String phone;
    private String role;
    private Integer studentId;
    private String studentCode;
    private String adminRole;
    private String staffPosition;

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public String getAdminRole() {
        return adminRole;
    }

    public String getStaffPosition() {
        return staffPosition;
    }
}