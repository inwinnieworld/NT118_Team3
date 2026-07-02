package com.example.emotiondebugging.ui.community.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.CommunityPostResponse;

import java.util.ArrayList;
import java.util.List;

public class CommunityMediaAdapter extends RecyclerView.Adapter<CommunityMediaAdapter.MediaViewHolder> {

    public interface OnMediaClickListener {
        void onMediaClick(CommunityPostResponse.PostItem post);
    }

    private List<CommunityPostResponse.PostItem> posts = new ArrayList<>();
    private final OnMediaClickListener listener;

    public CommunityMediaAdapter(OnMediaClickListener listener) {
        this.listener = listener;
    }

    public void setPosts(List<CommunityPostResponse.PostItem> newPosts) {
        this.posts = newPosts != null ? newPosts : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        CommunityPostResponse.PostItem post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class MediaViewHolder extends RecyclerView.ViewHolder {

        ImageView ivMedia;

        MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMedia = itemView.findViewById(R.id.iv_media);
        }

        void bind(CommunityPostResponse.PostItem post) {
            if (post == null) return;

            String imageUrl = post.imageUrl;

            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                ivMedia.setImageResource(R.drawable.bg_post_card);
                return;
            }

            imageUrl = imageUrl.trim();

            if (imageUrl.startsWith("/")) {
                imageUrl = "http://10.0.2.2:3000" + imageUrl;
            }

            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.bg_post_card)
                    .error(R.drawable.bg_post_card)
                    .into(ivMedia);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMediaClick(post);
                }
            });
        }
    }
}