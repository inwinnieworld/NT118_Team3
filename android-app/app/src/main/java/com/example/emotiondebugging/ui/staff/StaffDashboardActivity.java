package com.example.emotiondebugging.ui.staff;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

public class StaffDashboardActivity extends AppCompatActivity {

    private TextView tvStaffStatus;
    private TextView btnManageQuest;
    private TextView btnQuestReport;
    private TextView btnTraceQuestion;
    private ImageView ivProfile;
    private ImageView ivMessage;
    private ImageView ivSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dashboard);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        initData();
        initActions();
    }

    private void initViews() {
        tvStaffStatus = findViewById(R.id.tvStaffStatus);
        btnManageQuest = findViewById(R.id.btnManageQuest);
        btnQuestReport = findViewById(R.id.btnQuestReport);
        btnTraceQuestion = findViewById(R.id.btnTraceQuestion);
        ivProfile = findViewById(R.id.ivProfile);
        ivMessage = findViewById(R.id.ivMessage);
        ivSettings = findViewById(R.id.ivSettings);
    }

    private void initData() {
        SharedPrefsHelper prefsHelper = new SharedPrefsHelper(this);
        String staffName = prefsHelper.getUserName();

        if (staffName == null || staffName.trim().isEmpty()) {
            staffName = "Người dùng";
        }

        setDynamicStatusText(staffName);
    }

    private void initActions() {
        btnManageQuest.setOnClickListener(v -> {
            Intent intent = new Intent(StaffDashboardActivity.this, ManageQuestActivity.class);
            startActivity(intent);
        });

        btnQuestReport.setOnClickListener(v ->
                Toast.makeText(this, "Mở Báo cáo quest", Toast.LENGTH_SHORT).show()
        );

        btnTraceQuestion.setOnClickListener(v ->
                Toast.makeText(this, "Mở Quản lý bộ câu hỏi Trace Error", Toast.LENGTH_SHORT).show()
        );

        ivProfile.setOnClickListener(v ->
                Toast.makeText(this, "Trang cá nhân", Toast.LENGTH_SHORT).show()
        );

        ivMessage.setOnClickListener(v ->
                Toast.makeText(this, "Tin nhắn", Toast.LENGTH_SHORT).show()
        );

        ivSettings.setOnClickListener(v ->
                Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show()
        );
    }

    private void setDynamicStatusText(String staffName) {
        String fullText = "Staff [" + staffName + "] đang đăng nhập";
        SpannableString spannable = new SpannableString(fullText);

        int start = fullText.indexOf(staffName);
        int end = start + staffName.length();

        if (start >= 0) {
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#27D8FF")),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.BOLD),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tvStaffStatus.setText(spannable);
    }

}