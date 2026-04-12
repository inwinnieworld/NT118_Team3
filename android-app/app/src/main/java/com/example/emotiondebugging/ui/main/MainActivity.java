package com.example.emotiondebugging.ui.main;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.emotiondebugging.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ẩn Action Bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // 1. Xử lý tên động và tô màu
        TextView tvWelcome = findViewById(R.id.tvWelcomeMessage);

        // Giả lập tên lấy từ Database/SharedPrefs
        String fullName = "Trương Nguyên Đại Thắng";
        setDynamicWelcome(tvWelcome, fullName);

        // 2. Cài đặt Carousel 5 Icon
        setupIconCarousel();
    }

    // ================= HÀM XỬ LÝ CHỮ =================
    private void setDynamicWelcome(TextView tv, String fullName) {
        // Tách lấy tên cuối cùng
        String[] parts = fullName.trim().split("\\s+");
        String lastName = parts[parts.length - 1];

        // Chuỗi đầy đủ
        String fullText = "Xin chào [" + lastName + "]!\nMột ngày tốt lành nhé";
        SpannableString spannable = new SpannableString(fullText);

        // Tìm vị trí chữ cần tô màu
        int start = fullText.indexOf(lastName);
        int end = start + lastName.length();

        // Tô màu #179FB5 cho tên
        if (start >= 0) {
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#179FB5")),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // Có thể in đậm thêm nếu thích
            spannable.setSpan(new StyleSpan(Typeface.BOLD),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tv.setText(spannable);
    }

    // ================= HÀM XỬ LÝ CAROUSEL ICON =================
    private void setupIconCarousel() {
        ViewPager2 vpIcons = findViewById(R.id.vpIcons);

        // Mảng chứa ID của 5 file PNG icon chức năng bạn vừa up
        int[] iconList = {
                R.drawable.ic_errorlog,
                R.drawable.ic_emergencyhotfixes,
                R.drawable.ic_gitcommitjournal,
                R.drawable.ic_debuggingcommunity,
                R.drawable.ic_exammode
        };

        // Gắn Adapter thu nhỏ trực tiếp
        vpIcons.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ImageView iv = new ImageView(parent.getContext());
                iv.setLayoutParams(new ViewGroup.LayoutParams(160, 160)); // Kích thước gốc của icon
                iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                return new RecyclerView.ViewHolder(iv) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                ((ImageView) holder.itemView).setImageResource(iconList[position]);
            }

            @Override
            public int getItemCount() { return iconList.length; }
        });

        // Thiết lập hiệu ứng Lướt -> Cạnh thu nhỏ, Giữa to lên
        vpIcons.setOffscreenPageLimit(3);
        RecyclerView rv = (RecyclerView) vpIcons.getChildAt(0);
        rv.setPadding(300, 0, 300, 0); // Ép icon vào giữa
        rv.setClipToPadding(false);

        vpIcons.setPageTransformer((page, position) -> {
            float absPos = Math.abs(position);
            // Phóng to lên 1.4x ở giữa, xung quanh chỉ 0.8x
            float scale = 1.4f - (absPos * 0.6f);
            page.setScaleX(scale);
            page.setScaleY(scale);
            // Icon ở xa sẽ bị nhạt màu đi
            page.setAlpha(1.0f - (absPos * 0.5f));
        });

        // Mặc định chọn icon số 3 (ở giữa)
        vpIcons.setCurrentItem(2, false);
    }
}