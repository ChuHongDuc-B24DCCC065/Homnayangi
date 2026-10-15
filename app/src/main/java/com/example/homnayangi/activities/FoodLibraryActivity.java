package com.example.homnayangi.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homnayangi.R;
import com.example.homnayangi.adapters.FoodLibraryAdapter;
import com.example.homnayangi.repository.FoodRepository;
import com.example.homnayangi.utils.FoodLibraryDatabase;
import com.example.homnayangi.utils.VietnameseUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FoodLibraryActivity extends AppCompatActivity {

    private FoodLibraryAdapter adapter;
    private List<DocumentSnapshot> allLibraryDocs = new ArrayList<>();
    private String selectedCategory = null;
    private String searchQuery = "";
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private LinearLayout linearCategories;
    private LinearLayout layoutErrorState;
    private LinearLayout layoutErrorBanner;
    private TextView tvStatusMessage;
    private View layoutStatus;
    private RecyclerView recyclerView;
    private ListenerRegistration libraryListener;
    private ListenerRegistration userFoodsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_library);

        Button btnBack = findViewById(R.id.btnLibBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerLibrary);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FoodLibraryAdapter();
        recyclerView.setAdapter(adapter);

        linearCategories  = findViewById(R.id.linearCategories);
        layoutErrorState  = findViewById(R.id.layoutErrorState);
        layoutErrorBanner = findViewById(R.id.layoutErrorBanner);
        layoutStatus      = findViewById(R.id.layoutStatus);
        tvStatusMessage   = findViewById(R.id.tvStatusMessage);

        MaterialButton btnRetryConnect = findViewById(R.id.btnRetryConnect);
        if (btnRetryConnect != null) {
            btnRetryConnect.setOnClickListener(v -> loadLibraryData());
        }

        TextView btnRefreshStatus = findViewById(R.id.btnRefreshStatus);
        if (btnRefreshStatus != null) {
            btnRefreshStatus.setOnClickListener(v -> loadLibraryData());
        }

        EditText edtSearch = findViewById(R.id.edtSearchLibrary);
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                    searchRunnable = () -> {
                        searchQuery = s.toString().trim().toLowerCase();
                        applyFilter();
                    };
                    searchHandler.postDelayed(searchRunnable, 300);
                }
            });
        }

        loadUserFoods();
        loadLibraryData();
    }

    private void loadUserFoods() {
        // 📌 [GHI CHÚ]: Kiểm tra user đã đăng nhập chưa trước khi truy vấn Firestore để tránh crash
        if (FoodRepository.getInstance().getUid() == null) return;

        userFoodsListener = FoodRepository.getInstance().getFoodsQuery().addSnapshotListener((snapshots, e) -> {
            // 📌 [GHI CHÚ]: Kiểm tra lỗi Firestore, dữ liệu rỗng, hoặc Activity đang bị hủy (isFinishing) 
            // để return sớm, tránh cập nhật UI vào Activity không còn tồn tại
            if (e != null || snapshots == null || isFinishing()) return;

            Map<String, String> map = new HashMap<>();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                String name = doc.getString("name");
                if (name != null) {
                    map.put(name, doc.getId());
                }
            }
            adapter.setUserFoodMap(map);
        });
    }

    private void loadLibraryData() {
        if (libraryListener != null) {
            libraryListener.remove();
        }

        showLoading();

        libraryListener = FoodRepository.getInstance().getLibraryQuery().addSnapshotListener((value, error) -> {
            if (error != null) {
                showError("Lỗi kết nối: " + error.getMessage());
                return;
            }

            if (value != null) {
                hideError();
                if (value.isEmpty()) {
                    seedLibraryToFirestore();
                } else {
                    allLibraryDocs = value.getDocuments();
                    updateCategoryChips();
                    applyFilter();
                }
            }
        });
    }

    private void showLoading() {
        if (layoutStatus != null) {
            layoutStatus.setVisibility(View.VISIBLE);
            if (tvStatusMessage != null) tvStatusMessage.setText("Đang tải thư viện món ăn...");
        }
        if (layoutErrorState != null) layoutErrorState.setVisibility(View.GONE);
        if (layoutErrorBanner != null) layoutErrorBanner.setVisibility(View.GONE);
    }

    private void showError(String msg) {
        if (allLibraryDocs.isEmpty()) {
            if (layoutErrorState != null) layoutErrorState.setVisibility(View.VISIBLE);
            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        } else {
            if (layoutErrorBanner != null) layoutErrorBanner.setVisibility(View.VISIBLE);
        }
        if (layoutStatus != null) layoutStatus.setVisibility(View.GONE);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void hideError() {
        if (layoutErrorState != null) layoutErrorState.setVisibility(View.GONE);
        if (layoutErrorBanner != null) layoutErrorBanner.setVisibility(View.GONE);
        if (layoutStatus != null) layoutStatus.setVisibility(View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
    }

    private void updateCategoryChips() {
        if (linearCategories == null) return;
        linearCategories.removeAllViews();

        addCategoryChip("Tất cả", null);

        Set<String> categories = new LinkedHashSet<>();
        for (DocumentSnapshot doc : allLibraryDocs) {
            String cat = doc.getString("category");
            if (cat != null && !cat.isEmpty()) categories.add(cat);
        }

        for (String cat : categories) {
            addCategoryChip(cat, cat);
        }
    }

    private void addCategoryChip(String label, @Nullable String categoryValue) {
        Chip chip = new Chip(this);
        chip.setText(label);
        chip.setCheckable(true);
        chip.setChecked(categoryValue == null && selectedCategory == null
                || categoryValue != null && categoryValue.equals(selectedCategory));
        chip.setChipBackgroundColorResource(R.color.chip_choice_background);
        chip.setCheckedIconTint(android.content.res.ColorStateList.valueOf(0xFFFF6B35));
        chip.setEnsureMinTouchTargetSize(false);
        chip.setOnClickListener(v -> {
            selectedCategory = categoryValue;
            updateCategoryChips();
            applyFilter();
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int marginEndPx = (int) (8 * getResources().getDisplayMetrics().density);
        lp.setMarginEnd(marginEndPx);
        chip.setLayoutParams(lp);

        linearCategories.addView(chip);
    }

    private void applyFilter() {
        List<DocumentSnapshot> result = new ArrayList<>();

        for (DocumentSnapshot doc : allLibraryDocs) {
            String name = doc.getString("name");
            String cat = doc.getString("category");
            String recipe = doc.getString("recipe");

            boolean matchCategory = (selectedCategory == null)
                    || (cat != null && cat.equalsIgnoreCase(selectedCategory));

            boolean matchSearch = searchQuery.isEmpty()
                    || VietnameseUtils.containsIgnoreCaseAndAccents(name, searchQuery);

            if (matchCategory && matchSearch) {
                result.add(doc);
            }
        }

        adapter.setFoods(result);

        if (result.isEmpty() && !allLibraryDocs.isEmpty()) {
            if (layoutStatus != null) layoutStatus.setVisibility(View.VISIBLE);
            if (tvStatusMessage != null) tvStatusMessage.setText("Không tìm thấy món ăn nào phù hợp với từ khóa.");
        } else {
            if (layoutStatus != null && tvStatusMessage != null 
                    && "Không tìm thấy món ăn nào phù hợp với từ khóa.".contentEquals(tvStatusMessage.getText())) {
                layoutStatus.setVisibility(View.GONE);
            }
        }
    }

    private void seedLibraryToFirestore() {
        Toast.makeText(this, "Đang đồng bộ thư viện món ăn...", Toast.LENGTH_SHORT).show();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("library_foods").get().addOnSuccessListener(queryDocumentSnapshots -> {
            Set<String> existingNames = new java.util.HashSet<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String name = doc.getString("name");
                if (name != null) existingNames.add(name);
            }

            List<FoodLibraryDatabase.LibraryFood> defaultFoods = FoodLibraryDatabase.getFoods();
            int countAdded = 0;
            for (FoodLibraryDatabase.LibraryFood item : defaultFoods) {
                if (!existingNames.contains(item.name)) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", item.name);
                    data.put("category", item.category);
                    data.put("price", item.price);
                    data.put("recipe", item.recipe);
                    if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
                        data.put("imageUrl", item.imageUrl);
                    }
                    db.collection("library_foods").add(data);
                    countAdded++;
                }
            }
            if (countAdded > 0) {
                Toast.makeText(this, "Đã đồng bộ thêm " + countAdded + " món mới!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            showError("Lỗi đồng bộ: " + e.getMessage());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (libraryListener != null) {
            libraryListener.remove();
        }
        if (userFoodsListener != null) {
            userFoodsListener.remove();
        }
    }
}
