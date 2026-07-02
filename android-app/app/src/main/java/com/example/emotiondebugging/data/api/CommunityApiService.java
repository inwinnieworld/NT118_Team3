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
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import com.example.emotiondebugging.model.chat.ChatHistoryResponse;
import com.example.emotiondebugging.model.community.FollowListResponse;
import com.example.emotiondebugging.model.response.ChatConversationResponse;
public interface CommunityApiService {

    @GET("api/community/posts")
    Call<ApiResponse<CommunityPostResponse>> getPosts(
            @Header("Authorization") String token,
            @Query("filter") String filter,
            @Query("page") int page,
            @Query("search") String search,
            @Query("error_type_id") Integer errorTypeId
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
    Call<ApiResponse<List<Map<String, Object>>>> getPostTopics(
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
}
