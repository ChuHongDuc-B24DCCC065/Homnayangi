package com.example.homnayangi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import com.example.homnayangi.R;
import com.example.homnayangi.repository.FoodRepository;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodLibraryAdapter extends RecyclerView.Adapter<FoodLibraryAdapter.LibViewHolder> {

    private List<DocumentSnapshot> list = new ArrayList<>();
    // Lưu map: Tên món ăn -> Food ID trong Firestore của người dùng
    private Map<String, String> userFoodMap = new HashMap<>();
    private final FoodRepository repo = FoodRepository.getInstance();

    public void setFoods(List<DocumentSnapshot> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void setUserFoodMap(Map<String, String> map) {
        this.userFoodMap = map != null ? map : new HashMap<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LibViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_library_food, parent, false);
        return new LibViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LibViewHolder holder, int position) {
        DocumentSnapshot doc = list.get(position);

        String name = doc.getString("name");
        String category = doc.getString("category");
        Double p = doc.getDouble("price");
        double price = p != null ? p : 0;
        String recipe = doc.getString("recipe");

        holder.tvName.setText("🍽️ " + (name != null ? name : ""));
        holder.tvCategory.setText(category != null ? category : "");
        DecimalFormat fmt = new DecimalFormat("#,###");
        holder.tvPrice.setText(fmt.format(price) + " VNĐ");
        holder.tvRecipe.setText(recipe != null ? recipe : "");

        // Load ảnh món ăn từ URL (trường imageUrl trong Firestore)
        String imageUrl = doc.getString("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            holder.imgFood.setVisibility(View.VISIBLE);
            Glide.with(holder.imgFood.getContext())
                    .load(imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(holder.imgFood);
        } else {
            holder.imgFood.setVisibility(View.GONE);
        }
        
        // Expand/collapse recipe on click
        holder.tvRecipe.setOnClickListener(v -> {
            if (holder.tvRecipe.getMaxLines() == 3) {
                holder.tvRecipe.setMaxLines(50);
            } else {
                holder.tvRecipe.setMaxLines(3);
            }
        });

        // Kiểm tra xem món này đã có trong danh sách cá nhân chưa
        boolean isAdded = name != null && userFoodMap.containsKey(name);

        if (isAdded) {
            holder.btnAdd.setText("Đã thêm ✓ (Bấm để bỏ)");
            holder.btnAdd.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50));
            holder.btnAdd.setTextColor(0xFFFFFFFF);
        } else {
            holder.btnAdd.setText("+ Thêm vào danh sách của tôi");
            holder.btnAdd.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFFFFF));
            holder.btnAdd.setTextColor(0xFF29251F);
        }
        holder.btnAdd.setEnabled(true);

        holder.btnAdd.setOnClickListener(v -> {
            holder.btnAdd.setEnabled(false);
            if (name == null) return;

            if (userFoodMap.containsKey(name)) {
                // ĐANG CÓ -> BỎ THÊM (XÓA KHỎI DANH SÁCH MÓN CỦA TÔI)
                String existingFoodId = userFoodMap.get(name);
                if (existingFoodId != null) {
                    repo.deleteFood(existingFoodId, new FoodRepository.OnCompleteListener() {
                        @Override
                        public void onSuccess() {
                            userFoodMap.remove(name);
                            int pos = holder.getBindingAdapterPosition();
                            if (pos != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                                notifyItemChanged(pos);
                            }
                            com.google.android.material.snackbar.Snackbar.make(v, "Đã bỏ " + name + " khỏi danh sách!", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String error) {
                            holder.btnAdd.setEnabled(true);
                            Toast.makeText(v.getContext(), "Lỗi khi bỏ thêm: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                // CHƯA CÓ -> THÊM VÀO DANH SÁCH
                repo.addFood(name, category, price, recipe, imageUrl, new FoodRepository.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        // Firebase snapshot listener sẽ tự cập nhật userFoodMap,
                        // nhưng tạm thời đánh dấu để UI đổi ngay lập tức
                        userFoodMap.put(name, "pending");
                        int pos = holder.getBindingAdapterPosition();
                        if (pos != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                            notifyItemChanged(pos);
                        }
                        com.google.android.material.snackbar.Snackbar.make(v, "Đã thêm " + name + " vào danh sách!", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        holder.btnAdd.setEnabled(true);
                        Toast.makeText(v.getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class LibViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvPrice, tvRecipe;
        Button btnAdd;
        ImageView imgFood;

        public LibViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName    = itemView.findViewById(R.id.tvLibFoodName);
            tvCategory = itemView.findViewById(R.id.tvLibCategory);
            tvPrice   = itemView.findViewById(R.id.tvLibPrice);
            tvRecipe  = itemView.findViewById(R.id.tvLibRecipe);
            btnAdd    = itemView.findViewById(R.id.btnAddFromLib);
            imgFood   = itemView.findViewById(R.id.imgLibFood);
        }
    }
}
