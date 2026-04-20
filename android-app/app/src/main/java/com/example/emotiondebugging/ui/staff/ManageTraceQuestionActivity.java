package com.example.emotiondebugging.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.TraceQuestionResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ManageTraceQuestionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvTraceQuestions;
    private Button btnAddTrace;
    private Button btnAddBottom;
    private Spinner spinnerErrorFilter;

    private TraceQuestionAdapter adapter;
    private ManageTraceQuestionViewModel viewModel;
    private final List<Integer> errorTypeIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_trace_question);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        btnBack = findViewById(R.id.btnBack);
        rvTraceQuestions = findViewById(R.id.rvTraceQuestions);
        btnAddTrace = findViewById(R.id.btnAddTrace);
        btnAddBottom = findViewById(R.id.btnAddBottom);
        spinnerErrorFilter = findViewById(R.id.spinnerErrorFilter);

        adapter = new TraceQuestionAdapter(this);
        rvTraceQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvTraceQuestions.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ManageTraceQuestionViewModel.class);
        viewModel.observeSource();

        viewModel.getGroupedTraceQuestions().observe(this, list -> adapter.submitList(list));

        viewModel.getMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());

        btnAddTrace.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateTraceQuestionActivity.class);
            startActivity(intent);
        });

        btnAddBottom.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateTraceQuestionActivity.class);
            startActivity(intent);
        });

        viewModel.loadTraceQuestions();

        viewModel.getGroupedTraceQuestions().observe(this, list -> {
            if (list != null) {
                setupFilterFromGroups(list);
            }
        });
    }

    private void setupFilterFromGroups(List<TraceQuestionGroupItem> groups) {
        List<String> labels = new ArrayList<>();
        errorTypeIds.clear();

        labels.add("Tất cả");
        errorTypeIds.add(-1);

        for (TraceQuestionGroupItem item : groups) {
            labels.add(formatErrorName(item.getErrorName()));
            errorTypeIds.add(item.getErrorTypeId());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                labels
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerErrorFilter.setAdapter(spinnerAdapter);

        spinnerErrorFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                viewModel.setSelectedErrorTypeId(errorTypeIds.get(position));
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    private String formatErrorName(String raw) {
        if (raw == null) return "";
        if (raw.contains(":")) {
            return raw.substring(0, raw.indexOf(":"));
        }
        return raw;
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadTraceQuestions();
    }
}