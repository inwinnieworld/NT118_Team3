package com.example.emotiondebugging.ui.aichat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.response.AiChatModels;
import com.example.emotiondebugging.model.response.AiChatModels.ChatAction;
import com.example.emotiondebugging.model.response.AiChatModels.MessageData;
import com.example.emotiondebugging.model.response.AiChatModels.ProblemRef;
import com.example.emotiondebugging.model.response.AiChatModels.Quest;
import com.example.emotiondebugging.model.response.AiChatModels.StartSessionData;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.model.response.ProfileResponse;
import com.example.emotiondebugging.ui.community.CommunityActivity;
import com.example.emotiondebugging.ui.journal.GitJournalActivity;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn AI Chat (Dr.Bug's chat room) — nối backend thật (/api/aichat/*).
 *
 * Luồng:
 *  - Mở màn: gọi /sessions/start → lời chào + gợi ý Tầng 2. CHƯA tạo session.
 *  - User gửi lượt đầu (text/gợi ý): /messages với session_id=null → backend tạo session,
 *    trả về session_id. Các lượt sau gửi kèm session_id đó.
 *  - Bong bóng AI render kèm action: quest cards / nút redirect / popup select_priority.
 */
public class AiChatActivity extends AppCompatActivity {

    private RecyclerView recyclerChat;
    private EditText etMessage;
    private ImageView btnSend, btnVoice;
    private ChatAdapter adapter;

    private String authToken;
    // session_id hiện tại; 0 = chưa tạo (chưa gửi lượt nào).
    private int currentSessionId = 0;
    // status session hiện tại ("active" | "pending_feedback" | "completed"); null = chưa rõ.
    // Khác "active" nghĩa là session đã kết thúc → gõ tiếp sẽ mở phiên mới.
    private String currentSessionStatus = null;
    // Đang chờ phản hồi server → chặn gửi tiếp.
    private boolean sending = false;

    // true nếu màn này mở từ danh sách session (xem 1 session cũ) — transient.
    // Khi đó, mở lại list sẽ tự finish màn này để không chồng nhiều màn "xem session"
    // trên back stack (back luôn về chat gốc/giao diện chính).
    private boolean openedFromList = false;

    // Map nội dung gợi ý (title) → problem_id Tầng 2 để gửi picked_problem_id khi user bấm.
    private final Map<String, String> suggestionIdByTitle = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        recyclerChat = findViewById(R.id.recyclerChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnVoice = findViewById(R.id.btnVoice);

        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        String token = prefs.getToken();
        authToken = token != null ? "Bearer " + token : null;

        // Cho phép mở lại 1 session cũ từ màn danh sách (truyền session_id qua Intent).
        int openSessionId = getIntent().getIntExtra("session_id", 0);
        openedFromList = openSessionId > 0;

        setupRecycler(prefs);
        setupListeners();

        if (openSessionId > 0) {
            loadExistingSession(openSessionId);
        } else {
            startSession();
        }
    }

    private void setupRecycler(SharedPrefsHelper prefs) {
        adapter = new ChatAdapter(this::onSuggestionChosen);
        adapter.setUserInfo(null, prefs.getName());
        adapter.setOnActionListener(new ChatAdapter.OnActionListener() {
            @Override public void onQuestClick(Quest quest) {
                // TODO(quest): Intent sang Quest Engine bằng quest.questId khi module sẵn sàng.
                Toast.makeText(AiChatActivity.this,
                        "Quest: " + quest.title + " (sắp có)", Toast.LENGTH_SHORT).show();
            }
            @Override public void onRedirectClick(String targetScreen) {
                handleRedirect(targetScreen);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerChat.setLayoutManager(layoutManager);
        recyclerChat.setAdapter(adapter);

        loadUserAvatar(prefs);
    }

    /** Lấy avatar thật từ API profile (SharedPrefs không lưu URL), rồi refresh bubble user. */
    private void loadUserAvatar(SharedPrefsHelper prefs) {
        if (authToken == null) return;
        RetrofitClient.getProfileApi().getProfile(authToken)
                .enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ProfileResponse.Data data = response.body().getData();
                            if (data != null) {
                                adapter.setUserInfo(data.avatarUrl, data.name);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<ProfileResponse> call, Throwable t) { }
                });
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> sendCurrentInput());

        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND
                    || actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                sendCurrentInput();
                return true;
            }
            return false;
        });

        btnVoice.setOnClickListener(v ->
                Toast.makeText(this, "Tính năng voice sẽ sớm có mặt", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnSessions).setOnClickListener(v -> openSessionList());
        findViewById(R.id.btnSearch).setOnClickListener(v -> openSessionList());

        // "+" mở phiên chat mới. Session chỉ thực sự tạo khi user gửi lượt đầu (backend lo).
        findViewById(R.id.btnAddSession).setOnClickListener(v -> {
            currentSessionId = 0;
            currentSessionStatus = null;
            adapter.setMessages(new ArrayList<>());
            startSession();
        });
    }

    /**
     * Mở màn danh sách session. Nếu màn chat này được mở TỪ danh sách (xem 1 session cũ),
     * finish() chính nó để không chồng lên stack — tránh việc back nhảy về session đã xem
     * thay vì về giao diện chính. Màn chat gốc (mở từ Main) thì giữ nguyên trên stack.
     */
    private void openSessionList() {
        startActivity(new Intent(this, SessionListActivity.class));
        if (openedFromList) finish();
    }

    // ===================== START / LOAD =====================

    /** Mở UI: gọi /sessions/start để lấy lời chào + gợi ý Tầng 2. KHÔNG tạo session. */
    private void startSession() {
        if (authToken == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        RetrofitClient.getAiChatApi().startSession(authToken)
                .enqueue(new Callback<ApiResponse<StartSessionData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<StartSessionData>> call,
                                           Response<ApiResponse<StartSessionData>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            renderOpening(response.body().getData());
                        } else {
                            adapter.addMessage(ChatMessage.ai(
                                    "Mình chưa kết nối được. Bạn thử lại sau một chút nhé."));
                            scrollToBottom();
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<StartSessionData>> call, Throwable t) {
                        adapter.addMessage(ChatMessage.ai(
                                "Mình chưa kết nối được server. Bạn kiểm tra mạng giúp mình nhé."));
                        scrollToBottom();
                    }
                });
    }

    private void renderOpening(StartSessionData data) {
        adapter.addMessage(ChatMessage.ai(data.openingMessage));

        suggestionIdByTitle.clear();
        List<String> titles = new ArrayList<>();
        if (data.suggestions != null) {
            for (ProblemRef ref : data.suggestions) {
                if (ref == null || ref.title == null) continue;
                titles.add(ref.title);
                suggestionIdByTitle.put(ref.title, ref.id);
            }
        }
        if (!titles.isEmpty()) {
            adapter.addMessage(ChatMessage.suggestions(titles));
        }
        scrollToBottom();
    }

    /** Mở lại 1 session cũ: load chat_history rồi render. */
    private void loadExistingSession(int sessionId) {
        currentSessionId = sessionId;
        RetrofitClient.getAiChatApi().getSession(authToken, sessionId)
                .enqueue(new Callback<ApiResponse<com.example.emotiondebugging.model.response.AiChatModels.SessionDetail>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<com.example.emotiondebugging.model.response.AiChatModels.SessionDetail>> call,
                                           Response<ApiResponse<com.example.emotiondebugging.model.response.AiChatModels.SessionDetail>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            currentSessionStatus = response.body().getData().status;
                            renderHistory(response.body().getData().chatHistory);
                        } else {
                            startSession();
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<com.example.emotiondebugging.model.response.AiChatModels.SessionDetail>> call, Throwable t) {
                        Toast.makeText(AiChatActivity.this,
                                "Không tải được session", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderHistory(List<com.example.emotiondebugging.model.response.AiChatModels.HistoryItem> history) {
        List<ChatMessage> messages = new ArrayList<>();
        if (history != null) {
            for (com.example.emotiondebugging.model.response.AiChatModels.HistoryItem item : history) {
                if (item == null || item.content == null) continue;
                if ("user".equals(item.sender)) {
                    messages.add(ChatMessage.user(item.content));
                } else {
                    messages.add(ChatMessage.ai(item.content, item.metadata));
                }
            }
        }
        adapter.setMessages(messages);
        scrollToBottom();
    }

    // ===================== SEND =====================

    /** User bấm 1 dòng gợi ý → gửi kèm picked_problem_id (nếu map được). */
    private void onSuggestionChosen(String suggestion) {
        adapter.removeSuggestions();
        String pickedId = suggestionIdByTitle.get(suggestion);
        adapter.addMessage(ChatMessage.user(suggestion));
        scrollToBottom();
        postMessage(suggestion, pickedId);
    }

    /** User tự nhập rồi bấm Gửi. */
    private void sendCurrentInput() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        if (sending) return;

        // Session đã kết thúc mà user gõ tiếp → mở phiên mới ngay trong màn này:
        // xoá hội thoại cũ + reset session_id, tin này thành lượt 1 (không có lời chào turn-0).
        if (currentSessionId > 0 && currentSessionStatus != null
                && !"active".equals(currentSessionStatus)) {
            adapter.setMessages(new ArrayList<>());
            currentSessionId = 0;
            currentSessionStatus = null;
        }

        adapter.removeSuggestions();
        etMessage.setText("");
        adapter.addMessage(ChatMessage.user(text));
        scrollToBottom();
        postMessage(text, null);
    }

    /** Gọi POST /messages. session_id=0 → backend tạo session mới. */
    private void postMessage(String text, String pickedProblemId) {
        if (authToken == null) return;
        sending = true;
        btnSend.setEnabled(false);

        Map<String, Object> body = new HashMap<>();
        body.put("text", text);
        if (currentSessionId > 0) body.put("session_id", currentSessionId);
        if (pickedProblemId != null) body.put("picked_problem_id", pickedProblemId);

        RetrofitClient.getAiChatApi().sendMessage(authToken, body)
                .enqueue(new Callback<ApiResponse<MessageData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<MessageData>> call,
                                           Response<ApiResponse<MessageData>> response) {
                        sending = false;
                        btnSend.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            handleMessageResponse(response.body().getData());
                        } else {
                            String msg = response.code() == 503
                                    ? "Trợ lý AI chưa được cấu hình. Vui lòng thử lại sau."
                                    : "Mình chưa trả lời được lúc này, bạn thử lại nhé.";
                            adapter.addMessage(ChatMessage.ai(msg));
                            scrollToBottom();
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<MessageData>> call, Throwable t) {
                        sending = false;
                        btnSend.setEnabled(true);
                        adapter.addMessage(ChatMessage.ai(
                                "Mình chưa kết nối được server. Bạn kiểm tra mạng giúp mình nhé."));
                        scrollToBottom();
                    }
                });
    }

    private void handleMessageResponse(MessageData data) {
        if (data.sessionId > 0) currentSessionId = data.sessionId;
        if (data.status != null) currentSessionStatus = data.status;

        ChatAction action = data.action;
        // select_priority: không có ai_message, hiện popup cho user chọn.
        if (action != null && "select_priority".equals(action.actionType)) {
            showPriorityDialog(action);
            return;
        }
        // select_route (lượt 4.2): không có ai_message, hiện popup 2 lựa chọn cho user.
        if (action != null && "select_route".equals(action.actionType)) {
            showRouteDialog(action);
            return;
        }

        String content = data.aiMessage != null ? data.aiMessage.content : null;
        if (content != null) {
            adapter.addMessage(ChatMessage.ai(content, action));
            scrollToBottom();
        }
    }

    // ===================== SELECT PRIORITY =====================

    private void showPriorityDialog(ChatAction action) {
        final List<ProblemRef> candidates =
                action.data != null ? action.data.candidates : null;
        if (candidates == null || candidates.isEmpty()) return;

        String[] titles = new String[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) titles[i] = candidates.get(i).title;

        new AlertDialog.Builder(this)
                .setTitle("Bạn muốn ưu tiên gỡ rối vấn đề nào trước?")
                .setItems(titles, (dialog, which) -> {
                    ProblemRef chosen = candidates.get(which);
                    pickPriority(chosen.id);
                })
                .setCancelable(false)
                .show();
    }

    private void pickPriority(String problemId) {
        if (authToken == null || currentSessionId <= 0) return;
        sending = true;
        btnSend.setEnabled(false);

        Map<String, String> body = new HashMap<>();
        body.put("problem_id", problemId);

        RetrofitClient.getAiChatApi().pickPriority(authToken, currentSessionId, body)
                .enqueue(new Callback<ApiResponse<MessageData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<MessageData>> call,
                                           Response<ApiResponse<MessageData>> response) {
                        sending = false;
                        btnSend.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            handleMessageResponse(response.body().getData());
                        } else {
                            adapter.addMessage(ChatMessage.ai(
                                    "Mình chưa xử lý được lựa chọn này, bạn thử lại nhé."));
                            scrollToBottom();
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<MessageData>> call, Throwable t) {
                        sending = false;
                        btnSend.setEnabled(true);
                        adapter.addMessage(ChatMessage.ai(
                                "Mình chưa kết nối được server. Bạn kiểm tra mạng giúp mình nhé."));
                        scrollToBottom();
                    }
                });
    }

    // ===================== SELECT ROUTE (lượt 4.2) =====================

    /** Popup 2 lựa chọn khi lượt cuối vẫn mơ hồ: quest thư giãn / lên cộng đồng. */
    private void showRouteDialog(ChatAction action) {
        final List<AiChatModels.RouteOption> options =
                action.data != null ? action.data.options : null;
        if (options == null || options.isEmpty()) return;

        String[] labels = new String[options.size()];
        for (int i = 0; i < options.size(); i++) labels[i] = options.get(i).label;

        new AlertDialog.Builder(this)
                .setTitle("Bạn muốn mình hỗ trợ theo hướng nào?")
                .setItems(labels, (dialog, which) -> {
                    AiChatModels.RouteOption chosen = options.get(which);
                    pickRoute(chosen.key);
                })
                .setCancelable(false)
                .show();
    }

    private void pickRoute(String routeKey) {
        if (authToken == null || currentSessionId <= 0) return;
        sending = true;
        btnSend.setEnabled(false);

        Map<String, String> body = new HashMap<>();
        body.put("route", routeKey);

        RetrofitClient.getAiChatApi().pickRoute(authToken, currentSessionId, body)
                .enqueue(new Callback<ApiResponse<MessageData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<MessageData>> call,
                                           Response<ApiResponse<MessageData>> response) {
                        sending = false;
                        btnSend.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            handleMessageResponse(response.body().getData());
                        } else {
                            adapter.addMessage(ChatMessage.ai(
                                    "Mình chưa xử lý được lựa chọn này, bạn thử lại nhé."));
                            scrollToBottom();
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<MessageData>> call, Throwable t) {
                        sending = false;
                        btnSend.setEnabled(true);
                        adapter.addMessage(ChatMessage.ai(
                                "Mình chưa kết nối được server. Bạn kiểm tra mạng giúp mình nhé."));
                        scrollToBottom();
                    }
                });
    }

    // ===================== REDIRECT =====================

    private void handleRedirect(String targetScreen) {
        if ("git_journal".equals(targetScreen)) {
            startActivity(new Intent(this, GitJournalActivity.class));
        } else if ("community".equals(targetScreen)) {
            startActivity(new Intent(this, CommunityActivity.class));
        }
    }

    private void scrollToBottom() {
        recyclerChat.post(() -> recyclerChat.smoothScrollToPosition(adapter.getLastPosition()));
    }
}
