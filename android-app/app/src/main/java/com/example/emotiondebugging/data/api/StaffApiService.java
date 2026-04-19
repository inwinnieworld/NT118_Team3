package com.example.emotiondebugging.data.api;

import com.example.emotiondebugging.model.request.CreateQuestRequest;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.model.response.QuestResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface StaffApiService {

    @GET("api/staff/quests")
    Call<ApiResponse<List<QuestResponse>>> getAllQuests();

    @POST("api/staff/quests")
    Call<ApiResponse<QuestResponse>> createQuest(@Body CreateQuestRequest request);

    @DELETE("api/staff/quests/{questId}")
    Call<ApiResponse<Object>> deleteQuest(@Path("questId") int questId);
}