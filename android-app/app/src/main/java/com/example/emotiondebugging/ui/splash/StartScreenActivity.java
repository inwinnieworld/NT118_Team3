package com.example.emotiondebugging.ui.splash;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

public class StartScreenActivity extends AppCompatActivity {

    private StartViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Làm màn hình tràn viền hoàn toàn
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.activity_start_screen);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(StartViewModel.class);

        ProgressBar progressBar = findViewById(R.id.progressBar);

        // Quan sát kết quả điều hướng
        viewModel.getNavigationTarget().observe(this, targetClass -> {
            Intent intent = new Intent(StartScreenActivity.this, targetClass);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });

        // Quan sát thông báo Toast
        viewModel.getToastMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(StartScreenActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });

        // Chạy loading từ 0 đến 100
        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        animation.setDuration(5500);
        animation.setInterpolator(new android.view.animation.DecelerateInterpolator());

        animation.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                SharedPrefsHelper prefsHelper = new SharedPrefsHelper(StartScreenActivity.this);
                // Gọi ViewModel thực hiện logic kiểm tra
                viewModel.checkNavigation(prefsHelper);
            }
        });

        animation.start();
    }
}