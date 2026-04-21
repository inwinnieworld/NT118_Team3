package com.example.emotiondebugging.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.emotiondebugging.model.response.QuestTrendReportResponse;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.data.api.StaffApiService;
import com.example.emotiondebugging.model.request.CreateQuestRequest;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.model.response.QuestRankingReportResponse;
import com.example.emotiondebugging.model.response.QuestResponse;
import com.example.emotiondebugging.model.response.QuestSummaryReportResponse;
import com.example.emotiondebugging.model.request.CreateTraceQuestionRequest;
import com.example.emotiondebugging.model.request.UpdateTraceQuestionRequest;
import com.example.emotiondebugging.model.response.TraceQuestionResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.emotiondebugging.model.response.QuestMonthlyMetricResponse;
import com.example.emotiondebugging.model.response.QuestRankingBoardResponse;
public class StaffRepository {

    private final StaffApiService apiService =
            RetrofitClient.getInstance().create(StaffApiService.class);

    public void getAllQuests(MutableLiveData<List<QuestResponse>> result,
                             MutableLiveData<String> message,
                             MutableLiveData<Boolean> loading) {
        loading.postValue(true);

        apiService.getAllQuests().enqueue(new Callback<ApiResponse<List<QuestResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<QuestResponse>>> call,
                                   Response<ApiResponse<List<QuestResponse>>> response) {
                loading.postValue(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.postValue(response.body().getData());
                } else {
                    message.postValue("Không lấy được danh sách quest");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<QuestResponse>>> call, Throwable t) {
                loading.postValue(false);
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

        apiService.deleteQuest(questId).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call,
                                   Response<ApiResponse<Object>> response) {
                loading.postValue(false);

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
                message.postValue(t.getMessage());
            }
        });
    }

    public void getQuestSummaryReport(MutableLiveData<QuestSummaryReportResponse> result,
                                      MutableLiveData<String> message,
                                      MutableLiveData<Boolean> loading) {
        loading.postValue(true);

        apiService.getQuestSummaryReport().enqueue(new Callback<ApiResponse<QuestSummaryReportResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<QuestSummaryReportResponse>> call,
                                   Response<ApiResponse<QuestSummaryReportResponse>> response) {
                loading.postValue(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.postValue(response.body().getData());
                } else {
                    message.postValue("Không lấy được báo cáo tổng quan");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<QuestSummaryReportResponse>> call, Throwable t) {
                loading.postValue(false);
                message.postValue(t.getMessage());
            }
        });
    }

    public void getQuestRankingReport(MutableLiveData<List<QuestRankingReportResponse>> result,
                                      MutableLiveData<String> message,
                                      MutableLiveData<Boolean> loading) {
        loading.postValue(true);

        apiService.getQuestRankingReport().enqueue(new Callback<ApiResponse<List<QuestRankingReportResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<QuestRankingReportResponse>>> call,
                                   Response<ApiResponse<List<QuestRankingReportResponse>>> response) {
                loading.postValue(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.postValue(response.body().getData());
                } else {
                    message.postValue("Không lấy được BXH quest");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<QuestRankingReportResponse>>> call, Throwable t) {
                loading.postValue(false);
                message.postValue(t.getMessage());
            }
        });
    }

    public void getQuestTrendReport(MutableLiveData<QuestTrendReportResponse> result,
                                    MutableLiveData<String> message,
                                    MutableLiveData<Boolean> loading) {
        loading.postValue(true);

        apiService.getQuestTrendReport().enqueue(new Callback<ApiResponse<QuestTrendReportResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<QuestTrendReportResponse>> call,
                                   Response<ApiResponse<QuestTrendReportResponse>> response) {
                loading.postValue(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.postValue(response.body().getData());
                } else {
                    message.postValue("Không lấy được dữ liệu biểu đồ quest");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<QuestTrendReportResponse>> call, Throwable t) {
                loading.postValue(false);
                message.postValue(t.getMessage());
            }
        });
    }

    public void getAllTraceQuestions(MutableLiveData<List<TraceQuestionResponse>> result,
                                     MutableLiveData<String> message,
                                     MutableLiveData<Boolean> loading) {
        loading.postValue(true);

        apiService.getAllTraceQuestions().enqueue(new Callback<ApiResponse<List<TraceQuestionResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<TraceQuestionResponse>>> call,
                                   Response<ApiResponse<List<TraceQuestionResponse>>> response) {
                loading.postValue(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.postValue(response.body().getData());
                } else {
                    message.postValue("Không lấy được danh sách câu hỏi trace");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<TraceQuestionResponse>>> call, Throwable t) {
                loading.postValue(false);
                message.postValue(t.getMessage());
            }
        });
    }

    public void getTraceQuestionDetail(int questionId,
                                       MutableLiveData<TraceQuestionResponse> result,
                                       MutableLiveData<String> message,
                                       MutableLiveData<Boolean> loading) {
        loading.postValue(true);

        apiService.getTraceQuestionDetail(questionId).enqueue(new Callback<ApiResponse<TraceQuestionResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<TraceQuestionResponse>> call,
                                   Response<ApiResponse<TraceQuestionResponse>> response) {
                loading.postValue(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.postValue(response.body().getData());
                } else {
                    message.postValue("Không lấy được chi tiết câu hỏi");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<TraceQuestionResponse>> call, Throwable t) {
                loading.postValue(false);
                message.postValue(t.getMessage());
            }
        });
    }

    public void createTraceQuestion(CreateTraceQuestionRequest request,
                                    MutableLiveData<Boolean> success,
                                    MutableLiveData<String> message,
                                    MutableLiveData<Boolean> loading) {
        loading.postValue(true);

        apiService.createTraceQuestion(request).enqueue(new Callback<ApiResponse<TraceQuestionResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<TraceQuestionResponse>> call,
                                   Response<ApiResponse<TraceQuestionResponse>> response) {
                loading.postValue(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    success.postValue(true);
                    message.postValue(response.body().getMessage());
                } else {
                    success.postValue(false);
                    message.postValue("Tạo câu hỏi thất bại");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<TraceQuestionResponse>> call, Throwable t) {
                loading.postValue(false);
                success.postValue(false);
                message.postValue(t.getMessage());
            }
        });
    }

    public void updateTraceQuestion(int questionId,
                                    UpdateTraceQuestionRequest request,
                                    MutableLiveData<Boolean> success,
                                    MutableLiveData<String> message,
                                    MutableLiveData<Boolean> loading) {
        loading.postValue(true);

        apiService.updateTraceQuestion(questionId, request).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call,
                                   Response<ApiResponse<Object>> response) {
                loading.postValue(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    success.postValue(true);
                    message.postValue(response.body().getMessage());
                } else {
                    success.postValue(false);
                    message.postValue("Cập nhật câu hỏi thất bại");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                loading.postValue(false);
                success.postValue(false);
                message.postValue(t.getMessage());
            }
        });
    }

    public void deleteTraceQuestion(int questionId,
                                    MutableLiveData<Boolean> success,
                                    MutableLiveData<String> message,
                                    MutableLiveData<Boolean> loading) {
        loading.postValue(true);

        apiService.deleteTraceQuestion(questionId).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call,
                                   Response<ApiResponse<Object>> response) {
                loading.postValue(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    success.postValue(true);
                    message.postValue(response.body().getMessage());
                } else {
                    success.postValue(false);
                    message.postValue("Xóa câu hỏi thất bại");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                loading.postValue(false);
                success.postValue(false);
                message.postValue(t.getMessage());
            }
        });
    }

    public void getQuestMonthlyMetrics(MutableLiveData<List<QuestMonthlyMetricResponse>> result,
                                       MutableLiveData<String> message,
                                       MutableLiveData<Boolean> loading) {
        loading.postValue(true);

        apiService.getQuestMonthlyMetrics().enqueue(new Callback<ApiResponse<List<QuestMonthlyMetricResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<QuestMonthlyMetricResponse>>> call,
                                   Response<ApiResponse<List<QuestMonthlyMetricResponse>>> response) {
                loading.postValue(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.postValue(response.body().getData());
                } else {
                    message.postValue("Không lấy được chỉ số theo tháng");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<QuestMonthlyMetricResponse>>> call, Throwable t) {
                loading.postValue(false);
                message.postValue(t.getMessage());
            }
        });
    }

    public void getQuestRankingBoard(MutableLiveData<List<QuestRankingBoardResponse>> result,
                                     MutableLiveData<String> message,
                                     MutableLiveData<Boolean> loading) {
        loading.postValue(true);

        apiService.getQuestRankingBoard().enqueue(new Callback<ApiResponse<List<QuestRankingBoardResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<QuestRankingBoardResponse>>> call,
                                   Response<ApiResponse<List<QuestRankingBoardResponse>>> response) {
                loading.postValue(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.postValue(response.body().getData());
                } else {
                    message.postValue("Không lấy được BXH quest");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<QuestRankingBoardResponse>>> call, Throwable t) {
                loading.postValue(false);
                message.postValue(t.getMessage());
            }
        });
    }
}