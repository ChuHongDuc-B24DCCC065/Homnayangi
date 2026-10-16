"""
Mock Data Seeder cho Firebase Cloud Firestore
=============================================
Script này nạp dữ liệu mẫu (kèm imageUrl trực quan từ Unsplash)
trực tiếp vào Cloud Firestore của ứng dụng "Hôm Nay Ăn Gì".

Các collection được nạp:
  1. library_foods  -> Thư viện món ăn (hiển thị trong FoodLibraryActivity)
  2. community      -> Các bài viết mẫu (hiển thị trong CommunityFragment)

Cài đặt:
    pip install firebase-admin

Cách chạy:
    1. Tải serviceAccountKey.json từ:
       Firebase Console -> Project Settings -> Service accounts -> Generate new private key
    2. Đặt file vào thư mục gốc dự án: d:\\homnayangi\\serviceAccountKey.json
    3. Chạy lệnh:
       python seed_mock_data.py
"""

import time
import os
import sys

# Đảm bảo hiển thị Unicode tiếng Việt trên console Windows
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

import firebase_admin
from firebase_admin import credentials, firestore

SERVICE_ACCOUNT_KEY = os.path.join(os.path.dirname(__file__), "serviceAccountKey.json")

# ──────────────────────────────────────────────────────────────
# MOCK DATA - Thư viện món ăn kèm ảnh chất lượng cao từ Unsplash
# ──────────────────────────────────────────────────────────────
LIBRARY_FOODS = [
    {
        "name": "Phở Bò",
        "category": "Phở/Bún",
        "price": 50000.0,
        "note": "Nước dùng thanh ngọt từ xương bò hầm, thịt tái mềm. Ăn kèm quẩy, rau thơm và tương ớt.",
        "imageUrl": "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=800&q=80"
    },
    {
        "name": "Bún Chả Hà Nội",
        "category": "Phở/Bún",
        "price": 45000.0,
        "note": "Chả nướng than hoa thơm lừng, ăn kèm bún tươi và nước mắm chua ngọt. Đặc sản Hà Nội.",
        "imageUrl": "https://images.unsplash.com/photo-1580822184713-fc5400e7fe10?w=800&q=80"
    },
    {
        "name": "Cơm Tấm Sườn Bì",
        "category": "Cơm",
        "price": 45000.0,
        "note": "Sườn nướng đậm đà, bì giòn thính thơm, mỡ hành béo ngậy. Món sáng Sài Gòn.",
        "imageUrl": "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=800&q=80"
    },
    {
        "name": "Bánh Mì Thịt Nướng",
        "category": "Đồ ăn nhanh",
        "price": 25000.0,
        "note": "Bánh mì giòn, thịt nướng mềm, pate béo, đồ chua thanh mát. Street food Việt Nam.",
        "imageUrl": "https://images.unsplash.com/photo-1529543544282-ea669407fca3?w=800&q=80"
    },
    {
        "name": "Gà Rán Giòn Tan",
        "category": "Đồ ăn nhanh",
        "price": 60000.0,
        "note": "Gà tẩm bột chiên giòn rụm, thịt mềm ngọt bên trong. Ăn kèm cơm hoặc khoai tây.",
        "imageUrl": "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=800&q=80"
    },
    {
        "name": "Bún Bò Huế",
        "category": "Phở/Bún",
        "price": 50000.0,
        "note": "Nước dùng cay nồng đặc trưng với sả và mắm ruốc. Có chả cua và bắp bò.",
        "imageUrl": "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=800&q=80"
    },
    {
        "name": "Trà Sữa Trân Châu Đường Đen",
        "category": "Đồ uống",
        "price": 35000.0,
        "note": "Trà đen béo ngậy kết hợp trân châu đường đen dai giòn. Thức uống hot nhất.",
        "imageUrl": "https://images.unsplash.com/photo-1558857563-b37fe4357ef3?w=800&q=80"
    },
    {
        "name": "Bún Đậu Mắm Tôm",
        "category": "Khác",
        "price": 45000.0,
        "note": "Đậu phụ chiên giòn, chả cốm, thịt luộc chấm mắm tôm sủi bọt. Hà Nội vibe.",
        "imageUrl": "https://images.unsplash.com/photo-1555126634-323283e090fa?w=800&q=80"
    },
    {
        "name": "Lẩu Thái Tomyum Hải Sản",
        "category": "Lẩu/Nướng",
        "price": 250000.0,
        "note": "Nước lẩu chua cay đậm đà với hải sản tươi, nấm và rau. Ăn nhóm 2-4 người.",
        "imageUrl": "https://images.unsplash.com/photo-1547928576-a4a33237cbc3?w=800&q=80"
    },
    {
        "name": "Cơm Rang Dưa Bò",
        "category": "Cơm",
        "price": 45000.0,
        "note": "Cơm rang tơi với thịt bò xào dưa chua. Đơn giản mà ngon, no bụng.",
        "imageUrl": "https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=800&q=80"
    }
]

# ──────────────────────────────────────────────────────────────
# MOCK DATA - Bài đăng cộng đồng mẫu kèm ảnh
# ──────────────────────────────────────────────────────────────
now_ms = int(time.time() * 1000)
COMMUNITY_POSTS = [
    {
        "foodName": "Phở Bò Tái Nạm Đặc Biệt",
        "restaurantName": "Phở Thìn Lò Đúc, Hà Nội",
        "note": "Nước dùng béo ngậy thơm nức mùi hành xào, thịt bò mềm tan ăn kèm quẩy giòn cực dính luôn các bác ơi! 10/10 điểm ⭐",
        "price": 65000.0,
        "authorName": "Linh Thảo Foodie",
        "authorId": "mock_author_01",
        "timestamp": now_ms - 3600000 * 2,  # 2 giờ trước
        "likes": 18,
        "commentCount": 3,
        "imageUrl": "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=800&q=80"
    },
    {
        "foodName": "Bún Chả Nem Cua Bể",
        "restaurantName": "Bún Chả Hương Liên, Hoàn Kiếm",
        "note": "Chả nướng than hoa thơm lừng không bị khô, nem cua bể giòn rụm đầy đặn tôm cua. Buổi trưa ghé ăn là hết ý!",
        "price": 50000.0,
        "authorName": "Đạt Nguyễn",
        "authorId": "mock_author_02",
        "timestamp": now_ms - 3600000 * 5,  # 5 giờ trước
        "likes": 24,
        "commentCount": 5,
        "imageUrl": "https://images.unsplash.com/photo-1580822184713-fc5400e7fe10?w=800&q=80"
    },
    {
        "foodName": "Cơm Tấm Sườn Cốt Lết Trứng Ốp La",
        "restaurantName": "Cơm Tấm Ba Ghiền, Phú Nhuận",
        "note": "Miếng sườn to che kín đĩa cơm, ướp đậm đà vừa miệng, mỡ hành tóp mỡ giòn rụm chan nước mắm kẹo kẹo chuẩn bài Sài Gòn.",
        "price": 55000.0,
        "authorName": "Minh Khang",
        "authorId": "mock_author_03",
        "timestamp": now_ms - 3600000 * 12, # 12 giờ trước
        "likes": 32,
        "commentCount": 7,
        "imageUrl": "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=800&q=80"
    }
]


def seed_library_foods(db):
    """Nạp thư viện món ăn vào Firestore collection 'library_foods'"""
    print("\n📚 Đang nạp collection: library_foods...")
    col_ref = db.collection("library_foods")

    # Lấy danh sách món hiện có để tránh tạo trùng tên
    existing_docs = col_ref.stream()
    existing_names = {doc.to_dict().get("name") for doc in existing_docs if doc.to_dict().get("name")}

    count_added = 0
    count_updated = 0

    for food in LIBRARY_FOODS:
        name = food["name"]
        if name in existing_names:
            # Cập nhật imageUrl và note cho món đã có
            for doc in col_ref.where("name", "==", name).limit(1).stream():
                doc.reference.update({
                    "imageUrl": food["imageUrl"],
                    "note": food["note"],
                    "price": food["price"],
                    "category": food["category"]
                })
                count_updated += 1
                print(f"  🔄 Đã cập nhật ảnh & thông tin: {name}")
        else:
            col_ref.add(food)
            count_added += 1
            print(f"  ➕ Đã thêm mới: {name}")

    print(f"  → Hoàn tất library_foods (Thêm mới: {count_added}, Cập nhật: {count_updated})")


def seed_community_posts(db):
    """Nạp bài viết vào Firestore collection 'community'"""
    print("\n🌐 Đang nạp collection: community...")
    col_ref = db.collection("community")

    count_added = 0
    for post in COMMUNITY_POSTS:
        col_ref.add(post)
        count_added += 1
        print(f"  ➕ Đã thêm bài viết: {post['foodName']} bởi {post['authorName']}")

    print(f"  → Đã thêm {count_added} bài đăng mẫu vào community!")


def main():
    print("=" * 60)
    print("  HÔM NAY ĂN GÌ - Firestore Mock Data Seeder")
    print("=" * 60)

    if not os.path.exists(SERVICE_ACCOUNT_KEY):
        print("\n❌ Không tìm thấy file 'serviceAccountKey.json'!")
        print(f"  Đường dẫn mong đợi: {SERVICE_ACCOUNT_KEY}")
        print("\n👉 Hướng dẫn lấy file trong 1 phút:")
        print("  1. Truy cập: https://console.firebase.google.com/")
        print("  2. Chọn Project 'homnayangi-b84da'")
        print("  3. Bấm icon ⚙️ (Project settings) -> Tab 'Service accounts'")
        print("  4. Bấm 'Generate new private key'")
        print("  5. Đổi tên file tải về thành 'serviceAccountKey.json' và đặt vào thư mục d:\\homnayangi")
        return

    try:
        cred = credentials.Certificate(SERVICE_ACCOUNT_KEY)
        firebase_admin.initialize_app(cred)
        db = firestore.client()
        print("\n✅ Kết nối Cloud Firestore thành công!")
    except Exception as e:
        print(f"\n❌ Lỗi xác thực Firebase: {e}")
        return

    seed_library_foods(db)
    seed_community_posts(db)

    print("\n" + "=" * 60)
    print("  🎉 SEED MOCK DATA THÀNH CÔNG RỰC RỠ!")
    print("  → Mở app Android để chiêm ngưỡng thư viện món & cộng đồng có ảnh đẹp!")
    print("=" * 60)


if __name__ == "__main__":
    main()
