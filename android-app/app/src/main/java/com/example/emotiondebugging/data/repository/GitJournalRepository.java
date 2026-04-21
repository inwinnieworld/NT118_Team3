package com.example.emotiondebugging.data.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.emotiondebugging.data.api.GitJournalApiService;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.Emotion;
import com.example.emotiondebugging.model.request.CreateCommitRequest;
import com.example.emotiondebugging.model.response.CreateCommitResponse;
import com.example.emotiondebugging.model.response.EmotionsResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GitJournalRepository {

    private static final String TAG = "GitJournalRepository";
    private final GitJournalApiService apiService;

    // Cache for emotions (emotion_name -> emotion_id mapping)
    private Map<String, Integer> emotionNameToIdMap = new HashMap<>();

    public GitJournalRepository() {
        this.apiService = RetrofitClient.getInstance().create(GitJournalApiService.class);
    }

    /**
     * Load emotions from backend and cache the mapping
     */
    public void loadEmotions(String token, MutableLiveData<List<Emotion>> emotionsLiveData, MutableLiveData<String> errorLiveData) {
        String authHeader = "Bearer " + token;

        apiService.getEmotions(authHeader).enqueue(new Callback<EmotionsResponse>() {
            @Override
            public void onResponse(Call<EmotionsResponse> call, Response<EmotionsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EmotionsResponse emotionsResponse = response.body();
                    if (emotionsResponse.isSuccess() && emotionsResponse.getData() != null) {
                        List<Emotion> emotions = emotionsResponse.getData();
                        
                        // Build emotion name -> id mapping
                        emotionNameToIdMap.clear();
                        for (Emotion emotion : emotions) {
                            emotionNameToIdMap.put(emotion.getEmotion_name(), emotion.getEmotion_id());
                        }
                        
                        emotionsLiveData.postValue(emotions);
                        Log.d(TAG, "Loaded " + emotions.size() + " emotions from backend");
                    } else {
                        errorLiveData.postValue("Failed to load emotions: " + emotionsResponse.getMessage());
                    }
                } else {
                    errorLiveData.postValue("Failed to load emotions: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<EmotionsResponse> call, Throwable t) {
                Log.e(TAG, "Error loading emotions", t);
                errorLiveData.postValue("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Get emotion_id from emotion_name
     */
    public Integer getEmotionId(String emotionName) {
        return emotionNameToIdMap.get(emotionName);
    }

    /**
     * Create a new commit
     */
    public void createCommit(String token, String emotionName, int intensityLevel, String message,
                             MutableLiveData<CreateCommitResponse> commitResponseLiveData,
                             MutableLiveData<String> errorLiveData) {
        
        // Get emotion_id from name
        Integer emotionId = getEmotionId(emotionName);
        if (emotionId == null) {
            errorLiveData.postValue("Không tìm thấy emotion_id cho: " + emotionName);
            return;
        }

        // Create request (main branch only for now)
        CreateCommitRequest request = new CreateCommitRequest(emotionId, intensityLevel, message);
        String authHeader = "Bearer " + token;

        apiService.createCommit(authHeader, request).enqueue(new Callback<CreateCommitResponse>() {
            @Override
            public void onResponse(Call<CreateCommitResponse> call, Response<CreateCommitResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CreateCommitResponse commitResponse = response.body();
                    if (commitResponse.isSuccess()) {
                        commitResponseLiveData.postValue(commitResponse);
                        Log.d(TAG, "Commit created successfully");
                        
                        // Check for severity alert
                        if (commitResponse.getData() != null && commitResponse.getData().getAlert() != null) {
                            CreateCommitResponse.SeverityAlert alert = commitResponse.getData().getAlert();
                            if (alert.isShouldAlert()) {
                                Log.w(TAG, "Severity Alert: " + alert.getMessage());
                            }
                        }
                    } else {
                        errorLiveData.postValue("Failed to create commit: " + commitResponse.getMessage());
                    }
                } else {
                    errorLiveData.postValue("Failed to create commit: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CreateCommitResponse> call, Throwable t) {
                Log.e(TAG, "Error creating commit", t);
                errorLiveData.postValue("Network error: " + t.getMessage());
            }
        });
    }
}
