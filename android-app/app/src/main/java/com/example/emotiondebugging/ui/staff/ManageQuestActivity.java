package com.example.emotiondebugging.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.QuestDraftDetail;
import com.example.emotiondebugging.model.response.QuestDraftSummary;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

import java.util.Collections;
import java.util.Map;

public class ManageQuestActivity extends AppCompatActivity {
    private QuestAdapter adapter;
    private ManageQuestViewModel viewModel;
    private String token;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_quest);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        token = bearer(new SharedPrefsHelper(this).getToken());
        ImageView back = findViewById(R.id.btnBack);
        EditText search = findViewById(R.id.etSearchQuest);
        RecyclerView list = findViewById(R.id.rvQuest);
        Button add = findViewById(R.id.btnAddQuest);

        adapter = new QuestAdapter(new QuestAdapter.Listener() {
            @Override public void onOpen(QuestDraftSummary item) { openQuest(item); }
            @Override public void onDelete(QuestDraftSummary item) { confirmDelete(item); }
        });
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ManageQuestViewModel.class);
        viewModel.getQuests().observe(this, adapter::submitList);
        viewModel.getPreview().observe(this, detail -> {
            if (detail != null) {
                openReadOnlyPreview(detail);
                viewModel.clearPreview();
            }
        });
        viewModel.getMessage().observe(this, value -> {
            if (value != null && !value.isEmpty()) Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
        });

        back.setOnClickListener(v -> finish());
        add.setOnClickListener(v -> startActivity(new Intent(this, QuestBuilderActivity.class)));
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { adapter.getFilter().filter(s); }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        if (viewModel != null) viewModel.loadQuests(token);
    }

    private void openQuest(QuestDraftSummary item) {
        if (item.latest_version_id == null) {
            Toast.makeText(this, "Quest has no saved version", Toast.LENGTH_SHORT).show();
            return;
        }
        String status = item.approval_status == null ? "draft" : item.approval_status;
        if ("draft".equals(status) || "rejected".equals(status)) {
            Intent intent = new Intent(this, QuestBuilderActivity.class);
            intent.putExtra(QuestBuilderActivity.EXTRA_VERSION_ID, item.latest_version_id);
            startActivity(intent);
        } else {
            viewModel.loadPreview(token, item.latest_version_id);
        }
    }

    private void confirmDelete(QuestDraftSummary item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete draft?")
                .setMessage("Delete \"" + item.quest_title + "\" permanently? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteDraft(token, item.quest_id))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openReadOnlyPreview(QuestDraftDetail detail) {
        if (detail.flow == null) return;
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
