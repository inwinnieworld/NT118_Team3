package com.example.emotiondebugging.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StaffListResponse {
    @SerializedName("data") public List<StaffItem> data;
    @SerializedName("total") public int total;
    @SerializedName("page") public int page;
    @SerializedName("totalPages") public int totalPages;

    public static class StaffItem {
        @SerializedName("user_id") public int userId;
        @SerializedName("staff_id") public int staffId;
        @SerializedName("name") public String name;
        @SerializedName("email") public String email;
        @SerializedName("phone") public String phone;
        @SerializedName("avatar_url") public String avatarUrl;
        @SerializedName("position") public String position;
        @SerializedName("department") public String department;
        @SerializedName("hire_date") public String hireDate;
        @SerializedName("is_locked") public int isLockedInt;
        public boolean isLocked() { return isLockedInt == 1; }
    }
}
