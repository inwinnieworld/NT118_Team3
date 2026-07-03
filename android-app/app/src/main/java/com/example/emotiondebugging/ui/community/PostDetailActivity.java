package com.example.emotiondebugging.ui.community;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.api.CommunityApiService;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.model.response.PostDetailResponse;
import com.example.emotiondebugging.ui.community.adapter.CommentAdapter;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDetailActivity extends AppCompatActivity {

    private CommunityApiService api;
    private CommentAdapter commentAdapter;
    private String authToken;
    private int postId;
    private Integer replyingToCommentId = null; // null = bình luận bài viết, có giá trị = reply comment

    private TextView tvTitle, tvContent, tvTag, tvTime, tvViewCount, tvUpvote, tvDownvote;
    private ImageButton btnUpvote, btnDownvote;
    private LinearLayout layoutCommentInput;
    private EditText etComment;
    private TextView btnPostComment, btnCloseKeyboard, tvCommentLabel;
    private android.widget.CheckBox cbAnonymous;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        String token = prefs.getToken();
        authToken = token != null ? "Bearer " + token : "";
        api = RetrofitClient.getCommunityApi();
        postId = getIntent().getIntExtra("post_id", -1);

        initViews();
        loadPostDetail();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvContent = findViewById(R.id.tv_content);
        tvTag = findViewById(R.id.tv_tag);
        tvTime = findViewById(R.id.tv_time);
        tvViewCount = findViewById(R.id.tv_view_count);
        tvUpvote = findViewById(R.id.tv_upvote_count);
        tvDownvote = findViewById(R.id.tv_downvote_count);
        btnUpvote = findViewById(R.id.btn_upvote);
        btnDownvote = findViewById(R.id.btn_downvote);
        tvCommentLabel = findViewById(R.id.tv_comment_label);
        layoutCommentInput = findViewById(R.id.layout_comment_input);
        etComment = findViewById(R.id.et_comment);
        btnPostComment = findViewById(R.id.btn_post_comment);
        btnCloseKeyboard = findViewById(R.id.btn_close_keyboard);
        cbAnonymous = findViewById(R.id.cb_anonymous);

        RecyclerView rvComments = findViewById(R.id.rv_comments);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(
            new CommentAdapter.OnCommentVoteListener() {
                @Override
                public void onUpvote(PostDetailResponse.CommentItem comment) {
                    voteComment(comment.commentId, "UPVOTE");
                }
                @Override
                public void onDownvote(PostDetailResponse.CommentItem comment) {
                    voteComment(comment.commentId, "DOWNVOTE");
                }
            },
            comment -> showCommentInput(comment.commentId, comment.authorName)
        );
        commentAdapter.setOnReportClickListener(this::showCommentReportDialog);
        rvComments.setAdapter(commentAdapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnUpvote.setOnClickListener(v -> votePost("UPVOTE"));
        btnDownvote.setOnClickListener(v -> votePost("DOWNVOTE"));

        // Bấm "Bình luận" trên bài viết → hiện input
        tvCommentLabel.setOnClickListener(v -> showCommentInput(null, null));

        btnPostComment.setOnClickListener(v -> postComment());

        btnCloseKeyboard.setOnClickListener(v -> hideCommentInput());
    }

    private void showCommentInput(Integer parentCommentId, String replyToName) {
        replyingToCommentId = parentCommentId;
        layoutCommentInput.setVisibility(View.VISIBLE);
        etComment.setHint(replyToName != null ? "Trả lời " + replyToName + "..." : "Tạo bình luận...");
        etComment.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(etComment, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideCommentInput() {
        replyingToCommentId = null;
        layoutCommentInput.setVisibility(View.GONE);
        etComment.setText("");
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
    }

    private void loadPostDetail() {
        api.getPostDetail(authToken, postId).enqueue(new Callback<ApiResponse<PostDetailResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<PostDetailResponse>> call,
                                   Response<ApiResponse<PostDetailResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    PostDetailResponse data = response.body().getData();
                    bindPost(data.post);
                    commentAdapter.setComments(data.comments);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<PostDetailResponse>> call, Throwable t) {
                Toast.makeText(PostDetailActivity.this, "Lỗi tải bài viết", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindPost(PostDetailResponse.PostDetail post) {
        tvTitle.setText(post.title);
        tvContent.setText(post.content);
        tvTag.setText(post.errorName != null ? "#" + post.errorName : "");
        tvViewCount.setText(String.valueOf(post.viewCount));
        tvUpvote.setText(String.valueOf(post.upvoteCount));
        tvDownvote.setText(String.valueOf(post.downvoteCount));
        tvTime.setText(formatTime(post.createdAt));
    }

    private void votePost(String voteType) {
        Map<String, String> body = new HashMap<>();
        body.put("vote_type", voteType);
        api.votePost(authToken, postId, body).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                loadPostDetail();
            }
            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {}
        });
    }

    private void voteComment(int commentId, String voteType) {
        Map<String, String> body = new HashMap<>();
        body.put("vote_type", voteType);
        api.voteComment(authToken, postId, commentId, body).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                loadPostDetail();
            }
            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {}
        });
    }

    private void showCommentReportDialog(PostDetailResponse.CommentItem comment) {
        if (comment == null) return;
        final String[] reasons = {
                "Nội dung spam hoặc quảng cáo",
                "Ngôn từ thù ghét, quấy rối",
                "Thông tin sai lệch",
                "Nội dung nhạy cảm, không phù hợp",
                "Lý do khác"
        };

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Báo cáo bình luận")
                .setItems(reasons, (dialog, which) -> {
                    Map<String, String> body = new HashMap<>();
                    body.put("reason", reasons[which]);
                    api.reportComment(authToken, postId, comment.commentId, body)
                            .enqueue(new Callback<ApiResponse<Object>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                                    Toast.makeText(PostDetailActivity.this,
                                            response.isSuccessful() ? "Đã gửi báo cáo" : "Không thể gửi báo cáo",
                                            Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                                    Toast.makeText(PostDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void postComment() {
        String content = etComment.getText().toString().trim();
        if (content.isEmpty()) return;

        Map<String, String> body = new HashMap<>();
        body.put("content", content);
        if (replyingToCommentId != null) {
            body.put("parent_comment_id", String.valueOf(replyingToCommentId));
        }
        body.put("is_anonymous", cbAnonymous.isChecked() ? "1" : "0");

        api.createComment(authToken, postId, body).enqueue(
                new Callback<ApiResponse<PostDetailResponse.CommentItem>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PostDetailResponse.CommentItem>> call,
                                           Response<ApiResponse<PostDetailResponse.CommentItem>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            hideCommentInput();
                            commentAdapter.addComment(response.body().getData());
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<PostDetailResponse.CommentItem>> call, Throwable t) {
                        Toast.makeText(PostDetailActivity.this, "Lỗi đăng bình luận", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String formatTime(String createdAt) {
        if (createdAt == null) return "";
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Date date = sdf.parse(createdAt);
            long diff = System.currentTimeMillis() - date.getTime();
            long hours = diff / (1000 * 60 * 60);
            if (hours < 24) return hours + " h. ago";
            return (hours / 24) + " d. ago";
        } catch (Exception e) {
            return createdAt;
        }
    }
}
