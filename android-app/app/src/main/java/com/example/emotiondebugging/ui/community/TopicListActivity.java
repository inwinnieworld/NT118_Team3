package com.example.emotiondebugging.ui.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.community.TopicListResponse;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.ui.community.adapter.TopicAdapter;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TopicListActivity extends AppCompatActivity {

    private String authToken;
    private RecyclerView rvTopics;
    private TextView tvEmpty;
    private TopicAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_list);

        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        String token = prefs.getToken();
        authToken = token != null ? "Bearer " + token : "";

        View back = findViewById(R.id.btn_back);
        if (back != null) back.setOnClickListener(v -> finish());

        rvTopics = findViewById(R.id.rv_topics);
        tvEmpty = findViewById(R.id.tv_empty);

        rvTopics.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TopicAdapter(topic -> {
            Intent data = new Intent();
            data.putExtra("topic_id", topic.topicId);
            data.putExtra("topic_name", topic.topicName);
            setResult(RESULT_OK, data);
            finish();
        });
        rvTopics.setAdapter(adapter);

        loadTopics();
    }

    private void loadTopics() {
        RetrofitClient.getCommunityApi()
                .getPostTopics(authToken)
                .enqueue(new Callback<ApiResponse<TopicListResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<TopicListResponse>> call,
                                           Response<ApiResponse<TopicListResponse>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null
                                && response.body().getData().topics != null
                                && !response.body().getData().topics.isEmpty()) {
                            adapter.setTopics(response.body().getData().topics);
                            tvEmpty.setVisibility(View.GONE);
                            rvTopics.setVisibility(View.VISIBLE);
                        } else {
                            tvEmpty.setVisibility(View.VISIBLE);
                            rvTopics.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<TopicListResponse>> call, Throwable t) {
                        Toast.makeText(TopicListActivity.this,
                                "Lỗi tải chủ đề: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
