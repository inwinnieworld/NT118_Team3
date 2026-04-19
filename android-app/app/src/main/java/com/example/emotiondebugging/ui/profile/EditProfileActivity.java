package com.example.emotiondebugging.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.request.UpdateProfileRequest;
import com.example.emotiondebugging.model.response.BaseResponse;
import com.example.emotiondebugging.model.response.ProfileResponse;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private TextView tvName, tvStudentId;
    private ImageView ivAvatar;
    private EditText etFaculty, etMajor, etSchoolYear, etEmail, etPhone, etEmergencyPhone;
    private String currentName = "";
    private String authToken;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    uploadAvatar(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Lấy token từ SharedPreferences
        com.example.emotiondebugging.utils.SharedPrefsHelper prefsHelper = 
            new com.example.emotiondebugging.utils.SharedPrefsHelper(this);
        String token = prefsHelper.getToken();
        if (token != null) {
            authToken = "Bearer " + token;
        }

        tvName = findViewById(R.id.tv_name);
        tvStudentId = findViewById(R.id.tv_student_id);
        ivAvatar = findViewById(R.id.iv_avatar);
        etFaculty = findViewById(R.id.et_faculty);
        etMajor = findViewById(R.id.et_major);
        etSchoolYear = findViewById(R.id.et_school_year);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etEmergencyPhone = findViewById(R.id.et_emergency_phone);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save).setOnClickListener(v -> saveProfile());

        // Nút camera → chọn ảnh từ gallery
        findViewById(R.id.iv_camera_badge).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        // Nút bút chì → dialog đổi tên
        findViewById(R.id.btn_edit_name).setOnClickListener(v -> showEditNameDialog());

        loadCurrentProfile();
    }

    private void loadCurrentProfile() {
        RetrofitClient.getProfileApi().getProfile(authToken)
                .enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ProfileResponse.Data data = response.body().getData();
                            currentName = data.name != null ? data.name : "";
                            tvName.setText(currentName);
                            tvStudentId.setText("ID: " + data.studentCode);
                            etFaculty.setText(data.faculty);
                            etMajor.setText(data.major);
                            etSchoolYear.setText(String.valueOf(data.yearOfStudy));
                            etEmail.setText(data.email);
                            etPhone.setText(data.phone);
                            etEmergencyPhone.setText(data.emergencyPhone);

                            if (data.avatarUrl != null && !data.avatarUrl.isEmpty()) {
                                Glide.with(EditProfileActivity.this)
                                        .load("http://10.0.2.2:3000" + data.avatarUrl)
                                        .circleCrop()
                                        .into(ivAvatar);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ProfileResponse> call, Throwable t) {
                        Toast.makeText(EditProfileActivity.this, "Không kết nối được server", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showEditNameDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentName);
        input.setSelection(currentName.length());

        new AlertDialog.Builder(this)
                .setTitle("Đổi tên")
                .setView(input)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        currentName = newName;
                        tvName.setText(currentName);
                    }
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void saveProfile() {
        String phone = etPhone.getText().toString().trim();
        String emergencyPhone = etEmergencyPhone.getText().toString().trim();

        if (!phone.isEmpty() && !phone.matches("\\d{10}")) {
            etPhone.setError("Số điện thoại phải đúng 10 chữ số");
            etPhone.requestFocus();
            return;
        }
        if (!emergencyPhone.isEmpty() && !emergencyPhone.matches("\\d{10}")) {
            etEmergencyPhone.setError("Số điện thoại phải đúng 10 chữ số");
            etEmergencyPhone.requestFocus();
            return;
        }

        UpdateProfileRequest request = new UpdateProfileRequest(
                currentName,
                etPhone.getText().toString().trim(),
                etMajor.getText().toString().trim(),
                etFaculty.getText().toString().trim(),
                etSchoolYear.getText().toString().trim(),
                etEmergencyPhone.getText().toString().trim()
        );

        RetrofitClient.getProfileApi().updateProfile(authToken, request)
                .enqueue(new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(EditProfileActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse> call, Throwable t) {
                        Toast.makeText(EditProfileActivity.this, "Không kết nối được server", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadAvatar(Uri uri) {
        try {
            String path = getRealPathFromUri(uri);
            if (path == null) return;

            File file = new File(path);
            RequestBody reqBody = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("avatar", file.getName(), reqBody);

            RetrofitClient.getProfileApi().uploadAvatar(authToken, part)
                    .enqueue(new Callback<BaseResponse>() {
                        @Override
                        public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                            if (response.isSuccessful()) {
                                Glide.with(EditProfileActivity.this).load(uri).circleCrop().into(ivAvatar);
                                Toast.makeText(EditProfileActivity.this, "Cập nhật ảnh thành công", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<BaseResponse> call, Throwable t) {
                            Toast.makeText(EditProfileActivity.this, "Upload thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private String getRealPathFromUri(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        try (android.database.Cursor cursor = getContentResolver().query(uri, proj, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(idx);
            }
        }
        return null;
    }
}
