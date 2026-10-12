# =============================================================
# proguard-rules.pro - Hôm Nay Ăn Gì?
# =============================================================
# Áp dụng cho bản build Release khi optimization { enable = true }
# =============================================================


# ─── 1. KEEP CHUNG ───────────────────────────────────────────

# Giữ tên class khi dùng reflection (Gson, Firestore data mapping)
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Giữ source file name & line numbers để stacktrace còn đọc được
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile


# ─── 2. GOOGLE MATERIAL 3 ────────────────────────────────────

# Giữ toàn bộ Material Components để inflate XML không bị lỗi
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Ngăn xóa các view custom của Material (dùng trong layout XML)
-keep public class * extends com.google.android.material.bottomnavigation.BottomNavigationView
-keep public class * extends com.google.android.material.card.MaterialCardView
-keep public class * extends com.google.android.material.button.MaterialButton
-keep public class * extends com.google.android.material.textfield.TextInputLayout


# ─── 3. FIREBASE AUTH ────────────────────────────────────────

-keep class com.google.firebase.auth.** { *; }
-keep class com.google.firebase.auth.internal.** { *; }
-dontwarn com.google.firebase.auth.**

# Google Sign-In (dùng nội bộ bởi Firebase Auth)
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }
-dontwarn com.google.android.gms.**


# ─── 4. FIREBASE FIRESTORE ───────────────────────────────────

-keep class com.google.firebase.firestore.** { *; }
-dontwarn com.google.firebase.firestore.**

# Giữ các model class dùng để map document Firestore
# → Firestore dùng reflection để serialize/deserialize field
# → Đổi com.example.homnayangi.model thành package thực tế nếu có
-keep class com.example.homnayangi.model.** { *; }
-keepclassmembers class com.example.homnayangi.model.** {
    <init>();          # constructor không tham số bắt buộc cho Firestore
    <fields>;
}


# ─── 5. FIREBASE CORE / COMMON ───────────────────────────────

-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Protobuf (dùng bởi Firestore)
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

# gRPC (dùng bởi Firestore)
-keep class io.grpc.** { *; }
-dontwarn io.grpc.**

# OkHttp (dùng bởi Firebase)
-dontwarn okhttp3.**
-dontwarn okio.**


# ─── 6. ANDROIDX ─────────────────────────────────────────────

-keep class androidx.** { *; }
-dontwarn androidx.**

# Fragment & ViewModel
-keep class * extends androidx.fragment.app.Fragment { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }

# RecyclerView Adapter (ViewHolder dùng reflection khi bind)
-keep class * extends androidx.recyclerview.widget.RecyclerView$Adapter { *; }
-keep class * extends androidx.recyclerview.widget.RecyclerView$ViewHolder { *; }


# ─── 7. PROJECT-SPECIFIC ─────────────────────────────────────

# Giữ tất cả Activity, Fragment, Adapter trong project
-keep class com.example.homnayangi.activities.** { *; }
-keep class com.example.homnayangi.adapters.** { *; }
-keep class com.example.homnayangi.repository.** { *; }
-keep class com.example.homnayangi.interfaces.** { *; }
-keep class com.example.homnayangi.utils.** { *; }


# ─── 8. SUPPRESS WARNINGS KHÔNG CẦN THIẾT ───────────────────

-dontwarn java.lang.invoke.**
-dontwarn javax.annotation.**
-dontwarn org.checkerframework.**

# Google Sign-In - Credential Manager
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
