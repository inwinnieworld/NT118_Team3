package com.example.emotiondebugging.ui.gitgraph;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.data.api.GitJournalApiService;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.response.GitGraphResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ViewModel for Git Graph
 */
public class GitGraphViewModel extends ViewModel {

    private final GitJournalApiService apiService;

    private final MutableLiveData<GitGraphResponse.GraphData> _graphData = new MutableLiveData<>();
    public LiveData<GitGraphResponse.GraphData> getGraphData() {
        return _graphData;
    }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    public LiveData<Boolean> getLoading() {
        return _loading;
    }

    public GitGraphViewModel() {
        this.apiService = RetrofitClient.getInstance().create(GitJournalApiService.class);
    }

    /**
     * Load Git Graph data from backend
     */
    public void loadGraphData(String token, String startDate, String endDate, Integer limit, Integer offset) {
        _loading.setValue(true);

        String authHeader = "Bearer " + token;

        apiService.getGitGraphData(authHeader, startDate, endDate, limit, offset)
                .enqueue(new Callback<GitGraphResponse>() {
                    @Override
                    public void onResponse(Call<GitGraphResponse> call, Response<GitGraphResponse> response) {
                        _loading.postValue(false);

                        if (response.isSuccessful() && response.body() != null) {
                            GitGraphResponse graphResponse = response.body();
                            if (graphResponse.isSuccess() && graphResponse.getData() != null) {
                                _graphData.postValue(graphResponse.getData());
                                android.util.Log.d("GitGraphViewModel", 
                                        "Loaded " + graphResponse.getData().getTotal_commits() + " commits, " +
                                        graphResponse.getData().getTotal_merges() + " merges");
                            } else {
                                _errorMessage.postValue("Failed to load graph data: " + graphResponse.getMessage());
                            }
                        } else {
                            _errorMessage.postValue("Failed to load graph data: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<GitGraphResponse> call, Throwable t) {
                        _loading.postValue(false);
                        _errorMessage.postValue("Network error: " + t.getMessage());
                        android.util.Log.e("GitGraphViewModel", "Error loading graph data", t);
                    }
                });
    }
}
