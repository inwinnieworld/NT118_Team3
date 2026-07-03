package com.example.emotiondebugging.data.api;

import com.example.emotiondebugging.model.community.CommunityProfile;
import com.example.emotiondebugging.model.community.UpdateCommunityProfileRequest;
import com.example.emotiondebugging.model.request.CreatePostRequest;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.model.response.CommunityPostResponse;
import com.example.emotiondebugging.model.response.PostDetailResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import okhttp3.MultipartBody;
import com.example.emotiondebugging.model.chat.ChatHistoryResponse;
import com.example.emotiondebugging.model.community.FollowListResponse;
import com.example.emotiondebugging.model.community.NotificationListResponse;
import com.example.emotiondebugging.model.community.TopicListResponse;
import com.example.emotiondebugging.model.community.UnreadCountResponse;
import com.example.emotiondebugging.model.response.ChatConversationResponse;
public interface CommunityApiService {

    @GET("api/community/posts")
    Call<ApiResponse<CommunityPostResponse>> getPosts(
            @Header("Authorization") String token,
            @Query("filter") String filter,
            @Query("page") int page,
            @Query("search") String search,
            @Query("topic_id") Integer topicId
    );

    @GET("api/community/error-types")
    Call<ApiResponse<List<Map<String, Object>>>> getErrorTypes(
            @Header("Authorization") String token
    );

    @POST("api/community/posts")
    Call<ApiResponse<Map<String, Object>>> createPost(
            @Header("Authorization") String token,
            @Body CreatePostRequest request
    );

    @PUT("api/community/posts/{postId}")
    Call<ApiResponse<Map<String, Object>>> updatePost(
            @Header("Authorization") String token,
            @Path("postId") int postId,
            @Body CreatePostRequest request
    );

    @DELETE("api/community/posts/{postId}")
    Call<ApiResponse<Object>> deletePost(
            @Header("Authorization") String token,
            @Path("postId") int postId
    );

    @Multipart
    @POST("api/community/profile/me/music")
    Call<ApiResponse<Map<String, Object>>> uploadProfileMusic(
            @Header("Authorization") String token,
            @Part MultipartBody.Part music
    );

    @POST("api/community/posts/{postId}/vote")
    Call<ApiResponse<Object>> votePost(
            @Header("Authorization") String token,
            @Path("postId") int postId,
            @Body Map<String, String> body
    );

    @GET("api/community/posts/{postId}")
    Call<ApiResponse<PostDetailResponse>> getPostDetail(
            @Header("Authorization") String token,
            @Path("postId") int postId
    );

    @POST("api/community/posts/{postId}/comments")
    Call<ApiResponse<PostDetailResponse.CommentItem>> createComment(
            @Header("Authorization") String token,
            @Path("postId") int postId,
            @Body Map<String, String> body
    );

    @POST("api/community/posts/{postId}/comments/{commentId}/vote")
    Call<ApiResponse<Object>> voteComment(
            @Header("Authorization") String token,
            @Path("postId") int postId,
            @Path("commentId") int commentId,
            @Body Map<String, String> body
    );

    @POST("api/community/posts/{postId}/save")
    Call<ApiResponse<Object>> toggleSavePost(
            @Header("Authorization") String token,
            @Path("postId") int postId
    );

    @POST("api/community/posts/{postId}/mute")
    Call<ApiResponse<Object>> muteAuthor(
            @Header("Authorization") String token,
            @Path("postId") int postId
    );

    @GET("api/community/saved")
    Call<ApiResponse<CommunityPostResponse>> getSavedPosts(
            @Header("Authorization") String token
    );

    @POST("api/community/quests/accept")
    Call<ApiResponse<Object>> acceptQuest(
            @Header("Authorization") String token,
            @Body Map<String, Integer> body
    );

    @GET("api/community/profile/me")
    Call<ApiResponse<CommunityProfile>> getMyCommunityProfile(
            @Header("Authorization") String token
    );

    @GET("api/community/profile/{studentId}")
    Call<ApiResponse<CommunityProfile>> getCommunityProfile(
            @Header("Authorization") String token,
            @Path("studentId") int studentId
    );

    @PUT("api/community/profile/me")
    Call<ApiResponse<Object>> updateMyCommunityProfile(
            @Header("Authorization") String token,
            @Body UpdateCommunityProfileRequest request
    );

    @GET("api/community/profile/{studentId}/posts")
    Call<ApiResponse<CommunityPostResponse>> getCommunityProfilePosts(
            @Header("Authorization") String token,
            @Path("studentId") int studentId
    );

    @POST("api/community/users/{studentId}/follow")
    Call<ApiResponse<Object>> followUser(
            @Header("Authorization") String token,
            @Path("studentId") int studentId
    );

    @DELETE("api/community/users/{studentId}/follow")
    Call<ApiResponse<Object>> unfollowUser(
            @Header("Authorization") String token,
            @Path("studentId") int studentId
    );

    // =========================
    // COMMUNITY TOPICS
    // =========================

    @GET("api/community/topics")
    Call<ApiResponse<TopicListResponse>> getPostTopics(
            @Header("Authorization") String token
    );

    @GET("api/community/profile/{studentId}/replies")
    Call<ApiResponse<CommunityPostResponse>> getCommunityProfileReplies(
            @Header("Authorization") String token,
            @Path("studentId") int studentId
    );

    @GET("api/community/profile/{studentId}/media")
    Call<ApiResponse<CommunityPostResponse>> getCommunityProfileMedia(
            @Header("Authorization") String token,
            @Path("studentId") int studentId
    );

    @GET("api/community/profile/{studentId}/reposts")
    Call<ApiResponse<CommunityPostResponse>> getCommunityProfileReposts(
            @Header("Authorization") String token,
            @Path("studentId") int studentId
    );

    @GET("api/community/profile/{studentId}/upvoted")
    Call<ApiResponse<CommunityPostResponse>> getCommunityProfileUpvoted(
            @Header("Authorization") String token,
            @Path("studentId") int studentId
    );

    @GET("/api/chat/with/{studentId}")
    Call<ApiResponse<ChatHistoryResponse>> getMessagesWithUser(
            @Header("Authorization") String token,
            @Path("studentId") int studentId
    );

    @POST("api/community/posts/{postId}/repost")
    Call<ApiResponse<Object>> toggleRepostPost(
            @Header("Authorization") String token,
            @Path("postId") int postId
    );

    @GET("api/community/profile/{studentId}/followers")
    Call<ApiResponse<FollowListResponse>> getProfileFollowers(
            @Header("Authorization") String token,
            @Path("studentId") int studentId
    );

    @GET("api/community/profile/{studentId}/following")
    Call<ApiResponse<FollowListResponse>> getProfileFollowing(
            @Header("Authorization") String token,
            @Path("studentId") int studentId
    );

    @GET("api/chat/conversations")
    Call<ApiResponse<ChatConversationResponse>> getChatConversations(
            @Header("Authorization") String token
    );

    // =========================
    // TOPIC POSTS
    // =========================

    @GET("api/community/topics/{topicId}/posts")
    Call<ApiResponse<CommunityPostResponse>> getTopicPosts(
            @Header("Authorization") String token,
            @Path("topicId") int topicId
    );

    // =========================
    // NOTIFICATIONS
    // =========================

    @GET("api/community/notifications")
    Call<ApiResponse<NotificationListResponse>> getNotifications(
            @Header("Authorization") String token
    );

    @GET("api/community/notifications/unread-count")
    Call<ApiResponse<UnreadCountResponse>> getUnreadNotificationCount(
            @Header("Authorization") String token
    );

    @POST("api/community/notifications/{id}/read")
    Call<ApiResponse<Object>> markNotificationRead(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @POST("api/community/notifications/read-all")
    Call<ApiResponse<Object>> markAllNotificationsRead(
            @Header("Authorization") String token
    );

    // =========================
    // REPORT / REVIEW REQUEST
    // =========================

    @POST("api/community/posts/{postId}/report")
    Call<ApiResponse<Object>> reportPost(
            @Header("Authorization") String token,
            @Path("postId") int postId,
            @Body Map<String, String> body
    );

    @POST("api/community/posts/{postId}/comments/{commentId}/report")
    Call<ApiResponse<Object>> reportComment(
            @Header("Authorization") String token,
            @Path("postId") int postId,
            @Path("commentId") int commentId,
            @Body Map<String, String> body
    );

    @POST("api/community/posts/{postId}/review-request")
    Call<ApiResponse<Object>> createReviewRequest(
            @Header("Authorization") String token,
            @Path("postId") int postId,
            @Body Map<String, String> body
    );

    // =========================
    // BLOCK / MUTE (author-based)
    // =========================

    @POST("api/community/users/{studentId}/block")
    Call<ApiResponse<Object>> blockUser(
            @Header("Authorization") String token,
            @Path("studentId") int studentId
    );

    @DELETE("api/community/users/{studentId}/block")
    Call<ApiResponse<Object>> unblockUser(
            @Header("Authorization") String token,
            @Path("studentId") int studentId
    );

    @GET("api/community/profile/me/blocked")
    Call<ApiResponse<FollowListResponse>> getBlockedAuthors(
            @Header("Authorization") String token
    );

    @POST("api/community/users/{studentId}/mute")
    Call<ApiResponse<Object>> muteAuthorById(
            @Header("Authorization") String token,
            @Path("studentId") int studentId
    );

    @DELETE("api/community/users/{studentId}/mute")
    Call<ApiResponse<Object>> unmuteAuthorById(
            @Header("Authorization") String token,
            @Path("studentId") int studentId
    );

    // =========================
    // SAVED (profile tab)
    // =========================

    @GET("api/community/profile/me/saved")
    Call<ApiResponse<CommunityPostResponse>> getMySavedPosts(
            @Header("Authorization") String token
    );
}
