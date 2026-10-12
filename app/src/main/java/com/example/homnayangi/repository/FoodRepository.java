package com.example.homnayangi.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.homnayangi.MyApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository trung tâm xử lý dữ liệu Food với Firestore.
 * Cấu trúc Firestore:
 *   /users/{uid}/foods/{foodId}   — Món ăn riêng của user
 *   /users/{uid}/history/{id}     — Lịch sử quay của user
 *   /community/{postId}           — Bài đăng chia sẻ công khai
 */
public class FoodRepository {

    private static FoodRepository instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    private FoodRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized FoodRepository getInstance() {
        if (instance == null) instance = new FoodRepository();
        return instance;
    }

    // --- Lấy uid hiện tại ---
    // 📌 [GHI CHÚ]: getUid() trả về null nếu user chưa đăng nhập hoặc token hết hạn.
    // Tất cả các hàm truy vấn bên dưới đều kiểm tra uid == null để tránh crash IllegalArgumentException từ Firestore.
    public String getUid() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public String getDisplayName() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getDisplayName() : "Ẩn danh";
    }

    // === FOODS ===

    public Query getFoodsQuery() {
        String uid = getUid();
        if (uid == null) return null;
        return db.collection("users").document(uid)
                .collection("foods")
                .orderBy("createdAt", Query.Direction.DESCENDING);
    }

    public Query getFavoriteFoodsQuery() {
        String uid = getUid();
        if (uid == null) return null;
        return db.collection("users").document(uid)
                .collection("foods")
                .whereEqualTo("isFavorite", true);
    }

    public void addFood(String name, String category, double price, String recipe, OnCompleteListener listener) {
        addFood(name, category, price, recipe, null, listener);
    }

    public void addFood(String name, String category, double price, String recipe, String imageUrl, OnCompleteListener listener) {
        String uid = getUid();
        if (uid == null) {
            listener.onFailure("Người dùng chưa đăng nhập");
            return;
        }

        Map<String, Object> food = new HashMap<>();
        food.put("name", name);
        food.put("category", category);
        food.put("price", price);
        food.put("recipe", recipe != null ? recipe : "");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            food.put("imageUrl", imageUrl);
        }
        food.put("isFavorite", false);
        food.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(uid)
                .collection("foods")
                .add(food)
                .addOnSuccessListener(ref -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void updateFood(String foodId, String name, String category, double price, String recipe, OnCompleteListener listener) {
        String uid = getUid();
        if (uid == null) {
            listener.onFailure("Người dùng chưa đăng nhập");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("category", category);
        updates.put("price", price);
        updates.put("recipe", recipe != null ? recipe : "");

        db.collection("users").document(uid)
                .collection("foods").document(foodId)
                .update(updates)
                .addOnSuccessListener(v -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void toggleFavorite(String foodId, boolean currentFav, OnCompleteListener listener) {
        String uid = getUid();
        if (uid == null) {
            listener.onFailure("Người dùng chưa đăng nhập");
            return;
        }

        db.collection("users").document(uid)
                .collection("foods").document(foodId)
                .update("isFavorite", !currentFav)
                .addOnSuccessListener(v -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void deleteFood(String foodId, OnCompleteListener listener) {
        String uid = getUid();
        if (uid == null) {
            listener.onFailure("Người dùng chưa đăng nhập");
            return;
        }

        db.collection("users").document(uid)
                .collection("foods").document(foodId)
                .delete()
                .addOnSuccessListener(v -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // === HISTORY ===

    public Query getHistoryQuery() {
        String uid = getUid();
        if (uid == null) return null;
        return db.collection("users").document(uid)
                .collection("history")
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }

    public void addHistory(String foodName, double price, OnCompleteListener listener) {
        String uid = getUid();
        if (uid == null) {
            listener.onFailure("Người dùng chưa đăng nhập");
            return;
        }

        Map<String, Object> history = new HashMap<>();
        history.put("foodName", foodName);
        history.put("price", price);
        history.put("timestamp", System.currentTimeMillis());

        db.collection("users").document(uid)
                .collection("history")
                .add(history)
                .addOnSuccessListener(ref -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // 📌 [GHI CHÚ]: Xóa toàn bộ lịch sử quay.
    // Vì Firestore WriteBatch có giới hạn tối đa 500 thao tác/commit,
    // nên ta chia thành từng mẻ (batch) tối đa 400 documents để xóa an toàn, đệ quy gọi mẻ tiếp theo.
    public void clearHistory(OnCompleteListener listener) {
        String uid = getUid();
        if (uid == null) {
            listener.onFailure("Người dùng chưa đăng nhập");
            return;
        }

        db.collection("users").document(uid)
                .collection("history")
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots == null || snapshots.isEmpty()) {
                        listener.onSuccess();
                        return;
                    }
                    List<com.google.firebase.firestore.DocumentSnapshot> docs = snapshots.getDocuments();
                    deleteHistoryBatches(docs, 0, listener);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    private void deleteHistoryBatches(List<com.google.firebase.firestore.DocumentSnapshot> docs, int startIndex, OnCompleteListener listener) {
        if (startIndex >= docs.size()) {
            listener.onSuccess();
            return;
        }
        com.google.firebase.firestore.WriteBatch batch = db.batch();
        int endIndex = Math.min(startIndex + 400, docs.size());
        for (int i = startIndex; i < endIndex; i++) {
            batch.delete(docs.get(i).getReference());
        }
        batch.commit()
                .addOnSuccessListener(v -> deleteHistoryBatches(docs, endIndex, listener))
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // === COMMUNITY ===

    public Query getCommunityQuery() {
        return db.collection("community")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);
    }

    public void postToCommunity(String foodName, String restaurantName, String note, double price, OnCompleteListener listener) {
        String uid = getUid();
        if (uid == null) {
            listener.onFailure("Người dùng chưa đăng nhập");
            return;
        }

        Map<String, Object> post = new HashMap<>();
        post.put("foodName", foodName);
        post.put("restaurantName", restaurantName);
        post.put("note", note);
        post.put("price", price);
        post.put("authorName", getDisplayName());
        post.put("authorId", uid);
        post.put("timestamp", System.currentTimeMillis());
        post.put("likes", 0);
        post.put("commentCount", 0);

        db.collection("community")
                .add(post)
                .addOnSuccessListener(ref -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void toggleLikePost(String postId, boolean isLiked, OnCompleteListener listener) {
        String uid = getUid();
        if (uid == null) {
            listener.onFailure("Người dùng chưa đăng nhập");
            return;
        }

        com.google.firebase.firestore.DocumentReference postRef = db.collection("community").document(postId);
        com.google.firebase.firestore.DocumentReference likeRef = postRef.collection("likes").document(uid);
        com.google.firebase.firestore.WriteBatch batch = db.batch();

        if (isLiked) {
            // Unlike: xóa document trong subcollection và giảm đếm
            batch.delete(likeRef);
            batch.update(postRef, "likes", com.google.firebase.firestore.FieldValue.increment(-1));
        } else {
            // Like: thêm document vào subcollection và tăng đếm
            Map<String, Object> likeData = new HashMap<>();
            likeData.put("timestamp", System.currentTimeMillis());
            batch.set(likeRef, likeData);
            batch.update(postRef, "likes", com.google.firebase.firestore.FieldValue.increment(1));
        }

        batch.commit()
                .addOnSuccessListener(v -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public Query getCommentsQuery(String postId) {
        return db.collection("community").document(postId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING);
    }

    public void addComment(String postId, String text, OnCompleteListener listener) {
        Map<String, Object> comment = new HashMap<>();
        comment.put("text", text);
        comment.put("authorName", getDisplayName());
        comment.put("authorId", getUid());
        comment.put("timestamp", System.currentTimeMillis());

        // Thêm comment vào subcollection
        db.collection("community").document(postId).collection("comments")
                .add(comment)
                .addOnSuccessListener(ref -> {
                    // Tăng số lượng comment ở post gốc
                    db.collection("community").document(postId)
                            .update("commentCount", com.google.firebase.firestore.FieldValue.increment(1));
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // === ROULETTE POOL (Persisted in SharedPreferences) ===

    public static class RouletteItem {
        public final String id;
        public final String name;
        public final double price;

        public RouletteItem(String id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }
    }

    private SharedPreferences getPrefs() {
        return MyApplication.getInstance().getSharedPreferences("homnayangi_prefs", Context.MODE_PRIVATE);
    }

    public List<RouletteItem> getRoulettePool() {
        List<RouletteItem> pool = new ArrayList<>();
        String json = getPrefs().getString("roulette_pool_" + getUid(), "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                pool.add(new RouletteItem(obj.getString("id"), obj.getString("name"), obj.getDouble("price")));
            }
        } catch (JSONException ignored) {}
        return pool;
    }

    private void savePool(List<RouletteItem> pool) {
        JSONArray array = new JSONArray();
        try {
            for (RouletteItem item : pool) {
                JSONObject obj = new JSONObject();
                obj.put("id", item.id);
                obj.put("name", item.name);
                obj.put("price", item.price);
                array.put(obj);
            }
        } catch (JSONException ignored) {}
        getPrefs().edit().putString("roulette_pool_" + getUid(), array.toString()).apply();
    }

    public boolean isInRoulettePool(String foodId) {
        for (RouletteItem item : getRoulettePool()) {
            if (item.id.equals(foodId)) return true;
        }
        return false;
    }

    /** Trả về true nếu thêm mới, false nếu đã có sẵn */
    public boolean addToRoulettePool(RouletteItem item) {
        List<RouletteItem> pool = getRoulettePool();
        for (RouletteItem r : pool) {
            if (r.id.equals(item.id)) return false;
        }
        pool.add(item);
        savePool(pool);
        return true;
    }

    public void removeFromRoulettePool(String foodId) {
        List<RouletteItem> pool = getRoulettePool();
        boolean removed = pool.removeIf(item -> item.id.equals(foodId));
        if (removed) savePool(pool);
    }

    public void clearRoulettePool() {
        savePool(new ArrayList<>());
    }

    // === LIBRARY FOODS ===
    public Query getLibraryQuery() {
        return db.collection("library_foods").orderBy("name", Query.Direction.ASCENDING);
    }

    // === Callback interface ===
    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }
}
