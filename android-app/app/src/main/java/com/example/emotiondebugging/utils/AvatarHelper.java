package com.example.emotiondebugging.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.widget.ImageView;

public class AvatarHelper {

    // Màu nền theo chữ cái
    private static final int[] COLORS = {
        0xFF12B2C1, 0xFF6C63FF, 0xFFFF6B6B, 0xFF4CAF50, 0xFFFF9800,
        0xFF9C27B0, 0xFF2196F3, 0xFFE91E63, 0xFF00BCD4, 0xFF8BC34A
    };

    public static void loadAvatar(ImageView view, String avatarUrl, String name) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            int size = view.getLayoutParams() != null && view.getLayoutParams().width > 0
                    ? view.getLayoutParams().width : 80;
            android.graphics.drawable.BitmapDrawable placeholder =
                    new android.graphics.drawable.BitmapDrawable(
                            view.getResources(), generateBitmap(name, size));
            com.bumptech.glide.Glide.with(view.getContext())
                    .load("http://10.0.2.2:3000" + avatarUrl)
                    .circleCrop()
                    .placeholder(placeholder)
                    .into(view);
        } else {
            int size = view.getLayoutParams() != null && view.getLayoutParams().width > 0
                    ? view.getLayoutParams().width : 80;
            view.setImageBitmap(generateBitmap(name, size));
        }
    }

    public static Bitmap generateBitmap(String name, int sizePx) {
        if (sizePx <= 0) sizePx = 80;

        String letter = getInitial(name);
        int color = getColor(name);

        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Vẽ nền tròn
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(color);
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, bgPaint);

        // Vẽ chữ
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(sizePx * 0.45f);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);

        float y = sizePx / 2f - (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(letter, sizePx / 2f, y, textPaint);

        return bitmap;
    }

    private static String getInitial(String name) {
        if (name == null || name.isEmpty()) return "?";
        // Lấy chữ cái cuối cùng của tên (tiếng Việt thường để tên ở cuối)
        String[] parts = name.trim().split("\\s+");
        String lastPart = parts[parts.length - 1];
        return String.valueOf(lastPart.charAt(0)).toUpperCase();
    }

    private static int getColor(String name) {
        if (name == null || name.isEmpty()) return COLORS[0];
        return COLORS[Math.abs(name.hashCode()) % COLORS.length];
    }
}
