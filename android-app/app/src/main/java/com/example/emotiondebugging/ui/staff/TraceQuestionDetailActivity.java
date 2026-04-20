package com.example.emotiondebugging.ui.staff;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.emotiondebugging.R;

import java.util.Arrays;
import java.util.List;

public class TraceQuestionDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText edtQuestionName;
    private EditText edtOption1;
    private EditText edtOption2;
    private EditText edtOption3;
    private EditText edtOption4;
    private Button btnEdit;
    private Button btnDelete;
    private TextView tvBackText;
    private TextView tvErrorCodeLabel;
    private TextView tvErrorNameLabel;
    private Spinner spinnerErrorType;

    private TraceQuestionDetailViewModel viewModel;
    private int questionId;
    private int errorTypeId = 2;

    private final List<String> errorNames = Arrays.asList(
            "Build Failed",
            "Runtime Error",
            "Connection Timeout",
            "Resource Exhaustion"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trace_question_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        btnBack = findViewById(R.id.btnBack);
        edtQuestionName = findViewById(R.id.edtQuestionName);
        edtOption1 = findViewById(R.id.edtOption1);
        edtOption2 = findViewById(R.id.edtOption2);
        edtOption3 = findViewById(R.id.edtOption3);
        edtOption4 = findViewById(R.id.edtOption4);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        tvBackText = findViewById(R.id.tvBackText);
        tvErrorCodeLabel = findViewById(R.id.tvErrorCodeLabel);
        tvErrorNameLabel = findViewById(R.id.tvErrorNameLabel);
        spinnerErrorType = findViewById(R.id.spinnerErrorType);

        questionId = getIntent().getIntExtra("question_id", -1);

        viewModel = new ViewModelProvider(this).get(TraceQuestionDetailViewModel.class);

        setupSpinner();
        observeViewModel();

        if (questionId != -1) {
            viewModel.loadDetail(questionId);
        }

        btnBack.setOnClickListener(v -> finish());
        tvBackText.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            errorTypeId = spinnerErrorType.getSelectedItemPosition() + 1;

            viewModel.updateQuestion(
                    questionId,
                    errorTypeId,
                    edtQuestionName.getText().toString(),
                    edtOption1.getText().toString(),
                    edtOption2.getText().toString(),
                    edtOption3.getText().toString(),
                    edtOption4.getText().toString()
            );
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xóa câu hỏi")
                    .setMessage("Bạn có chắc muốn xóa câu hỏi này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> viewModel.deleteQuestion(questionId))
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                errorNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerErrorType.setAdapter(adapter);

        spinnerErrorType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                tvErrorCodeLabel.setText("bug_report");
                tvErrorNameLabel.setText(errorNames.get(position).toUpperCase());
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    private void observeViewModel() {
        viewModel.getDetail().observe(this, data -> {
            if (data != null) {
                errorTypeId = data.getError_type_id();

                edtQuestionName.setText(data.getQuestion_text());
                edtOption1.setText(data.getOption_1());
                edtOption2.setText(data.getOption_2());
                edtOption3.setText(data.getOption_3());
                edtOption4.setText(data.getOption_4());

                tvErrorCodeLabel.setText("bug_report");
                tvErrorNameLabel.setText(formatErrorName(data.getError_name()));

                int spinnerPosition = Math.max(0, errorTypeId - 1);
                if (spinnerPosition < errorNames.size()) {
                    spinnerErrorType.setSelection(spinnerPosition);
                }
            }
        });

        viewModel.getMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getUpdateSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                finish();
            }
        });

        viewModel.getDeleteSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                finish();
            }
        });
    }

    private String formatErrorName(String raw) {
        if (raw == null) return "";
        if (raw.contains(":")) {
            return raw.substring(0, raw.indexOf(":")).toUpperCase();
        }
        return raw.toUpperCase();
    }
}