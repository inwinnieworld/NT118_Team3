package com.example.emotiondebugging.data.api;

import com.example.emotiondebugging.model.domain.QuestEngine;
import com.example.emotiondebugging.model.domain.QuestCategory;
import com.example.emotiondebugging.model.request.QuestDraftRequest;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.model.response.QuestDraftDetail;
import com.example.emotiondebugging.model.response.QuestDraftSummary;
import com.example.emotiondebugging.model.response.QuestMonthlyMetricResponse;
import com.example.emotiondebugging.model.response.QuestRankingBoardResponse;
import com.example.emotiondebugging.model.response.QuestNodeMetricResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface QuestBuilderApiService {

    @GET("api/quest-builder/engines")
    Call<ApiResponse<List<QuestEngine>>> getEngines(
            @Header("Authorization") String token
    );

    @Multipart
    @POST("api/quest-builder/media")
    Call<ApiResponse<Map<String, Object>>> uploadMedia(
            @Header("Authorization") String token,
            @Part MultipartBody.Part media
    );

    @GET("api/quest-builder/categories")
    Call<ApiResponse<List<QuestCategory>>> getCategories(
            @Header("Authorization") String token
    );

    @POST("api/quest-builder/quests/draft")
    Call<ApiResponse<Map<String, Object>>> saveDraft(
            @Header("Authorization") String token,
            @Body QuestDraftRequest request
    );

    @GET("api/quest-builder/quests")
    Call<ApiResponse<List<QuestDraftSummary>>> getDrafts(
            @Header("Authorization") String token,
            @Query("status") String status
    );

    @GET("api/quest-builder/catalog")
    Call<ApiResponse<List<QuestDraftSummary>>> getApprovedCatalog(
            @Header("Authorization") String token,
            @Query("error_type_id") Integer errorTypeId
    );

    @GET("api/quest-builder/versions/{versionId}")
    Call<ApiResponse<QuestDraftDetail>> getDraftVersion(
            @Header("Authorization") String token,
            @Path("versionId") int versionId
    );

    @POST("api/quest-builder/quests/{questId}/submit-review")
    Call<ApiResponse<Map<String, Object>>> submitReview(
            @Header("Authorization") String token,
            @Path("questId") int questId
    );

    @DELETE("api/quest-builder/quests/{questId}/draft")
    Call<ApiResponse<Object>> deleteDraft(
            @Header("Authorization") String token,
            @Path("questId") int questId
    );

    @GET("api/quest-builder/reports/monthly")
    Call<ApiResponse<List<QuestMonthlyMetricResponse>>> getOwnMonthlyReport(
            @Header("Authorization") String token
    );

    @GET("api/quest-builder/reports/ranking")
    Call<ApiResponse<List<QuestRankingBoardResponse>>> getOwnRankingReport(
            @Header("Authorization") String token
    );

    @GET("api/quest-builder/reports/events")
    Call<ApiResponse<List<QuestNodeMetricResponse>>> getOwnEventReport(
            @Header("Authorization") String token
    );

    @POST("api/quest-builder/quests/{questId}/review")
    Call<ApiResponse<Map<String, Object>>> reviewQuest(
            @Header("Authorization") String token,
            @Path("questId") int questId,
            @Body Map<String, Object> request
    );

    @POST("api/quest-builder/quests/{questId}/visibility")
    Call<ApiResponse<Map<String, Object>>> updateQuestVisibility(
            @Header("Authorization") String token,
            @Path("questId") int questId,
            @Body Map<String, Object> request
    );

    @GET("api/quest-builder/quests/{questId}/approved-flow")
    Call<ApiResponse<QuestDraftDetail>> getApprovedFlow(
            @Header("Authorization") String token,
            @Path("questId") int questId
    );

    @POST("api/quest-builder/runs/start")
    Call<ApiResponse<Map<String, Object>>> startQuestRun(
            @Header("Authorization") String token,
            @Body Map<String, Object> request
    );

    @POST("api/quest-builder/runs/{runId}/events")
    Call<ApiResponse<Object>> appendRunEvent(
            @Header("Authorization") String token,
            @Path("runId") int runId,
            @Body Map<String, Object> request
    );

    @POST("api/quest-builder/runs/{runId}/finish")
    Call<ApiResponse<Object>> finishQuestRun(
            @Header("Authorization") String token,
            @Path("runId") int runId,
            @Body Map<String, Object> request
    );
}
