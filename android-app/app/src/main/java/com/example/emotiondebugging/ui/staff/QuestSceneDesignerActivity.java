package com.example.emotiondebugging.ui.staff;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.api.RetrofitClient;

public class QuestSceneDesignerActivity extends AppCompatActivity {
    public static final String EXTRA_SCENE_X = "scene_x";
    public static final String EXTRA_SCENE_Y = "scene_y";
    public static final String EXTRA_SCENE_WIDTH = "scene_width";
    public static final String EXTRA_SCENE_HEIGHT = "scene_height";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_quest_scene_designer);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        QuestSceneCanvasView canvas = findViewById(R.id.sceneCanvas);
        ImageView background = findViewById(R.id.imgSceneBackground);
        TextView title = findViewById(R.id.tvSceneDesignerTitle);
        Button cancel = findViewById(R.id.btnCancelSceneDesign);
        Button apply = findViewById(R.id.btnApplySceneDesign);

        if (QuestSceneDesignStore.selectedNodeId == null) {
            finish();
            return;
        }
        title.setText("Position: " + QuestSceneDesignStore.selectedNodeId);
        try {
            findViewById(R.id.sceneRoot).setBackgroundColor(
                    Color.parseColor(QuestSceneDesignStore.backgroundColor));
        } catch (Exception ignored) {
            findViewById(R.id.sceneRoot).setBackgroundColor(Color.WHITE);
        }
        if (QuestSceneDesignStore.backgroundUrl != null
                && !QuestSceneDesignStore.backgroundUrl.trim().isEmpty()) {
            Glide.with(this)
                    .load(RetrofitClient.resolveMediaUrl(QuestSceneDesignStore.backgroundUrl))
                    .into(background);
        }
        canvas.setScene(QuestSceneDesignStore.sceneNodes, QuestSceneDesignStore.selectedNodeId);

        cancel.setOnClickListener(view -> finish());
        apply.setOnClickListener(view -> {
            Intent result = new Intent();
            result.putExtra(EXTRA_SCENE_X, canvas.getSceneX());
            result.putExtra(EXTRA_SCENE_Y, canvas.getSceneY());
            result.putExtra(EXTRA_SCENE_WIDTH, canvas.getSceneWidth());
            result.putExtra(EXTRA_SCENE_HEIGHT, canvas.getSceneHeight());
            setResult(Activity.RESULT_OK, result);
            finish();
        });
    }

    @Override protected void onDestroy() {
        if (isFinishing()) QuestSceneDesignStore.clear();
        super.onDestroy();
    }
}
