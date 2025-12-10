package com.bkap.aispark.security;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategorySafetyService {

    // Danh sách từ khóa cấm (có thể mở rộng)
    private static final List<String> bannedWords = List.of(

    // =========================
    // 1. TỤC TĨU / CHỬI THỀ
    // =========================
    "dm", "đm", "dm", "dmm", "đmm",
    "dkm", "đkm", "dkmm", "đkm", "địt", "dit",
    "đụ", "đu má", "đu me", "đụ mẹ", "du me",
    "mẹ mày", "me may", "bố mày", "bo may",
    "vkl", "vl", "vcl", "vcc", "clgt", "wtf",
    "lol", "lồn", "lon", "cặc", "cak", "cu", "chim",
    "đụ má", "đu má", "khốn", "khon", "khốn nạn", "khon nan",
    "láo", "lao", "láo toét",
    "óc chó", "oc cho", "não phẳng", "ngáo", "ngao",
    "đồ điên", "do dien", "ngu", "ngu vãi", "đần", "dan",

    // =========================
    // 2. SEX / 18+ / KHIÊU DÂM
    // =========================
    "sex", "sexy", "sexual", "porn", "porno", "pornhub",
    "hentai", "xxx", "18+", "jav", "fap", "fetish", "bdsm",
    "threesome", " blowjob", "handjob",
    "nude", "nudes", "naked",

    "tình dục", "tinh duc", "quan hệ", "quan he",
    "kích dục", "kich duc", "khoả thân", "khoa than",
    "mát mẻ", "mat me", "gợi cảm", "goi cam",
    "khiêu dâm", "khieu dam", "thủ dâm", "thu dam",

    // Bộ phận nhạy cảm
    "vú", "vu", "ngực", "nguc", "mông", "mong",
    "háng", "hang", "âm đạo", "am dao",
    "dương vật", "duong vat",

    // =========================
    // 3. BẠO LỰC / GIẾT NGƯỜI
    // =========================
    "giết", "giet", "giết người", "giet nguoi",
    "đâm chết", "dam chet", "đánh nhau", "danh nhau",
    "chém", "chem", "xẻ thịt", "xe thit",
    "tàn sát", "tan sat",
    "blood", "máu", "mau",
    "kill", "killer", "murder", "violent",

    // =========================
    // 4. TỰ TỬ / HẠI BẢN THÂN
    // =========================
    "tự tử", "tu tu", "tự sát", "tu sat",
    "nhảy lầu", "nhay lau", "nhảy cầu", "nhay cau",
    "suicide", "self-harm", "kill myself",

    // =========================
    // 5. MA TÚY / CHẤT CẤM
    // =========================
    "ma túy", "ma tuy", "thuốc lắc", "thuoc lac",
    "cần sa", "can sa", "heroin", "cocaine",
    "ketamine", "ke", "đập đá", "dap da",
    "hàng trắng", "hang trang",
    "drug", "drugs", "weed", "ecstasy",

    // =========================
    // 6. CỜ BẠC / ĐÁ GÀ / LÔ ĐỀ
    // =========================
    "cờ bạc", "co bac", "lô đề", "lo de", "đánh đề", "danh de",
    "casino", "bet", "gambling", "cược", "cuoc",
    "đá gà", "da ga", "đánh bạc", "danh bac",

    // =========================
    // 7. LỪA ĐẢO / HACK / VI PHẠM PHÁP LUẬT
    // =========================
    "lừa đảo", "lua dao", "scam", "fraud",
    "hack", "hacker", "crack", "phá khóa", "pha khoa",
    "phốt", "phot", "bóc phốt", "boc phot",

    // =========================
    // 8. KỲ THỊ / XÚC PHẠM / HATE SPEECH
    // =========================
    "racist", "hate", "thù ghét", "thu ghet",
    "kỳ thị", "ky thi", "phân biệt", "phan biet",
    "dân tộc", "dan toc" /* dùng cẩn thận */,

    // =========================
    // 9. NGÔN TỪ NHẠY CẢM KHÁC
    // =========================
    "đĩ", "di", "điếm", "diem", "gái gọi", "gai goi",
    "mại dâm", "mai dam",
    "biến thái", "bien thai",
    "sờ mó", "so mo",
    "hiếp", "hiep", "cưỡng hiếp", "cuong hiep",

    // =========================
    // 10. TỪ VIẾT TẮT LÁCH LUẬT
    // =========================
    "fz", "f*z", "s*x", "sx", "p0rn", "p0rn0",
    "f*ck", "fck", "fuk", "fvk",
    "dmz", "dcm", "dmm", "clm",
    "v*l", "v*cl", "l*n", "c*c",

    // =========================
    // 11. TỪ LÓNG HỌC SINH VIỆT NAM
    // =========================
    "ăn c**", "ăn cứt", "an cut",
    "vãi", "vai", "vãi l*n", "vai ln",
    "duma", "đuma", "dume", "đume",
    "mả cha", "ma cha",
    "dơ bẩn", "bẩn thỉu"
);


    /** 
     * Kiểm tra danh mục có chứa từ khóa cấm không
     * Nếu chứa → ném lỗi
     */
    public void validate(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Tên danh mục không hợp lệ!");
        }

        String lower = name.toLowerCase();

        for (String bad : bannedWords) {
            if (lower.contains(bad)) {
                throw new RuntimeException("Tên danh mục không phù hợp với môi trường học đường.");
            }
        }
    }
}
