package com.example.emotiondebugging.data.api;

import com.example.emotiondebugging.model.response.AiChatModels.MessageData;
import com.example.emotiondebugging.model.response.AiChatModels.SessionDetail;
import com.example.emotiondebugging.model.response.AiChatModels.SessionSummary;
import com.example.emotiondebugging.model.response.AiChatModels.StartSessionData;
import com.example.emotiondebugging.model.response.ApiResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * API cho luồng AI Chat (Dr.Bug). Map 1-1 với routes backend /api/aichat/*.
 * Tất cả endpoint đều cần header Authorization: Bearer <token>.
 */
public interface AiChatApiService {

    /** Mở UI chat: trả lời chào + gợi ý Tầng 2. KHÔNG tạo session. */
    @POST("api/aichat/sessions/start")
    Call<ApiResponse<StartSessionData>> startSession(
            @Header("Authorization") String token
    );

    /**
     * Gửi 1 lượt chat. Body: { session_id?, text, picked_problem_id? }.
     * session_id null = lượt đầu (backend sẽ tạo session).
     */
    @POST("api/aichat/messages")
    Call<ApiResponse<MessageData>> sendMessage(
            @Header("Authorization") String token,
            @Body Map<String, Object> body
    );

    /** Danh sách session của user (màn 2). */
    @GET("api/aichat/sessions")
    Call<ApiResponse<List<SessionSummary>>> getSessions(
            @Header("Authorization") String token
    );

    /** Load lại 1 session (trả chat_history để render). */
    @GET("api/aichat/sessions/{id}")
    Call<ApiResponse<SessionDetail>> getSession(
            @Header("Authorization") String token,
            @Path("id") int sessionId
    );

    /** Kết thúc session → backend sinh title. */
    @POST("api/aichat/sessions/{id}/end")
    Call<ApiResponse<SessionSummary>> endSession(
            @Header("Authorization") String token,
            @Path("id") int sessionId
    );

    /** User chọn vấn đề ưu tiên sau popup select_priority. Body: { problem_id }. */
    @POST("api/aichat/sessions/{id}/priority")
    Call<ApiResponse<MessageData>> pickPriority(
            @Header("Authorization") String token,
            @Path("id") int sessionId,
            @Body Map<String, String> body
    );

    /** User chọn hướng ở lượt 4.2 sau popup select_route. Body: { route: "quest" | "community" }. */
    @POST("api/aichat/sessions/{id}/route")
    Call<ApiResponse<MessageData>> pickRoute(
            @Header("Authorization") String token,
            @Path("id") int sessionId,
            @Body Map<String, String> body
    );
}
