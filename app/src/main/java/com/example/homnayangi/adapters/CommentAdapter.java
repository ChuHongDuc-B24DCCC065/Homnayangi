package com.example.homnayangi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homnayangi.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<DocumentSnapshot> comments = new ArrayList<>();

    public void setComments(List<DocumentSnapshot> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        DocumentSnapshot doc = comments.get(position);

        String author = doc.getString("authorName") != null ? doc.getString("authorName") : "Ẩn danh";
        String text = doc.getString("text") != null ? doc.getString("text") : "";
        Long timestamp = doc.getLong("timestamp");

        holder.tvCommentAvatar.setText(author.length() > 0 ? author.substring(0, 1).toUpperCase() : "A");
        holder.tvCommentAuthor.setText(author);
        holder.tvCommentText.setText(text);

        if (timestamp != null) {
            String time = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(new Date(timestamp));
            holder.tvCommentTime.setText(time);
        } else {
            holder.tvCommentTime.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommentAvatar, tvCommentAuthor, tvCommentTime, tvCommentText;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentAvatar = itemView.findViewById(R.id.tvCommentAvatar);
            tvCommentAuthor = itemView.findViewById(R.id.tvCommentAuthor);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
        }
    }
}
