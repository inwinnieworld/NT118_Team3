package com.example.emotiondebugging.ui.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.model.response.ChatConversationResponse;
import com.example.emotiondebugging.ui.community.adapter.ConversationAdapter;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConversationListActivity extends AppCompatActivity {

    private RecyclerView rvConversations;
    private TextView tvEmpty;
    private ConversationAdapter adapter;
    private String authToken;

    private boolean firstLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.util.Log.d("MSG_LIST", "ConversationListActivity onCreate");

        setContentView(R.layout.activity_conversation_list);

        authToken = getValidToken();

        android.util.Log.d("MSG_LIST", "authToken empty = "
                + (authToken == null || authToken.trim().isEmpty()));
        android.util.Log.d("MSG_LIST", "authToken starts Bearer = "
                + (authToken != null && authToken.startsWith("Bearer ")));
        android.util.Log.d("MSG_LIST", "authToken preview = " + getTokenPreview(authToken));

        if (authToken == null || authToken.trim().isEmpty()) {
            Toast.makeText(this, "Token rỗng, cần đăng nhập lại", Toast.LENGTH_LONG).show();
            return;
        }

        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        rvConversations = findViewById(R.id.rv_conversations);
        tvEmpty = findViewById(R.id.tv_empty);

        if (rvConversations == null) {
            Toast.makeText(this, "Không tìm thấy RecyclerView danh sách tin nhắn", Toast.LENGTH_LONG).show();
            android.util.Log.e("MSG_LIST", "rv_conversations is null. Check activity_conversation_list.xml");
            return;
        }

        adapter = new ConversationAdapter(item -> openChat(item));

        rvConversations.setLayoutManager(new LinearLayoutManager(this));
        rvConversations.setAdapter(adapter);

        firstLoaded = true;
        loadConversations();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (firstLoaded) {
            loadConversations();
        }
    }

    private String getValidToken() {
        SharedPrefsHelper prefs = new SharedPrefsHelper(this);

        String savedToken = prefs.getToken();
        String intentToken = getIntent().getStringExtra("auth_token");

        android.util.Log.d("MSG_LIST", "savedToken empty = "
                + (savedToken == null || savedToken.trim().isEmpty()));
        android.util.Log.d("MSG_LIST", "intentToken empty = "
                + (intentToken == null || intentToken.trim().isEmpty()));

        if (savedToken != null && !savedToken.trim().isEmpty()) {
            return normalizeBearerToken(savedToken);
        }

        return normalizeBearerToken(intentToken);
    }

    private void loadConversations() {
        android.util.Log.d("MSG_LIST", "loadConversations called");
        android.util.Log.d("MSG_LIST", "authToken empty = "
                + (authToken == null || authToken.trim().isEmpty()));
        android.util.Log.d("MSG_LIST", "authToken preview = " + getTokenPreview(authToken));

        if (authToken == null || authToken.trim().isEmpty()) {
            Toast.makeText(this, "Token rỗng, cần đăng nhập lại", Toast.LENGTH_LONG).show();
            return;
        }

        RetrofitClient.getCommunityApi()
                .getChatConversations(authToken)
                .enqueue(new Callback<ApiResponse<ChatConversationResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<ChatConversationResponse>> call,
                            Response<ApiResponse<ChatConversationResponse>> response
                    ) {
                        android.util.Log.d("MSG_LIST", "response code = " + response.code());

                        if (response.body() != null) {
                            android.util.Log.d("MSG_LIST", "success = " + response.body().isSuccess());
                            android.util.Log.d("MSG_LIST", "message = " + response.body().getMessage());
                            android.util.Log.d("MSG_LIST", "data null = " + (response.body().getData() == null));
                        }

                        if (
                                response.isSuccessful()
                                        && response.body() != null
                                        && response.body().getData() != null
                                        && response.body().getData().conversations != null
                        ) {
                            adapter.setItems(response.body().getData().conversations);

                            boolean isEmpty = response.body().getData().conversations.isEmpty();

                            if (tvEmpty != null) {
                                tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                            }

                            if (rvConversations != null) {
                                rvConversations.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                            }

                            return;
                        }

                        showConversationApiError(response);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ChatConversationResponse>> call, Throwable t) {
                        android.util.Log.e("MSG_LIST", "onFailure", t);
                        Toast.makeText(
                                ConversationListActivity.this,
                                "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private String normalizeBearerToken(String token) {
        if (token == null) return "";

        token = token.trim();

        if (token.startsWith("\"") && token.endsWith("\"") && token.length() > 1) {
            token = token.substring(1, token.length() - 1).trim();
        }

        while (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        if (token.isEmpty()) return "";

        return "Bearer " + token;
    }

    private String getTokenPreview(String token) {
        if (token == null || token.trim().isEmpty()) {
            return "EMPTY";
        }

        String clean = token.trim();

        if (clean.length() <= 20) {
            return clean;
        }

        return clean.substring(0, 20) + "...";
    }

    private void showConversationApiError(Response<ApiResponse<ChatConversationResponse>> response) {
        String message = "Không tải được danh sách tin nhắn";

        try {
            android.util.Log.e("MSG_LIST", "HTTP code = " + response.code());

            if (response.body() != null && response.body().getMessage() != null) {
                message = response.body().getMessage();
                android.util.Log.e("MSG_LIST", "body message = " + message);
            }

            if (response.errorBody() != null) {
                String error = response.errorBody().string();
                android.util.Log.e("MSG_LIST", "errorBody = " + error);

                if (response.code() == 401) {
                    if (error.contains("No token provided")) {
                        message = "Backend không nhận được token";
                    } else if (error.contains("Invalid") || error.contains("expired") || error.contains("jwt")) {
                        message = "Token không hợp lệ hoặc đã hết hạn";
                    } else {
                        message = "Token lỗi: " + error;
                    }
                } else if (response.code() == 404) {
                    message = "API danh sách tin nhắn chưa tồn tại ở backend";
                } else {
                    message = error;
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MSG_LIST", "read error body failed", e);
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void openChat(ChatConversationResponse.ConversationItem item) {
        if (item == null) {
            Toast.makeText(this, "Conversation null", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("CHAT_OPEN", "studentId = " + item.studentId);
        android.util.Log.d("CHAT_OPEN", "displayName = " + item.displayName);
        android.util.Log.d("CHAT_OPEN", "username = " + item.username);

        if (item.studentId <= 0) {
            Toast.makeText(this, "Không tìm thấy người nhắn", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("target_student_id", item.studentId);
        intent.putExtra("target_name", item.displayName);
        intent.putExtra("target_username", item.username);
        intent.putExtra("target_avatar_text", item.avatarText);
        intent.putExtra("target_follower_count", item.followerCount);
        intent.putExtra("target_followed_by_me", item.followedByMe);
        intent.putExtra("auth_token", authToken);

        startActivity(intent);
    }
}