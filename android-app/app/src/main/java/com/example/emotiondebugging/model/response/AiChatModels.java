package com.example.emotiondebugging.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Gom toàn bộ model cho luồng AI Chat (Dr.Bug) vào 1 file để dễ bảo hành.
 * Mỗi response/đối tượng con là 1 nested static class, map đúng shape JSON từ backend.
 *
 * Các action_type có thể gặp trong metadata:
 *   - "show_quests"      → data.quests (placeholder giai đoạn này)
 *   - "redirect_feature" → data.targetScreen
 *   - "select_priority"  → data.candidates (popup cho user chọn)
 */
public class AiChatModels {

    /** Tham chiếu 1 vấn đề (dùng cho gợi ý Tầng 2 và ứng viên select_priority). */
    public static class ProblemRef {
        @SerializedName("id") public String id;
        @SerializedName("title") public String title;
    }

    /** Response của POST /sessions/start: lời chào + gợi ý mở đầu. KHÔNG tạo session. */
    public static class StartSessionData {
        @SerializedName("opening_message") public String openingMessage;
        @SerializedName("suggestions") public List<ProblemRef> suggestions;
    }

    /** Quest gợi ý (placeholder giai đoạn này — backend trả mảng rỗng + placeholder=true). */
    public static class Quest {
        @SerializedName("quest_id") public int questId;
        @SerializedName("title") public String title;
        @SerializedName("rating") public double rating;
        @SerializedName("thumbnail_url") public String thumbnailUrl;
    }

    /** Dữ liệu kèm theo 1 action — các field chỉ có giá trị tùy action_type. */
    public static class ActionData {
        // show_quests
        @SerializedName("problem_id") public String problemId;
        @SerializedName("quests") public List<Quest> quests;
        @SerializedName("placeholder") public boolean placeholder;
        // redirect_feature
        @SerializedName("target_screen") public String targetScreen;
        // select_priority
        @SerializedName("candidates") public List<ProblemRef> candidates;
        // select_route (lượt 4.2: user tự chọn quest thư giãn / lên cộng đồng)
        @SerializedName("options") public List<RouteOption> options;
    }

    /** 1 lựa chọn trong popup select_route (key gửi lại backend, label hiển thị). */
    public static class RouteOption {
        @SerializedName("key") public String key;     // "quest" | "community"
        @SerializedName("label") public String label;
    }

    /** Một action/metadata gắn vào tin nhắn AI. */
    public static class ChatAction {
        @SerializedName("action_type") public String actionType;
        @SerializedName("data") public ActionData data;
    }

    /** Tin nhắn AI trong response /messages. */
    public static class AiMessage {
        @SerializedName("content") public String content;
        @SerializedName("metadata") public ChatAction metadata;
    }

    /** Response của POST /messages: kết quả 1 lượt chat. */
    public static class MessageData {
        @SerializedName("session_id") public int sessionId;
        @SerializedName("turn") public int turn;
        @SerializedName("ai_message") public AiMessage aiMessage;
        @SerializedName("action") public ChatAction action;
        @SerializedName("status") public String status;
        @SerializedName("title") public String title;
    }

    /** 1 phần tử trong chat_history (khi load lại session). */
    public static class HistoryItem {
        @SerializedName("turn") public int turn;
        @SerializedName("sender") public String sender;   // "user" | "ai"
        @SerializedName("content") public String content;
        @SerializedName("timestamp") public String timestamp;
        @SerializedName("metadata") public ChatAction metadata;
    }

    /** 1 dòng trong danh sách session (màn 2). session_title NULL khi chưa kết thúc. */
    public static class SessionSummary {
        @SerializedName("session_id") public int sessionId;
        @SerializedName("session_title") public String sessionTitle;
        @SerializedName("status") public String status;
        @SerializedName("turn_count") public int turnCount;
        @SerializedName("resolved_problem_id") public String resolvedProblemId;
        @SerializedName("created_at") public String createdAt;
        @SerializedName("updated_at") public String updatedAt;
    }

    /** Chi tiết 1 session (load lại để render chat_history). */
    public static class SessionDetail {
        @SerializedName("session_id") public int sessionId;
        @SerializedName("session_title") public String sessionTitle;
        @SerializedName("status") public String status;
        @SerializedName("turn_count") public int turnCount;
        @SerializedName("resolved_problem_id") public String resolvedProblemId;
        @SerializedName("chat_history") public List<HistoryItem> chatHistory;
    }
}
