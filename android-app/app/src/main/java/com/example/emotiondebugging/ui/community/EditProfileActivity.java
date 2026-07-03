package com.example.emotiondebugging.ui.community;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.community.UpdateCommunityProfileRequest;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

import java.io.File;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etBio;
    private EditText etMusicName;
    private TextView tvMusicFile;

    private String authToken;
    private String existingMusicUrl;
    private File pickedMusicFile;

    private final ActivityResultLauncher<Intent> pickAudioLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        handlePickedAudio(uri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_community_profile);

        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        String token = prefs.getToken();
        authToken = (token != null && !token.trim().isEmpty()) ? "Bearer " + token : "";

        etBio = findViewById(R.id.et_bio);
        etMusicName = findViewById(R.id.et_music_name);
        tvMusicFile = findViewById(R.id.tv_music_file);

        String bio = getIntent().getStringExtra("bio");
        String musicName = getIntent().getStringExtra("music_name");
        existingMusicUrl = getIntent().getStringExtra("music_url");

        if (etBio != null) etBio.setText(bio != null ? bio : "");
        if (etMusicName != null) etMusicName.setText(musicName != null ? musicName : "");

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_upload_music).setOnClickListener(v -> pickAudio());
        findViewById(R.id.btn_save).setOnClickListener(v -> save());
    }

    private void pickAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/mpeg");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pickAudioLauncher.launch(Intent.createChooser(intent, "Chọn file nhạc"));
    }

    private void handlePickedAudio(Uri uri) {
        File file = copyUriToCache(uri);
        if (file == null) {
            Toast.makeText(this, "Không đọc được file đã chọn", Toast.LENGTH_SHORT).show();
            return;
        }
        pickedMusicFile = file;
        if (tvMusicFile != null) tvMusicFile.setText(file.getName());
    }

    private void save() {
        if (pickedMusicFile != null) {
            uploadMusicThenSave();
        } else {
            updateProfile(existingMusicUrl);
        }
    }

    private void uploadMusicThenSave() {
        RequestBody reqBody = RequestBody.create(MediaType.parse("audio/mpeg"), pickedMusicFile);
        MultipartBody.Part part = MultipartBody.Part.createFormData("music", pickedMusicFile.getName(), reqBody);

        RetrofitClient.getCommunityApi().uploadProfileMusic(authToken, part)
                .enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Map<String, Object>>> call,
                                           Response<ApiResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess() && response.body().getData() != null) {
                            Object url = response.body().getData().get("music_url");
                            updateProfile(url != null ? url.toString() : existingMusicUrl);
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Upload nhạc thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                        Toast.makeText(EditProfileActivity.this, "Lỗi kết nối khi upload nhạc", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateProfile(String musicUrl) {
        UpdateCommunityProfileRequest request = new UpdateCommunityProfileRequest();
        request.setBio(etBio != null ? etBio.getText().toString().trim() : null);
        request.setMusicName(etMusicName != null ? etMusicName.getText().toString().trim() : null);
        request.setMusicUrl(musicUrl);

        RetrofitClient.getCommunityApi().updateMyCommunityProfile(authToken, request)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(EditProfileActivity.this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Cập nhật thất bại (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(EditProfileActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private File copyUriToCache(Uri uri) {
        File outFile = new File(getCacheDir(), "music_upload_" + System.currentTimeMillis() + ".mp3");
        try (java.io.InputStream in = getContentResolver().openInputStream(uri);
             java.io.OutputStream out = new java.io.FileOutputStream(outFile)) {
            if (in == null) return null;
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            return outFile;
        } catch (Exception e) {
            return null;
        }
    }
}
