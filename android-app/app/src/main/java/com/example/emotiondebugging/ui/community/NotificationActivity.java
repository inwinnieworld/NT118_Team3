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
import com.example.emotiondebugging.model.community.NotificationItem;
import com.example.emotiondebugging.model.community.NotificationListResponse;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.ui.community.adapter.NotificationAdapter;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity {

    private String authToken;
    private RecyclerView rvNotifications;
    private TextView tvEmpty;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        String token = prefs.getToken();
        authToken = token != null ? "Bearer " + token : "";

        rvNotifications = findViewById(R.id.rv_notifications);
        tvEmpty = findViewById(R.id.tv_empty);

        View back = findViewById(R.id.btn_back);
        if (back != null) back.setOnClickListener(v -> finish());

        View readAll = findViewById(R.id.btn_read_all);
        if (readAll != null) readAll.setOnClickListener(v -> markAllRead());

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(new NotificationAdapter.OnNotificationClickListener() {
            @Override
            public void onNotificationClick(NotificationItem item) {
                markRead(item);
                if (item.relatedPostId != null && item.relatedPostId > 0) {
                    Intent intent = new Intent(NotificationActivity.this, PostDetailActivity.class);
                    intent.putExtra("post_id", item.relatedPostId);
                    startActivity(intent);
                }
            }

            @Override
            public void onReviewRequest(NotificationItem item) {
                if (item.relatedPostId != null && item.relatedPostId > 0) {
                    showReviewRequestDialog(item.relatedPostId);
                }
            }
        });
        rvNotifications.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }

    private void loadNotifications() {
        RetrofitClient.getCommunityApi()
                .getNotifications(authToken)
                .enqueue(new Callback<ApiResponse<NotificationListResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<NotificationListResponse>> call,
                            Response<ApiResponse<NotificationListResponse>> response
                    ) {
                        List<NotificationItem> items = null;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            items = response.body().getData().notifications;
                        }

                        if (items == null || items.isEmpty()) {
                            adapter.setItems(null);
                            tvEmpty.setVisibility(View.VISIBLE);
                            rvNotifications.setVisibility(View.GONE);
                        } else {
                            adapter.setItems(items);
                            tvEmpty.setVisibility(View.GONE);
                            rvNotifications.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<NotificationListResponse>> call, Throwable t) {
                        Toast.makeText(NotificationActivity.this,
                                "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void markRead(NotificationItem item) {
        if (item == null || item.isRead == 1) return;
        RetrofitClient.getCommunityApi()
                .markNotificationRead(authToken, item.notificationId)
                .enqueue(new SilentCallback());
    }

    private void markAllRead() {
        RetrofitClient.getCommunityApi()
                .markAllNotificationsRead(authToken)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        loadNotifications();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(NotificationActivity.this,
                                "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showReviewRequestDialog(int postId) {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Lý do muốn xem xét lại (không bắt buộc)");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Gửi yêu cầu xem xét")
                .setView(input)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    Map<String, String> body = new HashMap<>();
                    body.put("message", input.getText().toString().trim());

                    RetrofitClient.getCommunityApi()
                            .createReviewRequest(authToken, postId, body)
                            .enqueue(new Callback<ApiResponse<Object>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                                    String msg = response.body() != null && response.body().getMessage() != null
                                            ? response.body().getMessage()
                                            : (response.isSuccessful() ? "Đã gửi yêu cầu" : "Không gửi được yêu cầu");
                                    Toast.makeText(NotificationActivity.this, msg, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                                    Toast.makeText(NotificationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private class SilentCallback implements Callback<ApiResponse<Object>> {
        @Override
        public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {}

        @Override
        public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {}
    }
}
