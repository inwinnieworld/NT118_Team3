package com.example.emotiondebugging.model.request;

public class RegisterRequest {
    private final String name;
    private final String email;
    private final String password;
    private final String phone;
    private final String studentCode;
    private final String major;
    private final String faculty;
    private final Integer yearOfStudy;

    public RegisterRequest(String name, String email, String password, String phone,
                           String studentCode, String major, String faculty, Integer yearOfStudy) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.studentCode = studentCode;
        this.major = major;
        this.faculty = faculty;
        this.yearOfStudy = yearOfStudy;
    }
}