package com.example.homnayangi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.homnayangi.R;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // ⚠️ Thay bằng Web Client ID từ Firebase Console
    // → Firebase Console → Authentication → Sign-in method → Google → Web client ID
    private static final String WEB_CLIENT_ID = "412521528998-5oek8l1gehk83on463jm0jietecaguh8.apps.googleusercontent.com";

    private TextInputEditText edtEmail, edtPassword;
    private MaterialButton btnLogin, btnGuestMode, btnGoogleSignIn;
    private MaterialButton tvGoToRegister;
    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);

        edtEmail        = findViewById(R.id.edtEmail);
        edtPassword     = findViewById(R.id.edtPassword);
        btnLogin        = findViewById(R.id.btnLogin);
        tvGoToRegister  = findViewById(R.id.tvGoToRegister);
        btnGuestMode    = findViewById(R.id.btnGuestMode);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        btnLogin.setOnClickListener(v -> loginUser());
        tvGoToRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        btnGuestMode.setOnClickListener(v -> loginAsGuest());
        btnGoogleSignIn.setOnClickListener(v -> loginWithGoogle());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) goToMain();
    }

    // ─── Email / Password ────────────────────────────────────

    private void loginUser() {
        String email    = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> goToMain())
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Đăng nhập");
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // ─── Guest (Anonymous) ───────────────────────────────────

    private void loginAsGuest() {
        btnGuestMode.setEnabled(false);
        btnGuestMode.setText("Đang vào...");
        mAuth.signInAnonymously()
                .addOnSuccessListener(r -> goToMain())
                .addOnFailureListener(e -> {
                    btnGuestMode.setEnabled(true);
                    btnGuestMode.setText("Chơi ngay không cần đăng nhập");
                    Toast.makeText(this, "Lỗi: Kiểm tra kết nối mạng.", Toast.LENGTH_LONG).show();
                });
    }

    // ─── Google Sign-In (Credential Manager API) ─────────────

    private void loginWithGoogle() {
        btnGoogleSignIn.setEnabled(false);
        btnGoogleSignIn.setText("Đang kết nối...");

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)   // false = hiện tất cả tài khoản Google trên máy
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)            // false = user tự chọn tài khoản
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleGoogleCredential(result.getCredential());
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        runOnUiThread(() -> {
                            btnGoogleSignIn.setEnabled(true);
                            btnGoogleSignIn.setText("Đăng nhập bằng Google");
                            Log.e(TAG, "Google Sign-In thất bại", e);
                            Toast.makeText(LoginActivity.this,
                                    "Không thể đăng nhập Google: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }

    private void handleGoogleCredential(Credential credential) {
        String idToken = null;

        if (credential instanceof GoogleIdTokenCredential) {
            // Trường hợp 1: trả về trực tiếp GoogleIdTokenCredential
            idToken = ((GoogleIdTokenCredential) credential).getIdToken();

        } else if (credential instanceof CustomCredential) {
            // Trường hợp 2 (phổ biến hơn): trả về dưới dạng CustomCredential
            CustomCredential custom = (CustomCredential) credential;
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(custom.getType())) {
                try {
                    idToken = GoogleIdTokenCredential.createFrom(custom.getData()).getIdToken();
                } catch (Exception e) {
                    Log.e(TAG, "Không thể parse GoogleIdTokenCredential", e);
                }
            }
        }

        if (idToken != null) {
            final String token = idToken;
            AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(token, null);
            mAuth.signInWithCredential(firebaseCredential)
                    .addOnSuccessListener(r -> goToMain())
                    .addOnFailureListener(e -> runOnUiThread(() -> {
                        btnGoogleSignIn.setEnabled(true);
                        btnGoogleSignIn.setText("Đăng nhập bằng Google");
                        Toast.makeText(this, "Lỗi xác thực Firebase: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }));
        } else {
            runOnUiThread(() -> {
                btnGoogleSignIn.setEnabled(true);
                btnGoogleSignIn.setText("Đăng nhập bằng Google");
                Toast.makeText(this, "Không thể lấy Google ID Token.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // ─── Navigate ────────────────────────────────────────────

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
