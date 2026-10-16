package com.example.homnayangi.utils;

import java.util.ArrayList;
import java.util.List;

public class FoodLibraryDatabase {
    public static class LibraryFood {
        public String name;
        public String category;
        public double price;
        public String note;
        public String imageUrl;

        public LibraryFood(String name, String category, double price, String note) {
            this(name, category, price, note, "");
        }

        public LibraryFood(String name, String category, double price, String note, String imageUrl) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.note = note;
            this.imageUrl = imageUrl;
        }
    }

    public static List<LibraryFood> getFoods() {
        List<LibraryFood> list = new ArrayList<>();
        list.add(new LibraryFood("Phở Bò", "Phở/Bún", 50000, "Nước dùng thanh ngọt từ xương bò hầm, thịt tái mềm. Ăn kèm quẩy, rau thơm và tương ớt.", "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=800&q=80"));
        list.add(new LibraryFood("Bún Chả", "Phở/Bún", 45000, "Chả nướng than hoa thơm lừng, ăn kèm bún tươi và nước mắm chua ngọt. Đặc sản Hà Nội.", "https://images.unsplash.com/photo-1580822184713-fc5400e7fe10?w=800&q=80"));
        list.add(new LibraryFood("Cơm Tấm Sườn Bì", "Cơm", 40000, "Sườn nướng đậm đà, bì giòn thính thơm, mỡ hành béo ngậy. Món sáng Sài Gòn.", "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=800&q=80"));
        list.add(new LibraryFood("Bánh Mì Thịt Nướng", "Đồ ăn nhanh", 25000, "Bánh mì giòn, thịt nướng mềm, pate béo, đồ chua thanh mát. Street food Việt Nam.", "https://images.unsplash.com/photo-1529543544282-ea669407fca3?w=800&q=80"));
        list.add(new LibraryFood("Gà Rán", "Đồ ăn nhanh", 60000, "Gà tẩm bột chiên giòn rụm, thịt mềm ngọt bên trong. Ăn kèm cơm hoặc khoai tây.", "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=800&q=80"));
        list.add(new LibraryFood("Bún Bò Huế", "Phở/Bún", 50000, "Nước dùng cay nồng đặc trưng với sả và mắm ruốc. Có chả cua và bắp bò.", "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=800&q=80"));
        list.add(new LibraryFood("Trà Sữa Trân Châu", "Đồ uống", 35000, "Trà đen béo ngậy kết hợp trân châu đường đen dai giòn. Thức uống hot nhất.", "https://images.unsplash.com/photo-1558857563-b37fe4357ef3?w=800&q=80"));
        list.add(new LibraryFood("Bún Đậu Mắm Tôm", "Khác", 45000, "Đậu phụ chiên giòn, chả cốm, thịt luộc chấm mắm tôm sủi bọt. Hà Nội vibe.", "https://images.unsplash.com/photo-1555126634-323283e090fa?w=800&q=80"));
        list.add(new LibraryFood("Lẩu Thái Tomyum", "Lẩu/Nướng", 250000, "Nước lẩu chua cay đậm đà với hải sản tươi, nấm và rau. Ăn nhóm 2-4 người.", "https://images.unsplash.com/photo-1547928576-a4a33237cbc3?w=800&q=80"));
        list.add(new LibraryFood("Cơm Rang Dưa Bò", "Cơm", 45000, "Cơm rang tơi với thịt bò xào dưa chua. Đơn giản mà ngon, no bụng.", "https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=800&q=80"));
        // 📌 Thêm 20 món ăn mới vào thư viện
        list.add(new LibraryFood("Mì Quảng", "Phở/Bún", 40000, "Mì sợi vàng đặc trưng, nước dùng sệt với tôm, thịt, đậu phụng. Đặc sản miền Trung.", ""));
        list.add(new LibraryFood("Cao Lầu", "Phở/Bún", 45000, "Sợi mì dai giòn đặc trưng Hội An, thịt xá xíu, rau sống và bánh tráng giòn.", ""));
        list.add(new LibraryFood("Hủ Tiếu Nam Vang", "Phở/Bún", 45000, "Nước dùng trong ngọt, sợi hủ tiếu mềm dai, tôm, thịt bằm và gan heo.", ""));
        list.add(new LibraryFood("Cơm Gà Hải Nam", "Cơm", 50000, "Cơm nấu nước dùng gà thơm, gà luộc mềm da giòn, nước chấm gừng đặc biệt.", ""));
        list.add(new LibraryFood("Cơm Sườn Nướng", "Cơm", 45000, "Sườn heo ướp đậm đà nướng than, ăn kèm cơm trắng, dưa leo và nước mắm.", ""));
        list.add(new LibraryFood("Xôi Gà", "Cơm", 30000, "Xôi nếp dẻo thơm với gà xé phay, hành phi vàng giòn. Bữa sáng quen thuộc.", ""));
        list.add(new LibraryFood("Bánh Cuốn", "Khác", 30000, "Bánh cuốn nóng mỏng mềm, nhân thịt nấm, chấm nước mắm pha chua ngọt.", ""));
        list.add(new LibraryFood("Bánh Xèo", "Khác", 35000, "Bánh giòn vàng, nhân tôm thịt giá đỗ, cuốn rau sống chấm nước mắm.", ""));
        list.add(new LibraryFood("Gỏi Cuốn", "Khác", 25000, "Bánh tráng cuốn tôm, thịt, bún, rau sống. Chấm tương đậu phộng béo ngậy.", ""));
        list.add(new LibraryFood("Nem Rán", "Khác", 30000, "Chả giò giòn rụm nhân thịt, miến, nấm mèo. Món khai vị quen thuộc.", ""));
        list.add(new LibraryFood("Pizza", "Đồ ăn nhanh", 89000, "Đế bánh giòn xốp, phô mai kéo sợi, topping đa dạng. Ăn nhóm 2-3 người.", ""));
        list.add(new LibraryFood("Hamburger", "Đồ ăn nhanh", 55000, "Bánh mỳ tròn mềm, patty thịt bò, rau xà lách, cà chua và sốt đặc biệt.", ""));
        list.add(new LibraryFood("Cháo Sườn", "Khác", 30000, "Cháo nấu nhừ mịn với sườn non mềm, hành phi thơm. Ấm bụng buổi sáng.", ""));
        list.add(new LibraryFood("Bún Riêu Cua", "Phở/Bún", 40000, "Nước dùng chua thanh từ cà chua, riêu cua đồng thơm ngậy. Ăn kèm rau sống.", ""));
        list.add(new LibraryFood("Bún Thịt Nướng", "Phở/Bún", 40000, "Thịt nướng thơm lừng trên bún tươi, rau sống và nước mắm pha. Món miền Nam.", ""));
        list.add(new LibraryFood("Nước Ép Cam", "Đồ uống", 25000, "Cam tươi vắt nguyên chất, bổ sung vitamin C. Tươi mát giải nhiệt.", ""));
        list.add(new LibraryFood("Cà Phê Sữa Đá", "Đồ uống", 25000, "Cà phê phin truyền thống, sữa đặc ngọt, đá viên mát lạnh. Đặc trưng Việt Nam.", ""));
        list.add(new LibraryFood("Sinh Tố Bơ", "Đồ uống", 30000, "Bơ chín béo ngậy xay nhuyễn với sữa đặc và đá. Bổ dưỡng giải khát.", ""));
        list.add(new LibraryFood("Chè Bưởi", "Tráng miệng", 20000, "Cùi bưởi giòn dai nấu nước cốt dừa béo ngậy, bột báng dai. Tráng miệng mát.", ""));
        list.add(new LibraryFood("Kem Bơ", "Tráng miệng", 20000, "Kem bơ mịn béo tan ngay trong miệng. Tráng miệng hoàn hảo sau bữa ăn.", ""));
        return list;
    }
}
