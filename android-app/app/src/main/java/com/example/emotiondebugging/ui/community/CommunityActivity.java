package com.example.emotiondebugging.ui.community;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.model.response.CommunityPostResponse;
import com.example.emotiondebugging.ui.community.adapter.CommunityPostAdapter;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.emotiondebugging.model.community.CommunityProfile;
import com.example.emotiondebugging.utils.AvatarHelper;
import android.widget.ImageView;
import com.example.emotiondebugging.model.community.CommunityProfile;
import com.example.emotiondebugging.utils.AvatarHelper;
public class CommunityActivity extends AppCompatActivity {

    private CommunityViewModel viewModel;
    private CommunityPostAdapter adapter;

    private RecyclerView rvPosts;
    private LinearLayout layoutFilterMenu;
    private LinearLayout btnCreatePost;
    private EditText etSearch;
    private CommunityProfile myProfile;
    private ImageView ivHeaderAvatar;
    private String authToken;
    private String currentFilter = "new";
    private boolean isFilterMenuVisible = false;
    private boolean isFirstLoad = true;

    private boolean isTagFiltering = false;
    private Integer currentErrorTypeId = null;

    private Integer currentTopicId = null;
    private TextView tvNotifBadge;
    private static final int REQ_NOTIFICATION = 3;
    private static final int REQ_TOPIC = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        String token = prefs.getToken();
        authToken = token != null ? "Bearer " + token : "";

        initViews();
        initViewModel();
        setupListeners();
        loadMyCommunityProfile();
    }

    private void initViews() {
        rvPosts = findViewById(R.id.rv_posts);
        layoutFilterMenu = findViewById(R.id.layout_filter_menu);
        btnCreatePost = findViewById(R.id.btn_create_post);
        etSearch = findViewById(R.id.et_search);

        ivHeaderAvatar = findViewById(R.id.iv_header_avatar);
        tvNotifBadge = findViewById(R.id.tv_notif_badge);

        rvPosts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CommunityPostAdapter(new CommunityPostAdapter.OnPostClickListener() {
            @Override
            public void onPostClick(CommunityPostResponse.PostItem post) {
                if (post == null) return;

                Intent intent = new Intent(CommunityActivity.this, PostDetailActivity.class);
                intent.putExtra("post_id", post.postId);
                startActivityForResult(intent, 0);
            }

            @Override
            public void onAuthorClick(CommunityPostResponse.PostItem post) {
                openCommunityProfile(post);
            }

            @Override
            public void onUpvote(CommunityPostResponse.PostItem post) {
                votePost(post, "upvote");
            }

            @Override
            public void onDownvote(CommunityPostResponse.PostItem post) {
                votePost(post, "downvote");
            }

            @Override
            public void onSave(CommunityPostResponse.PostItem post) {
                toggleSavePost(post);
            }

            @Override
            public void onMute(CommunityPostResponse.PostItem post) {
                muteAuthor(post);
            }

            @Override
            public void onRepost(CommunityPostResponse.PostItem post) {
                repostPost(post);
            }

            @Override
            public void onReport(CommunityPostResponse.PostItem post) {
                reportPost(post);
            }

            @Override
            public void onEdit(CommunityPostResponse.PostItem post) {
                // Feed không cho sửa inline; chỉ dùng ở trang cá nhân.
            }

            @Override
            public void onDelete(CommunityPostResponse.PostItem post) {
                // Feed không cho xóa inline; chỉ dùng ở trang cá nhân.
            }

            @Override
            public void onTagClick(int errorTypeId, String errorName) {
                if (errorTypeId <= 0) return;
                currentFilter = "topic";
                currentTopicId = errorTypeId;
                isTagFiltering = false;

                if (etSearch != null) {
                    etSearch.setText("");
                }

                viewModel.loadPosts(authToken, "topic", 1, "", errorTypeId);
            }
        });

        rvPosts.setAdapter(adapter);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(CommunityViewModel.class);

        viewModel.getMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getPosts().observe(this, response -> {
            if (response != null) {
                rvPosts.post(() -> renderPosts(response.posts));
            }
        });
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnCreatePost != null) {
            btnCreatePost.setOnClickListener(v -> {
                Intent intent = new Intent(this, CreatePostActivity.class);
                startActivityForResult(intent, 1);
            });
        }

        View btnFilter = findViewById(R.id.btn_filter);
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> {
                isFilterMenuVisible = !isFilterMenuVisible;
                layoutFilterMenu.setVisibility(isFilterMenuVisible ? View.VISIBLE : View.GONE);
            });
        }

        View btnNotifications = findViewById(R.id.btn_notifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> {
                Intent intent = new Intent(this, NotificationActivity.class);
                startActivity(intent);
            });
        }

        setupFilterOption(R.id.filter_new, "new");
        setupFilterOption(R.id.filter_trending, "trending");
        setupFilterOption(R.id.filter_best, "best");
        setupTopicFilterOption(R.id.filter_topic);

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isTagFiltering) return;

                    currentErrorTypeId = null;
                    reloadPosts();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
        if (ivHeaderAvatar != null) {
            ivHeaderAvatar.setOnClickListener(v -> openMyProfile());
        }
    }

    private void setupFilterOption(int viewId, String filter) {
        TextView tv = findViewById(viewId);
        if (tv == null) return;

        tv.setOnClickListener(v -> {
            currentFilter = filter;
            currentTopicId = null;
            currentErrorTypeId = null;
            isTagFiltering = false;
            isFilterMenuVisible = false;

            if (layoutFilterMenu != null) {
                layoutFilterMenu.setVisibility(View.GONE);
            }

            reloadPosts();
        });
    }

    private void setupTopicFilterOption(int viewId) {
        TextView tv = findViewById(viewId);
        if (tv == null) return;

        tv.setOnClickListener(v -> {
            isFilterMenuVisible = false;
            if (layoutFilterMenu != null) {
                layoutFilterMenu.setVisibility(View.GONE);
            }
            Intent intent = new Intent(this, TopicListActivity.class);
            startActivityForResult(intent, REQ_TOPIC);
        });
    }

    private void votePost(CommunityPostResponse.PostItem post, String voteType) {
        if (post == null) return;

        Map<String, String> body = new HashMap<>();
        body.put("vote_type", voteType);

        RetrofitClient.getCommunityApi()
                .votePost(authToken, post.postId, body)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<Object>> call,
                            Response<ApiResponse<Object>> response
                    ) {
                        if (response.isSuccessful()) {
                            reloadPosts();
                        } else {
                            Toast.makeText(
                                    CommunityActivity.this,
                                    "Không thể vote bài viết",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(
                                CommunityActivity.this,
                                "Lỗi kết nối khi vote: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void repostPost(CommunityPostResponse.PostItem post) {
        if (post == null) return;

        RetrofitClient.getCommunityApi()
                .toggleRepostPost(authToken, post.postId)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<Object>> call,
                            Response<ApiResponse<Object>> response
                    ) {
                        if (response.isSuccessful()) {
                            reloadPosts();
                        } else {
                            Toast.makeText(
                                    CommunityActivity.this,
                                    "Không thể đăng lại bài viết",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(
                                CommunityActivity.this,
                                "Lỗi kết nối khi đăng lại: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void toggleSavePost(CommunityPostResponse.PostItem post) {
        if (post == null) return;

        RetrofitClient.getCommunityApi()
                .toggleSavePost(authToken, post.postId)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<Object>> call,
                            Response<ApiResponse<Object>> response
                    ) {
                        if (response.isSuccessful()) {
                            reloadPosts();
                        } else {
                            Toast.makeText(
                                    CommunityActivity.this,
                                    "Không thể lưu/bỏ lưu bài viết",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(
                                CommunityActivity.this,
                                "Lỗi kết nối khi lưu bài viết: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void muteAuthor(CommunityPostResponse.PostItem post) {
        if (post == null) return;

        RetrofitClient.getCommunityApi()
                .muteAuthor(authToken, post.postId)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<Object>> call,
                            Response<ApiResponse<Object>> response
                    ) {
                        if (response.isSuccessful()) {
                            reloadPosts();
                        } else {
                            Toast.makeText(
                                    CommunityActivity.this,
                                    "Không thể ẩn tác giả",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(
                                CommunityActivity.this,
                                "Lỗi kết nối khi ẩn tác giả: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void reportPost(CommunityPostResponse.PostItem post) {
        if (post == null) return;

        final EditText input = new EditText(this);
        input.setHint("Lý do báo cáo (không bắt buộc)");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Báo cáo bài viết")
                .setView(input)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    Map<String, String> body = new HashMap<>();
                    body.put("reason", input.getText().toString().trim());

                    RetrofitClient.getCommunityApi()
                            .reportPost(authToken, post.postId, body)
                            .enqueue(new Callback<ApiResponse<Object>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                                    Toast.makeText(CommunityActivity.this,
                                            response.isSuccessful() ? "Đã gửi báo cáo" : "Không gửi được báo cáo",
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                                    Toast.makeText(CommunityActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void refreshNotificationBadge() {
        if (tvNotifBadge == null) return;

        RetrofitClient.getCommunityApi()
                .getUnreadNotificationCount(authToken)
                .enqueue(new Callback<ApiResponse<com.example.emotiondebugging.model.community.UnreadCountResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<com.example.emotiondebugging.model.community.UnreadCountResponse>> call,
                            Response<ApiResponse<com.example.emotiondebugging.model.community.UnreadCountResponse>> response
                    ) {
                        int count = 0;
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            count = response.body().getData().count;
                        }
                        if (count > 0) {
                            tvNotifBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                            tvNotifBadge.setVisibility(View.VISIBLE);
                        } else {
                            tvNotifBadge.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<com.example.emotiondebugging.model.community.UnreadCountResponse>> call,
                            Throwable t
                    ) {
                        tvNotifBadge.setVisibility(View.GONE);
                    }
                });
    }

    private void openCommunityProfile(CommunityPostResponse.PostItem post) {
        if (post == null) return;

        if (post.isAnonymous == 1) {
            Toast.makeText(this, "Bài viết ẩn danh nên không thể xem profile", Toast.LENGTH_SHORT).show();
            return;
        }

        if (post.studentId <= 0) {
            Toast.makeText(this, "Không tìm thấy thông tin người đăng", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(CommunityActivity.this, CommunityProfileActivity.class);
        intent.putExtra("student_id", post.studentId);

        // Thêm dòng này
        intent.putExtra("auth_token", authToken);

        startActivity(intent);
    }

    private void reloadPosts() {
        String searchText = etSearch != null
                ? etSearch.getText().toString().trim()
                : "";

        if ("topic".equals(currentFilter) && currentTopicId != null) {
            viewModel.loadPosts(authToken, "topic", 1, searchText, currentTopicId);
        } else {
            viewModel.loadPosts(authToken, currentFilter, 1, searchText);
        }
    }

    private void renderPosts(List<CommunityPostResponse.PostItem> posts) {
        if (posts == null) {
            adapter.setPosts(null);
            return;
        }

        adapter.setPosts(posts);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reload mỗi lần quay lại (kể cả back từ profile) để bài mới/sửa được cập nhật ngay.
        if (isFirstLoad) {
            isFirstLoad = false;
            viewModel.loadPosts(authToken, currentFilter, 1, "");
        } else {
            reloadPosts();
        }

        refreshNotificationBadge();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_TOPIC && resultCode == RESULT_OK && data != null) {
            int topicId = data.getIntExtra("topic_id", -1);
            if (topicId > 0) {
                currentFilter = "topic";
                currentTopicId = topicId;
                if (etSearch != null) etSearch.setText("");
                viewModel.loadPosts(authToken, "topic", 1, "", topicId);
            }
            return;
        }

        if (requestCode == 1 || requestCode == 0) {
            reloadPosts();
        }
    }
    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void loadMyCommunityProfile() {
        RetrofitClient.getCommunityApi()
                .getMyCommunityProfile(authToken)
                .enqueue(new Callback<ApiResponse<CommunityProfile>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<CommunityProfile>> call,
                            Response<ApiResponse<CommunityProfile>> response
                    ) {
                        if (
                                response.isSuccessful()
                                        && response.body() != null
                                        && response.body().getData() != null
                        ) {
                            myProfile = response.body().getData();

                            String name = myProfile.getDisplayName() != null
                                    ? myProfile.getDisplayName()
                                    : myProfile.getUsername();

                            if (ivHeaderAvatar != null) {
                                AvatarHelper.loadAvatar(
                                        ivHeaderAvatar,
                                        myProfile.getAvatarUrl(),
                                        name
                                );
                            }
                        } else {
                            android.util.Log.e("COMMUNITY_PROFILE", "Load my profile failed code = " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<CommunityProfile>> call, Throwable t) {
                        android.util.Log.e("COMMUNITY_PROFILE", "Load my profile error", t);
                    }
                });
    }

    private void openMyProfile() {
        Intent intent = new Intent(this, CommunityProfileActivity.class);

        // Mở profile của chính mình thì dùng profile/me
        intent.putExtra("student_id", -1);
        intent.putExtra("is_me", true);
        intent.putExtra("auth_token", authToken);

        startActivity(intent);
    }


}