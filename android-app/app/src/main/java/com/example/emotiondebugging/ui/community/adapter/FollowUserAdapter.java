package com.example.emotiondebugging.ui.community.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.community.FollowUser;
import com.example.emotiondebugging.utils.AvatarHelper;

import java.util.ArrayList;
import java.util.List;

public class FollowUserAdapter extends RecyclerView.Adapter<FollowUserAdapter.FollowViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(FollowUser user);
    }

    private List<FollowUser> users = new ArrayList<>();
    private final OnUserClickListener listener;

    public FollowUserAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<FollowUser> newUsers) {
        this.users = newUsers != null ? newUsers : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FollowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_follow_user, parent, false);
        return new FollowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class FollowViewHolder extends RecyclerView.ViewHolder {

        ImageView ivAvatar;
        TextView tvDisplayName;
        TextView tvUsername;

        FollowViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvDisplayName = itemView.findViewById(R.id.tv_display_name);
            tvUsername = itemView.findViewById(R.id.tv_username);
        }

        void bind(FollowUser user) {
            if (user == null) return;

            String name = user.displayName != null && !user.displayName.trim().isEmpty()
                    ? user.displayName
                    : user.username;

            tvDisplayName.setText(name != null ? name : "Người dùng");
            tvUsername.setText(user.username != null ? "@" + user.username : "");

            AvatarHelper.loadAvatar(ivAvatar, user.avatarUrl, name);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onUserClick(user);
            });
        }
    }
}
