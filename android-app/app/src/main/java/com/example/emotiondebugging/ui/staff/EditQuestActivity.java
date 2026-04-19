package com.example.emotiondebugging.ui.staff;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import  com.example.emotiondebugging.R;

public class EditQuestActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvBackText;

    private EditText edtQuestName;
    private EditText edtQuestDescription;
    private EditText edtBasePriority;
    private EditText edtTag;
    private EditText edtEstimatedDuration;

    private CheckBox cbLevel1;
    private CheckBox cbLevel2;
    private CheckBox cbLevel3;
    private CheckBox cbLevel4;
    private CheckBox cbLevel5;

    private Button btnUpdateQuest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_quest);

        initViews();
        loadQuestDataFromIntent();
        setupSingleChoiceLevel();
        setupActions();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvBackText = findViewById(R.id.tvBackText);

        edtQuestName = findViewById(R.id.edtQuestName);
        edtQuestDescription = findViewById(R.id.edtQuestDescription);
        edtBasePriority = findViewById(R.id.edtBasePriority);
        edtTag = findViewById(R.id.edtTag);
        edtEstimatedDuration = findViewById(R.id.edtEstimatedDuration);

        cbLevel1 = findViewById(R.id.cbLevel1);
        cbLevel2 = findViewById(R.id.cbLevel2);
        cbLevel3 = findViewById(R.id.cbLevel3);
        cbLevel4 = findViewById(R.id.cbLevel4);
        cbLevel5 = findViewById(R.id.cbLevel5);

        btnUpdateQuest = findViewById(R.id.btnUpdateQuest);
    }

    private void loadQuestDataFromIntent() {
        String questName = getIntent().getStringExtra("quest_name");
        String questDescription = getIntent().getStringExtra("quest_description");
        int basePriority = getIntent().getIntExtra("base_priority", 0);
        String tag = getIntent().getStringExtra("tag");
        int estimatedDuration = getIntent().getIntExtra("estimated_duration", 0);
        int levelSeverity = getIntent().getIntExtra("level_severity", 1);

        if (questName != null) {
            edtQuestName.setText(questName);
        }

        if (questDescription != null) {
            edtQuestDescription.setText(questDescription);
        }

        if (basePriority > 0) {
            edtBasePriority.setText(String.valueOf(basePriority));
        }

        if (tag != null) {
            edtTag.setText(tag);
        }

        if (estimatedDuration > 0) {
            edtEstimatedDuration.setText(String.valueOf(estimatedDuration));
        }

        switch (levelSeverity) {
            case 1:
                cbLevel1.setChecked(true);
                break;
            case 2:
                cbLevel2.setChecked(true);
                break;
            case 3:
                cbLevel3.setChecked(true);
                break;
            case 4:
                cbLevel4.setChecked(true);
                break;
            case 5:
                cbLevel5.setChecked(true);
                break;
            default:
                cbLevel1.setChecked(true);
                break;
        }
    }

    private void setupSingleChoiceLevel() {
        View.OnClickListener levelClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllLevelChecks();

                if (v.getId() == R.id.cbLevel1) {
                    cbLevel1.setChecked(true);
                } else if (v.getId() == R.id.cbLevel2) {
                    cbLevel2.setChecked(true);
                } else if (v.getId() == R.id.cbLevel3) {
                    cbLevel3.setChecked(true);
                } else if (v.getId() == R.id.cbLevel4) {
                    cbLevel4.setChecked(true);
                } else if (v.getId() == R.id.cbLevel5) {
                    cbLevel5.setChecked(true);
                }
            }
        };

        cbLevel1.setOnClickListener(levelClickListener);
        cbLevel2.setOnClickListener(levelClickListener);
        cbLevel3.setOnClickListener(levelClickListener);
        cbLevel4.setOnClickListener(levelClickListener);
        cbLevel5.setOnClickListener(levelClickListener);
    }

    private void clearAllLevelChecks() {
        cbLevel1.setChecked(false);
        cbLevel2.setChecked(false);
        cbLevel3.setChecked(false);
        cbLevel4.setChecked(false);
        cbLevel5.setChecked(false);
    }

    private void setupActions() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvBackText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnUpdateQuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSave();
            }
        });
    }

    private int getSelectedLevel() {
        if (cbLevel1.isChecked()) return 1;
        if (cbLevel2.isChecked()) return 2;
        if (cbLevel3.isChecked()) return 3;
        if (cbLevel4.isChecked()) return 4;
        if (cbLevel5.isChecked()) return 5;
        return 1;
    }

    private void validateAndSave() {
        String questName = edtQuestName.getText().toString().trim();
        String questDescription = edtQuestDescription.getText().toString().trim();
        String basePriorityText = edtBasePriority.getText().toString().trim();
        String tag = edtTag.getText().toString().trim();
        String estimatedDurationText = edtEstimatedDuration.getText().toString().trim();
        int levelSeverity = getSelectedLevel();

        if (TextUtils.isEmpty(questName)) {
            edtQuestName.setError("Vui lòng nhập tên quest");
            edtQuestName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(questDescription)) {
            edtQuestDescription.setError("Vui lòng nhập mô tả quest");
            edtQuestDescription.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(basePriorityText)) {
            edtBasePriority.setError("Vui lòng nhập base priority");
            edtBasePriority.requestFocus();
            return;
        }

        int basePriority;
        try {
            basePriority = Integer.parseInt(basePriorityText);
        } catch (NumberFormatException e) {
            edtBasePriority.setError("Base priority phải là số");
            edtBasePriority.requestFocus();
            return;
        }

        if (basePriority < 1 || basePriority > 100) {
            edtBasePriority.setError("Base priority phải từ 1 đến 100");
            edtBasePriority.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(estimatedDurationText)) {
            edtEstimatedDuration.setError("Vui lòng nhập thời gian");
            edtEstimatedDuration.requestFocus();
            return;
        }

        int estimatedDuration;
        try {
            estimatedDuration = Integer.parseInt(estimatedDurationText);
        } catch (NumberFormatException e) {
            edtEstimatedDuration.setError("Estimated duration phải là số");
            edtEstimatedDuration.requestFocus();
            return;
        }

        if (estimatedDuration <= 0) {
            edtEstimatedDuration.setError("Thời gian phải lớn hơn 0");
            edtEstimatedDuration.requestFocus();
            return;
        }

        // TODO: Gọi API update quest ở đây
        // Có thể gọi ViewModel hoặc Retrofit trực tiếp

        Toast.makeText(
                this,
                "Cập nhật quest thành công\nLevel: " + levelSeverity + "\nTag: " + tag,
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }
}