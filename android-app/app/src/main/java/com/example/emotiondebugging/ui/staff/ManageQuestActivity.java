package com.example.emotiondebugging.ui.staff;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
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
import com.example.emotiondebugging.model.response.QuestResponse;

public class ManageQuestActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etSearchQuest;
    private RecyclerView rvQuest;
    private Button btnAddQuest;

    private QuestAdapter adapter;
    private ManageQuestViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_quest);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        initViewModel();
        initRecyclerView();
        initActions();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etSearchQuest = findViewById(R.id.etSearchQuest);
        rvQuest = findViewById(R.id.rvQuest);
        btnAddQuest = findViewById(R.id.btnAddQuest);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(ManageQuestViewModel.class);

        viewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getQuests().observe(this, quests -> {
            if (adapter != null) {
                adapter.submitList(quests);
            }
        });

        viewModel.getCreateSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                viewModel.loadQuests();
            }
        });

        viewModel.loadQuests();
    }

    private void initRecyclerView() {
        adapter = new QuestAdapter(new QuestAdapter.OnQuestActionListener() {
            @Override
            public void onEdit(QuestResponse item) {
                Toast.makeText(ManageQuestActivity.this, "Sửa " + item.getQuest_title(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDelete(QuestResponse item) {
                Toast.makeText(ManageQuestActivity.this, "Xoá " + item.getQuest_title(), Toast.LENGTH_SHORT).show();
            }
        });

        rvQuest.setLayoutManager(new LinearLayoutManager(this));
        rvQuest.setHasFixedSize(true);
        rvQuest.setAdapter(adapter);
    }

    private void initActions() {
        btnBack.setOnClickListener(v -> finish());

        btnAddQuest.setOnClickListener(v -> showCreateQuestDialog());

        etSearchQuest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void showCreateQuestDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_quest, null, false);

        EditText etErrorTypeId = view.findViewById(R.id.etErrorTypeId);
        EditText etQuestTitle = view.findViewById(R.id.etQuestTitle);
        EditText etQuestDescription = view.findViewById(R.id.etQuestDescription);

        new AlertDialog.Builder(this)
                .setTitle("Thêm quest")
                .setView(view)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    String errorTypeId = etErrorTypeId.getText().toString();
                    String questTitle = etQuestTitle.getText().toString();
                    String questDescription = etQuestDescription.getText().toString();

                    viewModel.createQuest(errorTypeId, questTitle, questDescription);
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }
}