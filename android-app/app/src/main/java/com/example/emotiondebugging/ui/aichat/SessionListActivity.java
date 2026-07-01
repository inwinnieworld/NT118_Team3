package com.example.emotiondebugging.ui.aichat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.response.AiChatModels.SessionSummary;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn danh sách session (mở từ nút "Các session của bạn").
 *
 * Quy tắc quan trọng: session chỉ tồn tại sau khi user đã gửi lượt đầu tiên ở màn chat.
 * Mở UI chat hay bấm "+" KHÔNG tạo session. Danh sách này chỉ hiển thị các session đã thực
 * sự bắt đầu; chưa có thì hiện empty state. session_title NULL (chưa kết thúc) → hiển thị
 * nhãn tạm theo trạng thái.
 *
 * Ô tìm kiếm lọc tại chỗ theo tiêu đề (chưa gọi backend tìm kiếm).
 */
public class SessionListActivity extends AppCompatActivity {

    private RecyclerView recyclerSessions;
    private TextView tvEmpty;
    private EditText etSearch;
    private SessionAdapter adapter;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_list);

        recyclerSessions = findViewById(R.id.recyclerSessions);
        tvEmpty = findViewById(R.id.tvEmpty);
        etSearch = findViewById(R.id.etSearch);

        String token = new SharedPrefsHelper(this).getToken();
        authToken = token != null ? "Bearer " + token : null;

        setupRecycler();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại mỗi khi quay lại màn (session mới có thể vừa được tạo/kết thúc).
        loadSessions();
    }

    private void setupRecycler() {
        adapter = new SessionAdapter(this::onSessionClick);
        recyclerSessions.setLayoutManager(new LinearLayoutManager(this));
        recyclerSessions.setAdapter(adapter);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.tvBackToCurrent).setOnClickListener(v -> finish());

        // "+" mở màn chat mới. Session chỉ tạo khi user gửi lượt đầu (backend lo), không phải khi bấm "+".
        findViewById(R.id.btnAddSession).setOnClickListener(v -> {
            startActivity(new Intent(this, AiChatActivity.class));
            finish();
        });

        // Ô tìm kiếm: lọc tại chỗ theo tiêu đề.
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                adapter.filter(s.toString());
                updateEmptyState();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    /** Tải danh sách session thật của user từ /api/aichat/sessions. */
    private void loadSessions() {
        if (authToken == null) return;
        RetrofitClient.getAiChatApi().getSessions(authToken)
                .enqueue(new Callback<ApiResponse<List<SessionSummary>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<SessionSummary>>> call,
                                           Response<ApiResponse<List<SessionSummary>>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            adapter.setSessions(toSessions(response.body().getData()));
                        } else {
                            adapter.setSessions(new ArrayList<>());
                        }
                        updateEmptyState();
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<List<SessionSummary>>> call, Throwable t) {
                        Toast.makeText(SessionListActivity.this,
                                "Không tải được danh sách session", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Map response → model hiển thị. Tiêu đề NULL (chưa kết thúc) → nhãn tạm. */
    private List<Session> toSessions(List<SessionSummary> summaries) {
        List<Session> list = new ArrayList<>();
        for (SessionSummary s : summaries) {
            list.add(new Session(s.sessionId, displayTitle(s)));
        }
        return list;
    }

    /** Nhãn hiển thị dùng session_id thật trong DB (không phải số thứ tự trong danh sách). */
    private String displayTitle(SessionSummary s) {
        if (s.sessionTitle != null && !s.sessionTitle.trim().isEmpty()) {
            return String.format(Locale.getDefault(), "Session [%03d]: %s", s.sessionId, s.sessionTitle);
        }
        // Chưa kết thúc → chưa có tiêu đề.
        return String.format(Locale.getDefault(), "Session [%03d]: Đang trò chuyện...", s.sessionId);
    }

    private void onSessionClick(Session session) {
        Intent intent = new Intent(this, AiChatActivity.class);
        intent.putExtra("session_id", session.getId());
        startActivity(intent);
        finish();
    }

    private void updateEmptyState() {
        boolean empty = adapter.getItemCount() == 0;
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerSessions.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
