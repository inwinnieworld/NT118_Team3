package com.example.emotiondebugging.ui.staff;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.emotiondebugging.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class QuestReportActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private QuestReportViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_report);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        btnBack = findViewById(R.id.btnBack);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        viewModel = new ViewModelProvider(this).get(QuestReportViewModel.class);

        viewPager.setAdapter(new QuestReportPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "1" : "2");
        }).attach();

        btnBack.setOnClickListener(v -> finish());

        viewModel.loadMonthlyMetrics();
        viewModel.loadRankingBoard();
    }
}