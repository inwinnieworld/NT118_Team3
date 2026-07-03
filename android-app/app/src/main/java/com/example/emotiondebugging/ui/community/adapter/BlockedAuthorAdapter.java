package com.example.emotiondebugging.ui.community.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.community.FollowUser;
import com.example.emotiondebugging.utils.AvatarHelper;

import java.util.ArrayList;
import java.util.List;

public class BlockedAuthorAdapter extends RecyclerView.Adapter<BlockedAuthorAdapter.BlockedViewHolder> {

    public interface OnUnblockListener {
        void onUnblock(FollowUser user);
    }

    private List<FollowUser> users = new ArrayList<>();
    private final OnUnblockListener listener;

    public BlockedAuthorAdapter(OnUnblockListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<FollowUser> newUsers) {
        this.users = newUsers != null ? newUsers : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BlockedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blocked_author, parent, false);
        return new BlockedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlockedViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class BlockedViewHolder extends RecyclerView.ViewHolder {

        ImageView ivAvatar;
        TextView tvDisplayName;
        TextView tvUsername;
        Button btnUnblock;

        BlockedViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvDisplayName = itemView.findViewById(R.id.tv_display_name);
            tvUsername = itemView.findViewById(R.id.tv_username);
            btnUnblock = itemView.findViewById(R.id.btn_unblock);
        }

        void bind(FollowUser user) {
            if (user == null) return;

            String name = user.displayName != null && !user.displayName.trim().isEmpty()
                    ? user.displayName
                    : user.username;

            tvDisplayName.setText(name != null ? name : "Người dùng");
            tvUsername.setText(user.username != null ? "@" + user.username : "");

            AvatarHelper.loadAvatar(ivAvatar, user.avatarUrl, name);

            btnUnblock.setOnClickListener(v -> {
                if (listener != null) listener.onUnblock(user);
            });
        }
    }
}
