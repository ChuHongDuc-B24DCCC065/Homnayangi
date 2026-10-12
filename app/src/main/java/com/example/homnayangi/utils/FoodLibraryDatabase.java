package com.example.homnayangi.utils;

import java.util.ArrayList;
import java.util.List;

public class FoodLibraryDatabase {
    public static class LibraryFood {
        public String name;
        public String category;
        public double price;
        public String recipe;
        public String imageUrl;

        public LibraryFood(String name, String category, double price, String recipe) {
            this(name, category, price, recipe, "");
        }

        public LibraryFood(String name, String category, double price, String recipe, String imageUrl) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.recipe = recipe;
            this.imageUrl = imageUrl;
        }
    }

    public static List<LibraryFood> getFoods() {
        List<LibraryFood> list = new ArrayList<>();
        list.add(new LibraryFood("Phở Bò", "Phở/Bún", 50000, "Nguyên liệu:\n- Bánh phở: 150g\n- Thịt bò (nạm, gầu, tái): 100g\n- Nước dùng xương bò, quế, hồi, thảo quả\n- Hành lá, rau mùi, giá đỗ\n\nCách làm:\n1. Hầm xương bò với gia vị 4-6 tiếng.\n2. Chần bánh phở qua nước sôi.\n3. Thái thịt bò mỏng, chần qua nước dùng.\n4. Xếp phở, thịt, hành vào bát.\n5. Chan nước dùng đang sôi lên trên.", "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=800&q=80"));
        list.add(new LibraryFood("Bún Chả", "Phở/Bún", 45000, "Nguyên liệu:\n- Bún tươi: 200g\n- Thịt ba chỉ, thịt băm: 200g\n- Nước mắm pha (đường, chanh, tỏi, ớt, đu đủ xanh)\n- Rau sống (xà lách, tía tô, kinh giới)\n\nCách làm:\n1. Ướp thịt với nước hàng, sả, hành khô, nước mắm.\n2. Nướng thịt trên than hoa đến khi xém vàng.\n3. Pha nước chấm chua ngọt.\n4. Ăn kèm bún và rau sống.", "https://images.unsplash.com/photo-1580822184713-fc5400e7fe10?w=800&q=80"));
        list.add(new LibraryFood("Cơm Tấm Sườn Bì", "Cơm", 40000, "Nguyên liệu:\n- Gạo tấm\n- Sườn cốt lết\n- Bì lợn, thính gạo\n- Nước mắm chua ngọt, mỡ hành\n\nCách làm:\n1. Nấu cơm tấm chín mềm.\n2. Ướp sườn với sả, mật ong, nước tương rồi nướng.\n3. Trộn bì lợn luộc thái chỉ với thính.\n4. Bày ra đĩa, rưới mỡ hành và nước mắm.", "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=800&q=80"));
        list.add(new LibraryFood("Bánh Mì Thịt Nướng", "Đồ ăn nhanh", 25000, "Nguyên liệu:\n- Bánh mì baguette: 1 ổ\n- Thịt heo nướng: 100g\n- Pate, bơ, dưa chuột, đồ chua, ngò rí\n\nCách làm:\n1. Xẻ bánh mì, phết pate và bơ.\n2. Xếp thịt nướng, dưa chuột, đồ chua vào.\n3. Rưới nước sốt thịt hoặc tương ớt.", "https://images.unsplash.com/photo-1529543544282-ea669407fca3?w=800&q=80"));
        list.add(new LibraryFood("Gà Rán", "Đồ ăn nhanh", 60000, "Nguyên liệu:\n- Đùi/cánh gà: 300g\n- Bột chiên giòn, bột chiên xù\n- Trứng gà, sữa tươi\n\nCách làm:\n1. Ngâm gà với sữa tươi cho mềm.\n2. Lăn gà qua bột chiên giòn -> trứng -> bột xù.\n3. Chiên ngập dầu ở 170 độ C đến khi vàng giòn.", "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=800&q=80"));
        list.add(new LibraryFood("Bún Bò Huế", "Phở/Bún", 50000, "Nguyên liệu:\n- Bún sợi to\n- Chân giò lợn, bắp bò, chả cua\n- Sả, mắm ruốc, sa tế\n\nCách làm:\n1. Hầm xương bò và chân giò với sả.\n2. Nêm mắm ruốc và sa tế tạo màu/vị đặc trưng.\n3. Trụng bún, xếp thịt, chả, chan nước dùng.", "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=800&q=80"));
        list.add(new LibraryFood("Trà Sữa Trân Châu", "Đồ uống", 35000, "Nguyên liệu:\n- Trà đen: 10g\n- Sữa đặc/Sữa bột\n- Trân châu đen (bột năng, cacao)\n\nCách làm:\n1. Ủ trà lấy cốt.\n2. Luộc trân châu chín tới, ngâm đường đen.\n3. Lắc cốt trà với sữa, đá và thêm trân châu.", "https://images.unsplash.com/photo-1558857563-b37fe4357ef3?w=800&q=80"));
        list.add(new LibraryFood("Bún Đậu Mắm Tôm", "Khác", 45000, "Nguyên liệu:\n- Bún lá\n- Đậu phụ\n- Thịt chân giò luộc, chả cốm\n- Mắm tôm, chanh, đường, ớt\n\nCách làm:\n1. Chiên giòn đậu phụ và chả cốm.\n2. Thái thịt chân giò mỏng.\n3. Đánh mắm tôm với chanh, đường, mỡ nóng cho sủi bọt.\n4. Bày ra mẹt cùng rau kinh giới, tía tô.", "https://images.unsplash.com/photo-1555126634-323283e090fa?w=800&q=80"));
        list.add(new LibraryFood("Lẩu Thái Tomyum", "Lẩu/Nướng", 250000, "Nguyên liệu:\n- Nước cốt tomyum, sả, riềng, lá chanh\n- Tôm, mực, ngao, thịt bò\n- Nấm các loại, rau muống\n\nCách làm:\n1. Đun sôi nước dùng với cốt tomyum và gia vị thảo mộc.\n2. Nhúng hải sản, thịt bò và rau nấm khi ăn.", "https://images.unsplash.com/photo-1547928576-a4a33237cbc3?w=800&q=80"));
        list.add(new LibraryFood("Cơm Rang Dưa Bò", "Cơm", 45000, "Nguyên liệu:\n- Cơm nguội\n- Thịt bò thái mỏng\n- Dưa chua thái nhỏ\n- Hành tây, cà chua, gia vị\n\nCách làm:\n1. Xào thịt bò với dưa chua, đổ ra đĩa.\n2. Rang cơm với trứng cho hạt tơi vàng.\n3. Trộn đều hoặc ăn kèm bò xào dưa.", "https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=800&q=80"));
        return list;
    }
}
