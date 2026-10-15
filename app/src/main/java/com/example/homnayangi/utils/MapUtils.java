package com.example.homnayangi.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class MapUtils {

    /**
     * Mở Google Maps (hoặc trình duyệt) để tìm quán ăn gần nhất cho món được chọn.
     */
    public static void findNearbyRestaurants(Context context, String foodName) {
        if (context == null || foodName == null || foodName.trim().isEmpty()) {
            return;
        }

        String query = "quán " + foodName.trim() + " gần đây";
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        Toast.makeText(context, "🛵 Đang tìm quán " + foodName + " gần bạn...", Toast.LENGTH_SHORT).show();

        try {
            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
            } else {
                // Nếu máy không có app Google Maps thì mở qua web maps
                Uri webUri = Uri.parse("https://www.google.com/maps/search/" + Uri.encode(query));
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, webUri);
                context.startActivity(browserIntent);
            }
        } catch (Exception e) {
            // Trường hợp dự phòng mở web URL
            Uri webUri = Uri.parse("https://www.google.com/maps/search/" + Uri.encode(query));
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, webUri);
            context.startActivity(browserIntent);
        }
    }
}
