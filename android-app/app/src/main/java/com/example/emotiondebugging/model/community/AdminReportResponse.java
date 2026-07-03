package com.example.emotiondebugging.model.community;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AdminReportResponse {

    @SerializedName("postReports")
    public List<PostReport> postReports;

    @SerializedName("commentReports")
    public List<CommentReport> commentReports;

    public static class PostReport {
        @SerializedName("post_id")
        public int postId;

        @SerializedName("report_id")
        public int reportId;

        @SerializedName("title")
        public String title;

        @SerializedName("content")
        public String content;

        @SerializedName("is_hidden")
        public int isHidden;

        @SerializedName("author_student_id")
        public int authorStudentId;

        @SerializedName("author_name")
        public String authorName;

        @SerializedName("report_count")
        public int reportCount;

        @SerializedName("last_reported_at")
        public String lastReportedAt;

        @SerializedName("reasons")
        public String reasons;
    }

    public static class CommentReport {
        @SerializedName("comment_id")
        public int commentId;

        @SerializedName("report_id")
        public int reportId;

        @SerializedName("content")
        public String content;

        @SerializedName("is_hidden")
        public int isHidden;

        @SerializedName("post_id")
        public int postId;

        @SerializedName("author_student_id")
        public int authorStudentId;

        @SerializedName("author_name")
        public String authorName;

        @SerializedName("report_count")
        public int reportCount;

        @SerializedName("last_reported_at")
        public String lastReportedAt;

        @SerializedName("reasons")
        public String reasons;
    }
}
