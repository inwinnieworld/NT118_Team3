package com.example.emotiondebugging.data.api;

import com.example.emotiondebugging.model.request.CreateStaffRequest;
import com.example.emotiondebugging.model.request.UpdateStudentRequest;
import com.example.emotiondebugging.model.request.UpdateStaffRequest;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.model.response.BaseResponse;
import com.example.emotiondebugging.model.response.StudentListResponse;
import com.example.emotiondebugging.model.response.StaffListResponse;
import com.example.emotiondebugging.model.community.AdminReportResponse;
import com.example.emotiondebugging.model.community.AdminReviewRequestResponse;

import java.util.Map;

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

    // ==================== QUẢN LÝ CỘNG ĐỒNG ====================

    @GET("api/admin/community/reports")
    Call<ApiResponse<AdminReportResponse>> getCommunityReports(
            @Header("Authorization") String token,
            @Query("type") String type,
            @Query("status") String status
    );

    @POST("api/admin/community/reports/post/{postId}/resolve")
    Call<ApiResponse<Object>> resolvePostReport(
            @Header("Authorization") String token,
            @Path("postId") int postId,
            @Body Map<String, String> body
    );

    @POST("api/admin/community/reports/comment/{commentId}/resolve")
    Call<ApiResponse<Object>> resolveCommentReport(
            @Header("Authorization") String token,
            @Path("commentId") int commentId,
            @Body Map<String, String> body
    );

    @GET("api/admin/community/review-requests")
    Call<ApiResponse<AdminReviewRequestResponse>> getReviewRequests(
            @Header("Authorization") String token,
            @Query("status") String status
    );

    @POST("api/admin/community/review-requests/{requestId}/resolve")
    Call<ApiResponse<Object>> resolveReviewRequest(
            @Header("Authorization") String token,
            @Path("requestId") int requestId,
            @Body Map<String, String> body
    );
}
