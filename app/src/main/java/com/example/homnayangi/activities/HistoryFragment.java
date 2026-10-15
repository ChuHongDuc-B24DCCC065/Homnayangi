package com.example.homnayangi.activities;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homnayangi.R;
import com.example.homnayangi.adapters.HistoryAdapter;
import com.example.homnayangi.repository.FoodRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private LinearLayout layoutEmpty;
    private TextView tvTotalSpins, tvTotalSpent;
    private MaterialButton btnClearHistory;
    private FoodRepository repo;
    private View cardBudget;
    private ProgressBar progressBudget;
    private TextView tvBudgetLabel, tvBudgetStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repo = FoodRepository.getInstance();

        recyclerView    = view.findViewById(R.id.recyclerViewHistory);
        layoutEmpty     = view.findViewById(R.id.layoutEmpty);
        tvTotalSpins    = view.findViewById(R.id.tvTotalSpins);
        tvTotalSpent    = view.findViewById(R.id.tvTotalSpent);
        btnClearHistory = view.findViewById(R.id.btnClearHistory);
        cardBudget      = view.findViewById(R.id.cardBudget);
        progressBudget  = view.findViewById(R.id.progressBudget);
        tvBudgetLabel   = view.findViewById(R.id.tvBudgetLabel);
        tvBudgetStatus  = view.findViewById(R.id.tvBudgetStatus);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistoryAdapter();
        recyclerView.setAdapter(adapter);

        MaterialButton btnEmptySpin = view.findViewById(R.id.btnEmptySpin);
        if (btnEmptySpin != null) {
            btnEmptySpin.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToTab(R.id.nav_home);
                }
            });
        }

        loadHistory();

        btnClearHistory.setOnClickListener(v -> confirmClearHistory());
    }

    private void loadHistory() {
        com.google.firebase.firestore.Query query = repo.getHistoryQuery();
        if (query == null) return;
        query.addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null || !isAdded()) return;

            List<DocumentSnapshot> docs = snapshots.getDocuments();
            adapter.setHistoryDocs(docs);

            if (docs.isEmpty()) {
                layoutEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                btnClearHistory.setVisibility(View.GONE);
                tvTotalSpins.setText("0");
                tvTotalSpent.setText("0 VNĐ");
            } else {
                layoutEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                btnClearHistory.setVisibility(View.VISIBLE);

                // Tính thống kê
                tvTotalSpins.setText(String.valueOf(docs.size()));
                double totalSpent = 0;
                double totalSpentThisMonth = 0;
                
                // 📌 Lọc chi tiêu tháng hiện tại cho budget
                Calendar cal = Calendar.getInstance();
                int currentMonth = cal.get(Calendar.MONTH);
                int currentYear = cal.get(Calendar.YEAR);
                Calendar docCal = Calendar.getInstance();
                
                for (DocumentSnapshot doc : docs) {
                    Double price = doc.getDouble("price");
                    if (price != null) {
                        totalSpent += price;
                        Long ts = doc.getLong("timestamp");
                        if (ts != null) {
                            docCal.setTimeInMillis(ts);
                            if (docCal.get(Calendar.MONTH) == currentMonth 
                                    && docCal.get(Calendar.YEAR) == currentYear) {
                                totalSpentThisMonth += price;
                            }
                        }
                    }
                }
                DecimalFormat fmt = new DecimalFormat("#,###");
                tvTotalSpent.setText(fmt.format(totalSpent) + " VNĐ");

                // 📌 Cập nhật budget progress card (chỉ tính tháng hiện tại)
                loadBudget(totalSpentThisMonth);
            }
        });
    }

    private void confirmClearHistory() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa lịch sử")
                .setMessage("Bạn có chắc muốn xóa toàn bộ lịch sử quay?")
                .setPositiveButton("Xóa hết", (d, w) -> {
                    repo.clearHistory(new FoodRepository.OnCompleteListener() {
                        @Override
                        public void onSuccess() {
                            if (isAdded())
                                Toast.makeText(requireContext(), "✅ Đã xóa lịch sử", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String error) {
                            if (isAdded())
                                Toast.makeText(requireContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * 📌 Load ngân sách tháng từ Firestore và hiển thị progress card
     * Màu thanh tiến trình: xanh (< 80%), vàng (80-100%), đỏ (> 100%)
     */
    private void loadBudget(double totalSpentThisMonth) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || doc == null || !doc.exists()) return;

                    Boolean budgetEnabled = doc.getBoolean("budgetEnabled");
                    Double monthlyBudget = doc.getDouble("monthlyBudget");

                    // 📌 Ẩn card nếu chưa bật hoặc chưa thiết lập ngân sách
                    if (budgetEnabled == null || !budgetEnabled
                            || monthlyBudget == null || monthlyBudget <= 0) {
                        cardBudget.setVisibility(View.GONE);
                        return;
                    }

                    cardBudget.setVisibility(View.VISIBLE);

                    // Tính phần trăm
                    int percent = (int) ((totalSpentThisMonth / monthlyBudget) * 100);
                    progressBudget.setProgress(Math.min(percent, 100));

                    // 📌 Đổi màu progress bar theo mức chi tiêu (màu nổi hơn)
                    int color;
                    if (percent < 80) {
                        color = 0xFF2E7D32; // Green 800
                    } else if (percent <= 100) {
                        color = 0xFFFF8F00; // Amber 800
                    } else {
                        color = 0xFFD32F2F; // Red 700
                    }
                    progressBudget.setProgressTintList(ColorStateList.valueOf(color));

                    // Hiển thị trạng thái
                    DecimalFormat fmt = new DecimalFormat("#,###");

                    // 📌 Cảnh báo khi vượt ngân sách
                    if (percent > 100) {
                        tvBudgetStatus.setTextColor(0xFFD32F2F);
                        tvBudgetStatus.setText("⚠️ Đã vượt ngân sách! " + fmt.format(totalSpentThisMonth) + " / "
                                + fmt.format(monthlyBudget) + " VNĐ (" + percent + "%)");
                        // Đổi nền card đỏ nhạt
                        ((com.google.android.material.card.MaterialCardView) cardBudget)
                                .setCardBackgroundColor(0xFFFCEDEA);
                        ((com.google.android.material.card.MaterialCardView) cardBudget)
                                .setStrokeColor(0xFFD32F2F);
                    } else {
                        tvBudgetStatus.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.ds_text_secondary));
                        tvBudgetStatus.setText(fmt.format(totalSpentThisMonth) + " / "
                                + fmt.format(monthlyBudget) + " VNĐ (" + percent + "%)");
                        ((com.google.android.material.card.MaterialCardView) cardBudget)
                                .setCardBackgroundColor(
                                        ContextCompat.getColor(requireContext(), R.color.ds_surface));
                        ((com.google.android.material.card.MaterialCardView) cardBudget)
                                .setStrokeColor(
                                        ContextCompat.getColor(requireContext(), R.color.ds_border));
                    }

                    // Cập nhật label với tháng hiện tại
                    int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
                    tvBudgetLabel.setText("💰 Ngân sách tháng " + currentMonth);
                });
    }
}
