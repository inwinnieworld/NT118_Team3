package com.example.emotiondebugging.data.api;

import com.example.emotiondebugging.model.request.ForgotPasswordRequest;
import com.example.emotiondebugging.model.request.LoginRequest;
import com.example.emotiondebugging.model.request.RegisterRequest;
import com.example.emotiondebugging.model.request.ResetPasswordRequest;
import com.example.emotiondebugging.model.request.ValidateResetTokenRequest;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.model.response.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST("api/auth/register")
    Call<ApiResponse<Object>> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest request);

    @POST("api/auth/forgot-password")
    Call<ApiResponse<Object>> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("api/auth/validate-reset-token")
    Call<ApiResponse<Object>> validateResetToken(@Body ValidateResetTokenRequest request);

    @POST("api/auth/reset-password")
    Call<ApiResponse<Object>> resetPassword(@Body ResetPasswordRequest request);
}