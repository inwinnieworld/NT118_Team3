package com.example.emotiondebugging.model.response;

import com.google.gson.annotations.SerializedName;

public class ProfileResponse {

    @SerializedName("data")
    private Data data;

    public Data getData() { return data; }

    public static class Data {
        @SerializedName("user_id") public int userId;
        @SerializedName("name") public String name;
        @SerializedName("email") public String email;
        @SerializedName("phone") public String phone;
        @SerializedName("avatar_url") public String avatarUrl;
        @SerializedName("student_code") public String studentCode;
        @SerializedName("major") public String major;
        @SerializedName("faculty") public String faculty;
        @SerializedName("year_of_study") public String yearOfStudy;
        @SerializedName("emergency_phone") public String emergencyPhone;
    }
}
