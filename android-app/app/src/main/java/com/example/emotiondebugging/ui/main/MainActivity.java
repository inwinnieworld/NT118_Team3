package com.example.emotiondebugging.ui.main;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.emotiondebugging.R;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private TextView tvWelcome;
    private ViewPager2 vpIcons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        initViews();
        initViewModel();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcomeMessage);
        vpIcons = findViewById(R.id.vpIcons);
        
        // Thêm click listener cho icon userinfo
        ImageView ivUserInfo = findViewById(R.id.ivUserInfo);
        if (ivUserInfo != null) {
            ivUserInfo.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(
                    MainActivity.this, 
                    com.example.emotiondebugging.ui.profile.ProfileActivity.class
                );
                startActivity(intent);
            });
        }

    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Quan sát dữ liệu tên để cập nhật lời chào
        viewModel.getFullName().observe(this, fullName -> {
            setDynamicWelcome(tvWelcome, fullName);
        });

        // Quan sát danh sách icon để cài đặt Carousel
        viewModel.getIconList().observe(this, icons -> {
            setupIconCarousel(icons);
        });

        // Gọi hàm load dữ liệu ban đầu
        SharedPrefsHelper prefsHelper = new SharedPrefsHelper(this);
        viewModel.initData(prefsHelper);
    }

    // ================= HÀM XỬ LÝ CHỮ (GIỮ NGUYÊN LOGIC) =================
    private void setDynamicWelcome(TextView tv, String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        String lastName = parts[parts.length - 1];

        String fullText = "Xin chào [" + lastName + "]! Một ngày tốt lành nhé";
        SpannableString spannable = new SpannableString(fullText);

        int start = fullText.indexOf(lastName);
        int end = start + lastName.length();

        if (start >= 0) {
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#179FB5")),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.BOLD),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tv.setText(spannable);
    }

    // ================= HÀM XỬ LÝ CAROUSEL ICON (GIỮ NGUYÊN LOGIC) =================
    private void setupIconCarousel(int[] iconList) {
        vpIcons.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ImageView iv = new ImageView(parent.getContext());
                iv.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                iv.setPadding(40, 40, 40, 40);
                iv.setClickable(true);
                iv.setFocusable(true);
                return new RecyclerView.ViewHolder(iv) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                ImageView imageView = (ImageView) holder.itemView;
                imageView.setImageResource(iconList[position]);
                if (position == 5) {
                    imageView.setContentDescription("Healing quests");
                    imageView.setTooltipText("Healing quests");
                }
                
                // Add click listener for each icon
                imageView.setOnClickListener(v -> handleIconClick(position));
            }

            @Override
            public int getItemCount() { return iconList.length; }
        });

        vpIcons.setOffscreenPageLimit(3);
        RecyclerView rv = (RecyclerView) vpIcons.getChildAt(0);
        rv.setPadding(300, 0, 300, 0);
        rv.setClipToPadding(false);

        vpIcons.setPageTransformer((page, position) -> {
            float absPos = Math.abs(position);
            float scale = 1.4f - (absPos * 0.6f);
            page.setScaleX(scale);
            page.setScaleY(scale);
            page.setAlpha(1.0f - (absPos * 0.5f));
        });

        vpIcons.setCurrentItem(iconList.length - 1, false);
    }

    /**
     * Handle icon click navigation
     * Position mapping:
     * 0 - AI Chat
     * 1 - Emergency Hotfixes
     * 2 - Git Commit Journal
     * 3 - Debugging Community
     * 4 - Exam Mode
     * 5 - Healing Quests
     */
    private void handleIconClick(int position) {
        Intent intent;
        switch (position) {
            case 0: // AI Chat
                intent = new Intent(MainActivity.this, com.example.emotiondebugging.ui.aichat.AiChatActivity.class);
                startActivity(intent);
                break;

            case 1: // Emergency Hotfixes
                // TODO: Navigate to EmergencyActivity
                android.widget.Toast.makeText(this, "Emergency Hotfixes coming soon", android.widget.Toast.LENGTH_SHORT).show();
                break;

            case 2: // Git Commit Journal
                intent = new Intent(MainActivity.this, com.example.emotiondebugging.ui.journal.GitJournalActivity.class);
                startActivity(intent);
                break;

            case 3: // Debugging Community
                intent = new Intent(MainActivity.this, com.example.emotiondebugging.ui.community.CommunityActivity.class);
                startActivity(intent);
                break;

            case 4: // Exam Mode
                // TODO: Navigate to ExamModeActivity
                android.widget.Toast.makeText(this, "Exam Mode coming soon", android.widget.Toast.LENGTH_SHORT).show();
                break;

            case 5: // Healing Quests
                intent = new Intent(MainActivity.this, StudentQuestCatalogActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }
    }
}
