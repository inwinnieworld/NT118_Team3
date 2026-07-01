package com.example.emotiondebugging.ui.aichat;

import com.example.emotiondebugging.model.response.AiChatModels.ChatAction;

import java.util.List;

/**
 * Một dòng trong RecyclerView chat. Đại diện cho 1 phần tử hiển thị, không nhất thiết
 * trùng 1-1 với 1 tin nhắn backend (vd: khối gợi ý là 1 ChatMessage riêng).
 *
 * Bong bóng AI có thể kèm 1 ChatAction (metadata): quest cards hoặc nút redirect. Adapter
 * dựa vào action.actionType để render component phụ ngay dưới bong bóng.
 */
public class ChatMessage {

    public enum Type {
        AI_TEXT,      // Bong bóng trái + icon Dr.Bug (có thể kèm action)
        USER_TEXT,    // Bong bóng phải + avatar user
        SUGGESTIONS   // Khối các dòng gợi ý vấn đề (Tầng 2) ở lượt 0
    }

    private final Type type;
    private final String content;
    private final List<String> suggestions;
    private final ChatAction action;

    private ChatMessage(Type type, String content, List<String> suggestions, ChatAction action) {
        this.type = type;
        this.content = content;
        this.suggestions = suggestions;
        this.action = action;
    }

    public static ChatMessage ai(String content) {
        return new ChatMessage(Type.AI_TEXT, content, null, null);
    }

    public static ChatMessage ai(String content, ChatAction action) {
        return new ChatMessage(Type.AI_TEXT, content, null, action);
    }

    public static ChatMessage user(String content) {
        return new ChatMessage(Type.USER_TEXT, content, null, null);
    }

    public static ChatMessage suggestions(List<String> suggestions) {
        return new ChatMessage(Type.SUGGESTIONS, null, suggestions, null);
    }

    public Type getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public ChatAction getAction() {
        return action;
    }
}
