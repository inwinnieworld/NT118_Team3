package com.example.emotiondebugging.data.repository;

import com.example.emotiondebugging.data.api.CommunityApiService;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.request.CreatePostRequest;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.model.response.CommunityPostResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityRepository {

    private final CommunityApiService api = RetrofitClient.getCommunityApi();

    public interface RepositoryCallback<T> {
        void onSuccess(T data, String message);
        void onError(String message);
    }

    public void getPosts(String token, String filter, int page, String search,
                         Integer errorTypeId, RepositoryCallback<CommunityPostResponse> callback) {
        api.getPosts(token, filter, page, search, errorTypeId)
                .enqueue(new Callback<ApiResponse<CommunityPostResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<CommunityPostResponse>> call,
                                           Response<ApiResponse<CommunityPostResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<CommunityPostResponse> body = response.body();
                            if (body.isSuccess()) callback.onSuccess(body.getData(), body.getMessage());
                            else callback.onError(body.getMessage());
                        } else {
                            callback.onError("Tải bài viết thất bại");
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<CommunityPostResponse>> call, Throwable t) {
                        android.util.Log.e("CommunityRepo", "getPosts onFailure: " + t.getMessage(), t);
                        callback.onError("Lỗi kết nối server: " + t.getMessage());
                    }
                });
    }

    public void getErrorTypes(String token, RepositoryCallback<List<Map<String, Object>>> callback) {
        api.getErrorTypes(token).enqueue(new Callback<ApiResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Map<String, Object>>>> call,
                                   Response<ApiResponse<List<Map<String, Object>>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Map<String, Object>>> body = response.body();
                    if (body.isSuccess()) callback.onSuccess(body.getData(), body.getMessage());
                    else callback.onError(body.getMessage());
                } else {
                    callback.onError("Tải danh sách thất bại");
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<Map<String, Object>>>> call, Throwable t) {
                callback.onError("Lỗi kết nối server");
            }
        });
    }

    public void createPost(String token, CreatePostRequest request,
                           RepositoryCallback<Map<String, Object>> callback) {
        api.createPost(token, request).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call,
                                   Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Map<String, Object>> body = response.body();
                    if (body.isSuccess()) callback.onSuccess(body.getData(), body.getMessage());
                    else callback.onError(body.getMessage());
                } else {
                    callback.onError("Đăng bài thất bại");
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                callback.onError("Lỗi kết nối server");
            }
        });
    }

    public void votePost(String token, int postId, String voteType,
                         RepositoryCallback<Object> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("vote_type", voteType);
        api.votePost(token, postId, body).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> b = response.body();
                    if (b.isSuccess()) callback.onSuccess(null, b.getMessage());
                    else callback.onError(b.getMessage());
                } else {
                    callback.onError("Vote thất bại");
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                callback.onError("Lỗi kết nối server");
            }
        });
    }
}
