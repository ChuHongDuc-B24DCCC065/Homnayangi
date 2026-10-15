package com.example.homnayangi;

import android.app.Application;

import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {
    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Khởi tạo Cloudinary SDK
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "decmft094");
            MediaManager.init(this, config);
        } catch (Exception e) {
            // Đã init rồi, bỏ qua
        }
    }

    public static MyApplication getInstance() {
        return instance;
    }
}
