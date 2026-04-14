package com.example.emotiondebugging.model.request;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileRequest {
    @SerializedName("name") public String name;
    @SerializedName("phone") public String phone;
    @SerializedName("major") public String major;
    @SerializedName("faculty") public String faculty;
    @SerializedName("year_of_study") public String yearOfStudy;
    @SerializedName("emergency_phone") public String emergencyPhone;

    public UpdateProfileRequest(String name, String phone, String major,
                                 String faculty, String yearOfStudy, String emergencyPhone) {
        this.name = name;
        this.phone = phone;
        this.major = major;
        this.faculty = faculty;
        this.yearOfStudy = yearOfStudy;
        this.emergencyPhone = emergencyPhone;
    }
}
