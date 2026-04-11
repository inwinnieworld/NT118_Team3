package com.example.emotiondebugging.model.request;

import com.google.gson.annotations.SerializedName;

public class UpdateStaffRequest {
    @SerializedName("name") public String name;
    @SerializedName("email") public String email;
    @SerializedName("phone") public String phone;
    @SerializedName("position") public String position;
    @SerializedName("department") public String department;

    public UpdateStaffRequest(String name, String email, String phone,
                               String position, String department) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.position = position;
        this.department = department;
    }
}
