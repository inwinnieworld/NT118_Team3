package com.example.emotiondebugging.data.repository;

import com.example.emotiondebugging.data.api.AuthApiService;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.request.ForgotPasswordRequest;
import com.example.emotiondebugging.model.request.LoginRequest;
import com.example.emotiondebugging.model.request.RegisterRequest;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.model.response.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final AuthApiService authApiService = RetrofitClient.getAuthApiService();

    public interface RepositoryCallback<T> {
        void onSuccess(T data, String message);
        void onError(String message);
    }

    public void login(LoginRequest request, RepositoryCallback<LoginResponse> callback) {
        authApiService.login(request).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<LoginResponse> body = response.body();
                    if (body.isSuccess()) {
                        callback.onSuccess(body.getData(), body.getMessage());
                    } else {
                        callback.onError(body.getMessage());
                    }
                } else {
                    callback.onError("Đăng nhập thất bại");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Lỗi kết nối server");
            }
        });
    }

    public void register(RegisterRequest request, RepositoryCallback<Object> callback) {
        authApiService.register(request).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> body = response.body();
                    if (body.isSuccess()) {
                        callback.onSuccess(body.getData(), body.getMessage());
                    } else {
                        callback.onError(body.getMessage());
                    }
                } else {
                    callback.onError("Đăng ký thất bại");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Lỗi kết nối server");
            }
        });
    }

    public void forgotPassword(ForgotPasswordRequest request, RepositoryCallback<Object> callback) {
        authApiService.forgotPassword(request).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> body = response.body();
                    if (body.isSuccess()) {
                        callback.onSuccess(body.getData(), body.getMessage());
                    } else {
                        callback.onError(body.getMessage());
                    }
                } else {
                    callback.onError("Gửi yêu cầu thất bại");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Lỗi kết nối server");
            }
        });
    }
}