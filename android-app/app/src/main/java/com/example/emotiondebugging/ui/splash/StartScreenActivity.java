package com.example.emotiondebugging.ui.splash;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.emotiondebugging.R;
import com.example.emotiondebugging.ui.auth.LoginActivity;

public class StartScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Làm màn hình tràn viền hoàn toàn
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.activity_start_screen);

        ProgressBar progressBar = findViewById(R.id.progressBar);

        // Chạy loading từ 0 đến 100
        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        animation.setDuration(5500);
        animation.setInterpolator(new android.view.animation.DecelerateInterpolator());

        // CHỈ CÓ 1 LISTENER DUY NHẤT Ở ĐÂY
        animation.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {

                com.example.emotiondebugging.utils.SharedPrefsHelper prefsHelper =
                        new com.example.emotiondebugging.utils.SharedPrefsHelper(StartScreenActivity.this);

                Intent intent;

                // Kiểm tra xem đã đăng nhập chưa
                if (prefsHelper.isLoggedIn() && prefsHelper.getToken() != null) {

                    // Lấy Role đã lưu để chuyển đúng Home Screen
                    String savedRole = prefsHelper.getRole();
                    if (savedRole == null) savedRole = "STUDENT";

                    switch (savedRole.toUpperCase()) {
                        case "ADMIN":
                            intent = new Intent(StartScreenActivity.this, com.example.emotiondebugging.ui.admin.AdminDashboardActivity.class);
                            break;
                        case "STAFF":
                            intent = new Intent(StartScreenActivity.this, com.example.emotiondebugging.ui.staff.StaffDashboardActivity.class);
                            break;
                        default:
                            intent = new Intent(StartScreenActivity.this, com.example.emotiondebugging.ui.main.MainActivity.class);
                            break;
                    }
                } else {
                    // Chưa đăng nhập -> Vào Login
                    intent = new Intent(StartScreenActivity.this, LoginActivity.class);
                }

                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });

        animation.start();
    }
}