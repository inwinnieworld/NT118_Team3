package com.example.emotiondebugging.ui.community.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.PostDetailResponse;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    public interface OnCommentVoteListener {
        void onUpvote(PostDetailResponse.CommentItem comment);
        void onDownvote(PostDetailResponse.CommentItem comment);
    }

    public interface OnReplyClickListener {
        void onReply(PostDetailResponse.CommentItem comment);
    }

    private List<PostDetailResponse.CommentItem> comments = new ArrayList<>();
    private OnCommentVoteListener voteListener;
    private OnReplyClickListener replyListener;

    public CommentAdapter(OnCommentVoteListener voteListener, OnReplyClickListener replyListener) {
        this.voteListener = voteListener;
        this.replyListener = replyListener;
    }
    public void setComments(List<PostDetailResponse.CommentItem> newComments) {
        this.comments = newComments != null ? newComments : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addComment(PostDetailResponse.CommentItem comment) {
        if (comment.parentCommentId == null) {
            comment.replies = new ArrayList<>();
            comments.add(0, comment);
            notifyItemInserted(0);
        } else {
            // Tìm comment cha và thêm reply vào
            for (int i = 0; i < comments.size(); i++) {
                if (comments.get(i).commentId == comment.parentCommentId) {
                    if (comments.get(i).replies == null) comments.get(i).replies = new ArrayList<>();
                    comments.get(i).replies.add(comment);
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.bind(comments.get(position));
    }

    @Override
    public int getItemCount() { return comments.size(); }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthorName, tvContent, tvTime, tvUpvote, tvDownvote;
        ImageButton btnUpvote, btnDownvote, btnReply;
        LinearLayout layoutReplies;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthorName = itemView.findViewById(R.id.tv_author_name);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvUpvote = itemView.findViewById(R.id.tv_upvote_count);
            tvDownvote = itemView.findViewById(R.id.tv_downvote_count);
            btnUpvote = itemView.findViewById(R.id.btn_upvote);
            btnDownvote = itemView.findViewById(R.id.btn_downvote);
            btnReply = itemView.findViewById(R.id.btn_reply);
            layoutReplies = itemView.findViewById(R.id.layout_replies);
        }

        void bind(PostDetailResponse.CommentItem comment) {
            tvAuthorName.setText(comment.authorName != null ? comment.authorName : "Ẩn danh");
            com.example.emotiondebugging.utils.AvatarHelper.loadAvatar(
                itemView.findViewById(R.id.iv_avatar), comment.authorAvatar, comment.authorName);
            tvContent.setText(comment.content);
            tvTime.setText(formatTime(comment.createdAt));
            tvUpvote.setText(String.valueOf(comment.upvoteCount));
            tvDownvote.setText(String.valueOf(comment.downvoteCount));

            btnUpvote.setOnClickListener(v -> { if (voteListener != null) voteListener.onUpvote(comment); });
            btnDownvote.setOnClickListener(v -> { if (voteListener != null) voteListener.onDownvote(comment); });
            btnReply.setOnClickListener(v -> { if (replyListener != null) replyListener.onReply(comment); });

            // Render replies
            layoutReplies.removeAllViews();
            if (comment.replies != null) {
                for (PostDetailResponse.CommentItem reply : comment.replies) {
                    View replyView = LayoutInflater.from(itemView.getContext())
                            .inflate(R.layout.item_reply, layoutReplies, false);
                    ((TextView) replyView.findViewById(R.id.tv_author_name))
                            .setText(reply.authorName != null ? reply.authorName : "Ẩn danh");
                    ((TextView) replyView.findViewById(R.id.tv_content)).setText(reply.content);
                    ((TextView) replyView.findViewById(R.id.tv_time)).setText(formatTime(reply.createdAt));
                    ((TextView) replyView.findViewById(R.id.tv_upvote_count)).setText(String.valueOf(reply.upvoteCount));
                    ((TextView) replyView.findViewById(R.id.tv_downvote_count)).setText(String.valueOf(reply.downvoteCount));
                    layoutReplies.addView(replyView);
                }
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
                return (hours / 24) + " d. ago";
            } catch (Exception e) {
                return createdAt;
            }
        }
    }
}
