#!/bin/bash

# Fetch code mới nhất từ remote
git fetch origin

# Cập nhật master
git pull --ff-only origin master

# Xóa branch cũ nếu có
git branch -D Chung 2>/dev/null

# Checkout branch Chung mới
git checkout -b Chung

# Add toàn bộ thay đổi
git add .

# Commit với message truyền vào
git commit -m "$1"

# Push lên remote
git push origin -u Chung
