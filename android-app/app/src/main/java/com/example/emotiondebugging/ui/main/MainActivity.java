package com.example.emotiondebugging.ui.main;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.emotiondebugging.ui.admin.ManageStudentActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Tạm thời redirect sang ManageStudentActivity để test admin UI
        startActivity(new Intent(this, ManageStudentActivity.class));
        finish();
    }
}
