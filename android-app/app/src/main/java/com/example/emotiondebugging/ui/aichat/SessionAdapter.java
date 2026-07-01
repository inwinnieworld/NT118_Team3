package com.example.emotiondebugging.ui.aichat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter danh sách session. Giữ danh sách gốc riêng để lọc tại chỗ theo ô tìm kiếm
 * (lọc thuần UI, chưa gọi backend).
 */
public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {

    public interface OnSessionClickListener {
        void onSessionClick(Session session);
    }

    private final List<Session> allSessions = new ArrayList<>();
    private final List<Session> visibleSessions = new ArrayList<>();
    private final OnSessionClickListener listener;

    public SessionAdapter(OnSessionClickListener listener) {
        this.listener = listener;
    }

    public void setSessions(List<Session> sessions) {
        allSessions.clear();
        visibleSessions.clear();
        if (sessions != null) {
            allSessions.addAll(sessions);
            visibleSessions.addAll(sessions);
        }
        notifyDataSetChanged();
    }

    /** Lọc theo tiêu đề (không phân biệt hoa thường). Query rỗng = hiện tất cả. */
    public void filter(String query) {
        visibleSessions.clear();
        if (query == null || query.trim().isEmpty()) {
            visibleSessions.addAll(allSessions);
        } else {
            String q = query.trim().toLowerCase(Locale.getDefault());
            for (Session s : allSessions) {
                if (s.getTitle() != null
                        && s.getTitle().toLowerCase(Locale.getDefault()).contains(q)) {
                    visibleSessions.add(s);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        holder.bind(visibleSessions.get(position));
    }

    @Override
    public int getItemCount() {
        return visibleSessions.size();
    }

    class SessionViewHolder extends RecyclerView.ViewHolder {
        final TextView tvSessionTitle;

        SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSessionTitle = (TextView) itemView;
        }

        void bind(Session session) {
            tvSessionTitle.setText(session.getTitle());
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSessionClick(session);
            });
        }
    }
}
