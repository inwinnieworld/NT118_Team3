package com.example.emotiondebugging.ui.aichat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.AiChatModels.ChatAction;
import com.example.emotiondebugging.model.response.AiChatModels.Quest;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter render chat_history với 3 loại view: bong bóng AI, bong bóng user, khối gợi ý.
 * Khối gợi ý tự inflate từng dòng theo số lượng data (backend có thể trả khác 3).
 *
 * Bong bóng AI có thể kèm action (metadata): quest cards (show_quests) hoặc nút điều hướng
 * (redirect_feature). Adapter render component phụ ngay dưới bong bóng dựa vào action_type.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_AI = 0;
    private static final int TYPE_USER = 1;
    private static final int TYPE_SUGGESTIONS = 2;

    public interface OnSuggestionClickListener {
        void onSuggestionClick(String suggestion);
    }

    /** Tương tác với action gắn trên bong bóng AI (quest card / nút redirect). */
    public interface OnActionListener {
        void onQuestClick(Quest quest);
        void onRedirectClick(String targetScreen);
    }

    private final List<ChatMessage> messages = new ArrayList<>();
    private final OnSuggestionClickListener suggestionListener;
    private OnActionListener actionListener;
    private String userAvatarUrl;
    private String userName;

    public ChatAdapter(OnSuggestionClickListener suggestionListener) {
        this.suggestionListener = suggestionListener;
    }

    public void setOnActionListener(OnActionListener listener) {
        this.actionListener = listener;
    }

    public void setUserInfo(String avatarUrl, String name) {
        this.userAvatarUrl = avatarUrl;
        this.userName = name;
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void setMessages(List<ChatMessage> newMessages) {
        messages.clear();
        if (newMessages != null) messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    /** Gỡ khối gợi ý (gọi khi user đã chọn 1 gợi ý hoặc tự nhập). */
    public void removeSuggestions() {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getType() == ChatMessage.Type.SUGGESTIONS) {
                messages.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public int getLastPosition() {
        return messages.size() - 1;
    }

    @Override
    public int getItemViewType(int position) {
        switch (messages.get(position).getType()) {
            case USER_TEXT: return TYPE_USER;
            case SUGGESTIONS: return TYPE_SUGGESTIONS;
            case AI_TEXT:
            default: return TYPE_AI;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_USER:
                return new UserViewHolder(
                        inflater.inflate(R.layout.item_chat_user, parent, false));
            case TYPE_SUGGESTIONS:
                return new SuggestionsViewHolder(
                        inflater.inflate(R.layout.item_chat_suggestions, parent, false));
            case TYPE_AI:
            default:
                return new AiViewHolder(
                        inflater.inflate(R.layout.item_chat_ai, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof AiViewHolder) {
            ((AiViewHolder) holder).bind(message);
        } else if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(message);
        } else if (holder instanceof SuggestionsViewHolder) {
            ((SuggestionsViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class AiViewHolder extends RecyclerView.ViewHolder {
        final TextView tvMessage, tvQuestPlaceholder, btnRedirect;
        final RecyclerView recyclerQuests;

        AiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvQuestPlaceholder = itemView.findViewById(R.id.tvQuestPlaceholder);
            btnRedirect = itemView.findViewById(R.id.btnRedirect);
            recyclerQuests = itemView.findViewById(R.id.recyclerQuests);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.getContent());

            // Mặc định ẩn hết component phụ; chỉ bật theo action_type.
            recyclerQuests.setVisibility(View.GONE);
            tvQuestPlaceholder.setVisibility(View.GONE);
            btnRedirect.setVisibility(View.GONE);

            ChatAction action = message.getAction();
            if (action == null || action.actionType == null) return;

            switch (action.actionType) {
                case "show_quests":
                    bindQuests(action);
                    break;
                case "redirect_feature":
                    bindRedirect(action);
                    break;
                default:
                    break;
            }
        }

        private void bindQuests(ChatAction action) {
            List<Quest> quests = action.data != null ? action.data.quests : null;
            if (quests != null && !quests.isEmpty()) {
                recyclerQuests.setVisibility(View.VISIBLE);
                recyclerQuests.setLayoutManager(new LinearLayoutManager(
                        recyclerQuests.getContext(), LinearLayoutManager.HORIZONTAL, false));
                recyclerQuests.setAdapter(new QuestCardAdapter(quests, quest -> {
                    if (actionListener != null) actionListener.onQuestClick(quest);
                }));
            } else {
                // Quest Engine chưa build → placeholder.
                tvQuestPlaceholder.setVisibility(View.VISIBLE);
            }
        }

        private void bindRedirect(ChatAction action) {
            String target = action.data != null ? action.data.targetScreen : null;
            btnRedirect.setVisibility(View.VISIBLE);
            btnRedirect.setText(redirectLabel(target));
            btnRedirect.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onRedirectClick(target);
            });
        }

        private String redirectLabel(String target) {
            if ("git_journal".equals(target)) return "Đi tới Git Journal";
            if ("community".equals(target)) return "Chia sẻ lên Cộng đồng";
            return "Tiếp tục";
        }
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        final TextView tvMessage;
        final ImageView ivUserAvatar;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.getContent());
            com.example.emotiondebugging.utils.AvatarHelper.loadAvatar(
                    ivUserAvatar, userAvatarUrl, userName);
        }
    }

    class SuggestionsViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout container;

        SuggestionsViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.containerSuggestions);
        }

        void bind(ChatMessage message) {
            container.removeAllViews();
            List<String> suggestions = message.getSuggestions();
            if (suggestions == null) return;
            LayoutInflater inflater = LayoutInflater.from(container.getContext());
            for (String suggestion : suggestions) {
                TextView row = (TextView) inflater.inflate(
                        R.layout.item_chat_suggestion_row, container, false);
                row.setText(suggestion);
                row.setOnClickListener(v -> {
                    if (suggestionListener != null) {
                        suggestionListener.onSuggestionClick(suggestion);
                    }
                });
                container.addView(row);
            }
        }
    }
}
