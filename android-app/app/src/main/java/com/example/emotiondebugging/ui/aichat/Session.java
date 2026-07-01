package com.example.emotiondebugging.ui.aichat;

/**
 * Một session chat với Dr.Bug. Chỉ tồn tại sau khi user đã gửi lượt đầu tiên
 * (xem quy tắc: mở UI/bấm "+" KHÔNG tạo session — chỉ lượt nói đầu tiên mới tạo).
 *
 * id = session_id thật từ backend, dùng để mở lại session (truyền qua Intent extra).
 */
public class Session {

    private final int id;
    private final String title;

    public Session(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
