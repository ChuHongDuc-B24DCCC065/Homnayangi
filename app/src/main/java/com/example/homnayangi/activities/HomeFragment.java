package com.example.homnayangi.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.homnayangi.R;
import com.example.homnayangi.repository.FoodRepository;
import com.example.homnayangi.utils.ConfettiView;
import com.example.homnayangi.utils.RouletteWheelView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RouletteWheelView wheelView;
    private ConfettiView confettiView;
    private TextView tvSelectedFood, tvPoolEmpty, btnClearPool, tvRecentResults;
    private MaterialButton btnSpin;
    private LinearLayout linearPool, layoutRecentResults;
    private View scrollPool;
    private SwitchMaterial switchEliminate;

    private FoodRepository repo;
    private boolean isSpinning = false;

    // Lưu lịch sử quay gần đây (in-memory)
    private final List<String> recentResults = new ArrayList<>();
    private static final int MAX_RECENT = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repo = FoodRepository.getInstance();

        wheelView          = view.findViewById(R.id.wheelView);
        confettiView       = view.findViewById(R.id.confettiView);
        tvSelectedFood     = view.findViewById(R.id.tvSelectedFood);
        btnSpin            = view.findViewById(R.id.btnSpin);
        tvPoolEmpty        = view.findViewById(R.id.tvPoolEmpty);
        btnClearPool       = view.findViewById(R.id.btnClearPool);
        linearPool         = view.findViewById(R.id.linearPool);
        scrollPool         = view.findViewById(R.id.scrollPool);
        switchEliminate    = view.findViewById(R.id.switchEliminate);
        layoutRecentResults = view.findViewById(R.id.layoutRecentResults);
        tvRecentResults    = view.findViewById(R.id.tvRecentResults);

        btnSpin.setOnClickListener(v -> {
            if (!isSpinning) spinWheel();
        });

        btnClearPool.setOnClickListener(v -> {
            repo.clearRoulettePool();
            refreshPool();
        });

        refreshPool();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshPool();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            refreshPool();
        }
    }

    private void refreshPool() {
        if (!isAdded()) return;
        List<FoodRepository.RouletteItem> pool = repo.getRoulettePool();
        wheelView.setItems(pool); // Cập nhật danh sách trên vòng quay

        if (pool.isEmpty()) {
            tvPoolEmpty.setVisibility(View.VISIBLE);
            scrollPool.setVisibility(View.GONE);
            btnClearPool.setVisibility(View.GONE);
        } else {
            tvPoolEmpty.setVisibility(View.GONE);
            scrollPool.setVisibility(View.VISIBLE);
            btnClearPool.setVisibility(View.VISIBLE);

            linearPool.removeAllViews();
            for (FoodRepository.RouletteItem item : pool) {
                Chip chip = new Chip(requireContext());
                chip.setText(item.name);
                chip.setCloseIconVisible(true);
                chip.setChipBackgroundColorResource(R.color.chip_choice_background);
                chip.setOnCloseIconClickListener(v -> {
                    repo.removeFromRoulettePool(item.id);
                    refreshPool();
                });
                linearPool.addView(chip);
            }
        }
    }

    private void spinWheel() {
        List<FoodRepository.RouletteItem> pool = repo.getRoulettePool();

        if (pool.isEmpty()) {
            Toast.makeText(getContext(),
                    "Chưa có món nào trong vòng quay! Hãy chọn món từ danh sách.",
                    Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_food_list);
            }
            return;
        }

        btnSpin.setEnabled(false);
        isSpinning = true;
        tvSelectedFood.setText("Đang quay... 🎲");

        wheelView.spin(selected -> {
            isSpinning = false;
            btnSpin.setEnabled(true);
            tvSelectedFood.setText("🎉 " + selected.name);

            // Bắn confetti 🎊
            confettiView.fire();

            // Thêm vào lịch sử gần đây
            addRecentResult(selected.name);

            // Hiện popup kết quả
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("🎉 Hôm nay ăn gì?")
                    .setMessage("Kết quả vòng quay:\n\n🍽️ " + selected.name + "\n\nChúc bạn ngon miệng!")
                    .setPositiveButton("Tuyệt vời! 🤩", null)
                    .setNeutralButton("📍 Tìm quán gần đây", (dialog, which) -> {
                        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode("quán " + selected.name + " gần đây"));
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps"); // Khuyến khích mở bằng app Google Maps
                        // Đảm bảo máy có app xử lý intent này
                        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                            startActivity(mapIntent);
                        } else {
                            // Xóa package để mở bằng trình duyệt nếu không có app Maps
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            startActivity(browserIntent);
                        }
                    })
                    .show();

            // Lưu lịch sử vào Firestore
            repo.addHistory(selected.name, selected.price,
                    new FoodRepository.OnCompleteListener() {
                        @Override public void onSuccess() {}
                        @Override public void onFailure(String error) {}
                    });

            // Nếu bật chế độ loại trừ → xóa món vừa quay khỏi pool
            if (switchEliminate.isChecked()) {
                repo.removeFromRoulettePool(selected.id);
                refreshPool();

                // Thông báo nếu hết món
                if (repo.getRoulettePool().isEmpty()) {
                    Toast.makeText(getContext(),
                            "🔄 Đã quay hết tất cả món! Pool đã được reset.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /** Thêm kết quả vào danh sách gần đây (tối đa 3) */
    private void addRecentResult(String name) {
        recentResults.add(0, name);
        if (recentResults.size() > MAX_RECENT) {
            recentResults.remove(recentResults.size() - 1);
        }
        updateRecentUI();
    }

    private void updateRecentUI() {
        if (recentResults.isEmpty()) {
            layoutRecentResults.setVisibility(View.GONE);
            return;
        }

        layoutRecentResults.setVisibility(View.VISIBLE);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recentResults.size(); i++) {
            String emoji = i == 0 ? "1️⃣" : (i == 1 ? "2️⃣" : "3️⃣");
            sb.append(emoji).append(" ").append(recentResults.get(i));
            if (i < recentResults.size() - 1) sb.append("\n");
        }
        tvRecentResults.setText(sb.toString());
    }
}
