package com.example.emotiondebugging.ui.journal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.CreateCommitResponse;
import com.example.emotiondebugging.ui.gitgraph.GitGraphActivity;
import com.example.emotiondebugging.utils.SharedPrefsHelper;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GitJournalActivity extends AppCompatActivity {

    // UI Components
    private ImageView ivBack;
    private MaterialButton btnGitGraph;
    private TextView tvDateHeader;
    private LinearLayout layoutTerminalContent;
    private ScrollView scrollTerminal;
    private LinearLayout layoutScrollControls;
    private ImageView btnPrevPage;
    private ImageView btnNextPage;
    private TextView tvPageIndicator;

    // Bottom Sheet
    private LinearLayout bottomSheetGitJournal;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;

    // Bottom Sheet Steps
    private LinearLayout layoutEmotionSelector;
    private LinearLayout layoutIntensitySlider;
    private LinearLayout layoutMessageInput;

    // Step 1: Emotion Selector (Dynamic icons)
    private LinearLayout layoutEmotionIcons;

    // Step 2: Intensity Slider
    private TextView tvSliderTitle;
    private TextView tvIntensityValue;
    private SeekBar seekBarIntensity;
    private MaterialButton btnSliderBack;
    private MaterialButton btnSliderNext;

    // Step 3: Message Input
    private EditText etCommitMessage;
    private MaterialButton btnMessageBack;
    private MaterialButton btnPushCommit;

    // ViewModel
    private GitJournalViewModel viewModel;
    private SharedPrefsHelper prefsHelper;

    // Session State (persists until logout)
    private List<CommitLog> sessionCommits = new ArrayList<>();
    private int currentStep = 0; // 0: hidden, 1: emotion, 2: intensity, 3: message
    private String selectedEmotion = "";
    private int selectedEmotionIcon = 0;
    private int intensityValue = 0;
    private TextView currentPromptLine = null; // Reference to the clickable [+] line
    
    // Pagination State
    // Each page shows MAX 5 commits + [+] prompt = 6 items total
    // When 6th commit is added, it moves to new page immediately
    private static final int MAX_COMMITS_PER_PAGE = 5; // Changed from 6 to 5
    private int currentPage = 0; // Current page index (0 = oldest page)
    private int totalPages = 1;

    // Emotion Data (All 15 emotions available - no "Sốc" icon)
    private final String[] emotionNames = {
            "Ác Quỷ", "Buồn Một Chút", "Buồn Ngủ", "Buồn Nhiều Chút",
            "Chúa Hề", "Háo Hức", "Hối Lỗi", "Hơi Quạo",
            "Khinh Bỉ", "LMAO", "Suy Ngẫm", "Thiên Thần",
            "Vui Vẻ", "Yêu Thương", "Ý Kiến"
    };

    private final int[] emotionIcons = {
            R.drawable.icon_acquy, R.drawable.icon_buonmotchut, R.drawable.icon_buonngu,
            R.drawable.icon_buonnhieuchut, R.drawable.icon_chuahe, R.drawable.icon_haohuc,
            R.drawable.icon_hoiloi, R.drawable.icon_hoiquao, R.drawable.icon_khinhbi,
            R.drawable.icon_lmao, R.drawable.icon_suyngam, R.drawable.icon_thienthan,
            R.drawable.icon_vuive, R.drawable.icon_yeuthuong, R.drawable.icon_ykien
    };

    // Inner class to store commit logs
    private static class CommitLog {
        String timestamp;
        String emotion;
        int intensity;
        String message;

        CommitLog(String timestamp, String emotion, int intensity, String message) {
            this.timestamp = timestamp;
            this.emotion = emotion;
            this.intensity = intensity;
            this.message = message;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_git_journal);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        prefsHelper = new SharedPrefsHelper(this);

        initViews();
        initViewModel();
        initBottomSheet();
        initEmotionIcons();
        initListeners();

        // Initialize terminal with date and first prompt
        initializeTerminal();
    }

    private void initViews() {
        // Top Bar
        ivBack = findViewById(R.id.ivBack);
        btnGitGraph = findViewById(R.id.btnGitGraph);

        // Terminal
        tvDateHeader = findViewById(R.id.tvDateHeader);
        layoutTerminalContent = findViewById(R.id.layoutTerminalContent);
        scrollTerminal = findViewById(R.id.scrollTerminal);
        
        // Pagination Controls
        layoutScrollControls = findViewById(R.id.layoutScrollControls);
        btnPrevPage = findViewById(R.id.btnPrevPage);
        btnNextPage = findViewById(R.id.btnNextPage);
        tvPageIndicator = findViewById(R.id.tvPageIndicator);

        // Bottom Sheet
        bottomSheetGitJournal = findViewById(R.id.bottomSheetGitJournal);

        // Bottom Sheet Steps
        layoutEmotionSelector = findViewById(R.id.layoutEmotionSelector);
        layoutIntensitySlider = findViewById(R.id.layoutIntensitySlider);
        layoutMessageInput = findViewById(R.id.layoutMessageInput);

        // Step 1
        layoutEmotionIcons = findViewById(R.id.layoutEmotionIcons);

        // Step 2
        tvSliderTitle = findViewById(R.id.tvSliderTitle);
        tvIntensityValue = findViewById(R.id.tvIntensityValue);
        seekBarIntensity = findViewById(R.id.seekBarIntensity);
        btnSliderBack = findViewById(R.id.btnSliderBack);
        btnSliderNext = findViewById(R.id.btnSliderNext);

        // Step 3
        etCommitMessage = findViewById(R.id.etCommitMessage);
        btnMessageBack = findViewById(R.id.btnMessageBack);
        btnPushCommit = findViewById(R.id.btnPushCommit);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(GitJournalViewModel.class);

        // Observe commit success
        viewModel.getCommitSuccess().observe(this, success -> {
            if (success != null && success) {
                // Commit successful, add to terminal
                addCommitToTerminal();
                resetCurrentCommit();
            }
        });

        // Observe severity alerts
        viewModel.getSeverityAlert().observe(this, alert -> {
            if (alert != null && alert.isShouldAlert()) {
                // Show severity alert to user
                showSeverityAlert(alert);
            }
        });

        // Observe errors
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Load emotions from backend on startup
        String token = prefsHelper.getToken();
        if (token != null && !token.isEmpty()) {
            viewModel.loadEmotions(token);
        }
    }

    private void initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetGitJournal);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setHideable(true);
    }

    private void initEmotionIcons() {
        layoutEmotionIcons.removeAllViews();

        for (int i = 0; i < emotionNames.length; i++) {
            final int index = i;
            
            // Create container for each emotion (icon + label)
            LinearLayout emotionContainer = new LinearLayout(this);
            emotionContainer.setOrientation(LinearLayout.VERTICAL);
            emotionContainer.setGravity(android.view.Gravity.CENTER);
            
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    (int) (90 * getResources().getDisplayMetrics().density),
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            containerParams.setMargins(
                    (int) (12 * getResources().getDisplayMetrics().density), 
                    0, 
                    (int) (12 * getResources().getDisplayMetrics().density), 
                    0
            );
            emotionContainer.setLayoutParams(containerParams);
            emotionContainer.setClickable(true);
            emotionContainer.setFocusable(true);

            // Icon
            ImageView emotionIcon = new ImageView(this);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                    (int) (56 * getResources().getDisplayMetrics().density),
                    (int) (56 * getResources().getDisplayMetrics().density)
            );
            emotionIcon.setLayoutParams(iconParams);
            emotionIcon.setImageResource(emotionIcons[i]);
            emotionIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);

            // Label
            TextView emotionLabel = new TextView(this);
            emotionLabel.setText(emotionNames[i].toUpperCase());
            emotionLabel.setTextColor(Color.parseColor("#6FEAF2"));
            emotionLabel.setTextSize(10);
            emotionLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            emotionLabel.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            labelParams.topMargin = (int) (4 * getResources().getDisplayMetrics().density);
            emotionLabel.setLayoutParams(labelParams);

            emotionContainer.addView(emotionIcon);
            emotionContainer.addView(emotionLabel);

            emotionContainer.setOnClickListener(v -> {
                selectedEmotion = emotionNames[index];
                selectedEmotionIcon = emotionIcons[index];
                onEmotionSelected();
            });

            layoutEmotionIcons.addView(emotionContainer);
        }
    }

    private void initListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnGitGraph.setOnClickListener(v -> {
            // Navigate to Git Graph Activity
            Intent intent = new Intent(GitJournalActivity.this, GitGraphActivity.class);
            startActivity(intent);
        });

        // Pagination Controls
        // < button: Go to PREVIOUS page (older commits, lower page number)
        btnPrevPage.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                renderCurrentPage();
            }
        });

        // > button: Go to NEXT page (newer commits, higher page number)
        btnNextPage.setOnClickListener(v -> {
            if (currentPage < (totalPages - 1)) {
                currentPage++;
                renderCurrentPage();
            }
        });

        // Step 2: Slider listeners
        seekBarIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                intensityValue = progress;
                tvIntensityValue.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnSliderBack.setOnClickListener(v -> goToStep(1));
        btnSliderNext.setOnClickListener(v -> goToStep(3));

        // Step 3: Message listeners
        btnMessageBack.setOnClickListener(v -> goToStep(2));
        btnPushCommit.setOnClickListener(v -> pushCommit());
    }

    private void initializeTerminal() {
        // Set current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("[dd-MM-yyyy]", Locale.getDefault());
        tvDateHeader.setText(dateFormat.format(new Date()));

        // Load session from SharedPreferences
        loadSessionFromPrefs();

        // Render current page (will add [+] if on page 0)
        renderCurrentPage();
    }

    private void addPromptLine() {
        TextView promptLine = new TextView(this);
        promptLine.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // Initially show only [+]
        SpannableString spannable = new SpannableString("[+]");
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#8CF8FF")),
                0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        promptLine.setText(spannable);
        promptLine.setTextColor(Color.WHITE);
        promptLine.setTextSize(14);
        promptLine.setTypeface(android.graphics.Typeface.MONOSPACE);
        promptLine.setPadding(0, 0, 0, 8);

        // Make it clickable to open bottom sheet
        promptLine.setClickable(true);
        promptLine.setFocusable(true);
        promptLine.setOnClickListener(v -> {
            if (currentStep == 0) {
                // Update to show "Cảm thấy [Chọn cảm xúc]" when clicked
                SpannableString expandedSpannable = new SpannableString("[+] Cảm thấy [Chọn cảm xúc].");
                expandedSpannable.setSpan(new ForegroundColorSpan(Color.parseColor("#8CF8FF")),
                        0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                expandedSpannable.setSpan(new ForegroundColorSpan(Color.parseColor("#6FEAF2")),
                        13, expandedSpannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                promptLine.setText(expandedSpannable);
                
                goToStep(1);
            }
        });

        layoutTerminalContent.addView(promptLine);
        currentPromptLine = promptLine;
    }

    private void updatePromptLine(String text) {
        if (currentPromptLine != null) {
            SpannableString spannable = new SpannableString(text);
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#8CF8FF")),
                    0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            
            // Highlight emotion name
            int emotionStart = text.indexOf("[", 3);
            int emotionEnd = text.indexOf("]", emotionStart);
            if (emotionStart >= 0 && emotionEnd > emotionStart) {
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#6FEAF2")),
                        emotionStart, emotionEnd + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            // Highlight intensity
            int intensityStart = text.indexOf("[", emotionEnd + 1);
            int intensityEnd = text.indexOf("]", intensityStart);
            if (intensityStart >= 0 && intensityEnd > intensityStart) {
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#6FEAF2")),
                        intensityStart, intensityEnd + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            currentPromptLine.setText(spannable);
        }
    }

    private void onEmotionSelected() {
        // Update terminal prompt
        String promptText = "[+] Cảm thấy [" + selectedEmotion + "].";
        updatePromptLine(promptText);

        // Move to intensity step
        goToStep(2);
    }

    private void goToStep(int step) {
        currentStep = step;

        // Hide all steps
        layoutEmotionSelector.setVisibility(View.GONE);
        layoutIntensitySlider.setVisibility(View.GONE);
        layoutMessageInput.setVisibility(View.GONE);

        switch (step) {
            case 1:
                layoutEmotionSelector.setVisibility(View.VISIBLE);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;

            case 2:
                tvSliderTitle.setText("Điều chỉnh mức độ cảm xúc [" + selectedEmotion + "]");
                layoutIntensitySlider.setVisibility(View.VISIBLE);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                // Update terminal
                String promptText2 = "[+] Cảm thấy [" + selectedEmotion + "].";
                updatePromptLine(promptText2);
                break;

            case 3:
                layoutMessageInput.setVisibility(View.VISIBLE);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                // Update terminal
                String promptText3 = "[+] Cảm thấy [" + selectedEmotion + "]. Mức độ [" + intensityValue + "%].";
                updatePromptLine(promptText3);
                break;

            default:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                break;
        }
    }

    private void pushCommit() {
        String message = etCommitMessage.getText().toString().trim();

        if (message.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung commit", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get token from SharedPreferences
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call ViewModel to push commit to backend
        viewModel.pushCommit(token, selectedEmotion, intensityValue, message, "main");
    }

    private void addCommitToTerminal() {
        // Create commit log
        SimpleDateFormat timeFormat = new SimpleDateFormat("[HH:mm:ss]", Locale.getDefault());
        String timestamp = timeFormat.format(new Date());
        String message = etCommitMessage.getText().toString().trim();

        // Save to session
        sessionCommits.add(new CommitLog(timestamp, selectedEmotion, intensityValue, message));

        // Save session to SharedPreferences
        saveSessionToPrefs();

        // Calculate new total pages
        int newTotalPages = (int) Math.ceil((double) sessionCommits.size() / MAX_COMMITS_PER_PAGE);
        
        // Always jump to LAST page (highest page number) after new commit
        currentPage = newTotalPages - 1;
        
        // Re-render current page
        renderCurrentPage();

        // Hide bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void resetCurrentCommit() {
        // Reset state for next commit
        currentStep = 0;
        selectedEmotion = "";
        selectedEmotionIcon = 0;
        intensityValue = 0;
        seekBarIntensity.setProgress(0);
        etCommitMessage.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Session persistence is now handled by logout methods in ProfileActivity/AdminDashboardActivity
        // No need to check here anymore
    }

    @Override
    public void onBackPressed() {
        // If bottom sheet is open, close it first
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            currentStep = 0;
        } else {
            // Otherwise, finish activity (commits remain in session)
            super.onBackPressed();
        }
    }

    // ==================== SESSION PERSISTENCE METHODS ====================

    /**
     * Save session commits to SharedPreferences
     */
    private void saveSessionToPrefs() {
        SharedPreferences prefs = getSharedPreferences("GitJournalSession", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(sessionCommits);
        prefs.edit().putString("commits", json).apply();
    }

    /**
     * Load session commits from SharedPreferences and render to terminal
     */
    private void loadSessionFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("GitJournalSession", MODE_PRIVATE);
        String json = prefs.getString("commits", null);
        
        if (json != null && !json.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<CommitLog>>(){}.getType();
            List<CommitLog> loadedCommits = gson.fromJson(json, type);
            
            if (loadedCommits != null && !loadedCommits.isEmpty()) {
                sessionCommits = loadedCommits;
                renderSessionCommits();
            }
        }
    }

    /**
     * Render all session commits to terminal
     */
    private void renderSessionCommits() {
        // Just render current page (pagination handles everything)
        renderCurrentPage();
    }

    /**
     * Render the current page of commits
     */
    private void renderCurrentPage() {
        // Clear terminal content (keep date header)
        layoutTerminalContent.removeAllViews();
        
        // Re-add date header
        layoutTerminalContent.addView(tvDateHeader);
        
        // Calculate total pages
        totalPages = (int) Math.ceil((double) sessionCommits.size() / MAX_COMMITS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        
        // Ensure currentPage is within bounds
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0) currentPage = 0;
        
        int startIndex, endIndex;
        
        // Page 1 = oldest commits (index 0-5)
        // Page 2 = newer commits (index 6-11)
        // Page N = newest commits + [+]
        startIndex = currentPage * MAX_COMMITS_PER_PAGE;
        endIndex = Math.min(startIndex + MAX_COMMITS_PER_PAGE, sessionCommits.size());
        
        // Render commits in range
        for (int i = startIndex; i < endIndex; i++) {
            CommitLog commit = sessionCommits.get(i);
            addCommitLogView(commit);
        }
        
        // Add [+] prompt only on LAST page (highest page number)
        if (currentPage == totalPages - 1) {
            addPromptLine();
        }
        
        // Update pagination controls
        updatePaginationControls();
    }

    /**
     * Add a commit log view to terminal
     */
    private void addCommitLogView(CommitLog commit) {
        TextView commitLog = new TextView(this);
        commitLog.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        String logText = commit.timestamp + " Cảm thấy [" + commit.emotion + "]. Mức độ [" + commit.intensity + "%]. \"" + commit.message + "\"";

        SpannableString spannable = new SpannableString(logText);
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#8CF8FF")),
                0, commit.timestamp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        int emotionStart = logText.indexOf("[" + commit.emotion);
        int emotionEnd = logText.indexOf("]", emotionStart);
        if (emotionStart >= 0 && emotionEnd > emotionStart) {
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#6FEAF2")),
                    emotionStart, emotionEnd + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        int intensityStart = logText.indexOf("[" + commit.intensity);
        int intensityEnd = logText.indexOf("]", intensityStart);
        if (intensityStart >= 0 && intensityEnd > intensityStart) {
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#6FEAF2")),
                    intensityStart, intensityEnd + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        commitLog.setText(spannable);
        commitLog.setTextColor(Color.WHITE);
        commitLog.setTextSize(14);
        commitLog.setTypeface(android.graphics.Typeface.MONOSPACE);
        commitLog.setPadding(0, 0, 0, 8);

        layoutTerminalContent.addView(commitLog);
    }

    /**
     * Update pagination controls visibility and state
     */
    private void updatePaginationControls() {
        if (totalPages > 1) {
            layoutScrollControls.setVisibility(View.VISIBLE);
            tvPageIndicator.setText((currentPage + 1) + "/" + totalPages);
            
            // < button: Disabled on page 1 (oldest), enabled otherwise
            btnPrevPage.setAlpha(currentPage > 0 ? 1.0f : 0.3f);
            btnPrevPage.setEnabled(currentPage > 0);
            
            // > button: Disabled on last page (newest), enabled otherwise
            btnNextPage.setAlpha(currentPage < (totalPages - 1) ? 1.0f : 0.3f);
            btnNextPage.setEnabled(currentPage < (totalPages - 1));
        } else {
            layoutScrollControls.setVisibility(View.GONE);
        }
    }

    /**
     * Clear session from SharedPreferences (on logout)
     */
    private void clearSessionFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("GitJournalSession", MODE_PRIVATE);
        prefs.edit().remove("commits").apply();
    }

    /**
     * Show severity alert dialog to user
     */
    private void showSeverityAlert(CreateCommitResponse.SeverityAlert alert) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("⚠️ Cảnh báo mức độ tiêu cực")
                .setMessage(alert.getMessage() + "\n\nĐiểm severity: " + String.format("%.2f", alert.getSeverityScore()))
                .setPositiveButton("Đã hiểu", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }
}
