package com.example.homnayangi.activities;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.example.homnayangi.utils.CloudinaryUploader;
import com.example.homnayangi.utils.PriceFormatWatcher;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.bumptech.glide.Glide;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class CommunityFragment extends Fragment {

    private RecyclerView recyclerView;
    private CommunityAdapter adapter;
    private Button btnPost;
    private View bannerLoginPrompt;
    private FoodRepository repo;
    private Uri selectedPostImageUri = null;
    private ImageView imgPostPreview;
    private View cardPostPreview;

    private final ActivityResultLauncher<Intent> postImagePicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedPostImageUri = result.getData().getData();
                    if (imgPostPreview != null) {
                        if (cardPostPreview != null) cardPostPreview.setVisibility(View.VISIBLE);
                        Glide.with(this).load(selectedPostImageUri).into(imgPostPreview);
                    }
                }
            });

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

        // 📌 Image picker setup
        Button btnPickImage = dialogView.findViewById(R.id.btnPickPostImage);
        imgPostPreview = dialogView.findViewById(R.id.imgPostPreview);
        cardPostPreview = dialogView.findViewById(R.id.cardPostPreview);
        selectedPostImageUri = null;

        // 📌 Gắn PriceFormatWatcher tự động format số tiền
        edtPrice.addTextChangedListener(new PriceFormatWatcher(edtPrice));

        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            postImagePicker.launch(intent);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String foodName = edtFoodName.getText().toString().trim();
            if (TextUtils.isEmpty(foodName)) {
                edtFoodName.setError("Vui lòng nhập tên món ăn");
                return;
            }

            String restaurant = edtRestaurant.getText().toString().trim();
            String note = edtNote.getText().toString().trim();
            double price = PriceFormatWatcher.getCleanValue(edtPrice);

            btnSubmit.setEnabled(false);
            btnSubmit.setText("Đang đăng...");

            final double finalPrice = price;
            final String fFoodName = foodName;
            final String fRestaurant = restaurant;
            final String fNote = note;

            if (selectedPostImageUri != null) {
                // Upload ảnh trước, rồi đăng bài
                CloudinaryUploader.upload(requireContext(), selectedPostImageUri, "community",
                        new CloudinaryUploader.UploadListener() {
                            @Override
                            public void onSuccess(String imageUrl) {
                                repo.postToCommunity(fFoodName, fRestaurant, fNote, finalPrice, imageUrl,
                                        createPostCallback());
                                Toast.makeText(getContext(), "Đang chia sẻ... 🎉", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }

                            @Override
                            public void onError(String message) {
                                // Upload lỗi → đăng không ảnh
                                repo.postToCommunity(fFoodName, fRestaurant, fNote, finalPrice, "",
                                        createPostCallback());
                                Toast.makeText(getContext(), "Đăng bài (không có ảnh)", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
            } else {
                // Không có ảnh
                repo.postToCommunity(fFoodName, fRestaurant, fNote, finalPrice, "", createPostCallback());
                Toast.makeText(getContext(), "Đang chia sẻ... 🎉", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private FoodRepository.OnCompleteListener createPostCallback() {
        return new FoodRepository.OnCompleteListener() {
            @Override
            public void onSuccess() { }
            @Override
            public void onFailure(String error) {
                if (getContext() != null)
                    Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        };
    }
}
