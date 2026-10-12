package com.example.homnayangi.activities;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.homnayangi.R;
import com.example.homnayangi.utils.ReminderReceiver;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView tvAvatarInitials, tvProfileName, tvProfileEmail;
    private TextInputEditText edtDisplayName, edtEmail, edtDob, edtPhone;
    private MaterialAutoCompleteTextView spinnerGender;
    private MaterialButton btnSaveProfile, btnLogout;
    private com.google.android.material.switchmaterial.SwitchMaterial switchReminder;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth       = FirebaseAuth.getInstance();
        db          = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Ánh xạ views
        tvAvatarInitials = view.findViewById(R.id.tvAvatarInitials);
        tvProfileName    = view.findViewById(R.id.tvProfileName);
        tvProfileEmail   = view.findViewById(R.id.tvProfileEmail);
        edtDisplayName   = view.findViewById(R.id.edtDisplayName);
        edtEmail         = view.findViewById(R.id.edtEmail);
        edtDob           = view.findViewById(R.id.edtDob);
        edtPhone         = view.findViewById(R.id.edtPhone);
        spinnerGender    = view.findViewById(R.id.spinnerGender);
        btnSaveProfile   = view.findViewById(R.id.btnSaveProfile);
        btnLogout        = view.findViewById(R.id.btnLogout);
        switchReminder   = view.findViewById(R.id.switchReminder);
        
        prefs = requireActivity().getSharedPreferences("homnayangi_prefs", Context.MODE_PRIVATE);

        // Setup dropdown giới tính
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                Arrays.asList("Nam", "Nữ", "Khác"));
        spinnerGender.setAdapter(genderAdapter);

        // Mở DatePicker khi click ô ngày sinh
        edtDob.setOnClickListener(v -> showDatePicker());

        // Nút lưu
        btnSaveProfile.setOnClickListener(v -> saveProfile());

        // Nút đăng xuất
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });

        // Cài đặt nhắc nhở
        boolean isReminderOn = prefs.getBoolean("meal_reminder", false);
        switchReminder.setChecked(isReminderOn);
        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
                } else {
                    enableReminder();
                }
            } else {
                disableReminder();
            }
        });

        // Load dữ liệu người dùng
        loadUserData();
    }

    /** Load thông tin từ FirebaseAuth + Firestore */
    private void loadUserData() {
        if (currentUser == null) return;

        String name  = currentUser.getDisplayName();
        String email = currentUser.getEmail();

        edtEmail.setText(email);
        tvProfileEmail.setText(email);

        if (!TextUtils.isEmpty(name)) {
            edtDisplayName.setText(name);
            tvProfileName.setText(name);
            setInitial(name);
        }

        // Load thêm từ Firestore
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) return; // fragment đã detach
                    if (doc.exists()) {
                        String firestoreName = doc.getString("displayName");
                        String dob           = doc.getString("dob");
                        String gender        = doc.getString("gender");
                        String phone         = doc.getString("phone");

                        if (!TextUtils.isEmpty(firestoreName)) {
                            edtDisplayName.setText(firestoreName);
                            tvProfileName.setText(firestoreName);
                            setInitial(firestoreName);
                        }
                        if (!TextUtils.isEmpty(dob))    edtDob.setText(dob);
                        if (!TextUtils.isEmpty(gender)) spinnerGender.setText(gender, false);
                        if (!TextUtils.isEmpty(phone))  edtPhone.setText(phone);
                    }
                });
    }


    /** Hiển thị chữ cái đầu tên trên avatar */
    private void setInitial(String name) {
        String letter = name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase();
        tvAvatarInitials.setText(letter);
    }

    /** Hiển thị DatePickerDialog */
    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();

        String existing = edtDob.getText() != null ? edtDob.getText().toString() : "";
        if (!existing.isEmpty()) {
            try {
                String[] parts = existing.split("/");
                cal.set(Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[1]) - 1,
                        Integer.parseInt(parts[0]));
            } catch (Exception ignored) { }
        }

        new DatePickerDialog(
                requireContext(),
                (picker, year, month, day) ->
                        edtDob.setText(String.format("%02d/%02d/%04d", day, month + 1, year)),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ) {{
            getDatePicker().setMaxDate(System.currentTimeMillis());
        }}.show();
    }

    /** Lưu profile vào Firestore */
    private void saveProfile() {
        String displayName = getText(edtDisplayName);
        String dob         = getText(edtDob);
        String gender      = spinnerGender.getText() != null ? spinnerGender.getText().toString().trim() : "";
        String phone       = getText(edtPhone);

        if (TextUtils.isEmpty(displayName)) {
            edtDisplayName.setError("Vui lòng nhập tên hiển thị");
            return;
        }

        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Đang lưu...");

        Map<String, Object> data = new HashMap<>();
        data.put("displayName", displayName);
        data.put("email", currentUser.getEmail());
        data.put("dob", dob);
        data.put("gender", gender);
        data.put("phone", phone);

        // Cập nhật tên trong Firebase Auth
        currentUser.updateProfile(new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build());

        // 📌 [GHI CHÚ]: Chỉ cập nhật giao diện (Toast "Đã lưu", bật lại button) BÊN TRONG addOnSuccessListener
        // Tránh tình trạng thông báo thành công cho người dùng trước khi dữ liệu thực sự được ghi vào Firestore
        db.collection("users").document(currentUser.getUid())
                .set(data)
                .addOnSuccessListener(unused -> {
                    // Kiểm tra !isAdded() để đảm bảo Fragment vẫn đang hiển thị, tránh crash requireContext()
                    if (!isAdded()) return;
                    updateAuthorNameInCommunity(displayName);
                    // Cập nhật UI sau khi lưu thành công
                    tvProfileName.setText(displayName);
                    setInitial(displayName);
                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText("LƯU THÔNG TIN");
                    Toast.makeText(requireContext(), "✅ Đã lưu thông tin!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText("LƯU THÔNG TIN");
                    Toast.makeText(requireContext(), "Lỗi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getText(TextInputEditText edt) {
        return edt.getText() != null ? edt.getText().toString().trim() : "";
    }

    private void enableReminder() {
        prefs.edit().putBoolean("meal_reminder", true).apply();
        
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
            Toast.makeText(getContext(), "Đã bật nhắc nhở lúc 11:30 hàng ngày!", Toast.LENGTH_SHORT).show();
        }
    }

    private void disableReminder() {
        prefs.edit().putBoolean("meal_reminder", false).apply();
        
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Toast.makeText(getContext(), "Đã tắt nhắc nhở.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableReminder();
            } else {
                Toast.makeText(requireContext(), "Cần cấp quyền để nhận thông báo!", Toast.LENGTH_SHORT).show();
                switchReminder.setChecked(false);
            }
        }
    }

    private void updateAuthorNameInCommunity(String newDisplayName) {
        if (currentUser == null) return;
        String userId = currentUser.getUid();

        // Cập nhật tên trong các bài viết
        db.collection("community")
                .whereEqualTo("authorId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        batch.update(doc.getReference(), "authorName", newDisplayName);
                    }
                    batch.commit();
                });

        // Cập nhật tên trong các bình luận
        db.collectionGroup("comments")
                .whereEqualTo("authorId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        batch.update(doc.getReference(), "authorName", newDisplayName);
                    }
                    batch.commit();
                });
    }
}
