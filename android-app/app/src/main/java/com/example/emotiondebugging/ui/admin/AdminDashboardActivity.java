package com.example.emotiondebugging.ui.admin;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

public class AdminDashboardActivity extends AppCompatActivity {

    private AdminViewModel viewModel;
    private TextView tvAdminStatus;

    // Khai báo 6 nút bấm
    private Button btnManageAccount, btnSystemReport, btnManageCommunity,
            btnManageErrorCode, btnManageEmergency, btnManageDictionary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        initViews();
        initViewModel();
        setupClickListeners();
    }

    private void initViews() {
        tvAdminStatus = findViewById(R.id.tvAdminStatus);

        btnManageAccount = findViewById(R.id.btnManageAccount);
        btnSystemReport = findViewById(R.id.btnSystemReport);
        btnManageCommunity = findViewById(R.id.btnManageCommunity);
        btnManageErrorCode = findViewById(R.id.btnManageErrorCode);
        btnManageEmergency = findViewById(R.id.btnManageEmergency);
        btnManageDictionary = findViewById(R.id.btnManageDictionary);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        // Quan sát tên Admin để cập nhật thanh trạng thái bên dưới
        viewModel.getAdminName().observe(this, adminName -> {
            setDynamicStatusText(adminName);
        });

        // Kích hoạt lấy dữ liệu
        SharedPrefsHelper prefsHelper = new SharedPrefsHelper(this);
        viewModel.loadAdminData(prefsHelper);
    }

    // Tô màu cyan và in đậm tên Admin giống trong Figma
    private void setDynamicStatusText(String adminName) {
        String fullText = "Admin [" + adminName + "] đang đăng nhập";
        SpannableString spannable = new SpannableString(fullText);

        int start = fullText.indexOf(adminName);
        int end = start + adminName.length();

        if (start >= 0) {
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#2CE4F9")),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.BOLD),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tvAdminStatus.setText(spannable);
    }

    private void setupClickListeners() {
        btnManageAccount.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(
                AdminDashboardActivity.this,
                ManageStudentActivity.class
            );
            startActivity(intent);
        });

        btnSystemReport.setOnClickListener(v ->
                Toast.makeText(this, "Mở Báo cáo hệ thống", Toast.LENGTH_SHORT).show());

        btnManageCommunity.setOnClickListener(v ->
                Toast.makeText(this, "Mở Quản lý cộng đồng", Toast.LENGTH_SHORT).show());

        btnManageErrorCode.setOnClickListener(v ->
                Toast.makeText(this, "Mở Quản lý mã lỗi", Toast.LENGTH_SHORT).show());

        btnManageEmergency.setOnClickListener(v ->
                Toast.makeText(this, "Mở Quản lý khẩn cấp", Toast.LENGTH_SHORT).show());

        btnManageDictionary.setOnClickListener(v ->
                Toast.makeText(this, "Mở Bộ từ điển cảm xúc", Toast.LENGTH_SHORT).show());
        
        // THÊM: Logout khi long press vào status bar
        tvAdminStatus.setOnLongClickListener(v -> {
            showLogoutDialog();
            return true;
        });
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
        
        android.content.Intent intent = new android.content.Intent(
            this, 
            com.example.emotiondebugging.ui.auth.LoginActivity.class
        );
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}