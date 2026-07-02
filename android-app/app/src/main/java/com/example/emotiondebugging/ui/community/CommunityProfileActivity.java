package com.example.emotiondebugging.ui.community;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.community.CommunityProfile;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.model.response.CommunityPostResponse;
import com.example.emotiondebugging.ui.community.adapter.CommunityPostAdapter;
import com.example.emotiondebugging.utils.SharedPrefsHelper;
import com.example.emotiondebugging.ui.community.adapter.CommunityMediaAdapter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

public class CommunityProfileActivity extends AppCompatActivity {

    private int studentId = -1;
    private String authToken;
    private String currentTab = "posts";
    private boolean isMeFromIntent = false;

    private TextView tvDisplayName;
    private TextView tvUsername;
    private TextView tvAvatar;
    private TextView tvBio;
    private TextView tvStudentInfo;
    private TextView tvFollowInfo;
    private TextView tvEmpty;

    private TextView tabPosts;
    private TextView tabReplies;
    private TextView tabMedia;
    private TextView tabReposts;

    private Button btnFollow;
    private Button btnMessage;
    private RecyclerView rvProfilePosts;

    private CommunityPostAdapter adapter;
    private CommunityMediaAdapter mediaAdapter;

    private CommunityProfile currentProfile;
    private boolean usingMyProfileLayout = false;

    private android.widget.ImageView ivAvatar;
    private TextView tvFollowers;
    private TextView tvFollowing;

    private Button btnEditProfile;
    private TextView tvHeaderUsername;
    private android.widget.ImageView ivMyAvatar;

    private View layoutComposer;
    private TextView tvWhatsNew;
    private Button btnComposerPost;
    private View cardCreateThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isMeFromIntent = getIntent().getBooleanExtra("is_me", false);
        studentId = getIntent().getIntExtra("student_id", -1);

        authToken = getIntent().getStringExtra("auth_token");

        if (authToken == null || authToken.trim().isEmpty()) {
            SharedPrefsHelper prefs = new SharedPrefsHelper(this);
            authToken = normalizeBearerToken(prefs.getToken());
        } else {
            authToken = normalizeBearerToken(authToken);
        }

        usingMyProfileLayout = isMeFromIntent || studentId == -1;

        android.util.Log.d("PROFILE_LAYOUT", "studentId = " + studentId);
        android.util.Log.d("PROFILE_LAYOUT", "isMeFromIntent = " + isMeFromIntent);
        android.util.Log.d("PROFILE_TOKEN", "authToken empty = " + (authToken == null || authToken.trim().isEmpty()));

        if (studentId <= 0 && !isMeFromIntent && studentId != -1) {
            Toast.makeText(this, "Không tìm thấy thông tin profile", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (usingMyProfileLayout) {
            setContentView(R.layout.activity_my_community_profile);
        } else {
            setContentView(R.layout.activity_community_profile);
        }

        initViews();
        setupRecyclerView();
        setupListeners();

        loadProfile();
    }

    private void initViews() {
        View back = findViewById(R.id.btn_back);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        tvDisplayName = findViewById(R.id.tv_display_name);
        tvUsername = findViewById(R.id.tv_username);
        tvBio = findViewById(R.id.tv_bio);

        tabPosts = findViewById(R.id.tab_posts);
        tabReplies = findViewById(R.id.tab_replies);
        tabMedia = findViewById(R.id.tab_media);
        tabReposts = findViewById(R.id.tab_reposts);

        if (usingMyProfileLayout) {
            tvHeaderUsername = findViewById(R.id.tv_header_username);

            ivAvatar = findViewById(R.id.iv_avatar);
            ivMyAvatar = findViewById(R.id.iv_my_avatar);

            tvFollowers = findViewById(R.id.tv_followers);
            tvFollowing = findViewById(R.id.tv_following);

            btnEditProfile = findViewById(R.id.btn_edit_profile);
            btnFollow = btnEditProfile;
            btnMessage = findViewById(R.id.btn_message);

            layoutComposer = findViewById(R.id.layout_composer);
            tvWhatsNew = findViewById(R.id.tv_whats_new);
            btnComposerPost = findViewById(R.id.btn_composer_post);
            cardCreateThread = findViewById(R.id.card_create_thread);

            rvProfilePosts = findViewById(R.id.rv_posts);
            tvEmpty = findViewById(R.id.tv_empty_state);
        } else {
            tvAvatar = findViewById(R.id.tv_avatar);
            tvStudentInfo = findViewById(R.id.tv_student_info);
            tvFollowInfo = findViewById(R.id.tv_follow_info);

            btnFollow = findViewById(R.id.btn_follow);
            btnMessage = findViewById(R.id.btn_message);

            rvProfilePosts = findViewById(R.id.rv_profile_posts);
            tvEmpty = findViewById(R.id.tv_empty);
        }
    }

    private void setupRecyclerView() {
        if (rvProfilePosts == null) {
            android.util.Log.e("PROFILE_LAYOUT", "rvProfilePosts is null. Check layout IDs.");
            return;
        }

        rvProfilePosts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CommunityPostAdapter(new CommunityPostAdapter.OnPostClickListener() {
            @Override
            public void onPostClick(CommunityPostResponse.PostItem post) {
                if (post == null) return;

                Intent intent = new Intent(CommunityProfileActivity.this, PostDetailActivity.class);
                intent.putExtra("post_id", post.postId);
                startActivity(intent);
            }

            @Override
            public void onAuthorClick(CommunityPostResponse.PostItem post) {
                // Đang ở profile rồi nên không cần mở lại profile.
            }

            @Override
            public void onUpvote(CommunityPostResponse.PostItem post) {
                if (post == null) return;

                Map<String, String> body = new HashMap<>();
                body.put("vote_type", "upvote");

                RetrofitClient.getCommunityApi()
                        .votePost(authToken, post.postId, body)
                        .enqueue(new SimpleActionCallback(() -> loadTab(currentTab)));
            }

            @Override
            public void onDownvote(CommunityPostResponse.PostItem post) {
                if (post == null) return;

                Map<String, String> body = new HashMap<>();
                body.put("vote_type", "downvote");

                RetrofitClient.getCommunityApi()
                        .votePost(authToken, post.postId, body)
                        .enqueue(new SimpleActionCallback(() -> loadTab(currentTab)));
            }

            @Override
            public void onSave(CommunityPostResponse.PostItem post) {
                if (post == null) return;

                RetrofitClient.getCommunityApi()
                        .toggleSavePost(authToken, post.postId)
                        .enqueue(new SimpleActionCallback(() -> loadTab(currentTab)));
            }

            @Override
            public void onMute(CommunityPostResponse.PostItem post) {
                if (post == null) return;

                RetrofitClient.getCommunityApi()
                        .muteAuthor(authToken, post.postId)
                        .enqueue(new SimpleActionCallback(() -> loadTab(currentTab)));
            }

            @Override
            public void onTagClick(String hashtag) {
                if (hashtag == null || hashtag.trim().isEmpty()) return;

                Toast.makeText(
                        CommunityProfileActivity.this,
                        "#" + hashtag,
                        Toast.LENGTH_SHORT
                ).show();
            }

            @Override
            public void onRepost(CommunityPostResponse.PostItem post) {
                if (post == null) return;

                RetrofitClient.getCommunityApi()
                        .toggleRepostPost(authToken, post.postId)
                        .enqueue(new SimpleActionCallback(() -> loadTab(currentTab)));
            }
        });

        mediaAdapter = new CommunityMediaAdapter(post -> {
            if (post == null) return;

            Intent intent = new Intent(CommunityProfileActivity.this, PostDetailActivity.class);
            intent.putExtra("post_id", post.postId);
            startActivity(intent);
        });

        rvProfilePosts.setAdapter(adapter);
    }

    private void setupListeners() {
        if (tabPosts != null) {
            tabPosts.setOnClickListener(v -> loadTab("posts"));
        }

        if (tabReplies != null) {
            tabReplies.setOnClickListener(v -> loadTab("replies"));
        }

        if (tabMedia != null) {
            tabMedia.setOnClickListener(v -> loadTab("media"));
        }

        if (tabReposts != null) {
            tabReposts.setOnClickListener(v -> loadTab("reposts"));
        }

        View.OnClickListener openCreatePostListener = v -> openCreatePost();

        if (layoutComposer != null) {
            layoutComposer.setOnClickListener(openCreatePostListener);
        }

        if (tvWhatsNew != null) {
            tvWhatsNew.setOnClickListener(openCreatePostListener);
        }

        if (btnComposerPost != null) {
            btnComposerPost.setOnClickListener(openCreatePostListener);
        }

        if (cardCreateThread != null) {
            cardCreateThread.setOnClickListener(openCreatePostListener);
        }

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> openEditProfile());
        }

        if (tvFollowers != null) {
            tvFollowers.setOnClickListener(v ->
                    Toast.makeText(this, "Mở danh sách followers sau", Toast.LENGTH_SHORT).show()
            );
        }

        if (tvFollowing != null) {
            tvFollowing.setOnClickListener(v ->
                    Toast.makeText(this, "Mở danh sách following sau", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnMessage != null) {
            btnMessage.setOnClickListener(v -> {
                if (currentProfile == null) return;

                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("target_student_id", currentProfile.getStudentId());
                intent.putExtra("target_name", currentProfile.getDisplayName());
                intent.putExtra("target_username", currentProfile.getUsername());
                intent.putExtra("target_avatar_text", currentProfile.getAvatarText());
                intent.putExtra("target_follower_count", currentProfile.getFollowerCount());
                intent.putExtra("target_followed_by_me", currentProfile.isFollowedByMe());
                startActivity(intent);
            });
        }
    }

    private void loadProfile() {
        Call<ApiResponse<CommunityProfile>> call;

        if (studentId == -1) {
            call = RetrofitClient.getCommunityApi().getMyCommunityProfile(authToken);
        } else {
            call = RetrofitClient.getCommunityApi().getCommunityProfile(authToken, studentId);
        }

        call.enqueue(new Callback<ApiResponse<CommunityProfile>>() {
            @Override
            public void onResponse(
                    Call<ApiResponse<CommunityProfile>> call,
                    Response<ApiResponse<CommunityProfile>> response
            ) {
                if (
                        response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()
                                && response.body().getData() != null
                ) {
                    currentProfile = response.body().getData();

                    boolean shouldUseMyLayout = currentProfile.isMe() || isMeFromIntent;

                    if (shouldUseMyLayout && !usingMyProfileLayout) {
                        Intent intent = new Intent(CommunityProfileActivity.this, CommunityProfileActivity.class);
                        intent.putExtra("student_id", currentProfile.getStudentId());
                        intent.putExtra("is_me", true);
                        startActivity(intent);
                        finish();
                        return;
                    }

                    renderProfile(currentProfile);

                    if (studentId == -1) {
                        studentId = currentProfile.getStudentId();
                    }

                    loadTab("posts");
                } else {
                    showError("Không tải được thông tin profile");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CommunityProfile>> call, Throwable t) {
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void renderProfile(CommunityProfile profile) {
        if (tvDisplayName != null) {
            tvDisplayName.setText(profile.getDisplayName() != null ? profile.getDisplayName() : "Người dùng");
        }

        if (tvUsername != null) {
            if (usingMyProfileLayout) {
                tvUsername.setText(profile.getUsername() != null ? profile.getUsername() : "");
            } else {
                tvUsername.setText(profile.getFormattedUsername());
            }
        }

        if (tvAvatar != null) {
            tvAvatar.setText(profile.getAvatarText());
        }

        if (ivAvatar != null) {
            // Tạm dùng nền tròn + chữ avatar nếu AvatarHelper chưa map ImageView.
            // Nếu bạn đã có AvatarHelper.loadAvatar(ImageView,...), dùng dòng bên dưới:
            com.example.emotiondebugging.utils.AvatarHelper.loadAvatar(
                    ivAvatar,
                    profile.getAvatarUrl(),
                    profile.getDisplayName() != null ? profile.getDisplayName() : profile.getUsername()
            );
        }

        String bio = profile.getBio();
        if (tvBio != null) {
            tvBio.setText(bio != null && !bio.trim().isEmpty() ? bio : "Chưa có giới thiệu.");
        }

        if (tvStudentInfo != null) {
            tvStudentInfo.setText(profile.getStudentInfoText());
        }

        if (tvFollowInfo != null) {
            tvFollowInfo.setText(
                    profile.getFollowerCount() + " followers · "
                            + profile.getFollowingCount() + " following"
            );
        }

        if (tvFollowers != null) {
            tvFollowers.setText(profile.getFollowerCount() + " followers");
        }

        if (tvFollowing != null) {
            tvFollowing.setText(profile.getFollowingCount() + " following");
        }

        boolean isMe = profile.isMe() || isMeFromIntent;

        if (isMe) {
            if (btnFollow != null) {
                btnFollow.setText("Messages");
                btnFollow.setOnClickListener(v -> {
                    android.util.Log.d("MSG_BUTTON", "btnFollow/Messages clicked");
                    openMessages();
                });
            }

            if (btnEditProfile != null) {
                btnEditProfile.setText("Messages");
                btnEditProfile.setOnClickListener(v -> {
                    android.util.Log.d("MSG_BUTTON", "btnEditProfile/Messages clicked");
                    openMessages();
                });
            }

            if (btnMessage != null) {
                btnMessage.setVisibility(View.GONE);
            }
        } else {
            if (btnMessage != null) {
                btnMessage.setVisibility(View.VISIBLE);
            }

            if (btnFollow != null) {
                updateFollowButton(profile.isFollowedByMe());

                btnFollow.setOnClickListener(v -> {
                    if (currentProfile == null) return;

                    if (currentProfile.isFollowedByMe()) {
                        unfollowUser();
                    } else {
                        followUser();
                    }
                });
            }
        }
    }

    private void followUser() {
        RetrofitClient.getCommunityApi()
                .followUser(authToken, studentId)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful()) {
                            currentProfile.setFollowedByMe(true);
                            currentProfile.setFollowerCount(currentProfile.getFollowerCount() + 1);
                            updateFollowButton(true);
                            tvFollowInfo.setText(
                                    currentProfile.getFollowerCount() + " followers · "
                                            + currentProfile.getFollowingCount() + " following"
                            );
                        } else {
                            Toast.makeText(CommunityProfileActivity.this, "Không thể theo dõi", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(CommunityProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void openCreatePost() {
        Intent intent = new Intent(this, CreatePostActivity.class);
        startActivityForResult(intent, 1001);
    }

    private void unfollowUser() {
        RetrofitClient.getCommunityApi()
                .unfollowUser(authToken, studentId)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful()) {
                            currentProfile.setFollowedByMe(false);
                            currentProfile.setFollowerCount(Math.max(0, currentProfile.getFollowerCount() - 1));
                            updateFollowButton(false);
                            tvFollowInfo.setText(
                                    currentProfile.getFollowerCount() + " followers · "
                                            + currentProfile.getFollowingCount() + " following"
                            );
                        } else {
                            Toast.makeText(CommunityProfileActivity.this, "Không thể bỏ theo dõi", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(CommunityProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateFollowButton(boolean followed) {
        btnFollow.setText(followed ? "Đang theo dõi" : "Theo dõi");
    }

    private void loadTab(String tab) {
        currentTab = tab;
        setSelectedTab(tab);
        if ("media".equals(tab)) {
            switchToMediaGrid();
        } else {
            switchToPostList();
        }

        if (studentId <= 0) {
            showEmpty("Không tìm thấy người dùng");
            return;
        }

        Call<ApiResponse<CommunityPostResponse>> call;

        if ("posts".equals(tab)) {
            call = RetrofitClient.getCommunityApi()
                    .getCommunityProfilePosts(authToken, studentId);
        } else if ("replies".equals(tab)) {
            call = RetrofitClient.getCommunityApi()
                    .getCommunityProfileReplies(authToken, studentId);
        } else if ("media".equals(tab)) {
            call = RetrofitClient.getCommunityApi()
                    .getCommunityProfileMedia(authToken, studentId);
        } else {
            call = RetrofitClient.getCommunityApi()
                    .getCommunityProfileReposts(authToken, studentId);
        }

        call.enqueue(new Callback<ApiResponse<CommunityPostResponse>>() {
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
                    CommunityPostResponse data = response.body().getData();

                    if (data.posts == null || data.posts.isEmpty()) {
                        if ("media".equals(tab)) {
                            mediaAdapter.setPosts(null);
                        } else {
                            adapter.setPosts(null);
                        }

                        showEmpty(getEmptyText(tab));
                    } else {
                        if (tvEmpty != null) {
                            tvEmpty.setVisibility(View.GONE);
                        }

                        if (rvProfilePosts != null) {
                            rvProfilePosts.setVisibility(View.VISIBLE);
                        }

                        if ("media".equals(tab)) {
                            mediaAdapter.setPosts(data.posts);
                        } else {
                            adapter.setPosts(data.posts);
                        }
                    }
                } else {
                    adapter.setPosts(null);
                    showEmpty("Không tải được dữ liệu");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CommunityPostResponse>> call, Throwable t) {
                adapter.setPosts(null);
                showEmpty("Lỗi tải dữ liệu: " + t.getMessage());
            }
        });
    }

    private String getEmptyText(String tab) {
        if ("posts".equals(tab)) return "Chưa có bài viết nào";
        if ("replies".equals(tab)) return "Chưa có trả lời nào";
        if ("media".equals(tab)) return "Chưa có phương tiện nào";
        return "Chưa có bài đăng lại nào";
    }

    private void showEmpty(String message) {
        if (tvEmpty != null) {
            tvEmpty.setText(message != null ? message : "Không có dữ liệu");
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            android.util.Log.e(
                    "PROFILE_EMPTY",
                    "tvEmpty is null. usingMyProfileLayout = " + usingMyProfileLayout
            );
        }

        if (rvProfilePosts != null) {
            rvProfilePosts.setVisibility(View.GONE);
        }
    }

    private void setSelectedTab(String tab) {
        int active = usingMyProfileLayout ? Color.BLACK : Color.WHITE;
        int inactive = Color.parseColor("#9CA3AF");

        if (tabPosts != null) {
            tabPosts.setTextColor("posts".equals(tab) ? active : inactive);
            tabPosts.setTypeface(null, "posts".equals(tab) ? Typeface.BOLD : Typeface.NORMAL);
        }

        if (tabReplies != null) {
            tabReplies.setTextColor("replies".equals(tab) ? active : inactive);
            tabReplies.setTypeface(null, "replies".equals(tab) ? Typeface.BOLD : Typeface.NORMAL);
        }

        if (tabMedia != null) {
            tabMedia.setTextColor("media".equals(tab) ? active : inactive);
            tabMedia.setTypeface(null, "media".equals(tab) ? Typeface.BOLD : Typeface.NORMAL);
        }

        if (tabReposts != null) {
            tabReposts.setTextColor("reposts".equals(tab) ? active : inactive);
            tabReposts.setTypeface(null, "reposts".equals(tab) ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        showEmpty(message);
    }

    private class SimpleActionCallback implements Callback<ApiResponse<Object>> {
        private final Runnable onSuccess;

        SimpleActionCallback(Runnable onSuccess) {
            this.onSuccess = onSuccess;
        }

        @Override
        public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
            if (response.isSuccessful()) {
                if (onSuccess != null) onSuccess.run();
            } else {
                Toast.makeText(CommunityProfileActivity.this, "Thao tác thất bại", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
            Toast.makeText(CommunityProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001) {
            loadProfile();
            loadTab("posts");
        }
    }

    private void openEditProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);

        if (currentProfile != null) {
            intent.putExtra("display_name", currentProfile.getDisplayName());
            intent.putExtra("username", currentProfile.getUsername());
            intent.putExtra("bio", currentProfile.getBio());
            intent.putExtra("avatar_url", currentProfile.getAvatarUrl());
        }

        startActivityForResult(intent, 2001);
    }

    private String normalizeBearerToken(String token) {
        if (token == null) return "";

        token = token.trim();
        if (token.isEmpty()) return "";

        if (token.startsWith("Bearer ")) {
            return token;
        }

        return "Bearer " + token;
    }

    private void showProfileApiError(Response<ApiResponse<CommunityProfile>> response) {
        String message = "Không tải được thông tin profile";

        try {
            android.util.Log.e("PROFILE_API", "HTTP code = " + response.code());

            if (response.body() != null) {
                android.util.Log.e("PROFILE_API", "body message = " + response.body().getMessage());

                if (response.body().getMessage() != null && !response.body().getMessage().trim().isEmpty()) {
                    message = response.body().getMessage();
                }
            }

            if (response.errorBody() != null) {
                String error = response.errorBody().string();
                android.util.Log.e("PROFILE_API", "errorBody = " + error);

                if (error.contains("No token provided")) {
                    message = "Token bị thiếu khi tải profile";
                } else if (error.toLowerCase().contains("token") || error.toLowerCase().contains("jwt")) {
                    message = "Token không hợp lệ hoặc đã hết hạn";
                } else {
                    message = error;
                }
            }
        } catch (Exception e) {
            android.util.Log.e("PROFILE_API", "parse error body failed", e);
        }

        showError(message);
    }


    private void openMessages() {
        android.util.Log.d("MSG_BUTTON", "openMessages clicked");
        android.util.Log.d("MSG_BUTTON", "authToken empty = " + (authToken == null || authToken.trim().isEmpty()));

        try {
            Intent intent = new Intent(this, ConversationListActivity.class);
            intent.putExtra("auth_token", authToken);
            startActivity(intent);
        } catch (Exception e) {
            android.util.Log.e("MSG_BUTTON", "Cannot open ConversationListActivity", e);
            Toast.makeText(this, "Không mở được màn Messages: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void switchToPostList() {
        if (rvProfilePosts == null) return;

        rvProfilePosts.setLayoutManager(new LinearLayoutManager(this));
        rvProfilePosts.setAdapter(adapter);
    }

    private void switchToMediaGrid() {
        if (rvProfilePosts == null) return;

        rvProfilePosts.setLayoutManager(new GridLayoutManager(this, 3));
        rvProfilePosts.setAdapter(mediaAdapter);
    }

}