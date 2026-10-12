package com.example.homnayangi.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homnayangi.R;
import com.example.homnayangi.adapters.HistoryAdapter;
import com.example.homnayangi.repository.FoodRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DecimalFormat;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private LinearLayout layoutEmpty;
    private TextView tvTotalSpins, tvTotalSpent;
    private MaterialButton btnClearHistory;
    private FoodRepository repo;

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
                for (DocumentSnapshot doc : docs) {
                    Double price = doc.getDouble("price");
                    if (price != null) totalSpent += price;
                }
                DecimalFormat fmt = new DecimalFormat("#,###");
                tvTotalSpent.setText(fmt.format(totalSpent) + " VNĐ");
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
}
