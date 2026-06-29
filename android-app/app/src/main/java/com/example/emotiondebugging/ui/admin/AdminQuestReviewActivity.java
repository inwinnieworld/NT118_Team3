package com.example.emotiondebugging.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.QuestDraftDetail;
import com.example.emotiondebugging.model.response.QuestDraftSummary;
import com.example.emotiondebugging.ui.staff.QuestPreviewActivity;
import com.example.emotiondebugging.ui.staff.QuestPreviewStore;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

import java.util.Collections;
import java.util.Map;

public class AdminQuestReviewActivity extends AppCompatActivity implements AdminQuestReviewAdapter.Listener {
    private AdminQuestReviewViewModel viewModel;
    private AdminQuestReviewAdapter adapter;
    private ProgressBar progress;
    private TextView empty;
    private String token;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_quest_review);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        token = bearer(new SharedPrefsHelper(this).getToken());
        progress = findViewById(R.id.progressQuestReview);
        empty = findViewById(R.id.tvQuestReviewEmpty);
        findViewById(R.id.btnBackQuestReview).setOnClickListener(v -> finish());

        RecyclerView list = findViewById(R.id.rvQuestReview);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminQuestReviewAdapter(this);
        list.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(AdminQuestReviewViewModel.class);
        viewModel.getQuests().observe(this, quests -> {
            adapter.submit(quests);
            empty.setVisibility(quests == null || quests.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getLoading().observe(this, value -> progress.setVisibility(Boolean.TRUE.equals(value) ? View.VISIBLE : View.GONE));
        viewModel.getMessage().observe(this, value -> {
            if (value != null && !value.isEmpty()) Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
        });
        viewModel.getPreview().observe(this, this::openPreview);
        viewModel.loadPending(token);
    }

    @Override public void onPreview(QuestDraftSummary quest) {
        if (quest.latest_version_id == null) Toast.makeText(this, "Quest has no version", Toast.LENGTH_SHORT).show();
        else viewModel.loadPreview(token, quest.latest_version_id);
    }

    @Override public void onApprove(QuestDraftSummary quest) { showReviewDialog(quest, "approved"); }
    @Override public void onReject(QuestDraftSummary quest) { showReviewDialog(quest, "rejected"); }

    @Override public void onVisibility(QuestDraftSummary quest, boolean active) {
        new AlertDialog.Builder(this)
                .setTitle(active ? "Restore quest?" : "Hide quest?")
                .setMessage(active
                        ? "Students will be able to see and start this quest again."
                        : "This quest will disappear from the student catalog. Existing history is kept.")
                .setPositiveButton(active ? "Restore" : "Hide",
                        (dialog, which) -> viewModel.updateVisibility(token, quest.quest_id, active))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showReviewDialog(QuestDraftSummary quest, String action) {
        EditText note = new EditText(this);
        note.setHint("Review note (optional)");
        note.setMinLines(2);
        int padding = Math.round(20 * getResources().getDisplayMetrics().density);
        note.setPadding(padding, padding / 2, padding, padding / 2);
        new AlertDialog.Builder(this)
                .setTitle("approved".equals(action) ? "Approve quest?" : "Reject quest?")
                .setMessage(quest.quest_title)
                .setView(note)
                .setPositiveButton("approved".equals(action) ? "Approve" : "Reject",
                        (dialog, which) -> viewModel.review(token, quest.quest_id, action, note.getText().toString().trim()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openPreview(QuestDraftDetail detail) {
        if (detail == null || detail.flow == null) return;
        Map<String, Object> canvas = detail.canvas_config == null ? Collections.emptyMap() : detail.canvas_config;
        QuestPreviewStore.set(detail.quest_title, value(canvas, "background_url", ""),
                value(canvas, "background_color", "#FFFFFF"),
                value(canvas, "background_sound_url", ""),
                intValue(canvas, "background_sound_volume", 35), detail.flow.nodes, detail.flow.edges);
        startActivity(new Intent(this, QuestPreviewActivity.class));
    }

    private String value(Map<String, Object> map, String key, String fallback) {
        Object value = map.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private int intValue(Map<String, Object> map, String key, int fallback) {
        Object value = map.get(key);
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(String.valueOf(value)); }
        catch (Exception ignored) { return fallback; }
    }

    private String bearer(String raw) {
        if (raw == null) return "";
        return raw.startsWith("Bearer ") ? raw : "Bearer " + raw;
    }
}
