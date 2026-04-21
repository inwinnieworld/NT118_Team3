package com.example.emotiondebugging.ui.gitgraph;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.GitGraphCommit;
import com.example.emotiondebugging.model.GitGraphMerge;
import com.example.emotiondebugging.utils.SharedPrefsHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Git Graph Activity
 * Displays commits and merges in a git-like visualization
 */
public class GitGraphActivity extends AppCompatActivity {

    private ImageView ivBack;
    private GitGraphView gitGraphView;
    private ImageView btnZoomIn;
    private ImageView btnZoomOut;
    private FloatingActionButton fabDailyMerge;
    
    private GitGraphViewModel viewModel;
    private SharedPrefsHelper prefsHelper;
    
    private PopupWindow tooltipPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_git_graph);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        prefsHelper = new SharedPrefsHelper(this);

        initViews();
        initViewModel();
        initListeners();
        
        // Check if Daily Merge button should be visible
        updateDailyMergeButtonVisibility();
        
        // Load data
        loadGraphData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        gitGraphView = findViewById(R.id.gitGraphView);
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        fabDailyMerge = findViewById(R.id.fabDailyMerge);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(GitGraphViewModel.class);

        // Observe graph data
        viewModel.getGraphData().observe(this, graphData -> {
            if (graphData != null) {
                android.util.Log.d("GitGraphActivity", "Received graph data: " + 
                        graphData.getTotal_commits() + " commits, " + 
                        graphData.getTotal_merges() + " merges");
                
                if (graphData.getCommits() != null) {
                    android.util.Log.d("GitGraphActivity", "Commits list size: " + graphData.getCommits().size());
                    for (int i = 0; i < Math.min(5, graphData.getCommits().size()); i++) {
                        GitGraphCommit c = graphData.getCommits().get(i);
                        android.util.Log.d("GitGraphActivity", "Commit " + i + ": " + c.getMessage() + " [" + c.getBranch_type() + "]");
                    }
                } else {
                    android.util.Log.e("GitGraphActivity", "Commits list is NULL!");
                }
                
                if (graphData.getMerges() != null) {
                    android.util.Log.d("GitGraphActivity", "Merges list size: " + graphData.getMerges().size());
                } else {
                    android.util.Log.e("GitGraphActivity", "Merges list is NULL!");
                }
                
                gitGraphView.setData(graphData.getCommits(), graphData.getMerges());
            } else {
                android.util.Log.e("GitGraphActivity", "Graph data is NULL!");
            }
        });

        // Observe errors
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe loading state
        viewModel.getLoading().observe(this, loading -> {
            // TODO: Show/hide loading indicator
        });
    }

    private void initListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnZoomIn.setOnClickListener(v -> gitGraphView.zoomIn());

        btnZoomOut.setOnClickListener(v -> gitGraphView.zoomOut());

        fabDailyMerge.setOnClickListener(v -> {
            // TODO: Navigate to Daily Merge screen
            Toast.makeText(this, "Daily Merge coming soon", Toast.LENGTH_SHORT).show();
        });

        // Node click listener
        gitGraphView.setOnNodeClickListener((node, x, y) -> {
            showTooltip(node, x, y);
        });
    }

    private void loadGraphData() {
        String token = prefsHelper.getToken();
        if (token != null && !token.isEmpty()) {
            // Load ALL commits (no date filter) with limit 200
            viewModel.loadGraphData(token, null, null, 200, 0);
        } else {
            Toast.makeText(this, "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Show tooltip when node is clicked
     */
    private void showTooltip(GitGraphNode node, float x, float y) {
        // Dismiss existing tooltip
        if (tooltipPopup != null && tooltipPopup.isShowing()) {
            tooltipPopup.dismiss();
        }

        // Inflate tooltip layout
        View tooltipView = getLayoutInflater().inflate(R.layout.tooltip_git_node, null);
        
        TextView tvTooltipContent = tooltipView.findViewById(R.id.tvTooltipContent);
        
        String content;
        if (node.isCommitNode()) {
            content = formatCommitTooltip(node.getCommit());
        } else {
            content = formatMergeTooltip(node.getMerge());
        }
        
        tvTooltipContent.setText(content);

        // Create and show popup
        tooltipPopup = new PopupWindow(
                tooltipView,
                (int) (300 * getResources().getDisplayMetrics().density),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        
        tooltipPopup.setElevation(10);
        tooltipPopup.showAtLocation(gitGraphView, android.view.Gravity.NO_GRAVITY, (int) x, (int) y);
    }

    private String formatCommitTooltip(GitGraphCommit commit) {
        String branchName = commit.isMainBranch() ? "[main]" : "[Quest #" + commit.getUser_quest_id() + "]";
        String timestamp = formatTimestamp(commit.getCreated_at());
        String message = commit.getMessage();
        
        // Truncate message if too long
        if (message.length() > 50) {
            message = message.substring(0, 47) + "...";
        }
        
        return String.format(Locale.getDefault(),
                "%s Committed on %s\nCảm thấy [%s]. Mức độ [%d%%].\n\"%s\"",
                branchName,
                timestamp,
                commit.getEmotion_name(),
                commit.getIntensity_level(),
                message
        );
    }

    private String formatMergeTooltip(GitGraphMerge merge) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tổng kết ngày ").append(formatDate(merge.getMerge_date())).append("\n\n");
        sb.append("Cảm xúc chủ đạo: ").append(merge.getEmotion_name()).append("\n\n");
        sb.append("Thống kê:\n");
        
        // Show top 3 emotions
        if (merge.getEmotionStats() != null) {
            int count = 0;
            for (GitGraphMerge.EmotionStat stat : merge.getEmotionStats().values()) {
                if (count >= 3) break;
                sb.append(String.format(Locale.getDefault(),
                        "• %s: %.0f%% (Intensity: %.0f%%)\n",
                        stat.getEmotion_name(),
                        stat.getFrequency() * 100,
                        stat.getAvg_intensity()
                ));
                count++;
            }
        }
        
        if (merge.getUser_retrospective() != null && !merge.getUser_retrospective().isEmpty()) {
            sb.append("\nHồi tưởng: ").append(merge.getUser_retrospective());
        }
        
        return sb.toString();
    }

    private String formatTimestamp(String timestamp) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(timestamp);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return timestamp;
        }
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    /**
     * Update Daily Merge button visibility
     * Only show between 22:00 - 23:59
     */
    private void updateDailyMergeButtonVisibility() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        if (hour >= 22 && hour < 24) {
            fabDailyMerge.setVisibility(View.VISIBLE);
        } else {
            fabDailyMerge.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tooltipPopup != null && tooltipPopup.isShowing()) {
            tooltipPopup.dismiss();
        }
    }
}
