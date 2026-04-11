package com.example.emotiondebugging.data.api;

import com.example.emotiondebugging.model.request.CreateStaffRequest;
import com.example.emotiondebugging.model.request.UpdateStudentRequest;
import com.example.emotiondebugging.model.request.UpdateStaffRequest;
import com.example.emotiondebugging.model.response.BaseResponse;
import com.example.emotiondebugging.model.response.StudentListResponse;
import com.example.emotiondebugging.model.response.StaffListResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AdminApiService {

    // Sinh viên
    @GET("api/admin/students")
    Call<StudentListResponse> getStudents(
            @Header("Authorization") String token,
            @Query("page") int page,
            @Query("search") String search
    );

    @PUT("api/admin/students/{studentId}")
    Call<BaseResponse> updateStudent(
            @Header("Authorization") String token,
            @Path("studentId") int studentId,
            @Body UpdateStudentRequest request
    );

    @PUT("api/admin/students/{studentId}/toggle-lock")
    Call<BaseResponse> toggleStudentLock(
            @Header("Authorization") String token,
            @Path("studentId") int studentId
    );

    // Nhân viên
    @GET("api/admin/staff")
    Call<StaffListResponse> getStaff(
            @Header("Authorization") String token,
            @Query("page") int page,
            @Query("search") String search
    );

    @POST("api/admin/staff")
    Call<BaseResponse> createStaff(
            @Header("Authorization") String token,
            @Body CreateStaffRequest request
    );

    @PUT("api/admin/staff/{staffId}")
    Call<BaseResponse> updateStaff(
            @Header("Authorization") String token,
            @Path("staffId") int staffId,
            @Body UpdateStaffRequest request
    );

    @PUT("api/admin/staff/{staffId}/toggle-lock")
    Call<BaseResponse> toggleStaffLock(
            @Header("Authorization") String token,
            @Path("staffId") int staffId
    );
}
