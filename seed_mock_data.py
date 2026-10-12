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
        "recipe": "Nguyên liệu:\n- Bánh phở: 150g\n- Thịt bò (nạm, gầu, tái): 100g\n- Nước dùng xương bò, quế, hồi, thảo quả\n- Hành lá, rau mùi, giá đỗ\n\nCách làm:\n1. Hầm xương bò với gia vị 4-6 tiếng.\n2. Chần bánh phở qua nước sôi.\n3. Thái thịt bò mỏng, chần qua nước dùng.\n4. Xếp phở, thịt, hành vào bát.\n5. Chan nước dùng đang sôi lên trên.",
        "imageUrl": "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=800&q=80"
    },
    {
        "name": "Bún Chả Hà Nội",
        "category": "Phở/Bún",
        "price": 45000.0,
        "recipe": "Nguyên liệu:\n- Bún tươi: 200g\n- Thịt ba chỉ, thịt băm: 200g\n- Nước mắm pha (đường, chanh, tỏi, ớt, đu đủ xanh)\n- Rau sống (xà lách, tía tô, kinh giới)\n\nCách làm:\n1. Ướp thịt với nước hàng, sả, hành khô, nước mắm.\n2. Nướng thịt trên than hoa đến khi xém vàng.\n3. Pha nước chấm chua ngọt.\n4. Ăn kèm bún và rau sống.",
        "imageUrl": "https://images.unsplash.com/photo-1580822184713-fc5400e7fe10?w=800&q=80"
    },
    {
        "name": "Cơm Tấm Sườn Bì",
        "category": "Cơm",
        "price": 45000.0,
        "recipe": "Nguyên liệu:\n- Gạo tấm\n- Sườn cốt lết\n- Bì lợn, thính gạo\n- Nước mắm chua ngọt, mỡ hành\n\nCách làm:\n1. Nấu cơm tấm chín mềm.\n2. Ướp sườn với sả, mật ong, nước tương rồi nướng.\n3. Trộn bì lợn luộc thái chỉ với thính.\n4. Bày ra đĩa, rưới mỡ hành và nước mắm.",
        "imageUrl": "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=800&q=80"
    },
    {
        "name": "Bánh Mì Thịt Nướng",
        "category": "Đồ ăn nhanh",
        "price": 25000.0,
        "recipe": "Nguyên liệu:\n- Bánh mì baguette: 1 ổ\n- Thịt heo nướng: 100g\n- Pate, bơ, dưa chuột, đồ chua, ngò rí\n\nCách làm:\n1. Xẻ bánh mì, phết pate và bơ.\n2. Xếp thịt nướng, dưa chuột, đồ chua vào.\n3. Rưới nước sốt thịt hoặc tương ớt.",
        "imageUrl": "https://images.unsplash.com/photo-1529543544282-ea669407fca3?w=800&q=80"
    },
    {
        "name": "Gà Rán Giòn Tan",
        "category": "Đồ ăn nhanh",
        "price": 60000.0,
        "recipe": "Nguyên liệu:\n- Đùi/cánh gà: 300g\n- Bột chiên giòn, bột chiên xù\n- Trứng gà, sữa tươi\n\nCách làm:\n1. Ngâm gà với sữa tươi cho mềm.\n2. Lăn gà qua bột chiên giòn -> trứng -> bột xù.\n3. Chiên ngập dầu ở 170 độ C đến khi vàng giòn.",
        "imageUrl": "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=800&q=80"
    },
    {
        "name": "Bún Bò Huế",
        "category": "Phở/Bún",
        "price": 50000.0,
        "recipe": "Nguyên liệu:\n- Bún sợi to\n- Chân giò lợn, bắp bò, chả cua\n- Sả, mắm ruốc, sa tế\n\nCách làm:\n1. Hầm xương bò và chân giò với sả.\n2. Nêm mắm ruốc và sa tế tạo màu/vị đặc trưng.\n3. Trụng bún, xếp thịt, chả, chan nước dùng.",
        "imageUrl": "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=800&q=80"
    },
    {
        "name": "Trà Sữa Trân Châu Đường Đen",
        "category": "Đồ uống",
        "price": 35000.0,
        "recipe": "Nguyên liệu:\n- Trà đen: 10g\n- Sữa đặc/Sữa bột\n- Trân châu đen (bột năng, cacao)\n\nCách làm:\n1. Ủ trà lấy cốt.\n2. Luộc trân châu chín tới, ngâm đường đen.\n3. Lắc cốt trà với sữa, đá và thêm trân châu.",
        "imageUrl": "https://images.unsplash.com/photo-1558857563-b37fe4357ef3?w=800&q=80"
    },
    {
        "name": "Bún Đậu Mắm Tôm",
        "category": "Khác",
        "price": 45000.0,
        "recipe": "Nguyên liệu:\n- Bún lá\n- Đậu phụ\n- Thịt chân giò luộc, chả cốm\n- Mắm tôm, chanh, đường, ớt\n\nCách làm:\n1. Chiên giòn đậu phụ và chả cốm.\n2. Thái thịt chân giò mỏng.\n3. Đánh mắm tôm với chanh, đường, mỡ nóng cho sủi bọt.\n4. Bày ra mẹt cùng rau kinh giới, tía tô.",
        "imageUrl": "https://images.unsplash.com/photo-1555126634-323283e090fa?w=800&q=80"
    },
    {
        "name": "Lẩu Thái Tomyum Hải Sản",
        "category": "Lẩu/Nướng",
        "price": 250000.0,
        "recipe": "Nguyên liệu:\n- Nước cốt tomyum, sả, riềng, lá chanh\n- Tôm, mực, ngao, thịt bò\n- Nấm các loại, rau muống\n\nCách làm:\n1. Đun sôi nước dùng với cốt tomyum và gia vị thảo mộc.\n2. Nhúng hải sản, thịt bò và rau nấm khi ăn.",
        "imageUrl": "https://images.unsplash.com/photo-1547928576-a4a33237cbc3?w=800&q=80"
    },
    {
        "name": "Cơm Rang Dưa Bò",
        "category": "Cơm",
        "price": 45000.0,
        "recipe": "Nguyên liệu:\n- Cơm nguội\n- Thịt bò thái mỏng\n- Dưa chua thái nhỏ\n- Hành tây, cà chua, gia vị\n\nCách làm:\n1. Xào thịt bò với dưa chua, đổ ra đĩa.\n2. Rang cơm với trứng cho hạt tơi vàng.\n3. Trộn đều hoặc ăn kèm bò xào dưa.",
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
            # Cập nhật imageUrl và recipe cho món đã có
            for doc in col_ref.where("name", "==", name).limit(1).stream():
                doc.reference.update({
                    "imageUrl": food["imageUrl"],
                    "recipe": food["recipe"],
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
