package com.example.homnayangi.activities;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.homnayangi.R;
import com.example.homnayangi.adapters.FoodAdapter;
import com.example.homnayangi.repository.FoodRepository;
import com.example.homnayangi.utils.CloudinaryUploader;
import com.example.homnayangi.utils.MapUtils;
import com.example.homnayangi.utils.PriceFormatWatcher;
import com.example.homnayangi.utils.VietnameseUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FoodListFragment extends Fragment implements FoodAdapter.OnFoodItemClickListener {

    private RecyclerView recyclerView;
    private FoodAdapter foodAdapter;
    private Button btnAddFood;
    private Button btnLibrary;
    private LinearLayout linearCategories;
    private EditText edtSearch;
    private View layoutEmptyState;
    private Button btnClearFilter;
    private TextView tvEmptyTitle, tvEmptySubtitle;
    private FoodRepository repo;

    private List<DocumentSnapshot> allFoodDocs  = new ArrayList<>();
    private List<DocumentSnapshot> filteredDocs  = new ArrayList<>();
    private String selectedCategory = null; // null = hiện tất cả, "fav" = yêu thích
    private String searchQuery = "";

    // Debounce handler cho tìm kiếm
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // 📌 Image upload fields
    private Uri selectedFoodImageUri = null;
    private ImageView imgFoodPreview;
    private View cardFoodPreview;

    private final ActivityResultLauncher<Intent> foodImagePicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedFoodImageUri = result.getData().getData();
                    if (imgFoodPreview != null) {
                        if (cardFoodPreview != null) cardFoodPreview.setVisibility(View.VISIBLE);
                        Glide.with(this).load(selectedFoodImageUri).into(imgFoodPreview);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_food_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repo = FoodRepository.getInstance();

        recyclerView     = view.findViewById(R.id.recyclerViewFood);
        btnAddFood       = view.findViewById(R.id.btnTopAddFood);
        btnLibrary       = view.findViewById(R.id.btnExploreLibrary);
        linearCategories = view.findViewById(R.id.linearCategories);
        edtSearch        = view.findViewById(R.id.edtSearch);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        foodAdapter = new FoodAdapter();
        foodAdapter.setListener(this);
        recyclerView.setAdapter(foodAdapter);

        // Tìm kiếm realtime với debounce 300ms
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

        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        btnClearFilter   = view.findViewById(R.id.btnClearFilter);
        tvEmptyTitle     = view.findViewById(R.id.tvEmptyTitle);
        tvEmptySubtitle  = view.findViewById(R.id.tvEmptySubtitle);

        if (btnClearFilter != null) {
            btnClearFilter.setOnClickListener(v -> {
                if (edtSearch != null) edtSearch.setText("");
                searchQuery = "";
                selectedCategory = null;
                updateCategoryChips();
                applyFilter();
            });
        }

        loadFoodList();
        btnAddFood.setOnClickListener(v -> showAddEditDialog(null, null));
        btnLibrary.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), FoodLibraryActivity.class));
        });
    }

    // ========== LOAD DATA ==========

    private void loadFoodList() {
        com.google.firebase.firestore.Query query = repo.getFoodsQuery();
        if (query == null) return;
        query.addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null || !isAdded()) return;
            allFoodDocs = snapshots.getDocuments();
            updateCategoryChips();
            applyFilter();
        });
    }

    // ========== CATEGORY CHIPS ==========

    private void updateCategoryChips() {
        if (linearCategories == null) return;
        linearCategories.removeAllViews();

        // Chip "Tất cả"
        addCategoryChip("Tất cả", null);

        // Chip "⭐ Yêu thích"
        addCategoryChip("⭐ Yêu thích", "fav");

        // Thu thập các danh mục không trùng
        Set<String> categories = new LinkedHashSet<>();
        for (DocumentSnapshot doc : allFoodDocs) {
            String cat = doc.getString("category");
            if (cat != null && !cat.isEmpty()) categories.add(cat);
        }

        for (String cat : categories) {
            addCategoryChip(cat, cat);
        }
    }

    private void addCategoryChip(String label, @Nullable String categoryValue) {
        Chip chip = new Chip(requireContext());
        chip.setText(label);
        chip.setCheckable(true);
        chip.setChecked(categoryValue == null && selectedCategory == null
                || categoryValue != null && categoryValue.equals(selectedCategory));
        chip.setChipBackgroundColorResource(R.color.chip_choice_background);
        chip.setCheckedIconTint(android.content.res.ColorStateList.valueOf(0xFFFF6B35));
        chip.setEnsureMinTouchTargetSize(false);
        chip.setOnClickListener(v -> {
            selectedCategory = categoryValue;
            // Cập nhật trạng thái checked của tất cả chips
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
        List<DocumentSnapshot> result;

        // Bước 1: Lọc theo danh mục
        if (selectedCategory == null) {
            result = new ArrayList<>(allFoodDocs);
        } else if ("fav".equals(selectedCategory)) {
            result = new ArrayList<>();
            for (DocumentSnapshot doc : allFoodDocs) {
                Boolean isFav = doc.getBoolean("isFavorite");
                if (isFav != null && isFav) {
                    result.add(doc);
                }
            }
        } else {
            result = new ArrayList<>();
            for (DocumentSnapshot doc : allFoodDocs) {
                if (selectedCategory.equals(doc.getString("category"))) {
                    result.add(doc);
                }
            }
        }

        // Bước 2: Lọc theo từ khóa tìm kiếm (chỉ tìm theo tiêu đề/tên món ăn)
        if (!searchQuery.isEmpty()) {
            List<DocumentSnapshot> searched = new ArrayList<>();
            for (DocumentSnapshot doc : result) {
                String name = doc.getString("name");
                if (VietnameseUtils.containsIgnoreCaseAndAccents(name, searchQuery)) {
                    searched.add(doc);
                }
            }
            result = searched;
        }

        filteredDocs = result;
        foodAdapter.setFirestoreFoodList(filteredDocs);

        // Bước 3: Cập nhật Empty State
        if (layoutEmptyState != null && recyclerView != null) {
            if (filteredDocs.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                layoutEmptyState.setVisibility(View.VISIBLE);

                if (allFoodDocs.isEmpty()) {
                    if (tvEmptyTitle != null) tvEmptyTitle.setText("Chưa có món ăn nào");
                    if (tvEmptySubtitle != null) tvEmptySubtitle.setText("Hãy bấm \"+ Thêm món ăn\" hoặc khám phá thư viện để bắt đầu!");
                    if (btnClearFilter != null) btnClearFilter.setVisibility(View.GONE);
                } else {
                    if (tvEmptyTitle != null) tvEmptyTitle.setText("Chưa tìm thấy món ăn");
                    if (tvEmptySubtitle != null) tvEmptySubtitle.setText("Thử tìm kiếm với từ khóa khác hoặc bỏ bộ lọc để xem toàn bộ danh sách.");
                    if (btnClearFilter != null) btnClearFilter.setVisibility(View.VISIBLE);
                }
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                layoutEmptyState.setVisibility(View.GONE);
            }
        }
    }

    // ========== DIALOG THÊM / SỬA ==========

    private void showAddEditDialog(@Nullable DocumentSnapshot doc, @Nullable String docId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_food, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        android.widget.TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText edtName     = dialogView.findViewById(R.id.edtFoodName);
        AutoCompleteTextView edtCategory = dialogView.findViewById(R.id.edtCategory);
        EditText edtPrice    = dialogView.findViewById(R.id.edtPrice);
        EditText edtRecipe   = dialogView.findViewById(R.id.edtRecipe);
        Button btnSave       = dialogView.findViewById(R.id.btnSave);
        Button btnCancel     = dialogView.findViewById(R.id.btnCancel);

        // Smart Categories setup
        Set<String> categories = new LinkedHashSet<>();
        categories.add("Cơm");
        categories.add("Phở/Bún");
        categories.add("Đồ ăn nhanh");
        categories.add("Lẩu/Nướng");
        categories.add("Đồ uống");
        categories.add("Tráng miệng");
        
        // Add existing categories
        for (DocumentSnapshot d : allFoodDocs) {
            String cat = d.getString("category");
            if (cat != null && !cat.isEmpty()) categories.add(cat);
        }
        
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, new ArrayList<>(categories));
        edtCategory.setAdapter(catAdapter);

        // 📌 Image picker setup
        imgFoodPreview = dialogView.findViewById(R.id.imgFoodPreview);
        cardFoodPreview = dialogView.findViewById(R.id.cardFoodPreview);
        Button btnPickFoodImage = dialogView.findViewById(R.id.btnPickFoodImage);
        selectedFoodImageUri = null;

        // 📌 Gắn PriceFormatWatcher tự động format số tiền
        edtPrice.addTextChangedListener(new PriceFormatWatcher(edtPrice));

        if (btnPickFoodImage != null) {
            btnPickFoodImage.setOnClickListener(v -> {
                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                foodImagePicker.launch(pickIntent);
            });
        }

        boolean isEdit = doc != null;

        if (isEdit) {
            tvTitle.setText("Sửa món ăn");
            edtName.setText(doc.getString("name"));
            edtCategory.setText(doc.getString("category"));
            Double p = doc.getDouble("price");
            edtPrice.setText(p != null ? new DecimalFormat("#,###").format(p.intValue()) : "");
            // 📌 Đọc "note" trước, fallback "recipe" cho dữ liệu cũ
            String noteVal = doc.getString("note");
            if (noteVal == null) noteVal = doc.getString("recipe");
            edtRecipe.setText(noteVal);

            // If editing and has existing image, show it
            String existingImg = doc.getString("imageUrl");
            if (existingImg != null && !existingImg.isEmpty() && imgFoodPreview != null) {
                if (cardFoodPreview != null) cardFoodPreview.setVisibility(View.VISIBLE);
                Glide.with(this).load(existingImg).into(imgFoodPreview);
            }
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name     = edtName.getText().toString().trim();
            String category = edtCategory.getText().toString().trim();
            String recipe   = edtRecipe.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(getContext(), "Vui lòng nhập tên món ăn", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = PriceFormatWatcher.getCleanValue(edtPrice);
            final String fName = name;
            final String fCategory = category;
            final String fRecipe = recipe;

            btnSave.setEnabled(false);

            FoodRepository.OnCompleteListener foodCallback = new FoodRepository.OnCompleteListener() {
                @Override public void onSuccess() {}
                @Override public void onFailure(String error) {
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            };

            if (doc == null) {
                // Thêm mới
                if (selectedFoodImageUri != null) {
                    btnSave.setText("Đang lưu...");
                    CloudinaryUploader.upload(requireContext(), selectedFoodImageUri, "foods",
                            new CloudinaryUploader.UploadListener() {
                                @Override
                                public void onSuccess(String imageUrl) {
                                    repo.addFood(fName, fCategory, price, fRecipe, imageUrl, foodCallback);
                                    dialog.dismiss();
                                }
                                @Override
                                public void onError(String msg) {
                                    repo.addFood(fName, fCategory, price, fRecipe, "", foodCallback);
                                    dialog.dismiss();
                                }
                            });
                } else {
                    repo.addFood(fName, fCategory, price, fRecipe, foodCallback);
                    dialog.dismiss();
                }
                Toast.makeText(getContext(), "Đang thêm...", Toast.LENGTH_SHORT).show();
            } else {
                repo.updateFood(docId, fName, fCategory, price, fRecipe, foodCallback);
                Toast.makeText(getContext(), "Đang cập nhật...", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    // ========== LISTENER CALLBACKS ==========

    @Override
    public void onEditClick(int position) {
        if (position >= 0 && position < filteredDocs.size()) {
            DocumentSnapshot doc = filteredDocs.get(position);
            showAddEditDialog(doc, doc.getId());
        }
    }

    @Override
    public void onDeleteClick(int position) {
        if (position < 0 || position >= filteredDocs.size()) return;
        DocumentSnapshot doc = filteredDocs.get(position);
        String name = doc.getString("name");

        new AlertDialog.Builder(getContext())
                .setTitle("Xóa món ăn")
                .setMessage("Bạn có chắc muốn xóa '" + name + "'?")
                .setPositiveButton("Xóa", (d, w) ->
                        repo.deleteFood(doc.getId(), new FoodRepository.OnCompleteListener() {
                            @Override public void onSuccess() {
                                if (getContext() != null)
                                    Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show();
                            }
                            @Override public void onFailure(String error) {
                                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onAddToRouletteClick(int position) {
        if (position < 0 || position >= filteredDocs.size()) return;
        DocumentSnapshot doc = filteredDocs.get(position);
        String name  = doc.getString("name");
        Double price = doc.getDouble("price");

        if (repo.isInRoulettePool(doc.getId())) {
            // Bỏ khỏi vòng quay
            repo.removeFromRoulettePool(doc.getId());
            Toast.makeText(getContext(), "Đã bỏ \"" + name + "\" khỏi vòng quay", Toast.LENGTH_SHORT).show();
        } else {
            // Thêm vào vòng quay
            repo.addToRoulettePool(new FoodRepository.RouletteItem(
                    doc.getId(), name != null ? name : "", price != null ? price : 0));
            Toast.makeText(getContext(), "✅ Thêm \"" + name + "\" vào vòng quay!", Toast.LENGTH_SHORT).show();
        }
        // Cập nhật icon của item đó
        foodAdapter.notifyItemChanged(position);
    }

    @Override
    public void onFavoriteClick(int position) {
        if (position < 0 || position >= filteredDocs.size()) return;
        DocumentSnapshot doc = filteredDocs.get(position);
        Boolean currentFav = doc.getBoolean("isFavorite");
        boolean isFav = currentFav != null && currentFav;

        repo.toggleFavorite(doc.getId(), isFav, new FoodRepository.OnCompleteListener() {
            @Override
            public void onSuccess() {
                // Firestore snapshot listener sẽ tự cập nhật UI
            }

            @Override
            public void onFailure(String error) {
                if (getContext() != null)
                    Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFindRestaurantClick(int position) {
        if (position < 0 || position >= filteredDocs.size()) return;
        DocumentSnapshot doc = filteredDocs.get(position);
        String name = doc.getString("name");
        Double price = doc.getDouble("price");

        if (name == null || name.trim().isEmpty()) return;

        // 1. Mở bản đồ tìm quán ăn gần đây
        MapUtils.findNearbyRestaurants(requireContext(), name);

        // 2. Ghi nhận vào lịch sử ăn uống / chi tiêu
        repo.addHistory(name, price != null ? price : 0, new FoodRepository.OnCompleteListener() {
            @Override public void onSuccess() {}
            @Override public void onFailure(String error) {}
        });
    }
}
