package com.example.emotiondebugging.ui.admin;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.request.UpdateStaffRequest;
import com.example.emotiondebugging.model.response.BaseResponse;
import com.example.emotiondebugging.model.response.StaffListResponse.StaffItem;
import com.example.emotiondebugging.utils.ApiConstants;
import com.google.gson.Gson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditStaffActivity extends AppCompatActivity {

    public static final String EXTRA_STAFF = "extra_staff";
    private EditText etName, etEmail, etPhone, etPosition, etDepartment;
    private StaffItem staff;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_staff);

        // Lấy token từ SharedPreferences
        com.example.emotiondebugging.utils.SharedPrefsHelper prefsHelper = 
            new com.example.emotiondebugging.utils.SharedPrefsHelper(this);
        String token = prefsHelper.getToken();
        if (token != null) {
            authToken = "Bearer " + token;
        }

        staff = new Gson().fromJson(getIntent().getStringExtra(EXTRA_STAFF), StaffItem.class);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPosition = findViewById(R.id.et_position);
        etDepartment = findViewById(R.id.et_department);

        TextView tvName = findViewById(R.id.tv_name);
        ImageView ivAvatar = findViewById(R.id.iv_avatar);

        tvName.setText(staff.name);
        if (staff.avatarUrl != null && !staff.avatarUrl.isEmpty()) {
            // ⚠️ CHỈNH SỬA: Sử dụng ApiConstants.getFullUrl()
            Glide.with(this).load(ApiConstants.getFullUrl(staff.avatarUrl)).circleCrop().into(ivAvatar);
        }

        etName.setText(staff.name);
        etEmail.setText(staff.email);
        etPhone.setText(staff.phone);
        etPosition.setText(staff.position);
        etDepartment.setText(staff.department);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save).setOnClickListener(v -> save());
    }

    private void save() {
        UpdateStaffRequest req = new UpdateStaffRequest(
                etName.getText().toString().trim(),
                etEmail.getText().toString().trim(),
                etPhone.getText().toString().trim(),
                etPosition.getText().toString().trim(),
                etDepartment.getText().toString().trim()
        );

        RetrofitClient.getAdminApi().updateStaff(authToken, staff.staffId, req)
                .enqueue(new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(EditStaffActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(EditStaffActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<BaseResponse> call, Throwable t) {
                        Toast.makeText(EditStaffActivity.this, "Không kết nối được server", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
