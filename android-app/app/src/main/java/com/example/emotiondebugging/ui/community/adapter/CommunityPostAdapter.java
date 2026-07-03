package com.example.emotiondebugging.ui.community.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.CommunityPostResponse;
import com.example.emotiondebugging.utils.AvatarHelper;

import java.util.ArrayList;
import java.util.List;

public class CommunityPostAdapter extends RecyclerView.Adapter<CommunityPostAdapter.PostViewHolder> {

    public interface OnPostClickListener {
        void onUpvote(CommunityPostResponse.PostItem post);
        void onAuthorClick(CommunityPostResponse.PostItem post);
        void onDownvote(CommunityPostResponse.PostItem post);
        void onPostClick(CommunityPostResponse.PostItem post);
        void onSave(CommunityPostResponse.PostItem post);
        void onMute(CommunityPostResponse.PostItem post);
        void onTagClick(int errorTypeId, String errorName);
        void onRepost(CommunityPostResponse.PostItem post);
        void onReport(CommunityPostResponse.PostItem post);
        void onEdit(CommunityPostResponse.PostItem post);
        void onDelete(CommunityPostResponse.PostItem post);
    }

    private List<CommunityPostResponse.PostItem> posts = new ArrayList<>();
    private final OnPostClickListener listener;
    private int currentStudentId = -1;

    public CommunityPostAdapter(OnPostClickListener listener) {
        this.listener = listener;
    }

    public void setCurrentStudentId(int studentId) {
        this.currentStudentId = studentId;
        notifyDataSetChanged();
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
        CommunityPostResponse.PostItem post = posts.get(position);
        holder.bind(post);
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

        View layoutOwnerActions;
        TextView tvEditPost;
        TextView tvDeletePost;

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
            tvRepostCount = itemView.findViewById(R.id.tv_repost_count);

            btnUpvote = itemView.findViewById(R.id.btn_upvote);
            btnDownvote = itemView.findViewById(R.id.btn_downvote);
            btnRepost = itemView.findViewById(R.id.btn_repost);
            btnMore = itemView.findViewById(R.id.btn_more);

            layoutOwnerActions = itemView.findViewById(R.id.layout_owner_actions);
            tvEditPost = itemView.findViewById(R.id.tv_edit_post);
            tvDeletePost = itemView.findViewById(R.id.tv_delete_post);
        }

        void bind(CommunityPostResponse.PostItem post) {
            if (post == null) return;

            String authorName = post.authorName != null && !post.authorName.trim().isEmpty()
                    ? post.authorName
                    : "Ẩn danh";

            String title = post.title != null ? post.title : "";
            String content = post.content != null ? post.content : "";
            String preview = content.length() > 60
                    ? content.substring(0, 60) + "..."
                    : content;

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

            if (tvTitle != null) {
                tvTitle.setText(title);
            }

            if (tvPreview != null) {
                tvPreview.setText(preview);
            }

            if (tvTag != null) {
                String tagName = post.topicName != null && !post.topicName.trim().isEmpty()
                        ? post.topicName
                        : post.errorName;
                int tagId = post.topicId > 0 ? post.topicId : post.errorTypeId;

                if (tagName != null && !tagName.trim().isEmpty()) {
                    final String finalTagName = tagName;
                    tvTag.setText("#" + finalTagName);
                    tvTag.setVisibility(View.VISIBLE);
                    tvTag.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onTagClick(tagId, finalTagName);
                        }
                    });
                } else {
                    tvTag.setText("");
                    tvTag.setVisibility(View.GONE);
                }
            }

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
                        ? android.graphics.Color.parseColor("#12B2C1")
                        : android.graphics.Color.parseColor("#6B7280"));

                btnRepost.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRepost(post);
                    }
                });
            }

            if (btnMore != null) {
                btnMore.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(v.getContext(), v);
                    popup.getMenu().add(0, 1, 0, post.isSaved == 1 ? "Bỏ lưu bài viết" : "Lưu bài viết");
                    popup.getMenu().add(0, 2, 1, "Không quan tâm tác giả này");
                    popup.getMenu().add(0, 3, 2, "Báo cáo bài viết");

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

                        if (item.getItemId() == 3) {
                            listener.onReport(post);
                            return true;
                        }

                        return false;
                    });

                    popup.show();
                });
            }

            if (layoutOwnerActions != null) {
                boolean isOwner = currentStudentId > 0
                        && post.studentId == currentStudentId
                        && post.isAnonymous == 0;
                layoutOwnerActions.setVisibility(isOwner ? View.VISIBLE : View.GONE);

                if (isOwner) {
                    if (tvEditPost != null) {
                        tvEditPost.setOnClickListener(v -> {
                            if (listener != null) listener.onEdit(post);
                        });
                    }
                    if (tvDeletePost != null) {
                        tvDeletePost.setOnClickListener(v -> {
                            if (listener != null) listener.onDelete(post);
                        });
                    }
                }
            }
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