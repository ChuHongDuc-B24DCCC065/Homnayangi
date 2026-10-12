package com.example.homnayangi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homnayangi.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edtDisplayName, edtEmail, edtPassword;
    private MaterialButton btnRegister;
    private TextView tvGoToLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtDisplayName = findViewById(R.id.edtDisplayName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> registerUser());
        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String displayName = edtDisplayName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(displayName)) {
            edtDisplayName.setError("Vui lòng nhập tên hiển thị");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            return;
        }
        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Đang đăng ký...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // Cập nhật display name
                    UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .build();
                    authResult.getUser().updateProfile(profile)
                            .addOnCompleteListener(profileTask -> {
                                // Lưu thông tin user vào Firestore (dù updateProfile thành công hay thất bại)
                                String uid = authResult.getUser().getUid();
                                Map<String, Object> userDoc = new HashMap<>();
                                userDoc.put("displayName", displayName);
                                userDoc.put("email", email);
                                userDoc.put("createdAt", System.currentTimeMillis());

                                db.collection("users").document(uid).set(userDoc)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(this, "Đăng ký thành công! Chào " + displayName, Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(this, MainActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Vẫn vào app dù lưu Firestore thất bại
                                            Toast.makeText(this, "Đăng ký thành công! Chào " + displayName, Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(this, MainActivity.class));
                                            finish();
                                        });
                            });
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("ĐĂNG KÝ");
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
