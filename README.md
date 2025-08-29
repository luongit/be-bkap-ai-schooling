# 1. Hướng dẫn build dự án
**Quy trình chuẩn, gọn – an toàn** để làm việc nhánh/PR trên GitHub, đảm bảo master luôn sạch và hạn chế xung đột giữa các nhóm khác.

# 0) Nguyên tắc chung

* **Không commit trực tiếp lên `master`**. Mọi thay đổi đều qua **feature branch → Pull Request (PR)**.
* **Nhánh nhỏ, sống ngắn** (1–5 ngày), PR nhỏ (≤ 300 dòng) để review nhanh, ít conflict.
* Luôn **đồng bộ feature branch với `master` bằng `rebase`** trước khi mở/ cập nhật PR.


### Bước 1: Lấy code mới nhất từ `master`
**Nếu lần đầu tiên thì dùng lệnh clone**

```bash
git clone https://github.com/luongit/fe-bkap-ai-schooling
```

### Bước 2: Cài đặt dự án (Chỉ dành cho lần đàu clone dự án)
**cd và thưc mục gốc của dự án nếu chwua ử thư mục gốc**
```bash
cd fe-bkap-ai-schooling
```
Sau đó chạy tiếp lệnh cài thư viện
```bash
npm install
Hoặc: npm i
```

Tiếp theo: Chạy dự án
```bash
npm run start
```
Trình duyệt sẽ tự mở trình duyệt lên: http://127.0.0.1:3000


# 2. Hướng dẫn push dự án và merge vào nhánh chính

**Nếu đã phát triển xong task của mình, muốn đẩy lên**
Bước 1: Pull code mới nhất từ `master`
> Lệnh pull cũng thường dùng trước khi đẩy dự án lên để đảm bảo là không bị xung đột phiên bản
```bash
git fetch origin
git checkout master
git pull --ff-only origin master
```

Bước 2: Tại thư mục gốc dưới local, chạy lệnh sau
- Tạo nhánh `mỗi một task nên tạo một nhánh riêng`, VD task ngày `AI001-25-08-2025`
Lệnh tạo nhánh
```bash
git branch AI001-25-08-2025
```

Hoặc tạo và di chuyển vào nhánh luôn
```bash
git checkout -b AI001-25-08-2025
```

### Bước 2: Thực hiện code, commit thay đổi
```bash
git add .
```

**Tiếp theo là commmit**

```bash
git commit -m "Mô tả về task đang làm"
```

### Bước 3: Push nhánh mới lên remote lần đầu (tạo nhánh trên GitHub)

```bash
git push -u origin AI001-25-08-2025
```
**👉 Sau bước này, remote sẽ có thêm nhánh AI001-25-08-2025, và lần sau bạn chỉ cần:**

```bash
git push
```


# 3) Đồng bộ với `master` để tránh conflict về sau

> Làm thường xuyên (mỗi ngày/ trước khi push):

```bash
git fetch origin
git rebase origin/master
# nếu có conflict: sửa -> git add <file> -> git rebase --continue
# nếu muốn hủy rebase: git rebase --abort
```

# 4) Push nhánh và mở Pull Request

```bash
git push -u origin AI001-25-08-2025
```

Lên GitHub mở **PR vào `master`**:

* Title rõ ràng (mapping ticket/module).
* Mô tả: mục tiêu, thay đổi chính, ảnh/chụp màn hình nếu là UI, checklist test.
* Gắn reviewers, labels, project/milestone nếu dùng.

# 5) Cập nhật PR khi nhóm trưởng (hoặc người khác) merge code mới vào master

Khi PR còn mở, cứ **rebase lại trên `master`** để giữ lịch sử sạch:

```bash
git fetch origin
git checkout AI001-25-08-2025
git rebase origin/master
git push --force-with-lease
```

> Dùng `--force-with-lease` (thay vì `--force`) để an toàn, tránh ghi đè nhầm thay đổi người khác.

# 6) Review → CI pass → Merge

* Yêu cầu **≥1 review chấp thuận** và **CI xanh**.
* Cách merge khuyến nghị:

  * **Squash & merge**: gộp commit lặt vặt thành 1 commit sạch trên master (dễ đọc lịch sử).
  * Hoặc **Rebase & merge**: giữ từng commit nhưng vẫn linear (yêu cầu commit đã gọn gàng).
* Sau merge:

```bash
git checkout master
git pull --ff-only origin master
git branch -d AI001-25-08-2025
git push origin --delete AI001-25-08-2025   # dọn remote branch
```

# 7) Ra phiên bản (tùy dự án)

* Tag & release:

```bash
git tag -a v1.2.0 -m "Release v1.2.0 - Module AI001"
git push origin v1.2.0
```

---


# Tóm tắt lệnh cốt lõi (copy dùng hằng ngày)

```bash
# cập nhật master
git fetch origin
git checkout master
git pull --ff-only origin master

# tạo nhánh làm việc
git checkout -b AI001-25-08-2025

# làm việc & commit
git add .
git commit -m "AI001: <nội dung>"

# rebase với master trước khi push/ update PR
git fetch origin
git rebase origin/master

# push & mở PR
git push -u origin AI001-25-08-2025

# nếu master đổi, update PR
git fetch origin
git rebase origin/master
git push --force-with-lease
```