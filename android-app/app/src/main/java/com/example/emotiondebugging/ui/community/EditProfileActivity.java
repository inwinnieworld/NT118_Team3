package com.example.emotiondebugging.ui.community;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.emotiondebugging.R;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etDisplayName;
    private EditText etUsername;
    private EditText etBio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etDisplayName = findEditText("et_display_name");
        etUsername = findEditText("et_username");
        etBio = findEditText("et_bio");

        String displayName = getIntent().getStringExtra("display_name");
        String username = getIntent().getStringExtra("username");
        String bio = getIntent().getStringExtra("bio");

        if (etDisplayName != null) {
            etDisplayName.setText(displayName != null ? displayName : "");
        }

        if (etUsername != null) {
            etUsername.setText(username != null ? username : "");
        }

        if (etBio != null) {
            etBio.setText(bio != null ? bio : "");
        }

        bindClick("btn_back", this::finish);
        bindClick("btn_cancel", this::finish);

        bindClick("btn_save", () -> {
            setResult(RESULT_OK);
            finish();
        });
    }

    private EditText findEditText(String idName) {
        int id = getResources().getIdentifier(idName, "id", getPackageName());
        if (id == 0) return null;

        View view = findViewById(id);
        if (view instanceof EditText) {
            return (EditText) view;
        }

        return null;
    }

    private void bindClick(String idName, Runnable action) {
        int id = getResources().getIdentifier(idName, "id", getPackageName());
        if (id == 0) return;

        View view = findViewById(id);
        if (view != null) {
            view.setOnClickListener(v -> action.run());
        }
    }
}