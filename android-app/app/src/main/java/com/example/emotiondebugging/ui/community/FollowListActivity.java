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
import com.example.emotiondebugging.model.community.FollowListResponse;
import com.example.emotiondebugging.model.community.FollowUser;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.ui.community.adapter.FollowUserAdapter;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowListActivity extends AppCompatActivity {

    private String authToken;
    private int targetStudentId = -1;
    private String mode = "followers";

    private RecyclerView rvFollow;
    private TextView tvEmpty;
    private FollowUserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_list);

        targetStudentId = getIntent().getIntExtra("student_id", -1);
        mode = getIntent().getStringExtra("mode");
        if (mode == null || mode.trim().isEmpty()) mode = "followers";

        authToken = getIntent().getStringExtra("auth_token");
        if (authToken == null || authToken.trim().isEmpty()) {
            SharedPrefsHelper prefs = new SharedPrefsHelper(this);
            String token = prefs.getToken();
            authToken = token != null ? "Bearer " + token : "";
        }

        View back = findViewById(R.id.btn_back);
        if (back != null) back.setOnClickListener(v -> finish());

        TextView tvTitle = findViewById(R.id.tv_title);
        if (tvTitle != null) {
            tvTitle.setText("following".equals(mode) ? "Đang theo dõi" : "Người theo dõi");
        }

        rvFollow = findViewById(R.id.rv_follow);
        tvEmpty = findViewById(R.id.tv_empty);

        rvFollow.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FollowUserAdapter(this::openUserProfile);
        rvFollow.setAdapter(adapter);

        loadList();
    }

    private void loadList() {
        if (targetStudentId <= 0) {
            showEmpty();
            return;
        }

        Call<ApiResponse<FollowListResponse>> call = "following".equals(mode)
                ? RetrofitClient.getCommunityApi().getProfileFollowing(authToken, targetStudentId)
                : RetrofitClient.getCommunityApi().getProfileFollowers(authToken, targetStudentId);

        call.enqueue(new Callback<ApiResponse<FollowListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FollowListResponse>> call,
                                   Response<ApiResponse<FollowListResponse>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null
                        && response.body().getData().users != null
                        && !response.body().getData().users.isEmpty()) {
                    adapter.setUsers(response.body().getData().users);
                    tvEmpty.setVisibility(View.GONE);
                    rvFollow.setVisibility(View.VISIBLE);
                } else {
                    showEmpty();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FollowListResponse>> call, Throwable t) {
                Toast.makeText(FollowListActivity.this,
                        "Lỗi tải danh sách: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmpty();
            }
        });
    }

    private void showEmpty() {
        adapter.setUsers(null);
        tvEmpty.setVisibility(View.VISIBLE);
        rvFollow.setVisibility(View.GONE);
    }

    private void openUserProfile(FollowUser user) {
        if (user == null || user.studentId <= 0) return;

        Intent intent = new Intent(this, CommunityProfileActivity.class);
        intent.putExtra("student_id", user.studentId);
        intent.putExtra("auth_token", authToken);
        startActivity(intent);
    }
}
