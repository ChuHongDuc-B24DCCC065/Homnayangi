package com.example.homnayangi.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class VietnameseUtils {

    private static final Pattern DIACRITICAL_MARKS_PATTERN = 
            Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    /**
     * Chuyển chuỗi tiếng Việt có dấu thành không dấu và chữ thường.
     * Ví dụ: "Bún Bò Huế" -> "bun bo hue"
     */
    public static String removeAccents(String str) {
        if (str == null) return "";
        String nfd = Normalizer.normalize(str, Normalizer.Form.NFD);
        String withoutMarks = DIACRITICAL_MARKS_PATTERN.matcher(nfd).replaceAll("");
        return withoutMarks.replace('đ', 'd').replace('Đ', 'D').toLowerCase().trim();
    }

    /**
     * Kiểm tra text có chứa query hay không (hỗ trợ cả tiếng Việt có dấu và không dấu).
     */
    public static boolean containsIgnoreCaseAndAccents(String source, String query) {
        if (source == null || query == null) return false;
        String queryLower = query.trim().toLowerCase();
        if (queryLower.isEmpty()) return true;

        // 1. Kiểm tra so sánh trực tiếp
        if (source.toLowerCase().contains(queryLower)) return true;

        // 2. Kiểm tra khi đã loại bỏ dấu tiếng Việt
        String cleanSource = removeAccents(source);
        String cleanQuery = removeAccents(query);
        return cleanSource.contains(cleanQuery);
    }
}
