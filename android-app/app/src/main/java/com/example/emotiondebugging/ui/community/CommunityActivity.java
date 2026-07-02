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
    private ImageView ivMyAvatar;
    private View layoutComposer;
    private TextView tvWhatsNew;
    private Button btnComposerPost;
    private CommunityProfile myProfile;
    private ImageView ivHeaderAvatar;
    private LinearLayout layoutComposerHeader;
    private ImageView ivCurrentUserAvatar;
    private String authToken;
    private String currentFilter = "new";
    private boolean isFilterMenuVisible = false;
    private boolean isFirstLoad = true;

    private boolean isTagFiltering = false;
    private Integer currentErrorTypeId = null;

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
        loadCurrentUserAvatar();
    }

    private void initViews() {
        rvPosts = findViewById(R.id.rv_posts);
        layoutFilterMenu = findViewById(R.id.layout_filter_menu);
        btnCreatePost = findViewById(R.id.btn_create_post);
        etSearch = findViewById(R.id.et_search);
        layoutComposer = findViewById(R.id.layout_composer);
        ivMyAvatar = findViewById(R.id.iv_my_avatar);
        tvWhatsNew = findViewById(R.id.tv_whats_new);
        btnComposerPost = findViewById(R.id.btn_composer_post);

        ivHeaderAvatar = findViewById(R.id.iv_header_avatar);

        rvPosts.setLayoutManager(new LinearLayoutManager(this));
        addComposerHeader();

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
            public void onTagClick(int errorTypeId, String errorName) {
                currentErrorTypeId = errorTypeId;
                isTagFiltering = true;

                if (etSearch != null) {
                    etSearch.setText("");
                }

                viewModel.loadPosts(authToken, currentFilter, 1, "", errorTypeId);
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

        View btnSaved = findViewById(R.id.btn_saved);
        if (btnSaved != null) {
            btnSaved.setOnClickListener(v -> loadSavedPosts());
        }

        setupFilterOption(R.id.filter_new, "new");
        setupFilterOption(R.id.filter_trending, "trending");
        setupFilterOption(R.id.filter_best, "best");
        setupFilterOption(R.id.filter_unfixed, "unfixed");
        setupFilterOption(R.id.filter_my_logs, "my_logs");

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isTagFiltering) return;

                    currentErrorTypeId = null;
                    viewModel.loadPosts(
                            authToken,
                            currentFilter,
                            1,
                            s.toString().trim()
                    );
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
        View.OnClickListener openCreatePostListener = v -> {
            Intent intent = new Intent(this, CreatePostActivity.class);
            startActivityForResult(intent, 1);
        };

        if (layoutComposer != null) {
            layoutComposer.setOnClickListener(openCreatePostListener);
        }

        if (tvWhatsNew != null) {
            tvWhatsNew.setOnClickListener(openCreatePostListener);
        }

        if (btnComposerPost != null) {
            btnComposerPost.setOnClickListener(openCreatePostListener);
        }

        if (ivMyAvatar != null) {
            ivMyAvatar.setOnClickListener(v -> openMyProfile());
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
            currentErrorTypeId = null;
            isTagFiltering = false;
            isFilterMenuVisible = false;

            if (layoutFilterMenu != null) {
                layoutFilterMenu.setVisibility(View.GONE);
            }

            reloadPosts();
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

    private void loadSavedPosts() {
        RetrofitClient.getCommunityApi()
                .getSavedPosts(authToken)
                .enqueue(new Callback<ApiResponse<CommunityPostResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<CommunityPostResponse>> call,
                            Response<ApiResponse<CommunityPostResponse>> response
                    ) {
                        if (
                                response.isSuccessful()
                                        && response.body() != null
                                        && response.body().isSuccess()
                                        && response.body().getData() != null
                        ) {
                            renderPosts(response.body().getData().posts);
                        } else {
                            Toast.makeText(
                                    CommunityActivity.this,
                                    "Không tải được bài viết đã lưu",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<CommunityPostResponse>> call,
                            Throwable t
                    ) {
                        Toast.makeText(
                                CommunityActivity.this,
                                "Lỗi kết nối khi tải bài đã lưu: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
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

        if (currentErrorTypeId != null) {
            viewModel.loadPosts(authToken, currentFilter, 1, searchText, currentErrorTypeId);
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

        if (isFirstLoad) {
            isFirstLoad = false;
            viewModel.loadPosts(authToken, currentFilter, 1, "");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 || requestCode == 0) {
            reloadPosts();
        }
    }
    private void addComposerHeader() {
        if (rvPosts == null || rvPosts.getParent() == null) return;

        ViewGroup parent = (ViewGroup) rvPosts.getParent();

        if (layoutComposerHeader != null) return;

        layoutComposerHeader = new LinearLayout(this);
        layoutComposerHeader.setOrientation(LinearLayout.VERTICAL);
        layoutComposerHeader.setPadding(dp(14), dp(14), dp(14), dp(14));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F8FAFC"));
        bg.setCornerRadius(dp(14));
        layoutComposerHeader.setBackground(bg);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        ivCurrentUserAvatar = new ImageView(this);
        ivCurrentUserAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);

        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(Color.parseColor("#12B2C1"));
        ivCurrentUserAvatar.setBackground(avatarBg);

        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(42), dp(42));
        avatarParams.setMargins(0, 0, dp(12), 0);
        row.addView(ivCurrentUserAvatar, avatarParams);

        TextView tvHint = new TextView(this);
        tvHint.setText("What's new?");
        tvHint.setTextColor(Color.parseColor("#9CA3AF"));
        tvHint.setTextSize(16);
        tvHint.setGravity(android.view.Gravity.CENTER_VERTICAL);

        row.addView(tvHint, new LinearLayout.LayoutParams(
                0,
                dp(44),
                1
        ));

        Button btnPost = new Button(this);
        btnPost.setText("Post");
        btnPost.setAllCaps(false);
        btnPost.setTextColor(Color.BLACK);
        btnPost.setTextSize(14);
        btnPost.setTypeface(Typeface.DEFAULT_BOLD);
        btnPost.setBackground(makeComposerButtonBg());

        LinearLayout.LayoutParams postParams = new LinearLayout.LayoutParams(dp(78), dp(44));
        row.addView(btnPost, postParams);

        layoutComposerHeader.addView(row);

        View.OnClickListener openCreatePostListener = v -> {
            Intent intent = new Intent(CommunityActivity.this, CreatePostActivity.class);
            startActivityForResult(intent, 1);
        };

        layoutComposerHeader.setOnClickListener(openCreatePostListener);
        tvHint.setOnClickListener(openCreatePostListener);
        ivCurrentUserAvatar.setOnClickListener(openCreatePostListener);
        btnPost.setOnClickListener(openCreatePostListener);

        int rvIndex = parent.indexOfChild(rvPosts);

        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(dp(32), dp(12), dp(32), dp(10));

        layoutComposerHeader.setLayoutParams(params);
        parent.addView(layoutComposerHeader, rvIndex);
    }

    private GradientDrawable makeComposerButtonBg() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.WHITE);
        drawable.setCornerRadius(dp(14));
        drawable.setStroke(dp(1), Color.parseColor("#D1D5DB"));
        return drawable;
    }

    private void loadCurrentUserAvatar() {
        if (ivCurrentUserAvatar == null) return;

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
                            CommunityProfile profile = response.body().getData();

                            String name = profile.getDisplayName() != null
                                    ? profile.getDisplayName()
                                    : profile.getUsername();

                            AvatarHelper.loadAvatar(
                                    ivCurrentUserAvatar,
                                    profile.getAvatarUrl(),
                                    name
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<CommunityProfile>> call, Throwable t) {
                        android.util.Log.e("COMMUNITY_AVATAR", "Load avatar failed", t);
                    }
                });
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

                            if (ivMyAvatar != null) {
                                AvatarHelper.loadAvatar(
                                        ivMyAvatar,
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