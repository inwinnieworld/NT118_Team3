package com.example.emotiondebugging.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.response.ProfileResponse;
import com.example.emotiondebugging.ui.auth.LoginActivity;
import com.example.emotiondebugging.utils.SharedPrefsHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvStudentId, tvFaculty, tvMajor, tvSchoolYear;
    private ImageView ivAvatar;
    private SharedPrefsHelper prefsHelper;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefsHelper = new SharedPrefsHelper(this);
        
        // Lấy token từ SharedPreferences
        String token = prefsHelper.getToken();
        if (token != null) {
            authToken = "Bearer " + token;
        }

        tvName = findViewById(R.id.tv_name);
        tvStudentId = findViewById(R.id.tv_student_id);
        tvFaculty = findViewById(R.id.tv_faculty);
        tvMajor = findViewById(R.id.tv_major);
        tvSchoolYear = findViewById(R.id.tv_school_year);
        ivAvatar = findViewById(R.id.iv_avatar);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_edit_profile).setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        findViewById(R.id.btn_change_password).setOnClickListener(v ->
                startActivity(new Intent(this, ChangePasswordActivity.class)));

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            v.setEnabled(false); // chặn double-tap
            showLogoutDialog();
            v.postDelayed(() -> v.setEnabled(true), 1000);
        });
    }

    private AlertDialog logoutDialog;

    private void showLogoutDialog() {
        if (logoutDialog != null && logoutDialog.isShowing()) return;
        logoutDialog = new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (d, which) -> logout())
                .setNegativeButton("Huỷ", null)
                .create();
        logoutDialog.show();
    }

    private void logout() {
        if (logoutDialog != null && logoutDialog.isShowing()) logoutDialog.dismiss();
        prefsHelper.clearAll();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (logoutDialog != null && logoutDialog.isShowing()) logoutDialog.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    private void loadProfile() {
        RetrofitClient.getProfileApi().getProfile(authToken)
                .enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ProfileResponse.Data data = response.body().getData();
                            tvName.setText(data.name);
                            tvStudentId.setText("ID: " + data.studentCode);
                            tvFaculty.setText(data.faculty);
                            tvMajor.setText(data.major);
                            tvSchoolYear.setText(String.valueOf(data.yearOfStudy));

                            if (data.avatarUrl != null && !data.avatarUrl.isEmpty()) {
                                Glide.with(ProfileActivity.this)
                                        .load("http://10.0.2.2:3000" + data.avatarUrl)
                                        .circleCrop()
                                        .placeholder(R.drawable.bg_avatar_circle)
                                        .into(ivAvatar);
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, "Lỗi tải profile", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ProfileResponse> call, Throwable t) {
                        Toast.makeText(ProfileActivity.this, "Không kết nối được server", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
