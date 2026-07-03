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

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.community.CommunityProfile;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.model.response.CommunityPostResponse;
import com.example.emotiondebugging.ui.community.adapter.CommunityPostAdapter;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

public class CommunityProfileActivity extends AppCompatActivity {

    private int studentId;
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
    private TextView tabSaved;
    private TextView tabUpvoted;
    private View layoutMusic;
    private View layoutFinishProfile;

    private Button btnFollow;
    private Button btnMessage;
    private Button btnBlock;
    private RecyclerView rvProfilePosts;
    private CommunityPostAdapter adapter;

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

    private View dividerComposer;
    private TextView tvFinishLeft;
    private View layoutFinishCards;
    private View cardFollowProfiles;
    private View cardEditProfileTask;
    private TextView tvFinishCreateCheck;
    private TextView tvFinishFollowCheck;
    private TextView tvFinishBioCheck;
    private boolean finishShouldShow = false;

    private TextView btnPlayMusic;
    private TextView tvMusicName;
    private View layoutEqualizer;
    private View[] eqBars;
    private android.media.MediaPlayer musicPlayer;
    private boolean isMusicPlaying = false;
    private boolean isMusicPreparing = false;
    private final android.os.Handler eqHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable eqRunnable;

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
        tabSaved = findViewById(R.id.tab_saved);

        if (usingMyProfileLayout) {
            tvHeaderUsername = findViewById(R.id.tv_header_username);
            tvStudentInfo = findViewById(R.id.tv_student_info);

            ivAvatar = findViewById(R.id.iv_avatar);
            ivMyAvatar = findViewById(R.id.iv_my_avatar);

            tvFollowers = findViewById(R.id.tv_followers);
            tvFollowing = findViewById(R.id.tv_following);

            btnEditProfile = findViewById(R.id.btn_edit_profile);
            btnFollow = btnEditProfile;
            btnMessage = findViewById(R.id.btn_message);

            layoutComposer = findViewById(R.id.layout_composer);
            dividerComposer = findViewById(R.id.divider_composer);
            tvWhatsNew = findViewById(R.id.tv_whats_new);
            btnComposerPost = findViewById(R.id.btn_composer_post);
            cardCreateThread = findViewById(R.id.card_create_thread);

            layoutFinishProfile = findViewById(R.id.layout_finish_profile);
            tvFinishLeft = findViewById(R.id.tv_finish_left);
            layoutFinishCards = findViewById(R.id.layout_finish_cards);
            cardFollowProfiles = findViewById(R.id.card_follow_profiles);
            cardEditProfileTask = findViewById(R.id.card_add_bio);
            tvFinishCreateCheck = findViewById(R.id.tv_finish_create_check);
            tvFinishFollowCheck = findViewById(R.id.tv_finish_follow_check);
            tvFinishBioCheck = findViewById(R.id.tv_finish_bio_check);

            rvProfilePosts = findViewById(R.id.rv_posts);
            tvEmpty = findViewById(R.id.tv_empty_state);

            btnPlayMusic = findViewById(R.id.btn_play_music);
            tvMusicName = findViewById(R.id.tv_music_name);
            layoutEqualizer = findViewById(R.id.layout_equalizer);
            eqBars = new View[]{
                    findViewById(R.id.eq_bar_1),
                    findViewById(R.id.eq_bar_2),
                    findViewById(R.id.eq_bar_3),
                    findViewById(R.id.eq_bar_4),
                    findViewById(R.id.eq_bar_5)
            };
        } else {
            tvStudentInfo = findViewById(R.id.tv_student_info);
            tvFollowInfo = findViewById(R.id.tv_follow_info);

            ivAvatar = findViewById(R.id.iv_avatar);
            tvFollowers = findViewById(R.id.tv_followers);
            tvFollowing = findViewById(R.id.tv_following);

            tabUpvoted = findViewById(R.id.tab_upvoted);

            btnFollow = findViewById(R.id.btn_follow);
            btnMessage = findViewById(R.id.btn_message);
            btnBlock = findViewById(R.id.btn_block);

            layoutMusic = findViewById(R.id.layout_music);
            btnPlayMusic = findViewById(R.id.btn_play_music);
            tvMusicName = findViewById(R.id.tv_music_name);
            layoutEqualizer = findViewById(R.id.layout_equalizer);
            eqBars = new View[]{
                    findViewById(R.id.eq_bar_1),
                    findViewById(R.id.eq_bar_2),
                    findViewById(R.id.eq_bar_3),
                    findViewById(R.id.eq_bar_4),
                    findViewById(R.id.eq_bar_5)
            };

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
            public void onTagClick(int errorTypeId, String errorName) {
                Toast.makeText(CommunityProfileActivity.this, "#" + errorName, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onRepost(CommunityPostResponse.PostItem post) {
                if (post == null) return;

                RetrofitClient.getCommunityApi()
                        .toggleRepostPost(authToken, post.postId)
                        .enqueue(new SimpleActionCallback(() -> loadTab(currentTab)));
            }

            @Override
            public void onReport(CommunityPostResponse.PostItem post) {
                if (post == null) return;
                showReportDialog(post);
            }

            @Override
            public void onEdit(CommunityPostResponse.PostItem post) {
                if (post == null) return;
                Intent intent = new Intent(CommunityProfileActivity.this, EditPostActivity.class);
                intent.putExtra("post_id", post.postId);
                intent.putExtra("title", post.title);
                intent.putExtra("content", post.content);
                intent.putExtra("topic_id", post.topicId);
                intent.putExtra("topic_name", post.topicName);
                startActivityForResult(intent, 1001);
            }

            @Override
            public void onDelete(CommunityPostResponse.PostItem post) {
                if (post == null) return;
                new androidx.appcompat.app.AlertDialog.Builder(CommunityProfileActivity.this)
                        .setTitle("Xóa bài viết")
                        .setMessage("Bạn có chắc muốn xóa bài viết này? Hành động không thể hoàn tác.")
                        .setPositiveButton("Xóa", (dialog, which) ->
                                RetrofitClient.getCommunityApi()
                                        .deletePost(authToken, post.postId)
                                        .enqueue(new SimpleActionCallback(() -> {
                                            Toast.makeText(CommunityProfileActivity.this, "Đã xóa bài viết", Toast.LENGTH_SHORT).show();
                                            loadTab(currentTab);
                                        })))
                        .setNegativeButton("Huỷ", null)
                        .show();
            }
        });

        rvProfilePosts.setAdapter(adapter);
    }

    private void setupListeners() {
        if (tabPosts != null) {
            tabPosts.setOnClickListener(v -> loadTab("posts"));
        }

        if (tabReposts != null) {
            tabReposts.setOnClickListener(v -> loadTab("reposts"));
        }

        if (tabSaved != null) {
            tabSaved.setOnClickListener(v -> loadTab("saved"));
        }

        if (tabUpvoted != null) {
            tabUpvoted.setOnClickListener(v -> loadTab("upvoted"));
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

        if (usingMyProfileLayout) {
            View cardEditProfile = findViewById(R.id.card_add_bio);
            if (cardEditProfile != null) {
                cardEditProfile.setOnClickListener(v -> openEditProfile());
            }

            if (cardFollowProfiles != null) {
                cardFollowProfiles.setOnClickListener(v -> openFollowList("following"));
            }

            View btnMore = findViewById(R.id.btn_more);
            if (btnMore != null) {
                btnMore.setOnClickListener(v -> {
                    android.widget.PopupMenu popup = new android.widget.PopupMenu(this, v);
                    popup.getMenu().add(0, 1, 0, "Tài khoản bị chặn");
                    popup.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == 1) {
                            startActivity(new Intent(this, BlockedAuthorsActivity.class));
                            return true;
                        }
                        return false;
                    });
                    popup.show();
                });
            }
        }

        if (tvFollowers != null) {
            tvFollowers.setOnClickListener(v -> openFollowList("followers"));
        }

        if (tvFollowing != null) {
            tvFollowing.setOnClickListener(v -> openFollowList("following"));
        }

        if (btnPlayMusic != null) {
            btnPlayMusic.setOnClickListener(v -> toggleMusic());
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
        boolean viewingOwnProfile = profile.isMe() || isMeFromIntent;
        if (adapter != null) {
            // Chỉ profile của chính mình mới cho Sửa/Xóa. Xem người khác thì mọi bài là của họ.
            adapter.setCurrentStudentId(viewingOwnProfile ? profile.getStudentId() : -1);
        }

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

        if (ivMyAvatar != null) {
            com.example.emotiondebugging.utils.AvatarHelper.loadAvatar(
                    ivMyAvatar,
                    profile.getAvatarUrl(),
                    profile.getDisplayName() != null ? profile.getDisplayName() : profile.getUsername()
            );
        }

        String bio = profile.getBio();
        boolean hasBio = bio != null && !bio.trim().isEmpty();
        if (tvBio != null) {
            if (usingMyProfileLayout) {
                tvBio.setText(hasBio ? bio : "Chưa có giới thiệu.");
            } else if (hasBio) {
                tvBio.setText(bio);
                tvBio.setVisibility(View.VISIBLE);
            } else {
                tvBio.setVisibility(View.GONE);
            }
        }

        String musicName = profile.getMusicName();
        boolean hasMusic = profile.getMusicUrl() != null && !profile.getMusicUrl().trim().isEmpty();

        if (usingMyProfileLayout) {
            if (tvMusicName != null) {
                if (hasMusic) {
                    tvMusicName.setText(musicName != null && !musicName.trim().isEmpty() ? musicName : "Nhạc của tôi");
                } else {
                    tvMusicName.setText("Chưa có nhạc");
                }
            }
        } else {
            if (layoutMusic != null) {
                layoutMusic.setVisibility(hasMusic ? View.VISIBLE : View.GONE);
            }
            if (hasMusic && tvMusicName != null) {
                tvMusicName.setText(musicName != null && !musicName.trim().isEmpty() ? musicName : "Nhạc");
            }
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

        renderFinishTasks(profile);

        boolean isMe = profile.isMe() || isMeFromIntent;

        if (isMe) {
            if (btnEditProfile != null) {
                btnEditProfile.setText("Edit");
                btnEditProfile.setOnClickListener(v -> openEditProfile());
            }

            if (btnMessage != null) {
                btnMessage.setVisibility(View.VISIBLE);
                btnMessage.setText("Messages");
                btnMessage.setOnClickListener(v -> {
                    android.util.Log.d("MSG_BUTTON", "btnMessage/Messages clicked");
                    openMessages();
                });
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

            if (btnBlock != null) {
                btnBlock.setVisibility(View.VISIBLE);
                btnBlock.setOnClickListener(v -> confirmBlock());
            }
        }
    }

    private void showReportDialog(CommunityPostResponse.PostItem post) {
        final String[] reasons = {
                "Nội dung spam hoặc quảng cáo",
                "Ngôn từ thù ghét, quấy rối",
                "Thông tin sai lệch",
                "Nội dung nhạy cảm, không phù hợp",
                "Lý do khác"
        };

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Báo cáo bài viết")
                .setItems(reasons, (dialog, which) -> {
                    java.util.Map<String, String> body = new java.util.HashMap<>();
                    body.put("reason", reasons[which]);
                    RetrofitClient.getCommunityApi()
                            .reportPost(authToken, post.postId, body)
                            .enqueue(new SimpleActionCallback(() ->
                                    Toast.makeText(CommunityProfileActivity.this, "Đã gửi báo cáo", Toast.LENGTH_SHORT).show()));
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void confirmBlock() {
        if (currentProfile == null) return;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Chặn người dùng")
                .setMessage("Bạn sẽ không còn thấy bài viết hay tìm được tài khoản này nữa. Tiếp tục?")
                .setPositiveButton("Chặn", (dialog, which) -> blockUser())
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void blockUser() {
        RetrofitClient.getCommunityApi()
                .blockUser(authToken, studentId)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(CommunityProfileActivity.this, "Đã chặn người dùng", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(CommunityProfileActivity.this, "Không thể chặn", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(CommunityProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
                            updateFollowCounts();
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
                            updateFollowCounts();
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

    private void updateFollowCounts() {
        if (currentProfile == null) return;

        if (tvFollowers != null) {
            tvFollowers.setText(currentProfile.getFollowerCount() + " followers");
        }
        if (tvFollowing != null) {
            tvFollowing.setText(currentProfile.getFollowingCount() + " following");
        }
        if (tvFollowInfo != null) {
            tvFollowInfo.setText(
                    currentProfile.getFollowerCount() + " followers · "
                            + currentProfile.getFollowingCount() + " following"
            );
        }
    }

    private void loadTab(String tab) {
        currentTab = tab;
        setSelectedTab(tab);
        applyTabChrome(tab);

        if (studentId <= 0) {
            showEmpty("Không tìm thấy người dùng");
            return;
        }

        Call<ApiResponse<CommunityPostResponse>> call;

        if ("posts".equals(tab)) {
            call = RetrofitClient.getCommunityApi()
                    .getCommunityProfilePosts(authToken, studentId);
        } else if ("saved".equals(tab)) {
            call = RetrofitClient.getCommunityApi()
                    .getMySavedPosts(authToken);
        } else if ("upvoted".equals(tab)) {
            call = RetrofitClient.getCommunityApi()
                    .getCommunityProfileUpvoted(authToken, studentId);
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
                        adapter.setPosts(null);
                        showEmpty(getEmptyText(tab));
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvProfilePosts.setVisibility(View.VISIBLE);
                        adapter.setPosts(data.posts);
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
        if ("saved".equals(tab)) return "Chưa lưu bài viết nào";
        if ("upvoted".equals(tab)) return "Chưa upvote bài viết nào";
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
        if (usingMyProfileLayout) {
            applyTabPill(tabPosts, "posts".equals(tab));
            applyTabPill(tabReposts, "reposts".equals(tab));
            applyTabPill(tabSaved, "saved".equals(tab));
            return;
        }

        applyTabPill(tabPosts, "posts".equals(tab));
        applyTabPill(tabReposts, "reposts".equals(tab));
        applyTabPill(tabUpvoted, "upvoted".equals(tab));
    }

    private void applyTabPill(TextView tab, boolean selected) {
        if (tab == null) return;

        if (selected) {
            tab.setBackgroundResource(R.drawable.bg_profile_tab_selected);
            tab.setTextColor(Color.WHITE);
        } else {
            tab.setBackground(null);
            tab.setTextColor(Color.parseColor("#9CA3AF"));
        }
        tab.setTypeface(null, Typeface.BOLD);
    }

    // 3 nhiệm vụ beginner: tạo bài đầu tiên, follow >= 10 người, có bio.
    // Nhiệm vụ chưa xong được đẩy lên trước; đã xong hiện dấu tích. Cập nhật "N left".
    // Khi cả 3 xong (hoặc student từng hoàn thành hết) thì ẩn vĩnh viễn.
    private void renderFinishTasks(CommunityProfile profile) {
        if (!usingMyProfileLayout || layoutFinishProfile == null || layoutFinishCards == null) {
            finishShouldShow = false;
            return;
        }

        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        if (prefs.isOnboardingDone(profile.getStudentId())) {
            finishShouldShow = false;
            layoutFinishProfile.setVisibility(View.GONE);
            return;
        }

        boolean createDone = profile.getPostCount() > 0;
        boolean followDone = profile.getFollowingCount() >= 10;
        boolean bioDone = profile.getBio() != null && !profile.getBio().trim().isEmpty();

        if (tvFinishCreateCheck != null) {
            tvFinishCreateCheck.setVisibility(createDone ? View.VISIBLE : View.GONE);
        }
        if (tvFinishFollowCheck != null) {
            tvFinishFollowCheck.setVisibility(followDone ? View.VISIBLE : View.GONE);
        }
        if (tvFinishBioCheck != null) {
            tvFinishBioCheck.setVisibility(bioDone ? View.VISIBLE : View.GONE);
        }

        int done = (createDone ? 1 : 0) + (followDone ? 1 : 0) + (bioDone ? 1 : 0);
        int left = 3 - done;

        if (left == 0) {
            prefs.setOnboardingDone(profile.getStudentId());
            finishShouldShow = false;
            layoutFinishProfile.setVisibility(View.GONE);
            return;
        }

        if (tvFinishLeft != null) {
            tvFinishLeft.setText(left + " left");
        }

        // Sắp xếp lại: nhiệm vụ chưa xong lên trước, đã xong xuống sau.
        reorderFinishCards(createDone, followDone, bioDone);

        finishShouldShow = true;
        layoutFinishProfile.setVisibility(View.VISIBLE);
    }

    private void reorderFinishCards(boolean createDone, boolean followDone, boolean bioDone) {
        android.view.ViewGroup container = (android.view.ViewGroup) layoutFinishCards;

        java.util.List<View> incomplete = new java.util.ArrayList<>();
        java.util.List<View> complete = new java.util.ArrayList<>();

        addToBucket(cardCreateThread, createDone, incomplete, complete);
        addToBucket(cardFollowProfiles, followDone, incomplete, complete);
        addToBucket(cardEditProfileTask, bioDone, incomplete, complete);

        java.util.List<View> ordered = new java.util.ArrayList<>();
        ordered.addAll(incomplete);
        ordered.addAll(complete);

        container.removeAllViews();
        for (int i = 0; i < ordered.size(); i++) {
            View card = ordered.get(i);
            android.widget.LinearLayout.LayoutParams lp =
                    (android.widget.LinearLayout.LayoutParams) card.getLayoutParams();
            int gap = (int) (10 * getResources().getDisplayMetrics().density);
            lp.setMarginEnd(i < ordered.size() - 1 ? gap : 0);
            card.setLayoutParams(lp);
            container.addView(card);
        }
    }

    private void addToBucket(View card, boolean done,
                             java.util.List<View> incomplete, java.util.List<View> complete) {
        if (card == null) return;
        if (done) {
            complete.add(card);
        } else {
            incomplete.add(card);
        }
    }

    // Repost/Saved chỉ hiển thị bài đăng: ẩn composer + finish. Your Log hiện lại (nếu finish còn dở).
    private void applyTabChrome(String tab) {
        if (!usingMyProfileLayout) return;

        boolean isPostsTab = "posts".equals(tab);

        if (layoutComposer != null) {
            layoutComposer.setVisibility(isPostsTab ? View.VISIBLE : View.GONE);
        }

        if (dividerComposer != null) {
            dividerComposer.setVisibility(isPostsTab ? View.VISIBLE : View.GONE);
        }

        if (layoutFinishProfile != null) {
            layoutFinishProfile.setVisibility(
                    (isPostsTab && finishShouldShow) ? View.VISIBLE : View.GONE);
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

        if (requestCode == 1001 || requestCode == 2001 || requestCode == 3001) {
            loadProfile();
            loadTab(currentTab);
        }
    }

    private void openEditProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);

        if (currentProfile != null) {
            intent.putExtra("display_name", currentProfile.getDisplayName());
            intent.putExtra("username", currentProfile.getUsername());
            intent.putExtra("bio", currentProfile.getBio());
            intent.putExtra("avatar_url", currentProfile.getAvatarUrl());
            intent.putExtra("music_name", currentProfile.getMusicName());
            intent.putExtra("music_url", currentProfile.getMusicUrl());
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


    private void toggleMusic() {
        if (currentProfile == null) return;

        String musicUrl = currentProfile.getMusicUrl();
        if (musicUrl == null || musicUrl.trim().isEmpty()) {
            Toast.makeText(this, "Chưa có nhạc", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isMusicPlaying || isMusicPreparing) {
            stopMusic();
            return;
        }

        try {
            isMusicPreparing = true;
            musicPlayer = new android.media.MediaPlayer();
            musicPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
            musicPlayer.setDataSource(RetrofitClient.resolveMediaUrl(musicUrl));
            musicPlayer.setOnPreparedListener(mp -> {
                // Người dùng có thể đã bấm dừng trong lúc chờ prepare.
                if (!isMusicPreparing) {
                    mp.reset();
                    mp.release();
                    return;
                }
                isMusicPreparing = false;
                mp.start();
                isMusicPlaying = true;
                if (btnPlayMusic != null) btnPlayMusic.setText("⏸");
                startEqualizer();
            });
            musicPlayer.setOnCompletionListener(mp -> stopMusic());
            musicPlayer.setOnErrorListener((mp, what, extra) -> {
                stopMusic();
                Toast.makeText(this, "Không phát được nhạc", Toast.LENGTH_SHORT).show();
                return true;
            });
            musicPlayer.prepareAsync();
        } catch (Exception e) {
            stopMusic();
            Toast.makeText(this, "Không phát được nhạc", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopMusic() {
        isMusicPlaying = false;
        isMusicPreparing = false;
        stopEqualizer();
        if (btnPlayMusic != null) btnPlayMusic.setText("▶");
        if (musicPlayer != null) {
            try {
                musicPlayer.reset();
                musicPlayer.release();
            } catch (Exception ignored) {
            }
            musicPlayer = null;
        }
    }

    private void startEqualizer() {
        if (eqBars == null) return;
        if (layoutEqualizer != null) layoutEqualizer.setVisibility(View.VISIBLE);

        stopEqualizer();
        final java.util.Random random = new java.util.Random();
        eqRunnable = new Runnable() {
            @Override
            public void run() {
                for (View bar : eqBars) {
                    if (bar == null) continue;
                    int h = 6 + random.nextInt(20);
                    android.view.ViewGroup.LayoutParams lp = bar.getLayoutParams();
                    lp.height = (int) (h * getResources().getDisplayMetrics().density);
                    bar.setLayoutParams(lp);
                }
                eqHandler.postDelayed(this, 150);
            }
        };
        eqHandler.post(eqRunnable);
    }

    private void stopEqualizer() {
        if (eqRunnable != null) {
            eqHandler.removeCallbacks(eqRunnable);
            eqRunnable = null;
        }
        if (layoutEqualizer != null) layoutEqualizer.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
    }

    private void openFollowList(String mode) {
        int targetStudentId = currentProfile != null ? currentProfile.getStudentId() : studentId;
        if (targetStudentId <= 0) return;

        Intent intent = new Intent(this, FollowListActivity.class);
        intent.putExtra("student_id", targetStudentId);
        intent.putExtra("mode", mode);
        intent.putExtra("auth_token", authToken);
        startActivity(intent);
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
}