package com.example.homnayangi.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.Map;

/**
 * 📌 Utility class upload ảnh lên Cloudinary bằng unsigned preset.
 * Dùng chung cho cả Community lẫn Food upload.
 */
public class CloudinaryUploader {

    public interface UploadListener {
        void onSuccess(String imageUrl);
        void onError(String message);
    }

    /**
     * Upload ảnh lên Cloudinary.
     * @param imageUri URI ảnh từ gallery
     * @param folder   Thư mục trên Cloudinary ("community" hoặc "foods")
     * @param listener Callback khi hoàn tất
     */
    public static void upload(Context context, Uri imageUri, String folder, UploadListener listener) {
        MediaManager.get().upload(imageUri)
                .unsigned("homnayangi_unsigned")
                .option("folder", folder)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        new Handler(Looper.getMainLooper()).post(() -> listener.onSuccess(url != null ? url : ""));
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        new Handler(Looper.getMainLooper()).post(
                                () -> listener.onError(error != null ? error.getDescription() : "Upload failed"));
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }
}
