package com.example.emotiondebugging.model.request;

import com.google.gson.annotations.SerializedName;

public class UpdateStudentRequest {
    @SerializedName("name") public String name;
    @SerializedName("email") public String email;
    @SerializedName("phone") public String phone;
    @SerializedName("major") public String major;
    @SerializedName("faculty") public String faculty;
    @SerializedName("year_of_study") public String yearOfStudy;

    public UpdateStudentRequest(String name, String email, String phone,
                                 String major, String faculty, String yearOfStudy) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.major = major;
        this.faculty = faculty;
        this.yearOfStudy = yearOfStudy;
    }
}
