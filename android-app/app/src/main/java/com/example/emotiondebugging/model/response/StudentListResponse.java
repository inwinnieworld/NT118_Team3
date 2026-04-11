package com.example.emotiondebugging.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StudentListResponse {
    @SerializedName("data") public List<StudentItem> data;
    @SerializedName("total") public int total;
    @SerializedName("page") public int page;
    @SerializedName("totalPages") public int totalPages;

    public static class StudentItem {
        @SerializedName("user_id") public int userId;
        @SerializedName("student_id") public int studentId;
        @SerializedName("name") public String name;
        @SerializedName("email") public String email;
        @SerializedName("phone") public String phone;
        @SerializedName("avatar_url") public String avatarUrl;
        @SerializedName("student_code") public String studentCode;
        @SerializedName("major") public String major;
        @SerializedName("faculty") public String faculty;
        @SerializedName("year_of_study") public String yearOfStudy;
        @SerializedName("is_locked") public int isLockedInt;
        public boolean isLocked() { return isLockedInt == 1; }
    }
}
