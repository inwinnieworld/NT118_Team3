package com.example.emotiondebugging.ui.admin;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.community.AdminReportResponse;
import com.example.emotiondebugging.model.community.AdminReviewRequestResponse;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageCommunityActivity extends AppCompatActivity {

    private String authToken;
    private String currentTab = "posts";

    private TextView tabPosts, tabComments, tabReviews, tvEmpty;
    private RecyclerView rvItems;
    private ManageCommunityAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_community);

        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        String token = prefs.getToken();
        authToken = token != null ? "Bearer " + token : "";

        View back = findViewById(R.id.btn_back);
        if (back != null) back.setOnClickListener(v -> finish());

        tabPosts = findViewById(R.id.tab_posts);
        tabComments = findViewById(R.id.tab_comments);
        tabReviews = findViewById(R.id.tab_reviews);
        tvEmpty = findViewById(R.id.tv_empty);
        rvItems = findViewById(R.id.rv_items);

        rvItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ManageCommunityAdapter(new ManageCommunityAdapter.OnActionListener() {
            @Override
            public void onAccept(ManageCommunityAdapter.Row row) {
                resolve(row, "accept");
            }

            @Override
            public void onReject(ManageCommunityAdapter.Row row) {
                resolve(row, "reject");
            }
        });
        rvItems.setAdapter(adapter);

        tabPosts.setOnClickListener(v -> switchTab("posts"));
        tabComments.setOnClickListener(v -> switchTab("comments"));
        tabReviews.setOnClickListener(v -> switchTab("reviews"));

        switchTab("posts");
    }

    private void switchTab(String tab) {
        currentTab = tab;

        int active = Color.parseColor("#85E9FF");
        int inactive = Color.parseColor("#9CA3AF");
        tabPosts.setTextColor("posts".equals(tab) ? active : inactive);
        tabComments.setTextColor("comments".equals(tab) ? active : inactive);
        tabReviews.setTextColor("reviews".equals(tab) ? active : inactive);

        if ("reviews".equals(tab)) {
            loadReviewRequests();
        } else {
            loadReports(tab);
        }
    }

    private void loadReports(String tab) {
        String type = "comments".equals(tab) ? "comment" : "post";
        RetrofitClient.getAdminApi()
                .getCommunityReports(authToken, type, "pending")
                .enqueue(new Callback<ApiResponse<AdminReportResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AdminReportResponse>> call,
                                           Response<ApiResponse<AdminReportResponse>> response) {
                        if (!"posts".equals(currentTab) && !"comments".equals(currentTab)) return;

                        List<ManageCommunityAdapter.Row> rows = new ArrayList<>();
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            AdminReportResponse data = response.body().getData();
                            if ("comments".equals(tab) && data.commentReports != null) {
                                for (AdminReportResponse.CommentReport cr : data.commentReports) {
                                    ManageCommunityAdapter.Row row = new ManageCommunityAdapter.Row();
                                    row.kind = ManageCommunityAdapter.KIND_COMMENT;
                                    row.targetId = cr.commentId;
                                    row.kindLabel = "Bình luận";
                                    row.content = cr.content;
                                    row.author = cr.authorName;
                                    row.reasons = cr.reasons;
                                    row.reportCount = cr.reportCount;
                                    row.isHidden = cr.isHidden == 1;
                                    rows.add(row);
                                }
                            } else if ("posts".equals(tab) && data.postReports != null) {
                                for (AdminReportResponse.PostReport pr : data.postReports) {
                                    ManageCommunityAdapter.Row row = new ManageCommunityAdapter.Row();
                                    row.kind = ManageCommunityAdapter.KIND_POST;
                                    row.targetId = pr.postId;
                                    row.kindLabel = "Bài viết";
                                    row.title = pr.title;
                                    row.content = pr.content;
                                    row.author = pr.authorName;
                                    row.reasons = pr.reasons;
                                    row.reportCount = pr.reportCount;
                                    row.isHidden = pr.isHidden == 1;
                                    rows.add(row);
                                }
                            }
                        }
                        render(rows);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AdminReportResponse>> call, Throwable t) {
                        Toast.makeText(ManageCommunityActivity.this,
                                "Lỗi tải báo cáo: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadReviewRequests() {
        RetrofitClient.getAdminApi()
                .getReviewRequests(authToken, "pending")
                .enqueue(new Callback<ApiResponse<AdminReviewRequestResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AdminReviewRequestResponse>> call,
                                           Response<ApiResponse<AdminReviewRequestResponse>> response) {
                        if (!"reviews".equals(currentTab)) return;

                        List<ManageCommunityAdapter.Row> rows = new ArrayList<>();
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null
                                && response.body().getData().requests != null) {
                            for (AdminReviewRequestResponse.ReviewRequest rr : response.body().getData().requests) {
                                ManageCommunityAdapter.Row row = new ManageCommunityAdapter.Row();
                                row.kind = ManageCommunityAdapter.KIND_REVIEW;
                                row.targetId = rr.requestId;
                                row.kindLabel = "Xem xét lại";
                                row.title = rr.title;
                                row.content = rr.content;
                                row.author = rr.authorName;
                                row.reasons = rr.message;
                                row.isHidden = rr.isHidden == 1;
                                rows.add(row);
                            }
                        }
                        render(rows);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AdminReviewRequestResponse>> call, Throwable t) {
                        Toast.makeText(ManageCommunityActivity.this,
                                "Lỗi tải yêu cầu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void render(List<ManageCommunityAdapter.Row> rows) {
        if (rows == null || rows.isEmpty()) {
            adapter.setRows(null);
            tvEmpty.setVisibility(View.VISIBLE);
            rvItems.setVisibility(View.GONE);
        } else {
            adapter.setRows(rows);
            tvEmpty.setVisibility(View.GONE);
            rvItems.setVisibility(View.VISIBLE);
        }
    }

    private void resolve(ManageCommunityAdapter.Row row, String action) {
        if (row == null) return;

        Map<String, String> body = new HashMap<>();
        body.put("action", action);

        Call<ApiResponse<Object>> call;
        if (row.kind == ManageCommunityAdapter.KIND_POST) {
            call = RetrofitClient.getAdminApi().resolvePostReport(authToken, row.targetId, body);
        } else if (row.kind == ManageCommunityAdapter.KIND_COMMENT) {
            call = RetrofitClient.getAdminApi().resolveCommentReport(authToken, row.targetId, body);
        } else {
            call = RetrofitClient.getAdminApi().resolveReviewRequest(authToken, row.targetId, body);
        }

        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                Toast.makeText(ManageCommunityActivity.this,
                        response.isSuccessful() ? "Đã xử lý" : "Xử lý thất bại",
                        Toast.LENGTH_SHORT).show();
                switchTab(currentTab);
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(ManageCommunityActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
