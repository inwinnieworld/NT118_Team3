package com.example.emotiondebugging.ui.community;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.community.FollowListResponse;
import com.example.emotiondebugging.model.community.FollowUser;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.ui.community.adapter.BlockedAuthorAdapter;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlockedAuthorsActivity extends AppCompatActivity {

    private String authToken;
    private RecyclerView rvBlocked;
    private TextView tvEmpty;
    private BlockedAuthorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_authors);

        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        String token = prefs.getToken();
        authToken = token != null ? "Bearer " + token : "";

        View back = findViewById(R.id.btn_back);
        if (back != null) back.setOnClickListener(v -> finish());

        rvBlocked = findViewById(R.id.rv_blocked);
        tvEmpty = findViewById(R.id.tv_empty);

        rvBlocked.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BlockedAuthorAdapter(this::unblock);
        rvBlocked.setAdapter(adapter);

        loadBlocked();
    }

    private void loadBlocked() {
        RetrofitClient.getCommunityApi()
                .getBlockedAuthors(authToken)
                .enqueue(new Callback<ApiResponse<FollowListResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<FollowListResponse>> call,
                                           Response<ApiResponse<FollowListResponse>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null
                                && response.body().getData().users != null
                                && !response.body().getData().users.isEmpty()) {
                            adapter.setUsers(response.body().getData().users);
                            tvEmpty.setVisibility(View.GONE);
                            rvBlocked.setVisibility(View.VISIBLE);
                        } else {
                            adapter.setUsers(null);
                            tvEmpty.setVisibility(View.VISIBLE);
                            rvBlocked.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<FollowListResponse>> call, Throwable t) {
                        Toast.makeText(BlockedAuthorsActivity.this,
                                "Lỗi tải danh sách: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void unblock(FollowUser user) {
        if (user == null) return;

        RetrofitClient.getCommunityApi()
                .unblockUser(authToken, user.studentId)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        Toast.makeText(BlockedAuthorsActivity.this,
                                response.isSuccessful() ? "Đã bỏ chặn" : "Không bỏ chặn được",
                                Toast.LENGTH_SHORT).show();
                        loadBlocked();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(BlockedAuthorsActivity.this,
                                "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
