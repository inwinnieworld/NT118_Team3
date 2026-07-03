package com.example.emotiondebugging.ui.community;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.request.CreatePostRequest;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPostActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private TextView tvTitleError, tvContentError, tvErrorTypeError;
    private TextView tvContentCounter, tvSelectedErrorType;
    private LinearLayout layoutWarning, toggleAnonymous;
    private int selectedTopicId = -1;
    private java.util.List<com.example.emotiondebugging.model.community.TopicItem> topics = new java.util.ArrayList<>();
    private String authToken;
    private int postId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        String token = prefs.getToken();
        authToken = token != null ? "Bearer " + token : "";

        initViews();
        prefillFromIntent();
        setupListeners();
        loadTopics();
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        tvTitleError = findViewById(R.id.tv_title_error);
        tvContentError = findViewById(R.id.tv_content_error);
        tvErrorTypeError = findViewById(R.id.tv_error_type_error);
        tvContentCounter = findViewById(R.id.tv_content_counter);
        tvSelectedErrorType = findViewById(R.id.tv_selected_error_type);
        layoutWarning = findViewById(R.id.layout_warning);
        toggleAnonymous = findViewById(R.id.toggle_anonymous);

        // Chỉnh sửa bài không đổi chế độ ẩn danh → ẩn toggle + preview tác giả
        if (toggleAnonymous != null) toggleAnonymous.setVisibility(View.GONE);
        View authorPreview = findViewById(R.id.tv_author_preview);
        if (authorPreview != null) authorPreview.setVisibility(View.GONE);

        TextView tvScreenTitle = findViewById(R.id.tv_screen_title);
        if (tvScreenTitle != null) tvScreenTitle.setText("Chỉnh sửa bài viết");

        TextView tvPostLabel = findViewById(R.id.tv_post_button_label);
        if (tvPostLabel != null) tvPostLabel.setText("Lưu bài");
    }

    private void prefillFromIntent() {
        postId = getIntent().getIntExtra("post_id", -1);
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        selectedTopicId = getIntent().getIntExtra("topic_id", -1);
        String topicName = getIntent().getStringExtra("topic_name");

        if (etTitle != null && title != null) etTitle.setText(title);
        if (etContent != null && content != null) etContent.setText(content);
        if (tvSelectedErrorType != null && topicName != null && !topicName.trim().isEmpty()) {
            tvSelectedErrorType.setText(topicName);
            tvSelectedErrorType.setTextColor(getColor(R.color.text_primary));
        }
    }

    private void loadTopics() {
        RetrofitClient.getCommunityApi()
                .getPostTopics(authToken)
                .enqueue(new retrofit2.Callback<com.example.emotiondebugging.model.response.ApiResponse<com.example.emotiondebugging.model.community.TopicListResponse>>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<com.example.emotiondebugging.model.response.ApiResponse<com.example.emotiondebugging.model.community.TopicListResponse>> call,
                            retrofit2.Response<com.example.emotiondebugging.model.response.ApiResponse<com.example.emotiondebugging.model.community.TopicListResponse>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null
                                && response.body().getData().topics != null) {
                            topics = response.body().getData().topics;
                        }
                    }

                    @Override
                    public void onFailure(
                            retrofit2.Call<com.example.emotiondebugging.model.response.ApiResponse<com.example.emotiondebugging.model.community.TopicListResponse>> call,
                            Throwable t
                    ) {
                        android.util.Log.e("EDIT_POST", "Load topics failed", t);
                    }
                });
    }

    private void setupListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_post).setOnClickListener(v -> submitEdit());

        etContent.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                int len = s.length();
                tvContentCounter.setText(len + "/2000");
                tvContentCounter.setTextColor(len > 2000 ?
                        getColor(R.color.logout_text) : getColor(R.color.text_secondary));
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btn_select_error_type).setOnClickListener(v -> showErrorTypeDialog());

        View contactSupport = findViewById(R.id.btn_contact_support);
        if (contactSupport != null) contactSupport.setOnClickListener(v -> layoutWarning.setVisibility(View.GONE));

        View continuePost = findViewById(R.id.btn_continue_post);
        if (continuePost != null) continuePost.setOnClickListener(v -> layoutWarning.setVisibility(View.GONE));
    }

    private void showErrorTypeDialog() {
        if (topics == null || topics.isEmpty()) {
            Toast.makeText(this, "Đang tải danh sách...", Toast.LENGTH_SHORT).show();
            loadTopics();
            return;
        }

        String[] names = new String[topics.size()];
        for (int i = 0; i < topics.size(); i++) {
            names[i] = topics.get(i).topicName;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Chọn loại vấn đề")
                .setItems(names, (dialog, which) -> {
                    selectedTopicId = topics.get(which).topicId;
                    tvSelectedErrorType.setText(names[which]);
                    tvSelectedErrorType.setTextColor(getColor(R.color.text_primary));
                    tvErrorTypeError.setVisibility(View.GONE);
                })
                .show();
    }

    private void submitEdit() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        boolean hasError = false;

        if (title.length() < 10) {
            tvTitleError.setText("Tiêu đề phải có ít nhất 10 ký tự");
            tvTitleError.setVisibility(View.VISIBLE);
            hasError = true;
        } else if (title.length() > 200) {
            tvTitleError.setText("Tiêu đề không được vượt quá 200 ký tự");
            tvTitleError.setVisibility(View.VISIBLE);
            hasError = true;
        } else {
            tvTitleError.setVisibility(View.GONE);
        }

        if (selectedTopicId == -1) {
            tvErrorTypeError.setText("Vui lòng chọn loại vấn đề");
            tvErrorTypeError.setVisibility(View.VISIBLE);
            hasError = true;
        } else {
            tvErrorTypeError.setVisibility(View.GONE);
        }

        if (content.length() < 20) {
            tvContentError.setText("Nội dung phải có ít nhất 20 ký tự");
            tvContentError.setVisibility(View.VISIBLE);
            hasError = true;
        } else if (content.length() > 2000) {
            tvContentError.setText("Nội dung không được vượt quá 2000 ký tự");
            tvContentError.setVisibility(View.VISIBLE);
            hasError = true;
        } else {
            tvContentError.setVisibility(View.GONE);
        }

        if (hasError) return;
        if (postId <= 0) {
            Toast.makeText(this, "Không xác định được bài viết", Toast.LENGTH_SHORT).show();
            return;
        }

        View btnPost = findViewById(R.id.btn_post);
        if (btnPost != null) btnPost.setEnabled(false);

        CreatePostRequest request = new CreatePostRequest(title, content, selectedTopicId, false);
        RetrofitClient.getCommunityApi().updatePost(authToken, postId, request)
                .enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Map<String, Object>>> call,
                                           Response<ApiResponse<Map<String, Object>>> response) {
                        if (btnPost != null) btnPost.setEnabled(true);
                        if (response.isSuccessful()) {
                            Toast.makeText(EditPostActivity.this, "Đã cập nhật bài viết", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(EditPostActivity.this, "Cập nhật thất bại (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                        if (btnPost != null) btnPost.setEnabled(true);
                        Toast.makeText(EditPostActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
