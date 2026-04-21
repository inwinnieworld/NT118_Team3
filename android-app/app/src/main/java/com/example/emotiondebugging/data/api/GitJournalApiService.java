package com.example.emotiondebugging.data.api;

import com.example.emotiondebugging.model.request.CreateCommitRequest;
import com.example.emotiondebugging.model.response.CreateCommitResponse;
import com.example.emotiondebugging.model.response.EmotionsResponse;
import com.example.emotiondebugging.model.response.GitGraphResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GitJournalApiService {

    /**
     * Get all emotions from backend
     */
    @GET("api/gitjournal/emotions")
    Call<EmotionsResponse> getEmotions(
            @Header("Authorization") String token
    );

    /**
     * Create a new commit
     */
    @POST("api/gitjournal/commits")
    Call<CreateCommitResponse> createCommit(
            @Header("Authorization") String token,
            @Body CreateCommitRequest request
    );

    /**
     * Get Git Graph data (commits + merges)
     */
    @GET("api/gitjournal/graph")
    Call<GitGraphResponse> getGitGraphData(
            @Header("Authorization") String token,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate,
            @Query("limit") Integer limit,
            @Query("offset") Integer offset
    );
}
