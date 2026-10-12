package com.example.homnayangi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.homnayangi.R;
import com.example.homnayangi.repository.FoodRepository;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.PostViewHolder> {

    private List<DocumentSnapshot> posts = new ArrayList<>();
    private final FoodRepository repo = FoodRepository.getInstance();
    private OnCommentClickListener commentListener;
    // Cache trạng thái like để tránh gọi Firestore mỗi lần scroll
    private final Map<String, Boolean> likeCache = new HashMap<>();

    public interface OnCommentClickListener {
        void onCommentClick(String postId);
    }

    public void setPosts(List<DocumentSnapshot> posts) {
        this.posts = posts;
        likeCache.clear(); // Reset cache khi danh sách posts thay đổi
        notifyDataSetChanged();
    }

    public void setCommentListener(OnCommentClickListener listener) {
        this.commentListener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community_post, parent, false);
        return new PostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        DocumentSnapshot doc = posts.get(position);

        String author = doc.getString("authorName") != null ? doc.getString("authorName") : "Ẩn danh";
        String foodName = doc.getString("foodName") != null ? doc.getString("foodName") : "";
        String restaurant = doc.getString("restaurantName") != null ? doc.getString("restaurantName") : "";
        String note = doc.getString("note") != null ? doc.getString("note") : "";
        double price = doc.getDouble("price") != null ? doc.getDouble("price") : 0;
        long likes = doc.getLong("likes") != null ? doc.getLong("likes") : 0;
        long comments = doc.getLong("commentCount") != null ? doc.getLong("commentCount") : 0;
        Long timestamp = doc.getLong("timestamp");

        String postId = doc.getId();
        String uid = repo.getUid();
        // 📌 [GHI CHÚ]: Gắn tag = postId vào nút Like để khi Firestore trả về kết quả async, 
        // ta kiểm tra xem ViewHolder có còn hiển thị bài viết này không (tránh lỗi hiển thị sai do RecyclerView tái sử dụng view)
        holder.btnLike.setTag(postId);

        // 📌 [GHI CHÚ]: Sử dụng bộ nhớ đệm likeCache (HashMap) để lưu trạng thái đã like/chưa like.
        // Khi người dùng cuộn danh sách, Adapter lấy từ cache ra ngay mà không phải gửi liên tục request đọc Firestore,
        // giúp giảm chi phí quota Firebase và loại bỏ hoàn toàn hiện tượng nhấp nháy UI.
        if (uid != null && likeCache.containsKey(postId)) {
            // Dùng giá trị đã cache — không cần gọi Firestore
            boolean isLiked = likeCache.get(postId);
            setupLikeButton(holder, postId, isLiked);
        } else if (uid != null) {
            // Chưa có trong cache → truy vấn Firestore 1 lần rồi lưu vào cache
            holder.btnLike.setText("🤍");
            holder.btnLike.setOnClickListener(null);
            holder.btnLike.setEnabled(false);

            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("community").document(postId)
                    .collection("likes").document(uid)
                    .get()
                    .addOnSuccessListener(snap -> {
                        if (!postId.equals(holder.btnLike.getTag())) return;
                        boolean isLiked = snap.exists();
                        likeCache.put(postId, isLiked);
                        setupLikeButton(holder, postId, isLiked);
                    });
        } else {
            holder.btnLike.setText("🤍");
            holder.btnLike.setEnabled(false);
            holder.btnLike.setOnClickListener(null);
        }

        // Avatar chữ cái đầu
        holder.tvAvatar.setText(author.length() > 0 ? author.substring(0, 1).toUpperCase() : "A");
        holder.tvAuthorName.setText(author);

        // Thời gian
        if (timestamp != null) {
            String time = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(new Date(timestamp));
            holder.tvPostTime.setText(time);
        }

        holder.tvPostFoodName.setText("🍽️ " + foodName);
        holder.tvPostRestaurant.setText(restaurant.isEmpty() ? "" : "📍 " + restaurant);
        holder.tvPostNote.setText(note);
        holder.tvPostNote.setVisibility(note.isEmpty() ? View.GONE : View.VISIBLE);

        // Load ảnh bài đăng (nếu có)
        String imageUrl = doc.getString("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            holder.imgPostFood.setVisibility(View.VISIBLE);
            Glide.with(holder.imgPostFood.getContext())
                    .load(imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(holder.imgPostFood);
        } else {
            holder.imgPostFood.setVisibility(View.GONE);
        }

        DecimalFormat fmt = new DecimalFormat("#,###");
        holder.tvPostPrice.setText(price > 0 ? fmt.format(price) + " VNĐ" : "");
        holder.tvLikeCount.setText(String.valueOf(likes));
        holder.tvCommentCount.setText(String.valueOf(comments));

        // Comment
        holder.btnComment.setOnClickListener(v -> {
            if (commentListener != null) {
                commentListener.onCommentClick(doc.getId());
            }
        });
    }

    /** Cài đặt nút Like với trạng thái đã biết, tránh gọi Firestore lặp lại */
    private void setupLikeButton(PostViewHolder holder, String postId, boolean isLiked) {
        holder.btnLike.setText(isLiked ? "❤️" : "🤍");
        holder.btnLike.setEnabled(true);
        holder.btnLike.setOnClickListener(v -> {
            holder.btnLike.setEnabled(false);
            // Optimistic UI update
            boolean newState = !isLiked;
            holder.btnLike.setText(newState ? "❤️" : "🤍");
            likeCache.put(postId, newState);

            repo.toggleLikePost(postId, isLiked, new FoodRepository.OnCompleteListener() {
                @Override public void onSuccess() {
                    // Snapshot listener sẽ cập nhật adapter
                }
                @Override public void onFailure(String error) {
                    // Rollback nếu lỗi
                    likeCache.put(postId, isLiked);
                    if (postId.equals(holder.btnLike.getTag())) {
                        holder.btnLike.setEnabled(true);
                        holder.btnLike.setText(isLiked ? "❤️" : "🤍");
                    }
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvAuthorName, tvPostTime;
        TextView tvPostFoodName, tvPostRestaurant, tvPostNote, tvPostPrice;
        TextView tvLikeCount, btnLike, tvCommentCount, btnComment;
        ImageView imgPostFood;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar       = itemView.findViewById(R.id.tvAvatar);
            tvAuthorName   = itemView.findViewById(R.id.tvAuthorName);
            tvPostTime     = itemView.findViewById(R.id.tvPostTime);
            tvPostFoodName = itemView.findViewById(R.id.tvPostFoodName);
            tvPostRestaurant = itemView.findViewById(R.id.tvPostRestaurant);
            tvPostNote     = itemView.findViewById(R.id.tvPostNote);
            tvPostPrice    = itemView.findViewById(R.id.tvPostPrice);
            tvLikeCount    = itemView.findViewById(R.id.tvLikeCount);
            btnLike        = itemView.findViewById(R.id.btnLike);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            btnComment     = itemView.findViewById(R.id.btnComment);
            imgPostFood    = itemView.findViewById(R.id.imgPostFood);
        }
    }
}
