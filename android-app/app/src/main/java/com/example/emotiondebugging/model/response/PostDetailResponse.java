package com.example.emotiondebugging.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PostDetailResponse {

    @SerializedName("post") public PostDetail post;
    @SerializedName("comments") public List<CommentItem> comments;

    public static class PostDetail {
        @SerializedName("post_id") public int postId;
        @SerializedName("title") public String title;
        @SerializedName("content") public String content;
        @SerializedName("is_anonymous") public int isAnonymous;
        @SerializedName("created_at") public String createdAt;
        @SerializedName("view_count") public int viewCount;
        @SerializedName("error_type_id") public int errorTypeId;
        @SerializedName("error_name") public String errorName;
        @SerializedName("author_name") public String authorName;
        @SerializedName("author_avatar") public String authorAvatar;
        @SerializedName("upvote_count") public int upvoteCount;
        @SerializedName("downvote_count") public int downvoteCount;
        @SerializedName("comment_count") public int commentCount;
    }

    public static class CommentItem {
        @SerializedName("comment_id") public int commentId;
        @SerializedName("content") public String content;
        @SerializedName("created_at") public String createdAt;
        @SerializedName("view_count") public int viewCount;
        @SerializedName("parent_comment_id") public Integer parentCommentId;
        @SerializedName("author_name") public String authorName;
        @SerializedName("author_avatar") public String authorAvatar;
        @SerializedName("upvote_count") public int upvoteCount;
        @SerializedName("downvote_count") public int downvoteCount;
        @SerializedName("replies") public java.util.List<CommentItem> replies;
    }
}
