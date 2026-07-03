package com.example.emotiondebugging.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;

import java.util.ArrayList;
import java.util.List;

public class ManageCommunityAdapter extends RecyclerView.Adapter<ManageCommunityAdapter.ItemViewHolder> {

    // Loại mục hiển thị trong màn quản lý cộng đồng.
    public static final int KIND_POST = 0;
    public static final int KIND_COMMENT = 1;
    public static final int KIND_REVIEW = 2;

    public static class Row {
        public int kind;
        public int targetId;       // post_id / comment_id / request_id
        public String kindLabel;
        public String title;
        public String content;
        public String author;
        public String reasons;
        public int reportCount;
        public boolean isHidden;
    }

    public interface OnActionListener {
        void onAccept(Row row);
        void onReject(Row row);
    }

    private List<Row> rows = new ArrayList<>();
    private final OnActionListener listener;

    public ManageCommunityAdapter(OnActionListener listener) {
        this.listener = listener;
    }

    public void setRows(List<Row> newRows) {
        this.rows = newRows != null ? newRows : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_community, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bind(rows.get(position));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView tvKind;
        TextView tvReportCount;
        TextView tvHiddenFlag;
        TextView tvTitle;
        TextView tvContent;
        TextView tvAuthor;
        TextView tvReasons;
        Button btnAccept;
        Button btnReject;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKind = itemView.findViewById(R.id.tv_kind);
            tvReportCount = itemView.findViewById(R.id.tv_report_count);
            tvHiddenFlag = itemView.findViewById(R.id.tv_hidden_flag);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvReasons = itemView.findViewById(R.id.tv_reasons);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnReject = itemView.findViewById(R.id.btn_reject);
        }

        void bind(Row row) {
            if (row == null) return;

            tvKind.setText(row.kindLabel != null ? row.kindLabel : "");

            if (row.kind == KIND_REVIEW) {
                tvReportCount.setText("Yêu cầu xem xét lại");
            } else if (row.reportCount > 0) {
                tvReportCount.setText(row.reportCount + " lượt báo cáo");
            } else {
                tvReportCount.setText("");
            }

            tvHiddenFlag.setVisibility(row.isHidden ? View.VISIBLE : View.GONE);

            if (row.title != null && !row.title.trim().isEmpty()) {
                tvTitle.setVisibility(View.VISIBLE);
                tvTitle.setText(row.title);
            } else {
                tvTitle.setVisibility(View.GONE);
            }

            tvContent.setText(row.content != null ? row.content : "");
            tvAuthor.setText(row.author != null ? "Tác giả: " + row.author : "");

            if (row.reasons != null && !row.reasons.trim().isEmpty()) {
                tvReasons.setVisibility(View.VISIBLE);
                String prefix = row.kind == KIND_REVIEW ? "Lời nhắn: " : "Lý do: ";
                tvReasons.setText(prefix + row.reasons);
            } else {
                tvReasons.setVisibility(View.GONE);
            }

            // Nhãn nút theo ngữ cảnh.
            if (row.kind == KIND_REVIEW) {
                btnAccept.setText("Khôi phục");
                btnReject.setText("Giữ ẩn");
            } else {
                btnAccept.setText("Ẩn");
                btnReject.setText("Bỏ qua");
            }

            btnAccept.setOnClickListener(v -> {
                if (listener != null) listener.onAccept(row);
            });
            btnReject.setOnClickListener(v -> {
                if (listener != null) listener.onReject(row);
            });
        }
    }
}
