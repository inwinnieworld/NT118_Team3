package com.example.emotiondebugging.ui.community;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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
import com.example.emotiondebugging.model.response.CommunityPostResponse;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

import java.util.List;

import com.example.emotiondebugging.ui.community.adapter.CommunityPostAdapter;

public class CommunityActivity extends AppCompatActivity {

    private CommunityViewModel viewModel;
    private CommunityPostAdapter adapter;
    private RecyclerView rvPosts;
    private LinearLayout layoutFilterMenu;
    private LinearLayout btnCreatePost;
    private EditText etSearch;
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
    }

    private void initViews() {
        rvPosts = findViewById(R.id.rv_posts);
        layoutFilterMenu = findViewById(R.id.layout_filter_menu);
        btnCreatePost = findViewById(R.id.btn_create_post);
        etSearch = findViewById(R.id.et_search);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommunityPostAdapter(new CommunityPostAdapter.OnPostClickListener() {
            @Override
            public void onUpvote(com.example.emotiondebugging.model.response.CommunityPostResponse.PostItem post) {
                viewModel.votePost(authToken, post.postId, "UPVOTE");
                viewModel.loadPosts(authToken, currentFilter, 1, etSearch.getText().toString().trim());
            }
            @Override
            public void onDownvote(com.example.emotiondebugging.model.response.CommunityPostResponse.PostItem post) {
                viewModel.votePost(authToken, post.postId, "DOWNVOTE");
                viewModel.loadPosts(authToken, currentFilter, 1, etSearch.getText().toString().trim());
            }
            @Override
            public void onPostClick(com.example.emotiondebugging.model.response.CommunityPostResponse.PostItem post) {
                Intent intent = new Intent(CommunityActivity.this, PostDetailActivity.class);
                intent.putExtra("post_id", post.postId);
                startActivityForResult(intent, 0);
            }
            @Override
            public void onSave(com.example.emotiondebugging.model.response.CommunityPostResponse.PostItem post) {
                RetrofitClient.getCommunityApi().toggleSavePost(authToken, post.postId)
                    .enqueue(new retrofit2.Callback<com.example.emotiondebugging.model.response.ApiResponse<Object>>() {
                        @Override
                        public void onResponse(retrofit2.Call<com.example.emotiondebugging.model.response.ApiResponse<Object>> call,
                                               retrofit2.Response<com.example.emotiondebugging.model.response.ApiResponse<Object>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Toast.makeText(CommunityActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                viewModel.loadPosts(authToken, currentFilter, 1, etSearch.getText().toString().trim());
                            }
                        }
                        @Override public void onFailure(retrofit2.Call<com.example.emotiondebugging.model.response.ApiResponse<Object>> call, Throwable t) {}
                    });
            }
            @Override
            public void onTagClick(int errorTypeId, String errorName) {
                isTagFiltering = true;
                currentErrorTypeId = errorTypeId;
                etSearch.setText("");
                isTagFiltering = false;
                Toast.makeText(CommunityActivity.this, "#" + errorName, Toast.LENGTH_SHORT).show();
                viewModel.loadPosts(authToken, currentFilter, 1, "", currentErrorTypeId);
            }
            @Override
            public void onMute(com.example.emotiondebugging.model.response.CommunityPostResponse.PostItem post) {
                RetrofitClient.getCommunityApi().muteAuthor(authToken, post.postId)
                    .enqueue(new retrofit2.Callback<com.example.emotiondebugging.model.response.ApiResponse<Object>>() {
                        @Override
                        public void onResponse(retrofit2.Call<com.example.emotiondebugging.model.response.ApiResponse<Object>> call,
                                               retrofit2.Response<com.example.emotiondebugging.model.response.ApiResponse<Object>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Toast.makeText(CommunityActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                viewModel.loadPosts(authToken, currentFilter, 1, etSearch.getText().toString().trim());
                            }
                        }
                        @Override public void onFailure(retrofit2.Call<com.example.emotiondebugging.model.response.ApiResponse<Object>> call, Throwable t) {}
                    });
            }
        });
        rvPosts.setAdapter(adapter);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(CommunityViewModel.class);

        viewModel.getMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty())
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getLoading().observe(this, isLoading -> {
            // TODO: hiển thị progress bar nếu cần
        });

        viewModel.getPosts().observe(this, response -> {
            if (response != null) {
                rvPosts.post(() -> renderPosts(response.posts));
            }
        });

        // Load được xử lý trong onResume
    }

    private void setupListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnCreatePost.setOnClickListener(v ->
                startActivityForResult(new Intent(this, CreatePostActivity.class), 1));

        // Toggle filter menu
        findViewById(R.id.btn_filter).setOnClickListener(v -> {
            isFilterMenuVisible = !isFilterMenuVisible;
            layoutFilterMenu.setVisibility(isFilterMenuVisible ? View.VISIBLE : View.GONE);
        });

        // Nút tim — hiện bài viết đã lưu
        findViewById(R.id.btn_saved).setOnClickListener(v -> {
            RetrofitClient.getCommunityApi().getSavedPosts(authToken)
                .enqueue(new retrofit2.Callback<com.example.emotiondebugging.model.response.ApiResponse<com.example.emotiondebugging.model.response.CommunityPostResponse>>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.emotiondebugging.model.response.ApiResponse<com.example.emotiondebugging.model.response.CommunityPostResponse>> call,
                                           retrofit2.Response<com.example.emotiondebugging.model.response.ApiResponse<com.example.emotiondebugging.model.response.CommunityPostResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            rvPosts.post(() -> renderPosts(response.body().getData().posts));
                        }
                    }
                    @Override public void onFailure(retrofit2.Call<com.example.emotiondebugging.model.response.ApiResponse<com.example.emotiondebugging.model.response.CommunityPostResponse>> call, Throwable t) {}
                });
        });

        // Filter options
        setupFilterOption(R.id.filter_new, "new");
        setupFilterOption(R.id.filter_trending, "trending");
        setupFilterOption(R.id.filter_best, "best");
        setupFilterOption(R.id.filter_unfixed, "unfixed");
        setupFilterOption(R.id.filter_my_logs, "my_logs");

        // Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (isTagFiltering) return; // skip khi đang set tag filter
                currentErrorTypeId = null;
                viewModel.loadPosts(authToken, currentFilter, 1, s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilterOption(int viewId, String filter) {
        TextView tv = findViewById(viewId);
        if (tv != null) {
            tv.setOnClickListener(v -> {
                currentFilter = filter;
                layoutFilterMenu.setVisibility(View.GONE);
                isFilterMenuVisible = false;
                viewModel.loadPosts(authToken, currentFilter, 1, etSearch.getText().toString().trim());
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Chỉ load lần đầu, việc refresh sau CreatePost xử lý qua onActivityResult
        if (isFirstLoad) {
            isFirstLoad = false;
            viewModel.loadPosts(authToken, currentFilter, 1, "");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // requestCode 1 = từ CreatePost, reload danh sách
        if (requestCode == 1) {
            viewModel.loadPosts(authToken, currentFilter, 1, etSearch.getText().toString().trim());
        }
    }

    private void renderPosts(List<CommunityPostResponse.PostItem> posts) {
        adapter.setPosts(posts);
    }
}
