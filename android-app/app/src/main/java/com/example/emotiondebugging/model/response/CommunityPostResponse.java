package com.example.emotiondebugging.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CommunityPostResponse {

    @SerializedName("posts") public List<PostItem> posts;
    @SerializedName("total") public int total;
    @SerializedName("page") public int page;
    @SerializedName("totalPages") public int totalPages;

    public static class PostItem {
        @SerializedName("post_id") public int postId;
        @SerializedName("title") public String title;
        @SerializedName("content") public String content;
        @SerializedName("is_anonymous") public int isAnonymous; // 0 or 1 from server
        @SerializedName("created_at") public String createdAt;
        @SerializedName("error_type_id") public int errorTypeId;
        @SerializedName("error_name") public String errorName;
        @SerializedName("author_name") public String authorName;
        @SerializedName("author_avatar") public String authorAvatar;
        @SerializedName("upvote_count") public int upvoteCount;
        @SerializedName("downvote_count") public int downvoteCount;
        @SerializedName("comment_count") public int commentCount;
        @SerializedName("view_count") public int viewCount;
        @SerializedName("is_saved") public int isSaved; // 0 or 1 from server
    }
}
