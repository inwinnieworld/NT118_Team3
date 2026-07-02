package com.example.emotiondebugging.ui.community.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.CommunityPostResponse;
import com.example.emotiondebugging.utils.AvatarHelper;

import java.util.ArrayList;
import java.util.List;

public class CommunityPostAdapter extends RecyclerView.Adapter<CommunityPostAdapter.PostViewHolder> {

    public interface OnPostClickListener {
        void onUpvote(CommunityPostResponse.PostItem post);

        void onDownvote(CommunityPostResponse.PostItem post);

        void onPostClick(CommunityPostResponse.PostItem post);

        void onAuthorClick(CommunityPostResponse.PostItem post);

        void onSave(CommunityPostResponse.PostItem post);

        void onMute(CommunityPostResponse.PostItem post);

        void onRepost(CommunityPostResponse.PostItem post);

        void onTagClick(String hashtag);
    }

    private List<CommunityPostResponse.PostItem> posts = new ArrayList<>();
    private final OnPostClickListener listener;

    public CommunityPostAdapter(OnPostClickListener listener) {
        this.listener = listener;
    }

    public void setPosts(List<CommunityPostResponse.PostItem> newPosts) {
        this.posts = newPosts != null ? newPosts : new ArrayList<>();

        android.util.Log.d("CommunityAdapter", "setPosts: " + this.posts.size() + " items");

        for (CommunityPostResponse.PostItem post : this.posts) {
            android.util.Log.d(
                    "POST_IMAGE_CHECK",
                    "postId=" + post.postId
                            + ", title=" + post.title
                            + ", imageUrl=" + post.imageUrl
            );
        }

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
        CommunityPostResponse.PostItem post = posts.get(position);
        holder.bind(post, listener);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvPreview;
        TextView tvTag;
        TextView tvVoteCount;
        TextView tvDownvoteCount;
        TextView tvCommentCount;
        TextView tvViewCount;
        TextView tvTime;
        TextView tvAuthorName;
        TextView tvRepostCount;

        ImageButton btnUpvote;
        ImageButton btnDownvote;
        ImageButton btnRepost;
        ImageButton btnMore;

        ImageView ivAvatar;
        ImageView ivPostImage;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);

            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            ivPostImage = itemView.findViewById(R.id.iv_post_image);

            tvAuthorName = itemView.findViewById(R.id.tv_author_name);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPreview = itemView.findViewById(R.id.tv_preview);
            tvTag = itemView.findViewById(R.id.tv_tag);
            tvVoteCount = itemView.findViewById(R.id.tv_vote_count);
            tvDownvoteCount = itemView.findViewById(R.id.tv_downvote_count);
            tvCommentCount = itemView.findViewById(R.id.tv_comment_count);
            tvViewCount = itemView.findViewById(R.id.tv_view_count);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvRepostCount = itemView.findViewById(R.id.tv_repost_count);

            btnUpvote = itemView.findViewById(R.id.btn_upvote);
            btnDownvote = itemView.findViewById(R.id.btn_downvote);
            btnRepost = itemView.findViewById(R.id.btn_repost);
            btnMore = itemView.findViewById(R.id.btn_more);
        }

        public void bind(
                CommunityPostResponse.PostItem post,
                OnPostClickListener listener
        ) {
            if (post == null) return;

            bindAuthor(post, listener);
            bindContent(post);
            bindPostImage(post);
            bindStats(post);
            bindActions(post, listener);
            bindHashtags(post, listener);
        }

        private void bindAuthor(
                CommunityPostResponse.PostItem post,
                OnPostClickListener listener
        ) {
            String authorName = post.authorName != null && !post.authorName.trim().isEmpty()
                    ? post.authorName
                    : "Ẩn danh";

            if (tvAuthorName != null) {
                tvAuthorName.setText(authorName);
                tvAuthorName.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAuthorClick(post);
                    }
                });
            }

            if (ivAvatar != null) {
                AvatarHelper.loadAvatar(ivAvatar, post.authorAvatar, authorName);
                ivAvatar.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAuthorClick(post);
                    }
                });
            }
        }

        private void bindContent(CommunityPostResponse.PostItem post) {
            String title = post.title != null ? post.title : "";
            String content = post.content != null ? post.content : "";

            String preview = content.length() > 60
                    ? content.substring(0, 60) + "..."
                    : content;

            if (tvTitle != null) {
                tvTitle.setText(title);
            }

            if (tvPreview != null) {
                tvPreview.setText(preview);
            }
        }

        private void bindPostImage(CommunityPostResponse.PostItem post) {
            if (ivPostImage == null) return;

            android.util.Log.d(
                    "POST_IMAGE",
                    "postId=" + post.postId
                            + ", title=" + post.title
                            + ", imageUrl=" + post.imageUrl
            );

            if (post.imageUrl == null || post.imageUrl.trim().isEmpty()) {
                ivPostImage.setVisibility(View.GONE);
                ivPostImage.setImageDrawable(null);
                return;
            }

            String imageUrl = post.imageUrl.trim();

            if (imageUrl.startsWith("/")) {
                imageUrl = "http://10.0.2.2:3000" + imageUrl;
            }

            ivPostImage.setVisibility(View.VISIBLE);

            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .centerCrop()
                    .into(ivPostImage);
        }

        private void bindStats(CommunityPostResponse.PostItem post) {
            if (tvVoteCount != null) {
                tvVoteCount.setText(String.valueOf(post.upvoteCount));
            }

            if (tvDownvoteCount != null) {
                tvDownvoteCount.setText(String.valueOf(post.downvoteCount));
            }

            if (tvCommentCount != null) {
                tvCommentCount.setText(String.valueOf(post.commentCount));
            }

            if (tvViewCount != null) {
                tvViewCount.setText(String.valueOf(post.viewCount));
            }

            if (tvTime != null) {
                tvTime.setText(formatTime(post.createdAt));
            }

            if (tvRepostCount != null) {
                tvRepostCount.setText(String.valueOf(post.repostCount));
            }
        }

        private void bindActions(
                CommunityPostResponse.PostItem post,
                OnPostClickListener listener
        ) {
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPostClick(post);
                }
            });

            if (btnUpvote != null) {
                btnUpvote.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onUpvote(post);
                    }
                });
            }

            if (btnDownvote != null) {
                btnDownvote.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDownvote(post);
                    }
                });
            }

            if (btnRepost != null) {
                btnRepost.setColorFilter(post.isReposted == 1
                        ? Color.parseColor("#12B2C1")
                        : Color.parseColor("#6B7280"));

                btnRepost.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRepost(post);
                    }
                });
            }

            if (btnMore != null) {
                btnMore.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(v.getContext(), v);

                    popup.getMenu().add(
                            0,
                            1,
                            0,
                            post.isSaved == 1 ? "Bỏ lưu bài viết" : "Lưu bài viết"
                    );

                    popup.getMenu().add(
                            0,
                            2,
                            1,
                            "Không quan tâm tác giả này"
                    );

                    popup.setOnMenuItemClickListener(item -> {
                        if (listener == null) return false;

                        if (item.getItemId() == 1) {
                            listener.onSave(post);
                            return true;
                        }

                        if (item.getItemId() == 2) {
                            listener.onMute(post);
                            return true;
                        }

                        return false;
                    });

                    popup.show();
                });
            }
        }

        private void bindHashtags(
                CommunityPostResponse.PostItem post,
                OnPostClickListener listener
        ) {
            if (tvTag == null) return;

            List<String> hashtags = getHashtagsFromPost(post);

            if (hashtags.isEmpty()) {
                tvTag.setText("");
                tvTag.setVisibility(View.GONE);
                tvTag.setOnClickListener(null);
                return;
            }

            tvTag.setVisibility(View.VISIBLE);
            tvTag.setTextColor(Color.parseColor("#12B2C1"));
            tvTag.setText(buildHashtagText(hashtags));

            tvTag.setOnClickListener(v -> {
                if (listener == null) return;

                if (hashtags.size() == 1) {
                    listener.onTagClick(hashtags.get(0));
                    return;
                }

                PopupMenu popup = new PopupMenu(v.getContext(), v);

                for (int i = 0; i < hashtags.size(); i++) {
                    popup.getMenu().add(0, i, i, "#" + hashtags.get(i));
                }

                popup.setOnMenuItemClickListener(item -> {
                    int index = item.getItemId();

                    if (index >= 0 && index < hashtags.size()) {
                        listener.onTagClick(hashtags.get(index));
                        return true;
                    }

                    return false;
                });

                popup.show();
            });
        }

        private List<String> getHashtagsFromPost(CommunityPostResponse.PostItem post) {
            List<String> result = new ArrayList<>();

            if (post.hashtags != null && !post.hashtags.isEmpty()) {
                for (String tag : post.hashtags) {
                    String cleaned = cleanHashtag(tag);

                    if (!cleaned.isEmpty() && !result.contains(cleaned)) {
                        result.add(cleaned);
                    }
                }
            }

            if (result.isEmpty()
                    && post.errorName != null
                    && !post.errorName.trim().isEmpty()) {
                result.add(cleanHashtag(post.errorName));
            }

            return result;
        }

        private String buildHashtagText(List<String> hashtags) {
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < hashtags.size(); i++) {
                if (i > 0) {
                    builder.append("  ");
                }

                builder.append("#").append(hashtags.get(i));
            }

            return builder.toString();
        }

        private String cleanHashtag(String value) {
            if (value == null) return "";

            return value
                    .trim()
                    .replace("#", "")
                    .replace(" ", "-");
        }

        private String formatTime(String createdAt) {
            if (createdAt == null || createdAt.trim().isEmpty()) {
                return "";
            }

            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                        java.util.Locale.getDefault()
                );

                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

                java.util.Date date = sdf.parse(createdAt);
                if (date == null) return createdAt;

                long diff = System.currentTimeMillis() - date.getTime();
                long minutes = diff / (1000 * 60);
                long hours = diff / (1000 * 60 * 60);

                if (minutes < 1) return "now";
                if (minutes < 60) return minutes + " m. ago";
                if (hours < 24) return hours + " h. ago";

                long days = hours / 24;
                return days + " d. ago";
            } catch (Exception e) {
                return createdAt;
            }
        }
    }
}