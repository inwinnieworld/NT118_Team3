package com.example.emotiondebugging.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.graphics.Typeface;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.QuestDraftDetail;
import com.example.emotiondebugging.model.response.QuestDraftSummary;
import com.example.emotiondebugging.model.domain.QuestCategory;
import com.example.emotiondebugging.ui.staff.QuestPreviewActivity;
import com.example.emotiondebugging.ui.staff.QuestPreviewStore;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

import java.util.Collections;
import java.util.Map;
import java.util.List;

public class StudentQuestCatalogActivity extends AppCompatActivity implements StudentQuestCatalogAdapter.Listener {
    private StudentQuestCatalogViewModel viewModel;
    private StudentQuestCatalogAdapter adapter;
    private String token;
    private LinearLayout categoryTabs;
    private Integer selectedCategoryId;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_quest_catalog);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        token = bearer(new SharedPrefsHelper(this).getToken());
        ProgressBar progress = findViewById(R.id.progressStudentQuests);
        TextView empty = findViewById(R.id.tvStudentQuestEmpty);
        categoryTabs = findViewById(R.id.layoutQuestCategories);
        findViewById(R.id.btnBackStudentQuests).setOnClickListener(v -> finish());

        RecyclerView list = findViewById(R.id.rvStudentQuests);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentQuestCatalogAdapter(this);
        list.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(StudentQuestCatalogViewModel.class);
        viewModel.getQuests().observe(this, quests -> {
            adapter.submit(quests);
            empty.setVisibility(quests == null || quests.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getCategories().observe(this, this::renderCategories);
        viewModel.getLoading().observe(this, value -> progress.setVisibility(Boolean.TRUE.equals(value) ? View.VISIBLE : View.GONE));
        viewModel.getMessage().observe(this, value -> {
            if (value != null && !value.isEmpty()) Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
        });
        viewModel.getLaunch().observe(this, this::launchRun);
        viewModel.loadCategories(token);
        viewModel.loadCatalog(token, null);
    }

    @Override public void onStart(QuestDraftSummary quest) { viewModel.startQuest(token, quest.quest_id); }

    private void renderCategories(List<QuestCategory> categories) {
        categoryTabs.removeAllViews();
        addCategoryTab("All", null);
        if (categories == null) return;
        for (QuestCategory category : categories) {
            addCategoryTab(category.getErrorName(), category.getErrorTypeId());
        }
    }

    private void addCategoryTab(String label, Integer categoryId) {
        TextView tab = new TextView(this);
        tab.setText(label);
        tab.setGravity(android.view.Gravity.CENTER);
        tab.setPadding(dp(16), 0, dp(16), 0);
        boolean selected = categoryId == null ? selectedCategoryId == null
                : categoryId.equals(selectedCategoryId);
        tab.setTextColor(Color.parseColor(selected ? "#0F766E" : "#667085"));
        tab.setTypeface(Typeface.DEFAULT, selected ? Typeface.BOLD : Typeface.NORMAL);
        tab.setBackgroundColor(Color.parseColor(selected ? "#E6FFFB" : "#FFFFFF"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(dp(2), 0, dp(2), 0);
        tab.setLayoutParams(params);
        tab.setOnClickListener(view -> {
            selectedCategoryId = categoryId;
            renderCategories(viewModel.getCategories().getValue());
            viewModel.loadCatalog(token, categoryId);
        });
        categoryTabs.addView(tab);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void launchRun(StudentQuestCatalogViewModel.RunLaunch run) {
        if (run == null || run.detail == null || run.detail.flow == null) return;
        QuestDraftDetail detail = run.detail;
        Map<String, Object> canvas = detail.canvas_config == null ? Collections.emptyMap() : detail.canvas_config;
        QuestPreviewStore.set(detail.quest_title, value(canvas, "background_url", ""),
                value(canvas, "background_color", "#FFFFFF"),
                value(canvas, "background_sound_url", ""),
                intValue(canvas, "background_sound_volume", 35), detail.flow.nodes, detail.flow.edges);
        Intent intent = new Intent(this, QuestPreviewActivity.class);
        intent.putExtra(QuestPreviewActivity.EXTRA_RUN_ID, run.runId);
        intent.putExtra(QuestPreviewActivity.EXTRA_RUN_TOKEN, token);
        startActivity(intent);
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
