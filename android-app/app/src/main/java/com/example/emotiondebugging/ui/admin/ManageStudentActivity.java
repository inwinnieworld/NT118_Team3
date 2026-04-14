package com.example.emotiondebugging.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.response.BaseResponse;
import com.example.emotiondebugging.model.response.StaffListResponse;
import com.example.emotiondebugging.model.response.StaffListResponse.StaffItem;
import com.example.emotiondebugging.model.response.StudentListResponse;
import com.example.emotiondebugging.model.response.StudentListResponse.StudentItem;
import com.google.gson.Gson;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageStudentActivity extends AppCompatActivity {

    private static final String TEST_TOKEN = ""; // Sẽ lấy từ SharedPreferences
    private static final int TAB_STUDENT = 0;
    private static final int TAB_STAFF = 1;

    private LinearLayout listAccounts, tabStudent, tabStaff;
    private TextView tvCount;
    private EditText etSearch;
    private View fabAddStaff;
    private int currentTab = TAB_STUDENT;
    private int currentPage = 1;
    private int totalPages = 1;
    private String currentSearch = "";
    
    // Token từ SharedPreferences
    private String authToken;

    private final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> { if (result.getResultCode() == RESULT_OK) loadData(); }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_student);
        
        // Lấy token từ SharedPreferences
        com.example.emotiondebugging.utils.SharedPrefsHelper prefsHelper = 
            new com.example.emotiondebugging.utils.SharedPrefsHelper(this);
        String token = prefsHelper.getToken();
        authToken = token != null ? "Bearer " + token : "";

        listAccounts = findViewById(R.id.list_accounts);
        tvCount = findViewById(R.id.tv_count);
        etSearch = findViewById(R.id.et_search);
        tabStudent = findViewById(R.id.tab_student);
        tabStaff = findViewById(R.id.tab_staff);
        fabAddStaff = findViewById(R.id.fab_add_staff);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_prev).setOnClickListener(v -> {
            if (currentPage > 1) { currentPage--; loadData(); }
        });
        findViewById(R.id.btn_next).setOnClickListener(v -> {
            if (currentPage < totalPages) { currentPage++; loadData(); }
        });

        tabStudent.setOnClickListener(v -> switchTab(TAB_STUDENT));
        tabStaff.setOnClickListener(v -> switchTab(TAB_STAFF));

        fabAddStaff.setOnClickListener(v ->
                editLauncher.launch(new Intent(this, AddStaffActivity.class)));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (isTabSwitching) return;
                currentSearch = s.toString().trim();
                currentPage = 1;
                loadData();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadData();
    }

    private boolean isTabSwitching = false;

    private void switchTab(int tab) {
        currentTab = tab;
        currentPage = 1;
        currentSearch = "";
        isTabSwitching = true;
        etSearch.setText("");
        isTabSwitching = false;

        // Update tab UI
        tabStudent.setBackground(getDrawable(tab == TAB_STUDENT ?
                R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected));
        tabStaff.setBackground(getDrawable(tab == TAB_STAFF ?
                R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected));

        // Chỉ tab nhân viên mới có nút thêm
        fabAddStaff.setVisibility(tab == TAB_STAFF ? View.VISIBLE : View.GONE);

        // Update text color tabs
        ((TextView) tabStudent.getChildAt(1)).setTextColor(
                getColor(tab == TAB_STUDENT ? R.color.white : R.color.text_secondary));
        ((TextView) tabStaff.getChildAt(1)).setTextColor(
                getColor(tab == TAB_STAFF ? R.color.white : R.color.text_secondary));

        loadData();
    }

    private void loadData() {
        if (currentTab == TAB_STUDENT) loadStudents();
        else loadStaff();
    }

    private void loadStudents() {
        RetrofitClient.getAdminApi().getStudents(authToken, currentPage, currentSearch)
                .enqueue(new Callback<StudentListResponse>() {
                    @Override
                    public void onResponse(Call<StudentListResponse> call, Response<StudentListResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StudentListResponse body = response.body();
                            totalPages = body.totalPages;
                            tvCount.setText(body.total + " tài khoản");
                            renderStudents(body.data);
                            updatePagination(body.page, body.totalPages);
                        }
                    }
                    @Override
                    public void onFailure(Call<StudentListResponse> call, Throwable t) {
                        Toast.makeText(ManageStudentActivity.this, "Không kết nối được server", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadStaff() {
        RetrofitClient.getAdminApi().getStaff(authToken, currentPage, currentSearch)
                .enqueue(new Callback<StaffListResponse>() {
                    @Override
                    public void onResponse(Call<StaffListResponse> call, Response<StaffListResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StaffListResponse body = response.body();
                            totalPages = body.totalPages;
                            tvCount.setText(body.total + " tài khoản");
                            renderStaff(body.data);
                            updatePagination(body.page, body.totalPages);
                        }
                    }
                    @Override
                    public void onFailure(Call<StaffListResponse> call, Throwable t) {
                        Toast.makeText(ManageStudentActivity.this, "Không kết nối được server", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderStudents(List<StudentItem> students) {
        listAccounts.removeAllViews();
        for (StudentItem s : students) {
            View item = LayoutInflater.from(this).inflate(R.layout.item_account, listAccounts, false);
            bindItemView(item, s.name, s.studentCode, s.avatarUrl, s.isLocked());

            item.findViewById(R.id.btn_edit).setOnClickListener(v -> {
                Intent intent = new Intent(this, EditStudentActivity.class);
                intent.putExtra(EditStudentActivity.EXTRA_STUDENT, new Gson().toJson(s));
                editLauncher.launch(intent);
            });

            item.findViewById(R.id.btn_toggle_lock).setOnClickListener(v ->
                    confirmToggleLock(s.name, s.isLocked(), () ->
                            RetrofitClient.getAdminApi()
                                    .toggleStudentLock(authToken, s.studentId)
                                    .enqueue(lockCallback())));

            listAccounts.addView(item);
        }
    }

    private void renderStaff(List<StaffItem> staffList) {
        listAccounts.removeAllViews();
        for (StaffItem s : staffList) {
            View item = LayoutInflater.from(this).inflate(R.layout.item_account, listAccounts, false);
            bindItemView(item, s.name, s.position, s.avatarUrl, s.isLocked());

            item.findViewById(R.id.btn_edit).setOnClickListener(v -> {
                Intent intent = new Intent(this, EditStaffActivity.class);
                intent.putExtra(EditStaffActivity.EXTRA_STAFF, new Gson().toJson(s));
                editLauncher.launch(intent);
            });

            item.findViewById(R.id.btn_toggle_lock).setOnClickListener(v ->
                    confirmToggleLock(s.name, s.isLocked(), () ->
                            RetrofitClient.getAdminApi()
                                    .toggleStaffLock(authToken, s.staffId)
                                    .enqueue(lockCallback())));

            listAccounts.addView(item);
        }
    }

    private void bindItemView(View item, String name, String subtitle,
                               String avatarUrl, boolean isLocked) {
        ((TextView) item.findViewById(R.id.tv_name)).setText(name);
        ((TextView) item.findViewById(R.id.tv_student_id)).setText(subtitle);

        TextView badge = item.findViewById(R.id.tv_locked_badge);
        badge.setVisibility(isLocked ? View.VISIBLE : View.GONE);

        TextView lockLabel = item.findViewById(R.id.tv_lock_label);
        lockLabel.setText(isLocked ? "Kích hoạt" : "Vô hiệu");

        ImageView ivLock = item.findViewById(R.id.iv_lock_icon);
        ivLock.setImageResource(isLocked ? R.drawable.ic_person : R.drawable.ic_lock_1);

        ImageView ivAvatar = item.findViewById(R.id.iv_avatar);
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this).load("http://10.0.2.2:3000" + avatarUrl)
                    .circleCrop().into(ivAvatar);
        }
    }

    private void confirmToggleLock(String name, boolean isLocked, Runnable onConfirm) {
        String action = isLocked ? "kích hoạt" : "vô hiệu hóa";
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc muốn " + action + " tài khoản của " + name + "?")
                .setPositiveButton("Xác nhận", (d, w) -> onConfirm.run())
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private Callback<BaseResponse> lockCallback() {
        return new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ManageStudentActivity.this, response.body().message, Toast.LENGTH_SHORT).show();
                    loadData();
                }
            }
            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                Toast.makeText(ManageStudentActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void updatePagination(int page, int total) {
        TextView tvPage = findViewById(R.id.tv_page1);
        tvPage.setText("Trang " + page + " / " + Math.max(total, 1));
        ((ImageButton) findViewById(R.id.btn_prev)).setAlpha(page > 1 ? 1f : 0.3f);
        ((ImageButton) findViewById(R.id.btn_next)).setAlpha(page < total ? 1f : 0.3f);
    }
}
