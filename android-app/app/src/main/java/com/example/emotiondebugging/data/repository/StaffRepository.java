package com.example.emotiondebugging.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.data.api.StaffApiService;
import com.example.emotiondebugging.model.request.CreateQuestRequest;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.model.response.QuestResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffRepository {

    private final StaffApiService apiService =
            RetrofitClient.getInstance().create(StaffApiService.class);

    public void getAllQuests(MutableLiveData<List<QuestResponse>> result,
                             MutableLiveData<String> message,
                             MutableLiveData<Boolean> loading) {
        loading.postValue(true);
        android.util.Log.d("QUEST_DEBUG", "Repository getAllQuests start");

        apiService.getAllQuests().enqueue(new Callback<ApiResponse<List<QuestResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<QuestResponse>>> call,
                                   Response<ApiResponse<List<QuestResponse>>> response) {
                loading.postValue(false);

                android.util.Log.d("QUEST_DEBUG", "HTTP code = " + response.code());
                android.util.Log.d("QUEST_DEBUG", "body = " + response.body());

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.postValue(response.body().getData());
                } else {
                    message.postValue("Không lấy được danh sách quest");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<QuestResponse>>> call, Throwable t) {
                loading.postValue(false);
                android.util.Log.e("QUEST_DEBUG", "onFailure", t);
                message.postValue(t.getMessage());
            }
        });
    }

    public void createQuest(CreateQuestRequest request,
                            MutableLiveData<Boolean> success,
                            MutableLiveData<String> message,
                            MutableLiveData<Boolean> loading) {
        loading.postValue(true);

        apiService.createQuest(request).enqueue(new Callback<ApiResponse<QuestResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<QuestResponse>> call,
                                   Response<ApiResponse<QuestResponse>> response) {
                loading.postValue(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    success.postValue(true);
                    message.postValue(response.body().getMessage());
                } else {
                    success.postValue(false);
                    message.postValue("Tạo quest thất bại");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<QuestResponse>> call, Throwable t) {
                loading.postValue(false);
                success.postValue(false);
                message.postValue(t.getMessage());
            }
        });
    }

    public void deleteQuest(int questId,
                            MutableLiveData<Boolean> deleteSuccess,
                            MutableLiveData<String> message,
                            MutableLiveData<Boolean> loading) {
        loading.postValue(true);
        android.util.Log.d("QUEST_DEBUG", "Repository deleteQuest start, id = " + questId);

        apiService.deleteQuest(questId).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call,
                                   Response<ApiResponse<Object>> response) {
                loading.postValue(false);

                android.util.Log.d("QUEST_DEBUG", "delete HTTP code = " + response.code());
                android.util.Log.d("QUEST_DEBUG", "delete body = " + response.body());

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    deleteSuccess.postValue(true);
                    message.postValue(response.body().getMessage());
                } else {
                    deleteSuccess.postValue(false);
                    message.postValue("Xóa quest thất bại");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                loading.postValue(false);
                deleteSuccess.postValue(false);
                android.util.Log.e("QUEST_DEBUG", "delete onFailure", t);
                message.postValue(t.getMessage());
            }
        });
    }
}