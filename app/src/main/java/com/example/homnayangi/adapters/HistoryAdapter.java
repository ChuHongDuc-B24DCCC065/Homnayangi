package com.example.homnayangi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homnayangi.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<DocumentSnapshot> historyDocs = new ArrayList<>();

    public void setHistoryDocs(List<DocumentSnapshot> docs) {
        this.historyDocs = docs != null ? docs : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        DocumentSnapshot doc = historyDocs.get(position);

        String foodName = doc.getString("foodName");
        Double price = doc.getDouble("price");
        Long timestamp = doc.getLong("timestamp");

        holder.tvHistoryFoodName.setText(foodName != null ? foodName : "");

        if (timestamp != null) {
            String dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(new Date(timestamp));
            holder.tvHistoryDateTime.setText(dateTime);
        } else {
            holder.tvHistoryDateTime.setText("");
        }

        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.tvHistoryPrice.setText(price != null ? formatter.format(price) + " VNĐ" : "");
    }

    @Override
    public int getItemCount() {
        return historyDocs.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvHistoryFoodName, tvHistoryDateTime, tvHistoryPrice;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHistoryFoodName = itemView.findViewById(R.id.tvHistoryFoodName);
            tvHistoryDateTime = itemView.findViewById(R.id.tvHistoryDateTime);
            tvHistoryPrice = itemView.findViewById(R.id.tvHistoryPrice);
        }
    }
}
