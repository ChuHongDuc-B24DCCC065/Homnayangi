package com.example.homnayangi.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homnayangi.R;
import com.example.homnayangi.repository.FoodRepository;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private List<DocumentSnapshot> foodDocs = new ArrayList<>();
    private OnFoodItemClickListener listener;

    public interface OnFoodItemClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
        void onAddToRouletteClick(int position);
        void onFavoriteClick(int position);
        void onFindRestaurantClick(int position);
    }

    public void setFirestoreFoodList(List<DocumentSnapshot> docs) {
        this.foodDocs = docs != null ? docs : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setListener(OnFoodItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        DocumentSnapshot doc = foodDocs.get(position);

        String name     = doc.getString("name");
        String category = doc.getString("category");
        Double price    = doc.getDouble("price");
        Boolean isFav   = doc.getBoolean("isFavorite");

        holder.tvFoodName.setText(name != null ? name : "");
        holder.tvCategory.setText(category != null ? "📂 " + category : "");

        DecimalFormat fmt = new DecimalFormat("#,###");
        holder.tvPrice.setText(price != null ? "💰 " + fmt.format(price) + " VNĐ" : "");

        // 📌 [GHI CHÚ]: Đọc "note" (mới) với fallback "recipe" (cũ) — backward compat
        String recipe = doc.getString("note");
        if (recipe == null) recipe = doc.getString("recipe");
        if (recipe != null && !recipe.trim().isEmpty()) {
            holder.tvFoodRecipe.setVisibility(View.VISIBLE);
            holder.tvFoodRecipe.setText(recipe);
            holder.tvFoodRecipe.setMaxLines(2);
            holder.tvFoodRecipe.setOnClickListener(v -> {
                if (holder.tvFoodRecipe.getMaxLines() == 2) {
                    holder.tvFoodRecipe.setMaxLines(Integer.MAX_VALUE);
                } else {
                    holder.tvFoodRecipe.setMaxLines(2);
                }
            });
        } else {
            holder.tvFoodRecipe.setVisibility(View.GONE);
        }

        // Trạng thái nút yêu thích
        boolean favorite = isFav != null && isFav;
        holder.imgFavorite.setImageResource(
                favorite ? android.R.drawable.btn_star_big_on
                         : android.R.drawable.btn_star_big_off);
        holder.imgFavorite.setImageTintList(ColorStateList.valueOf(
                favorite ? 0xFFFFD700 : 0xFF9E9E9E));

        // 📌 [GHI CHÚ]: Kiểm tra món ăn này đã nằm trong vòng quay may mắn hay chưa để đổi icon/text tương ứng
        boolean inRoulette = FoodRepository.getInstance().isInRoulettePool(doc.getId());
        if (holder.imgAddToRoulette != null && holder.tvAddToRoulette != null) {
            if (inRoulette) {
                holder.imgAddToRoulette.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                holder.imgAddToRoulette.setImageTintList(ColorStateList.valueOf(0xFFF44336)); // Đỏ
                holder.tvAddToRoulette.setText("Bỏ quay");
                holder.tvAddToRoulette.setTextColor(0xFFF44336);
            } else {
                holder.imgAddToRoulette.setImageResource(android.R.drawable.ic_input_add);
                holder.imgAddToRoulette.setImageTintList(ColorStateList.valueOf(0xFF9E9E9E)); // Xám
                holder.tvAddToRoulette.setText("Quay");
                holder.tvAddToRoulette.setTextColor(0xFF9E9E9E);
            }
        }

        // 📌 [GHI CHÚ]: Dùng getBindingAdapterPosition() thay thế cho getAdapterPosition() (đã bị deprecated)
        // và kiểm tra NO_POSITION để tránh crash khi item vừa bị xóa hoặc đang animate
        holder.btnEditAction.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_POSITION) listener.onEditClick(pos);
        });
        holder.btnDeleteAction.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_POSITION) listener.onDeleteClick(pos);
        });
        holder.btnFavoriteAction.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_POSITION) listener.onFavoriteClick(pos);
        });
        holder.btnAddToRouletteAction.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_POSITION) listener.onAddToRouletteClick(pos);
        });
        if (holder.btnFindRestaurantAction != null) {
            holder.btnFindRestaurantAction.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) listener.onFindRestaurantClick(pos);
            });
        }
    }

    @Override
    public int getItemCount() {
        return foodDocs.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFavorite, imgAddToRoulette;
        View btnEditAction, btnDeleteAction, btnFavoriteAction, btnAddToRouletteAction, btnFindRestaurantAction;
        TextView tvFoodName, tvCategory, tvPrice, tvFoodRecipe, tvAddToRoulette;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFavorite       = itemView.findViewById(R.id.imgFavorite);
            btnEditAction     = itemView.findViewById(R.id.btnEditAction);
            btnDeleteAction   = itemView.findViewById(R.id.btnDeleteAction);
            btnFavoriteAction = itemView.findViewById(R.id.btnFavoriteAction);
            btnAddToRouletteAction = itemView.findViewById(R.id.btnAddToRouletteAction);
            btnFindRestaurantAction = itemView.findViewById(R.id.btnFindRestaurantAction);
            imgAddToRoulette  = itemView.findViewById(R.id.imgAddToRoulette);
            tvAddToRoulette   = itemView.findViewById(R.id.tvAddToRoulette);
            tvFoodName        = itemView.findViewById(R.id.tvFoodName);
            tvCategory        = itemView.findViewById(R.id.tvCategory);
            tvPrice           = itemView.findViewById(R.id.tvPrice);
            tvFoodRecipe      = itemView.findViewById(R.id.tvFoodRecipe);
        }
    }
}
