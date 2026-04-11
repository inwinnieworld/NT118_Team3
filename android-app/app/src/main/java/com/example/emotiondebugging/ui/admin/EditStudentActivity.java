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
import com.example.emotiondebugging.model.request.UpdateStudentRequest;
import com.example.emotiondebugging.model.response.BaseResponse;
import com.example.emotiondebugging.model.response.StudentListResponse.StudentItem;
import com.google.gson.Gson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditStudentActivity extends AppCompatActivity {

    public static final String EXTRA_STUDENT = "extra_student";
    private static final String TEST_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoyLCJlbWFpbCI6ImFkbWluQGdtYWlsLmNvbSIsImlhdCI6MTc3NTkxNDAwMCwiZXhwIjoxNzc2NTE4ODAwfQ.A5dD-yaFwRejbTVnn3Nuiw_FO7i5o3c5j8N7ee6V6QA";

    private EditText etName, etEmail, etPhone, etFaculty, etMajor, etYear;
    private StudentItem student;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student);

        // Nhận student object từ Intent
        String json = getIntent().getStringExtra(EXTRA_STUDENT);
        student = new Gson().fromJson(json, StudentItem.class);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etFaculty = findViewById(R.id.et_faculty);
        etMajor = findViewById(R.id.et_major);
        etYear = findViewById(R.id.et_year);

        // Hiển thị avatar + tên + mã SV
        ImageView ivAvatar = findViewById(R.id.iv_avatar);
        TextView tvName = findViewById(R.id.tv_name);
        TextView tvCode = findViewById(R.id.tv_student_code);

        tvName.setText(student.name);
        tvCode.setText(student.studentCode);
        if (student.avatarUrl != null && !student.avatarUrl.isEmpty()) {
            Glide.with(this).load("http://10.0.2.2:3000" + student.avatarUrl)
                    .circleCrop().into(ivAvatar);
        }

        // Điền data vào các ô
        etName.setText(student.name);
        etEmail.setText(student.email);
        etPhone.setText(student.phone);
        etFaculty.setText(student.faculty);
        etMajor.setText(student.major);
        etYear.setText(student.yearOfStudy);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save).setOnClickListener(v -> save());
    }

    private void save() {
        UpdateStudentRequest req = new UpdateStudentRequest(
                etName.getText().toString().trim(),
                etEmail.getText().toString().trim(),
                etPhone.getText().toString().trim(),
                etMajor.getText().toString().trim(),
                etFaculty.getText().toString().trim(),
                etYear.getText().toString().trim()
        );

        RetrofitClient.getAdminApi().updateStudent(TEST_TOKEN, student.studentId, req)
                .enqueue(new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(EditStudentActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(EditStudentActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse> call, Throwable t) {
                        Toast.makeText(EditStudentActivity.this, "Không kết nối được server", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
