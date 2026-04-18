package com.example.emotiondebugging.ui.staff;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;

import java.util.ArrayList;
import java.util.List;

public class ManageQuestActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etSearchQuest;
    private RecyclerView rvQuest;
    private Button btnAddQuest;

    private QuestAdapter adapter;
    private final List<QuestItem> questList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_quest);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        initData();
        initRecyclerView();
        initActions();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etSearchQuest = findViewById(R.id.etSearchQuest);
        rvQuest = findViewById(R.id.rvQuest);
        btnAddQuest = findViewById(R.id.btnAddQuest);
    }

    private void initData() {
        questList.clear();
        questList.add(new QuestItem("QUEST [0101]"));
        questList.add(new QuestItem("QUEST [0102]"));
        questList.add(new QuestItem("QUEST [0103]"));
        questList.add(new QuestItem("QUEST [0104]"));
        questList.add(new QuestItem("QUEST [0105]"));
        questList.add(new QuestItem("QUEST [0106]"));
    }

    private void initRecyclerView() {
        adapter = new QuestAdapter(questList, new QuestAdapter.OnQuestActionListener() {
            @Override
            public void onEdit(QuestItem item) {
                Toast.makeText(ManageQuestActivity.this,
                        "Sửa " + item.getQuestName(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDelete(QuestItem item) {
                Toast.makeText(ManageQuestActivity.this,
                        "Xoá " + item.getQuestName(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        rvQuest.setLayoutManager(new LinearLayoutManager(this));
        rvQuest.setHasFixedSize(true);
        rvQuest.setAdapter(adapter);
    }

    private void initActions() {
        btnBack.setOnClickListener(v -> finish());

        btnAddQuest.setOnClickListener(v ->
                Toast.makeText(ManageQuestActivity.this,
                        "Thêm quest",
                        Toast.LENGTH_SHORT).show()
        );

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
}