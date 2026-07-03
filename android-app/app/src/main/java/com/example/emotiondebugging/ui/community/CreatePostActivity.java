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
import androidx.lifecycle.ViewModelProvider;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

import java.util.List;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {

    private CommunityViewModel viewModel;
    private EditText etTitle, etContent;
    private TextView tvTitleError, tvContentError, tvErrorTypeError;
    private TextView tvContentCounter, tvSelectedErrorType, tvAuthorPreview;
    private LinearLayout layoutWarning, toggleAnonymous;
    private boolean isAnonymous = true;
    private int selectedTopicId = -1;
    private java.util.List<com.example.emotiondebugging.model.community.TopicItem> topics = new java.util.ArrayList<>();
    private String authToken;

    private static final String[] SENSITIVE_KEYWORDS = {
        "tự tử", "tự làm hại", "muốn chết", "không muốn sống",
        "kết thúc tất cả", "biến mất mãi mãi"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        String token = prefs.getToken();
        authToken = token != null ? "Bearer " + token : "";

        initViews();
        initViewModel();
        setupListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        tvTitleError = findViewById(R.id.tv_title_error);
        tvContentError = findViewById(R.id.tv_content_error);
        tvErrorTypeError = findViewById(R.id.tv_error_type_error);
        tvContentCounter = findViewById(R.id.tv_content_counter);
        tvSelectedErrorType = findViewById(R.id.tv_selected_error_type);
        tvAuthorPreview = findViewById(R.id.tv_author_preview);
        layoutWarning = findViewById(R.id.layout_warning);
        toggleAnonymous = findViewById(R.id.toggle_anonymous);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(CommunityViewModel.class);

        viewModel.getMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty())
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getLoading().observe(this, isLoading -> {
            if (isLoading != null)
                findViewById(R.id.btn_post).setEnabled(!isLoading);
        });

        // Đăng bài thành công → đóng màn hình
        viewModel.getCreatePostResult().observe(this, result -> {
            if (result != null) {
                setResult(RESULT_OK);
                finish();
            }
        });

        // Load danh sách topic (loại vấn đề)
        loadTopics();
    }

    private void loadTopics() {
        com.example.emotiondebugging.data.api.RetrofitClient.getCommunityApi()
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
                        android.util.Log.e("CREATE_POST", "Load topics failed", t);
                    }
                });
    }

    private void setupListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_post).setOnClickListener(v -> submitPost());

        etContent.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                int len = s.length();
                tvContentCounter.setText(len + "/2000");
                tvContentCounter.setTextColor(len > 2000 ?
                        getColor(R.color.logout_text) : getColor(R.color.text_secondary));
                scanSensitiveContent(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btn_select_error_type).setOnClickListener(v ->
                showErrorTypeDialog());

        toggleAnonymous.setOnClickListener(v -> {
            isAnonymous = !isAnonymous;
            updateToggleUI();
        });

        findViewById(R.id.btn_contact_support).setOnClickListener(v ->
                layoutWarning.setVisibility(View.GONE));

        findViewById(R.id.btn_continue_post).setOnClickListener(v ->
                layoutWarning.setVisibility(View.GONE));
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

    private void updateToggleUI() {
        View thumb = toggleAnonymous.getChildAt(0);
        if (isAnonymous) {
            toggleAnonymous.setBackgroundResource(R.drawable.bg_toggle_on);
            thumb.setTranslationX(24f);
            tvAuthorPreview.setText("👤 Ẩn danh");
        } else {
            toggleAnonymous.setBackgroundResource(R.drawable.bg_toggle_off);
            thumb.setTranslationX(0f);
            SharedPrefsHelper prefs = new SharedPrefsHelper(this);
            tvAuthorPreview.setText("👤 " + prefs.getName());
        }
    }

    private void scanSensitiveContent(String content) {
        String lower = content.toLowerCase();
        for (String keyword : SENSITIVE_KEYWORDS) {
            if (lower.contains(keyword)) {
                layoutWarning.setVisibility(View.VISIBLE);
                return;
            }
        }
    }

    private void submitPost() {
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

        viewModel.createPost(authToken, title, content, selectedTopicId, isAnonymous);
    }
}
