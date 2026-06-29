package com.example.emotiondebugging.data.repository;

import android.util.Log;
import com.example.emotiondebugging.data.api.QuestBuilderApiService;
import com.example.emotiondebugging.data.api.RetrofitClient;
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

import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuestBuilderRepository {

    private static final String TAG = "QuestRunEvents";

    private final QuestBuilderApiService api = RetrofitClient.getQuestBuilderApi();

    public interface RepositoryCallback<T> {
        void onSuccess(T data, String message);
        void onError(String message);
    }

    public void getEngines(String token, RepositoryCallback<List<QuestEngine>> callback) {
        api.getEngines(token).enqueue(new Callback<ApiResponse<List<QuestEngine>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<QuestEngine>>> call,
                                   Response<ApiResponse<List<QuestEngine>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<QuestEngine>> body = response.body();
                    if (body.isSuccess()) callback.onSuccess(body.getData(), body.getMessage());
                    else callback.onError(body.getMessage());
                } else {
                    callback.onError("Tải danh sách engine thất bại");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<QuestEngine>>> call, Throwable t) {
                callback.onError("Lỗi kết nối server: " + t.getMessage());
            }
        });
    }

    public void uploadMedia(String token, File file, String mimeType,
                            RepositoryCallback<String> callback) {
        MediaType mediaType = MediaType.parse(mimeType == null ? "application/octet-stream" : mimeType);
        RequestBody requestBody = RequestBody.create(mediaType, file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("media", file.getName(), requestBody);
        api.uploadMedia(token, part).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call,
                                   Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> data = response.body().getData();
                    Object url = data == null ? null : data.get("media_url");
                    if (url != null) callback.onSuccess(String.valueOf(url), response.body().getMessage());
                    else callback.onError("Upload completed without a media URL");
                } else callback.onError("Media upload failed");
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable throwable) {
                callback.onError("Cannot upload media: " + throwable.getMessage());
            }
        });
    }

    public void getCategories(String token, RepositoryCallback<List<QuestCategory>> callback) {
        api.getCategories(token).enqueue(new Callback<ApiResponse<List<QuestCategory>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<QuestCategory>>> call,
                                   Response<ApiResponse<List<QuestCategory>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<QuestCategory>> body = response.body();
                    if (body.isSuccess()) callback.onSuccess(body.getData(), body.getMessage());
                    else callback.onError(body.getMessage());
                } else {
                    callback.onError("Cannot load emotion categories");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<QuestCategory>>> call, Throwable throwable) {
                callback.onError("Cannot connect to category service: " + throwable.getMessage());
            }
        });
    }

    public void saveDraft(String token, QuestDraftRequest request,
                          RepositoryCallback<Map<String, Object>> callback) {
        api.saveDraft(token, request).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call,
                                   Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Map<String, Object>> body = response.body();
                    if (body.isSuccess()) callback.onSuccess(body.getData(), body.getMessage());
                    else callback.onError(body.getMessage());
                } else {
                    callback.onError("Lưu quest draft thất bại");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                callback.onError("Lỗi kết nối server: " + t.getMessage());
            }
        });
    }

    public void getDrafts(String token, RepositoryCallback<List<QuestDraftSummary>> callback) {
        getQuestsByStatus(token, "draft", callback);
    }

    public void getQuestsByStatus(String token, String status,
                                  RepositoryCallback<List<QuestDraftSummary>> callback) {
        api.getDrafts(token, status).enqueue(new Callback<ApiResponse<List<QuestDraftSummary>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<QuestDraftSummary>>> call,
                                   Response<ApiResponse<List<QuestDraftSummary>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<QuestDraftSummary>> body = response.body();
                    if (body.isSuccess()) callback.onSuccess(body.getData(), body.getMessage());
                    else callback.onError(body.getMessage());
                } else {
                    callback.onError("Cannot load saved drafts");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<QuestDraftSummary>>> call, Throwable throwable) {
                callback.onError("Cannot connect to draft service: " + throwable.getMessage());
            }
        });
    }

    public void getApprovedCatalog(String token, Integer errorTypeId,
                                   RepositoryCallback<List<QuestDraftSummary>> callback) {
        api.getApprovedCatalog(token, errorTypeId).enqueue(new Callback<ApiResponse<List<QuestDraftSummary>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<QuestDraftSummary>>> call,
                                   Response<ApiResponse<List<QuestDraftSummary>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData(), response.body().getMessage());
                } else callback.onError("Cannot load approved quests");
            }

            @Override
            public void onFailure(Call<ApiResponse<List<QuestDraftSummary>>> call, Throwable throwable) {
                callback.onError("Cannot connect to quest catalog: " + throwable.getMessage());
            }
        });
    }

    public void getDraftVersion(String token, int versionId,
                                RepositoryCallback<QuestDraftDetail> callback) {
        api.getDraftVersion(token, versionId).enqueue(new Callback<ApiResponse<QuestDraftDetail>>() {
            @Override
            public void onResponse(Call<ApiResponse<QuestDraftDetail>> call,
                                   Response<ApiResponse<QuestDraftDetail>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<QuestDraftDetail> body = response.body();
                    if (body.isSuccess()) callback.onSuccess(body.getData(), body.getMessage());
                    else callback.onError(body.getMessage());
                } else {
                    callback.onError("Cannot open the selected draft");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<QuestDraftDetail>> call, Throwable throwable) {
                callback.onError("Cannot connect to draft service: " + throwable.getMessage());
            }
        });
    }

    public void submitReview(String token, int questId, RepositoryCallback<Map<String, Object>> callback) {
        api.submitReview(token, questId).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call,
                                   Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Map<String, Object>> body = response.body();
                    if (body.isSuccess()) callback.onSuccess(body.getData(), body.getMessage());
                    else callback.onError(body.getMessage());
                } else {
                    callback.onError("Gửi duyệt quest thất bại");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                callback.onError("Lỗi kết nối server: " + t.getMessage());
            }
        });
    }

    public void deleteDraft(String token, int questId, RepositoryCallback<Object> callback) {
        api.deleteDraft(token, questId).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData(), response.body().getMessage());
                } else callback.onError("Only your draft quests can be deleted");
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable throwable) {
                callback.onError("Cannot connect to quest service: " + throwable.getMessage());
            }
        });
    }

    public void getOwnMonthlyReport(String token, RepositoryCallback<List<QuestMonthlyMetricResponse>> callback) {
        api.getOwnMonthlyReport(token).enqueue(new Callback<ApiResponse<List<QuestMonthlyMetricResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<QuestMonthlyMetricResponse>>> call,
                                   Response<ApiResponse<List<QuestMonthlyMetricResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData(), response.body().getMessage());
                } else callback.onError("Cannot load quest metrics");
            }

            @Override
            public void onFailure(Call<ApiResponse<List<QuestMonthlyMetricResponse>>> call, Throwable throwable) {
                callback.onError("Cannot connect to report service: " + throwable.getMessage());
            }
        });
    }

    public void getOwnRankingReport(String token, RepositoryCallback<List<QuestRankingBoardResponse>> callback) {
        api.getOwnRankingReport(token).enqueue(new Callback<ApiResponse<List<QuestRankingBoardResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<QuestRankingBoardResponse>>> call,
                                   Response<ApiResponse<List<QuestRankingBoardResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData(), response.body().getMessage());
                } else callback.onError("Cannot load quest performance");
            }

            @Override
            public void onFailure(Call<ApiResponse<List<QuestRankingBoardResponse>>> call, Throwable throwable) {
                callback.onError("Cannot connect to report service: " + throwable.getMessage());
            }
        });
    }

    public void getOwnEventReport(String token, RepositoryCallback<List<QuestNodeMetricResponse>> callback) {
        api.getOwnEventReport(token).enqueue(new Callback<ApiResponse<List<QuestNodeMetricResponse>>>() {
            @Override public void onResponse(Call<ApiResponse<List<QuestNodeMetricResponse>>> call,
                                             Response<ApiResponse<List<QuestNodeMetricResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData(), response.body().getMessage());
                } else callback.onError("Cannot load node event report");
            }

            @Override public void onFailure(Call<ApiResponse<List<QuestNodeMetricResponse>>> call,
                                            Throwable throwable) {
                callback.onError("Cannot connect to event report: " + throwable.getMessage());
            }
        });
    }

    public void reviewQuest(String token, int questId, String action, String note,
                            RepositoryCallback<Map<String, Object>> callback) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("action", action);
        body.put("review_note", note == null ? "" : note);
        api.reviewQuest(token, questId, body).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call,
                                   Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData(), response.body().getMessage());
                } else callback.onError("Quest review failed");
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable throwable) {
                callback.onError("Cannot connect to review service: " + throwable.getMessage());
            }
        });
    }

    public void updateQuestVisibility(String token, int questId, boolean active,
                                      RepositoryCallback<Map<String, Object>> callback) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("is_active", active);
        api.updateQuestVisibility(token, questId, body).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call,
                                   Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData(), response.body().getMessage());
                } else callback.onError("Cannot update quest visibility");
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable throwable) {
                callback.onError("Cannot connect to quest service: " + throwable.getMessage());
            }
        });
    }

    public void getApprovedFlow(String token, int questId, RepositoryCallback<QuestDraftDetail> callback) {
        api.getApprovedFlow(token, questId).enqueue(new Callback<ApiResponse<QuestDraftDetail>>() {
            @Override
            public void onResponse(Call<ApiResponse<QuestDraftDetail>> call,
                                   Response<ApiResponse<QuestDraftDetail>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData(), response.body().getMessage());
                } else callback.onError("Cannot open this quest");
            }

            @Override
            public void onFailure(Call<ApiResponse<QuestDraftDetail>> call, Throwable throwable) {
                callback.onError("Cannot connect to quest service: " + throwable.getMessage());
            }
        });
    }

    public void startQuestRun(String token, int questId,
                              RepositoryCallback<Map<String, Object>> callback) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("quest_id", questId);
        api.startQuestRun(token, body).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call,
                                   Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData(), response.body().getMessage());
                } else callback.onError("Cannot start this quest");
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable throwable) {
                callback.onError("Cannot connect to run service: " + throwable.getMessage());
            }
        });
    }

    public void appendRunEvent(String token, int runId, String nodeId, String eventType) {
        appendRunEvent(token, runId, nodeId, eventType, new java.util.HashMap<>());
    }

    public void appendRunEvent(String token, int runId, String nodeId, String eventType,
                               Map<String, Object> eventPayload) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("client_node_id", nodeId);
        body.put("event_type", eventType);
        Map<String, Object> payload = eventPayload == null
                ? new java.util.HashMap<>() : new java.util.HashMap<>(eventPayload);
        payload.put("client_timestamp_ms", System.currentTimeMillis());
        body.put("payload", payload);
        enqueueRunEvent(api.appendRunEvent(token, runId, body), 1);
    }

    private void enqueueRunEvent(Call<ApiResponse<Object>> call, int retriesRemaining) {
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override public void onResponse(Call<ApiResponse<Object>> request,
                                             Response<ApiResponse<Object>> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    Log.w(TAG, "Event rejected by server: HTTP " + response.code());
                }
            }

            @Override public void onFailure(Call<ApiResponse<Object>> request, Throwable throwable) {
                if (retriesRemaining > 0) enqueueRunEvent(request.clone(), retriesRemaining - 1);
                else Log.w(TAG, "Event delivery failed", throwable);
            }
        });
    }

    public void finishQuestRun(String token, int runId, String status,
                               RepositoryCallback<Object> callback) {
        finishQuestRun(token, runId, status, new java.util.HashMap<>(), callback);
    }

    public void finishQuestRun(String token, int runId, String status,
                               Map<String, Object> resultSummary,
                               RepositoryCallback<Object> callback) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("status", status);
        body.put("result_summary", resultSummary == null ? new java.util.HashMap<>() : resultSummary);
        api.finishQuestRun(token, runId, body).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData(), response.body().getMessage());
                } else callback.onError("Cannot finish quest run");
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable throwable) {
                callback.onError("Cannot connect to run service: " + throwable.getMessage());
            }
        });
    }
}
