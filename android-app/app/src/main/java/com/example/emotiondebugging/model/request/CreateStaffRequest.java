package com.example.emotiondebugging.model.request;

import com.google.gson.annotations.SerializedName;

public class CreateStaffRequest {
    @SerializedName("name") public String name;
    @SerializedName("email") public String email;
    @SerializedName("password") public String password;
    @SerializedName("phone") public String phone;
    @SerializedName("position") public String position;
    @SerializedName("department") public String department;

    public CreateStaffRequest(String name, String email, String password,
                               String phone, String position, String department) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.position = position;
        this.department = department;
    }
}
