package com.example.emotiondebugging.ui.community;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.response.ApiResponse;
import com.example.emotiondebugging.utils.SharedPrefsHelper;
import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.chat.ChatHistoryResponse;
import com.example.emotiondebugging.model.chat.ChatMessage;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.graphics.drawable.GradientDrawable;

import android.content.Intent;


public class ChatActivity extends AppCompatActivity {

    private int targetStudentId;
    private String targetName;
    private String targetUsername;
    private String targetAvatarText;
    private String authToken;

    private Socket socket;

    private LinearLayout messagesLayout;
    private ScrollView scrollView;
    private EditText etMessage;
    private int targetFollowerCount;
    private boolean targetFollowedByMe;

    // Dùng chung base URL với Retrofit (ApiConstants tự detect emulator/máy thật).
    // Bỏ "/" cuối vì Socket.IO URL không cần trailing slash.
    private final String BASE_SOCKET_URL =
            com.example.emotiondebugging.utils.ApiConstants.BASE_URL.replaceAll("/+$", "");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        targetStudentId = getIntent().getIntExtra("target_student_id", -1);
        targetName = getIntent().getStringExtra("target_name");
        targetUsername = getIntent().getStringExtra("target_username");
        targetAvatarText = getIntent().getStringExtra("target_avatar_text");
        targetFollowerCount = getIntent().getIntExtra("target_follower_count", 0);
        targetFollowedByMe = getIntent().getBooleanExtra("target_followed_by_me", false);

        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        String token = prefs.getToken();
        authToken = token != null ? "Bearer " + token : "";

        setupThreadsLikeLayout();
        loadMessageHistory();
        connectSocket();
    }

    private void setupThreadsLikeLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(36), dp(16), dp(10));
        header.setBackgroundColor(Color.WHITE);

        TextView btnBack = new TextView(this);
        btnBack.setText("←");
        btnBack.setTextSize(28);
        btnBack.setTextColor(Color.BLACK);
        btnBack.setGravity(Gravity.CENTER);
        btnBack.setOnClickListener(v -> finish());

        TextView title = new TextView(this);
        title.setText(targetUsername != null && !targetUsername.trim().isEmpty()
                ? targetUsername
                : targetName);
        title.setTextSize(20);
        title.setTextColor(Color.BLACK);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(18, 0, 0, 0);

        TextView more = new TextView(this);
        more.setText("⋯");
        more.setTextSize(26);
        more.setTextColor(Color.BLACK);
        more.setGravity(Gravity.CENTER);

        header.addView(btnBack, new LinearLayout.LayoutParams(dp(40), dp(44)));
        header.addView(title, new LinearLayout.LayoutParams(0, dp(44), 1));
        header.addView(more, new LinearLayout.LayoutParams(dp(44), dp(44)));

        root.addView(header);

        ViewLine(root);

        // Scroll messages
        scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.WHITE);

        messagesLayout = new LinearLayout(this);
        messagesLayout.setOrientation(LinearLayout.VERTICAL);
        messagesLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        messagesLayout.setPadding(24, 32, 24, 24);

        addProfileIntro();

        scrollView.addView(messagesLayout);

        root.addView(scrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        ViewLine(root);

        // Message request note
        TextView requestNote = new TextView(this);
        requestNote.setText("Send a message request to " + safeText(targetUsername, targetName));
        requestNote.setTextColor(Color.BLACK);
        requestNote.setTextSize(15);
        requestNote.setTypeface(Typeface.DEFAULT_BOLD);
        requestNote.setGravity(Gravity.CENTER);
        requestNote.setPadding(24, 14, 24, 2);
        root.addView(requestNote);

        TextView subNote = new TextView(this);
        subNote.setText("You can send up to 3 messages before they accept your request.");
        subNote.setTextColor(Color.parseColor("#9CA3AF"));
        subNote.setTextSize(13);
        subNote.setGravity(Gravity.CENTER);
        subNote.setPadding(24, 0, 24, 12);
        root.addView(subNote);

        // Input row
        LinearLayout inputRow = new LinearLayout(this);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setGravity(Gravity.CENTER_VERTICAL);
        inputRow.setPadding(dp(16), dp(8), dp(16), dp(12));
        inputRow.setBackgroundColor(Color.WHITE);

        etMessage = new EditText(this);
        etMessage.setHint("Message...");
        etMessage.setHintTextColor(Color.parseColor("#9CA3AF"));
        etMessage.setTextColor(Color.BLACK);
        etMessage.setTextSize(16);
        etMessage.setSingleLine(false);
        etMessage.setMinLines(1);
        etMessage.setMaxLines(4);
        etMessage.setImeOptions(EditorInfo.IME_ACTION_SEND);
        etMessage.setBackgroundColor(Color.parseColor("#F3F4F6"));
        etMessage.setPadding(22, 10, 22, 10);

        Button btnSend = new Button(this);
        btnSend.setText("Gửi");
        btnSend.setTextColor(Color.WHITE);
        btnSend.setBackgroundColor(Color.BLACK);
        btnSend.setOnClickListener(v -> sendMessage());

        inputRow.addView(etMessage, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        LinearLayout.LayoutParams sendParams = new LinearLayout.LayoutParams(
                dp(56),
                dp(48)
        );
        sendParams.setMargins(dp(8), 0, 0, 0);
        inputRow.addView(btnSend, sendParams);

        root.addView(inputRow);

        setContentView(root);
    }

    private void addProfileIntro() {
        LinearLayout profileBox = new LinearLayout(this);
        profileBox.setOrientation(LinearLayout.VERTICAL);
        profileBox.setGravity(Gravity.CENTER_HORIZONTAL);
        profileBox.setPadding(dp(24), dp(120), dp(24), dp(24));

        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        messagesLayout.addView(profileBox, boxParams);

        TextView avatar = new TextView(this);
        avatar.setText(targetAvatarText != null ? targetAvatarText : "?");
        avatar.setTextColor(Color.WHITE);
        avatar.setTextSize(24);
        avatar.setTypeface(Typeface.DEFAULT_BOLD);
        avatar.setGravity(Gravity.CENTER);

        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(Color.parseColor("#12B2C1"));
        avatar.setBackground(avatarBg);

        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(64), dp(64));
        avatarParams.gravity = Gravity.CENTER_HORIZONTAL;
        avatarParams.setMargins(0, 0, 0, dp(12));
        profileBox.addView(avatar, avatarParams);

        TextView username = new TextView(this);
        username.setText(safeText(targetUsername, targetName));
        username.setTextColor(Color.BLACK);
        username.setTextSize(22);
        username.setTypeface(Typeface.DEFAULT_BOLD);
        username.setGravity(Gravity.CENTER);
        username.setSingleLine(true);
        username.setEllipsize(android.text.TextUtils.TruncateAt.END);
        profileBox.addView(username);

        TextView displayName = new TextView(this);
        displayName.setText(targetName != null ? targetName : "");
        displayName.setTextColor(Color.parseColor("#9CA3AF"));
        displayName.setTextSize(15);
        displayName.setGravity(Gravity.CENTER);
        displayName.setPadding(0, dp(4), 0, 0);
        profileBox.addView(displayName);

        TextView followInfo = new TextView(this);
        followInfo.setText(targetFollowerCount + " followers");
        followInfo.setTextColor(Color.parseColor("#9CA3AF"));
        followInfo.setTextSize(14);
        followInfo.setGravity(Gravity.CENTER);
        followInfo.setPadding(0, dp(4), 0, 0);
        profileBox.addView(followInfo);

        TextView status = new TextView(this);
        status.setText(targetFollowedByMe
                ? "You follow this account on Threads"
                : "You don't follow each other on Threads");
        status.setTextColor(Color.parseColor("#9CA3AF"));
        status.setTextSize(14);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, dp(6), 0, dp(18));
        profileBox.addView(status);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setGravity(Gravity.CENTER);

        Button btnViewProfile = new Button(this);
        btnViewProfile.setText("View profile");
        btnViewProfile.setAllCaps(false);
        btnViewProfile.setTextColor(Color.BLACK);
        btnViewProfile.setTextSize(14);
        btnViewProfile.setTypeface(Typeface.DEFAULT_BOLD);
        btnViewProfile.setBackground(makeOutlineButtonBg());

        Button btnFollow = new Button(this);
        btnFollow.setText(targetFollowedByMe ? "Following" : "Follow");
        btnFollow.setAllCaps(false);
        btnFollow.setTextColor(Color.BLACK);
        btnFollow.setTextSize(14);
        btnFollow.setTypeface(Typeface.DEFAULT_BOLD);
        btnFollow.setBackground(makeOutlineButtonBg());

        LinearLayout.LayoutParams viewProfileParams = new LinearLayout.LayoutParams(dp(140), dp(48));
        viewProfileParams.setMargins(dp(4), 0, dp(4), 0);
        buttonRow.addView(btnViewProfile, viewProfileParams);

        LinearLayout.LayoutParams followParams = new LinearLayout.LayoutParams(dp(100), dp(48));
        followParams.setMargins(dp(4), 0, dp(4), 0);
        buttonRow.addView(btnFollow, followParams);

        profileBox.addView(buttonRow);

        btnViewProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, CommunityProfileActivity.class);
            intent.putExtra("student_id", targetStudentId);
            startActivity(intent);
        });

        btnFollow.setOnClickListener(v -> {
            if (targetStudentId <= 0) return;

            if (targetFollowedByMe) {
                RetrofitClient.getCommunityApi()
                        .unfollowUser(authToken, targetStudentId)
                        .enqueue(new Callback<ApiResponse<Object>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                                if (response.isSuccessful()) {
                                    targetFollowedByMe = false;
                                    targetFollowerCount = Math.max(0, targetFollowerCount - 1);

                                    btnFollow.setText("Follow");
                                    followInfo.setText(targetFollowerCount + " followers");
                                    status.setText("You don't follow each other on Threads");
                                } else {
                                    Toast.makeText(ChatActivity.this, "Không thể bỏ theo dõi", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                                Toast.makeText(ChatActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                RetrofitClient.getCommunityApi()
                        .followUser(authToken, targetStudentId)
                        .enqueue(new Callback<ApiResponse<Object>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                                if (response.isSuccessful()) {
                                    targetFollowedByMe = true;
                                    targetFollowerCount = targetFollowerCount + 1;

                                    btnFollow.setText("Following");
                                    followInfo.setText(targetFollowerCount + " followers");
                                    status.setText("You follow this account on Threads");
                                } else {
                                    Toast.makeText(ChatActivity.this, "Không thể theo dõi", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                                Toast.makeText(ChatActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private GradientDrawable makeOutlineButtonBg() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.WHITE);
        drawable.setCornerRadius(18);
        drawable.setStroke(2, Color.parseColor("#D1D5DB"));
        return drawable;
    }

    private void connectSocket() {
        try {
            IO.Options options = new IO.Options();
            options.auth = new java.util.HashMap<>();
            options.auth.put("token", authToken);
            options.reconnection = true;

            socket = IO.socket(BASE_SOCKET_URL, options);

            socket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() ->
                    addSystemMessage("Đã kết nối realtime")
            ));

            socket.on(Socket.EVENT_CONNECT_ERROR, args -> runOnUiThread(() -> {
                String msg = args != null && args.length > 0 ? String.valueOf(args[0]) : "Unknown";
                addSystemMessage("Socket lỗi: " + msg);
            }));

            socket.on("chat:new_message", args -> runOnUiThread(() -> {
                try {
                    JSONObject obj = (JSONObject) args[0];

                    int senderId = obj.getInt("sender_student_id");
                    int receiverId = obj.getInt("receiver_student_id");
                    String text = obj.getString("message_text");

                    // Nếu tin mình gửi, đã add local rồi nên bỏ qua echo từ server.
                    if (receiverId == targetStudentId && senderId != targetStudentId) {
                        return;
                    }

                    // Tin từ người đang chat gửi tới mình
                    if (senderId == targetStudentId) {
                        addMessage(text, false);
                    }

                } catch (Exception e) {
                    addSystemMessage("Parse message lỗi: " + e.getMessage());
                }
            }));

            socket.connect();
        } catch (Exception e) {
            addSystemMessage("Lỗi socket: " + e.getMessage());
        }
    }

    private void loadMessageHistory() {
        if (targetStudentId <= 0) {
            addSystemMessage("Không có targetStudentId nên không tải được lịch sử");
            return;
        }

        android.util.Log.d("CHAT_HISTORY", "targetStudentId = " + targetStudentId);
        android.util.Log.d("CHAT_HISTORY", "authToken empty = " + authToken.isEmpty());

        Call<ApiResponse<ChatHistoryResponse>> historyCall =
                RetrofitClient.getCommunityApi().getMessagesWithUser(authToken, targetStudentId);

        android.util.Log.d("CHAT_HISTORY", "url = " + historyCall.request().url());

        historyCall.enqueue(new Callback<ApiResponse<ChatHistoryResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<ChatHistoryResponse>> call,
                            Response<ApiResponse<ChatHistoryResponse>> response
                    ) {
                        try {
                            android.util.Log.d("CHAT_HISTORY", "code = " + response.code());

                            if (!response.isSuccessful()) {
                                String error = "";
                                if (response.errorBody() != null) {
                                    error = response.errorBody().string();
                                }

                                android.util.Log.e("CHAT_HISTORY", "error = " + error);
                                addSystemMessage("Không tải được lịch sử: " + response.code());
                                return;
                            }

                            if (response.body() == null || response.body().getData() == null) {
                                android.util.Log.e("CHAT_HISTORY", "body hoặc data null");
                                addSystemMessage("API lịch sử trả data rỗng");
                                return;
                            }

                            ChatHistoryResponse data = response.body().getData();

                            if (data.messages == null || data.messages.isEmpty()) {
                                android.util.Log.d("CHAT_HISTORY", "messages empty");
                                return;
                            }

                            android.util.Log.d(
                                    "CHAT_HISTORY",
                                    "currentStudentId = " + data.currentStudentId
                                            + ", targetStudentId = " + data.targetStudentId
                                            + ", size = " + data.messages.size()
                            );

                            for (ChatMessage msg : data.messages) {
                                if (msg == null) continue;

                                String text = msg.messageText != null ? msg.messageText.trim() : "";
                                if (text.isEmpty()) continue;

                                boolean isMine = msg.senderStudentId == data.currentStudentId;

                                addMessage(text, isMine);

                                android.util.Log.d(
                                        "CHAT_HISTORY",
                                        "show message id=" + msg.messageId
                                                + ", sender=" + msg.senderStudentId
                                                + ", receiver=" + msg.receiverStudentId
                                                + ", isMine=" + isMine
                                                + ", text=" + text
                                );
                            }
                        } catch (Exception e) {
                            android.util.Log.e("CHAT_HISTORY", "parse error", e);
                            addSystemMessage("Lỗi đọc lịch sử: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ChatHistoryResponse>> call, Throwable t) {
                        android.util.Log.e("CHAT_HISTORY", "request failed", t);
                        addSystemMessage("Không tải được lịch sử: " + t.getMessage());
                    }
                });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        if (socket == null || !socket.connected()) {
            addSystemMessage("Socket chưa kết nối, chưa gửi được");
            return;
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("receiver_student_id", targetStudentId);
            payload.put("message_text", text);

            socket.emit("chat:send", payload);

            addMessage(text, true);
            etMessage.setText("");

        } catch (Exception e) {
            addSystemMessage("Không gửi được: " + e.getMessage());
        }
    }

    private void addMessage(String text, boolean isMine) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(15);
        tv.setTextColor(isMine ? Color.WHITE : Color.BLACK);
        tv.setPadding(18, 12, 18, 12);
        tv.setMaxWidth(720);

        tv.setBackgroundColor(isMine
                ? Color.BLACK
                : Color.parseColor("#F3F4F6")
        );

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = isMine ? Gravity.END : Gravity.START;
        params.setMargins(0, 8, 0, 8);

        messagesLayout.addView(tv, params);
        scrollToBottom();
    }

    private void addSystemMessage(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.parseColor("#9CA3AF"));
        tv.setTextSize(12);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, 8, 0, 8);

        messagesLayout.addView(tv);
        scrollToBottom();
    }

    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void ViewLine(LinearLayout root) {
        android.view.View line = new android.view.View(this);
        line.setBackgroundColor(Color.parseColor("#E5E7EB"));
        root.addView(line, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        ));
    }

    private String safeText(String first, String fallback) {
        if (first != null && !first.trim().isEmpty()) return first;
        if (fallback != null && !fallback.trim().isEmpty()) return fallback;
        return "Chat";
    }

    private int getIntValue(Object value) {
        if (value == null) return 0;
        if (value instanceof Double) return ((Double) value).intValue();
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (socket != null) {
            socket.disconnect();
            socket.off();
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}