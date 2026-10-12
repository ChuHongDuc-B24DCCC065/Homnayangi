package com.example.homnayangi.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homnayangi.R;
import com.example.homnayangi.adapters.CommentAdapter;
import com.example.homnayangi.adapters.CommunityAdapter;
import com.example.homnayangi.repository.FoodRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CommunityFragment extends Fragment {

    private RecyclerView recyclerView;
    private CommunityAdapter adapter;
    private Button btnPost;
    private View bannerLoginPrompt;
    private FoodRepository repo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repo = FoodRepository.getInstance();
        recyclerView = view.findViewById(R.id.recyclerViewCommunity);
        btnPost = view.findViewById(R.id.btnTopPost);
        bannerLoginPrompt = view.findViewById(R.id.bannerLoginPrompt);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CommunityAdapter();
        adapter.setCommentListener(this::showCommentsDialog);
        recyclerView.setAdapter(adapter);

        // Kiểm tra chế độ guest
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean isGuest = currentUser == null || currentUser.isAnonymous();

        if (isGuest) {
            // Guest: ẩn nút đăng, hiện banner mời đăng nhập
            btnPost.setVisibility(View.GONE);
            if (bannerLoginPrompt != null) {
                bannerLoginPrompt.setVisibility(View.VISIBLE);
                bannerLoginPrompt.setOnClickListener(v -> {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getContext(), LoginActivity.class));
                    if (getActivity() != null) getActivity().finish();
                });
            }
        } else {
            if (bannerLoginPrompt != null) bannerLoginPrompt.setVisibility(View.GONE);
            btnPost.setVisibility(View.VISIBLE);
            btnPost.setOnClickListener(v -> showPostDialog());
        }

        loadPosts();
    }

    private void loadPosts() {
        repo.getCommunityQuery().addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null || !isAdded()) return;
            adapter.setPosts(snapshots.getDocuments());
        });
    }

    private void showCommentsDialog(String postId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_comments, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageView btnClose = dialogView.findViewById(R.id.btnCloseComments);
        RecyclerView recyclerComments = dialogView.findViewById(R.id.recyclerViewComments);
        TextView tvNoComments = dialogView.findViewById(R.id.tvNoComments);
        EditText edtComment = dialogView.findViewById(R.id.edtComment);
        ImageView btnSendComment = dialogView.findViewById(R.id.btnSendComment);

        // Kiểm tra guest: ẩn ô nhập comment
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean isGuest = currentUser == null || currentUser.isAnonymous();
        if (isGuest) {
            edtComment.setVisibility(View.GONE);
            btnSendComment.setVisibility(View.GONE);
        }

        CommentAdapter commentAdapter = new CommentAdapter();
        recyclerComments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerComments.setAdapter(commentAdapter);

        // Load comments
        com.google.firebase.firestore.ListenerRegistration commentsListener =
                repo.getCommentsQuery(postId).addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    if (snapshots.isEmpty()) {
                        tvNoComments.setVisibility(View.VISIBLE);
                        recyclerComments.setVisibility(View.GONE);
                    } else {
                        tvNoComments.setVisibility(View.GONE);
                        recyclerComments.setVisibility(View.VISIBLE);
                        commentAdapter.setComments(snapshots.getDocuments());
                        recyclerComments.scrollToPosition(snapshots.size() - 1);
                    }
                });

        dialog.setOnDismissListener(d -> {
            if (commentsListener != null) {
                commentsListener.remove();
            }
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        if (!isGuest) {
            btnSendComment.setOnClickListener(v -> {
                String text = edtComment.getText().toString().trim();
                if (TextUtils.isEmpty(text)) return;

                btnSendComment.setEnabled(false);
                repo.addComment(postId, text, new FoodRepository.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        edtComment.setText("");
                        btnSendComment.setEnabled(true);
                    }

                    @Override
                    public void onFailure(String error) {
                        btnSendComment.setEnabled(true);
                        if (getContext() != null)
                            Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        dialog.show();
    }

    private void showPostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_post_community, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextInputEditText edtFoodName = dialogView.findViewById(R.id.edtPostFoodName);
        TextInputEditText edtRestaurant = dialogView.findViewById(R.id.edtPostRestaurant);
        TextInputEditText edtNote = dialogView.findViewById(R.id.edtPostNote);
        TextInputEditText edtPrice = dialogView.findViewById(R.id.edtPostPrice);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelPost);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmitPost);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String foodName = edtFoodName.getText().toString().trim();
            if (TextUtils.isEmpty(foodName)) {
                edtFoodName.setError("Vui lòng nhập tên món ăn");
                return;
            }

            String restaurant = edtRestaurant.getText().toString().trim();
            String note = edtNote.getText().toString().trim();
            double price = 0;
            try {
                price = Double.parseDouble(edtPrice.getText().toString().trim());
            } catch (NumberFormatException ignored) {}

            btnSubmit.setEnabled(false);

            repo.postToCommunity(foodName, restaurant, note, price, new FoodRepository.OnCompleteListener() {
                @Override
                public void onSuccess() { }
                @Override
                public void onFailure(String error) {
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });

            Toast.makeText(getContext(), "Đang chia sẻ... 🎉", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}
