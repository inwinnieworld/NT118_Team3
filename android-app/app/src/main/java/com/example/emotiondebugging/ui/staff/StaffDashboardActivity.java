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
    private TextView btnQuestBuilder;
    private TextView btnQuestReport;
    private ImageView ivProfile;
    private ImageView ivMessage;
    private ImageView ivSettings;
    private ImageView ivLogout;

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
        btnQuestBuilder = findViewById(R.id.btnQuestBuilder);
        btnQuestReport = findViewById(R.id.btnQuestReport);
        ivProfile = findViewById(R.id.ivProfile);
        ivMessage = findViewById(R.id.ivMessage);
        ivSettings = findViewById(R.id.ivSettings);
        ivLogout = findViewById(R.id.ivLogout);
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

        btnQuestBuilder.setOnClickListener(v -> {
            Intent intent = new Intent(StaffDashboardActivity.this, QuestBuilderActivity.class);
            startActivity(intent);
        });

        btnQuestReport.setOnClickListener(v -> {
            Intent intent = new Intent(StaffDashboardActivity.this, QuestReportActivity.class);
            startActivity(intent);
        });

        ivProfile.setOnClickListener(v ->
                Toast.makeText(this, "Trang cá nhân", Toast.LENGTH_SHORT).show()
        );

        ivMessage.setOnClickListener(v ->
                Toast.makeText(this, "Tin nhắn", Toast.LENGTH_SHORT).show()
        );

        ivSettings.setOnClickListener(v ->
                Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show()
        );

        ivLogout.setOnClickListener(v -> showLogoutDialog());

        tvStaffStatus.setOnLongClickListener(v -> {
            showLogoutDialog();
            return true;
        });
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

    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void logout() {
        SharedPrefsHelper prefsHelper = new SharedPrefsHelper(this);
        prefsHelper.clearAll();
        prefsHelper.clearGitJournalSession(this);

        Intent intent = new Intent(
                this,
                com.example.emotiondebugging.ui.auth.LoginActivity.class
        );
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
