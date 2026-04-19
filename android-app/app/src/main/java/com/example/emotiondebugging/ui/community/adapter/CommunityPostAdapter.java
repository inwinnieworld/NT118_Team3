package com.example.emotiondebugging.ui.community.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.CommunityPostResponse;

import java.util.ArrayList;
import java.util.List;

public class CommunityPostAdapter extends RecyclerView.Adapter<CommunityPostAdapter.PostViewHolder> {

    public interface OnPostClickListener {
        void onUpvote(CommunityPostResponse.PostItem post);
        void onDownvote(CommunityPostResponse.PostItem post);
        void onPostClick(CommunityPostResponse.PostItem post);
        void onSave(CommunityPostResponse.PostItem post);
        void onMute(CommunityPostResponse.PostItem post);
        void onTagClick(int errorTypeId, String errorName);
    }

    private List<CommunityPostResponse.PostItem> posts = new ArrayList<>();
    private OnPostClickListener listener;

    public CommunityPostAdapter(OnPostClickListener listener) {
        this.listener = listener;
    }

    public void setPosts(List<CommunityPostResponse.PostItem> newPosts) {
        this.posts = newPosts != null ? newPosts : new ArrayList<>();
        android.util.Log.d("CommunityAdapter", "setPosts: " + this.posts.size() + " items");
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_community_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.bind(posts.get(position));
    }

    @Override
    public int getItemCount() { return posts.size(); }

    class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPreview, tvTag, tvVoteCount, tvDownvoteCount;
        TextView tvCommentCount, tvViewCount, tvTime, tvAuthorName;
        ImageButton btnUpvote, btnDownvote;
        android.widget.ImageView ivAvatar;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvAuthorName = itemView.findViewById(R.id.tv_author_name);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPreview = itemView.findViewById(R.id.tv_preview);
            tvTag = itemView.findViewById(R.id.tv_tag);
            tvVoteCount = itemView.findViewById(R.id.tv_vote_count);
            tvDownvoteCount = itemView.findViewById(R.id.tv_downvote_count);
            tvCommentCount = itemView.findViewById(R.id.tv_comment_count);
            tvViewCount = itemView.findViewById(R.id.tv_view_count);
            tvTime = itemView.findViewById(R.id.tv_time);
            btnUpvote = itemView.findViewById(R.id.btn_upvote);
            btnDownvote = itemView.findViewById(R.id.btn_downvote);
        }

        void bind(CommunityPostResponse.PostItem post) {
            tvAuthorName.setText(post.authorName != null ? post.authorName : "Ẩn danh");
            if (ivAvatar != null) {
                com.example.emotiondebugging.utils.AvatarHelper.loadAvatar(
                    ivAvatar, post.authorAvatar, post.authorName);
            }
            tvTitle.setText(post.title);
            tvPreview.setText(post.content.length() > 60
                    ? post.content.substring(0, 60) + "..." : post.content);
            tvTag.setText(post.errorName != null ? "#" + post.errorName : "");
            tvTag.setOnClickListener(v -> {
                if (listener != null && post.errorName != null)
                    listener.onTagClick(post.errorTypeId, post.errorName);
            });
            tvVoteCount.setText(String.valueOf(post.upvoteCount));
            tvDownvoteCount.setText(String.valueOf(post.downvoteCount));
            tvCommentCount.setText(String.valueOf(post.commentCount));
            tvViewCount.setText(String.valueOf(post.viewCount));
            tvTime.setText(formatTime(post.createdAt));

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onPostClick(post);
            });
            btnUpvote.setOnClickListener(v -> {
                if (listener != null) listener.onUpvote(post);
            });
            btnDownvote.setOnClickListener(v -> {
                if (listener != null) listener.onDownvote(post);
            });

            // Menu 3 chấm
            ImageButton btnMore = itemView.findViewById(R.id.btn_more);
            if (btnMore != null) {
                btnMore.setOnClickListener(v -> {
                    android.widget.PopupMenu popup = new android.widget.PopupMenu(v.getContext(), v);
                    popup.getMenu().add(0, 1, 0, post.isSaved == 1 ? "Bỏ lưu bài viết" : "Lưu bài viết");
                    popup.getMenu().add(0, 2, 1, "Không quan tâm tác giả này");
                    popup.setOnMenuItemClickListener(item -> {
                        if (listener == null) return false;
                        if (item.getItemId() == 1) listener.onSave(post);
                        else if (item.getItemId() == 2) listener.onMute(post);
                        return true;
                    });
                    popup.show();
                });
            }
        }

        private String formatTime(String createdAt) {
            if (createdAt == null) return "";
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                java.util.Date date = sdf.parse(createdAt);
                long diff = System.currentTimeMillis() - date.getTime();
                long hours = diff / (1000 * 60 * 60);
                if (hours < 24) return hours + " h. ago";
                long days = hours / 24;
                return days + " d. ago";
            } catch (Exception e) {
                return createdAt;
            }
        }
    }
}
