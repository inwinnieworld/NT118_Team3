package com.example.emotiondebugging.data.api;

import com.example.emotiondebugging.model.request.ChangePasswordRequest;
import com.example.emotiondebugging.model.request.UpdateProfileRequest;
import com.example.emotiondebugging.model.response.ProfileResponse;
import com.example.emotiondebugging.model.response.BaseResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface ProfileApiService {

    @GET("api/profile")
    Call<ProfileResponse> getProfile(@Header("Authorization") String token);

    @PUT("api/profile")
    Call<BaseResponse> updateProfile(
            @Header("Authorization") String token,
            @Body UpdateProfileRequest request
    );

    @Multipart
    @POST("api/profile/avatar")
    Call<BaseResponse> uploadAvatar(
            @Header("Authorization") String token,
            @Part MultipartBody.Part avatar
    );

    @PUT("api/profile/change-password")
    Call<BaseResponse> changePassword(
            @Header("Authorization") String token,
            @Body ChangePasswordRequest request
    );
}
