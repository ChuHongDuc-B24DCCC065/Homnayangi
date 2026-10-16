package com.example.homnayangi.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;

/**
 * 📌 TextWatcher tự động format số tiền với dấu phẩy ngăn cách hàng nghìn.
 * VD: 3000000 → 3,000,000
 */
public class PriceFormatWatcher implements TextWatcher {

    private final EditText editText;
    private String current = "";

    public PriceFormatWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().equals(current)) return;

        editText.removeTextChangedListener(this);

        // Xóa hết dấu phẩy, chỉ giữ số
        String cleanString = s.toString().replaceAll("[^0-9]", "");

        if (!cleanString.isEmpty()) {
            try {
                long parsed = Long.parseLong(cleanString);
                DecimalFormat fmt = new DecimalFormat("#,###");
                String formatted = fmt.format(parsed);
                current = formatted;
                editText.setText(formatted);
                editText.setSelection(formatted.length());
            } catch (NumberFormatException e) {
                // Số quá lớn, giữ nguyên
            }
        } else {
            current = "";
        }

        editText.addTextChangedListener(this);
    }

    /**
     * 📌 Lấy giá trị số từ EditText đã format.
     * VD: "3,000,000" → 3000000.0
     */
    public static double getCleanValue(EditText editText) {
        String text = editText.getText().toString().replaceAll("[^0-9]", "");
        if (text.isEmpty()) return 0;
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
