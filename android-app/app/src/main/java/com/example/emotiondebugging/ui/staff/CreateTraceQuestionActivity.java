package com.example.emotiondebugging.ui.staff;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.emotiondebugging.R;

import java.util.Arrays;
import java.util.List;

public class CreateTraceQuestionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText edtQuestionName;
    private EditText edtOption1;
    private EditText edtOption2;
    private EditText edtOption3;
    private EditText edtOption4;
    private Button btnSave;
    private TextView tvBackText;
    private TextView tvErrorCodeLabel;
    private TextView tvErrorNameLabel;
    private Spinner spinnerErrorType;

    private CreateTraceQuestionViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trace_question);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        btnBack = findViewById(R.id.btnBack);
        edtQuestionName = findViewById(R.id.edtQuestionName);
        edtOption1 = findViewById(R.id.edtOption1);
        edtOption2 = findViewById(R.id.edtOption2);
        edtOption3 = findViewById(R.id.edtOption3);
        edtOption4 = findViewById(R.id.edtOption4);
        btnSave = findViewById(R.id.btnSave);
        tvBackText = findViewById(R.id.tvBackText);
        tvErrorCodeLabel = findViewById(R.id.tvErrorCodeLabel);
        tvErrorNameLabel = findViewById(R.id.tvErrorNameLabel);
        spinnerErrorType = findViewById(R.id.spinnerErrorType);

        viewModel = new ViewModelProvider(this).get(CreateTraceQuestionViewModel.class);

        setupSpinner();

        viewModel.getMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getCreateSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                finish();
            }
        });

        btnBack.setOnClickListener(v -> finish());
        tvBackText.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            int errorTypeId = spinnerErrorType.getSelectedItemPosition() + 1;
            String selectedName = spinnerErrorType.getSelectedItem().toString();

            tvErrorCodeLabel.setText("bug_report");
            tvErrorNameLabel.setText(selectedName.toUpperCase());

            viewModel.createTraceQuestion(
                    errorTypeId,
                    edtQuestionName.getText().toString(),
                    edtOption1.getText().toString(),
                    edtOption2.getText().toString(),
                    edtOption3.getText().toString(),
                    edtOption4.getText().toString()
            );
        });
    }

    private void setupSpinner() {
        List<String> errorNames = Arrays.asList(
                "Build Failed",
                "Runtime Error",
                "Connection Timeout",
                "Resource Exhaustion"
        );

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
}