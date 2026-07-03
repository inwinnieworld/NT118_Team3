package com.example.emotiondebugging.model.community;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AdminReviewRequestResponse {

    @SerializedName("requests")
    public List<ReviewRequest> requests;

    public static class ReviewRequest {
        @SerializedName("request_id")
        public int requestId;

        @SerializedName("post_id")
        public int postId;

        @SerializedName("message")
        public String message;

        @SerializedName("status")
        public String status;

        @SerializedName("created_at")
        public String createdAt;

        @SerializedName("title")
        public String title;

        @SerializedName("content")
        public String content;

        @SerializedName("is_hidden")
        public int isHidden;

        @SerializedName("student_id")
        public int studentId;

        @SerializedName("author_name")
        public String authorName;
    }
}
