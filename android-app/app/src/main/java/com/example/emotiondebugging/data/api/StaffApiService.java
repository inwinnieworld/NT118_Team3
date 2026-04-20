package com.example.emotiondebugging.data.api;

import com.example.emotiondebugging.model.request.CreateQuestRequest;
import com.example.emotiondebugging.model.request.CreateTraceQuestionRequest;
import com.example.emotiondebugging.model.request.UpdateTraceQuestionRequest;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.model.response.QuestRankingReportResponse;
import com.example.emotiondebugging.model.response.QuestResponse;
import com.example.emotiondebugging.model.response.QuestSummaryReportResponse;
import com.example.emotiondebugging.model.response.QuestTrendReportResponse;
import com.example.emotiondebugging.model.response.TraceQuestionResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface StaffApiService {

    @GET("api/staff/quests")
    Call<ApiResponse<List<QuestResponse>>> getAllQuests();

    @POST("api/staff/quests")
    Call<ApiResponse<QuestResponse>> createQuest(@Body CreateQuestRequest request);

    @DELETE("api/staff/quests/{questId}")
    Call<ApiResponse<Object>> deleteQuest(@Path("questId") int questId);

    @GET("api/staff/reports/summary")
    Call<ApiResponse<QuestSummaryReportResponse>> getQuestSummaryReport();

    @GET("api/staff/reports/quests")
    Call<ApiResponse<List<QuestRankingReportResponse>>> getQuestRankingReport();

    @GET("api/staff/reports/quest-trend")
    Call<ApiResponse<QuestTrendReportResponse>> getQuestTrendReport();

    @GET("api/staff/trace-questions")
    Call<ApiResponse<List<TraceQuestionResponse>>> getAllTraceQuestions();

    @GET("api/staff/trace-questions/{questionId}")
    Call<ApiResponse<TraceQuestionResponse>> getTraceQuestionDetail(@Path("questionId") int questionId);

    @POST("api/staff/trace-questions")
    Call<ApiResponse<TraceQuestionResponse>> createTraceQuestion(@Body CreateTraceQuestionRequest request);

    @PUT("api/staff/trace-questions/{questionId}")
    Call<ApiResponse<Object>> updateTraceQuestion(@Path("questionId") int questionId,
                                                  @Body UpdateTraceQuestionRequest request);

    @DELETE("api/staff/trace-questions/{questionId}")
    Call<ApiResponse<Object>> deleteTraceQuestion(@Path("questionId") int questionId);
}