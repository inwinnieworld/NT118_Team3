# 🔄 Git Workflow - Quy trình làm việc an toàn

## 📋 Tình huống hiện tại
- Bạn đang ở nhánh `AnAn` (7 commits ahead of main)
- Muốn test merge trước khi merge vào `main`
- Làm việc local trước, push sau

---

## 🎯 Quy trình chuẩn (Local Testing)

### Bước 1: Backup công việc hiện tại
```bash
# Đảm bảo tất cả thay đổi đã được commit
git status

# Nếu có file chưa commit, commit ngay
git add .
git commit -m "feat: Complete staff dashboard with position-based access control"
```

### Bước 2: Tạo nhánh test để thử merge
```bash
# Tạo nhánh test từ nhánh hiện tại (AnAn)
git checkout -b test-merge-staff-dashboard

# Hoặc nếu đã có nhánh test, xóa và tạo lại
git branch -D test-merge-staff-dashboard
git checkout -b test-merge-staff-dashboard
```

### Bước 3: Fetch latest từ remote (không merge)
```bash
# Lấy thông tin mới nhất từ GitHub (không ảnh hưởng local)
git fetch origin

# Xem các nhánh remote
git branch -r
```

### Bước 4: Merge main vào nhánh test (Test conflicts)
```bash
# Merge main vào nhánh test để xem có conflict không
git merge origin/main

# Nếu có conflict, sẽ hiện:
# CONFLICT (content): Merge conflict in <file>
# Automatic merge failed; fix conflicts and then commit the result.
```

### Bước 5: Xử lý conflicts (nếu có)
```bash
# Xem các file bị conflict
git status

# Mở file conflict và sửa thủ công
# Tìm các dòng:
# <<<<<<< HEAD
# (code của bạn)
# =======
# (code từ main)
# >>>>>>> origin/main

# Sau khi sửa xong
git add <file-đã-sửa>
git commit -m "fix: Resolve merge conflicts"
```

### Bước 6: Test ứng dụng sau merge
```bash
# Test backend
cd backend
npm install
npm start

# Test Android (build project)
# Open Android Studio và build project
# Chạy app trên emulator/device
```

### Bước 7: Nếu test OK - Merge thật vào main
```bash
# Quay về nhánh AnAn
git checkout AnAn

# Merge main vào AnAn (để cập nhật)
git merge origin/main

# Xử lý conflict nếu có (giống bước 5)

# Push AnAn lên GitHub
git push origin AnAn

# Tạo Pull Request trên GitHub: AnAn -> main
# Hoặc merge trực tiếp local:
git checkout main
git pull origin main
git merge AnAn
git push origin main
```

### Bước 8: Dọn dẹp nhánh test
```bash
# Xóa nhánh test local
git branch -D test-merge-staff-dashboard
```

---

## 🚨 Quy trình an toàn hơn (Recommended)

### Option A: Sử dụng Git Stash (nếu chưa commit)
```bash
# Lưu tạm công việc hiện tại
git stash save "WIP: Staff dashboard changes"

# Chuyển sang main để test
git checkout main
git pull origin main

# Tạo nhánh test
git checkout -b test-merge
git merge AnAn

# Test xong, quay lại
git checkout AnAn
git stash pop
```

### Option B: Sử dụng Git Worktree (Advanced)
```bash
# Tạo worktree riêng để test (không ảnh hưởng code hiện tại)
git worktree add ../emotion-debugging-test main

# Vào thư mục test
cd ../emotion-debugging-test

# Merge AnAn vào đây để test
git merge AnAn

# Test xong, xóa worktree
cd ../emotion-debugging
git worktree remove ../emotion-debugging-test
```

---

## 📊 Checklist trước khi merge

- [ ] Tất cả code đã commit
- [ ] Không có file uncommitted quan trọng
- [ ] Đã test merge trên nhánh test
- [ ] Không có conflict hoặc đã resolve
- [ ] Backend chạy OK sau merge
- [ ] Android app build thành công
- [ ] Tất cả features hoạt động đúng
- [ ] Database schema tương thích

---

## 🔍 Commands hữu ích

### Xem lịch sử commit
```bash
# Xem graph các nhánh
git log --oneline --graph --all --decorate

# Xem commits khác biệt giữa AnAn và main
git log main..AnAn
```

### Xem thay đổi giữa các nhánh
```bash
# Xem files khác nhau
git diff main..AnAn --name-only

# Xem chi tiết thay đổi
git diff main..AnAn
```

### Hủy merge nếu có vấn đề
```bash
# Nếu đang trong quá trình merge và muốn hủy
git merge --abort

# Quay về commit trước đó
git reset --hard HEAD~1
```

### Backup an toàn
```bash
# Tạo tag backup trước khi merge
git tag backup-before-merge-$(date +%Y%m%d)

# Restore từ backup nếu cần
git reset --hard backup-before-merge-20260421
```

---

## 🎯 Quy trình đề xuất cho bạn

Dựa vào ảnh GitHub, bạn đang ở nhánh `AnAn` với 7 commits ahead. Đây là quy trình tôi đề xuất:

```bash
# 1. Commit tất cả thay đổi hiện tại
git add .
git commit -m "feat: Add staff position-based access control and complete database redesign"

# 2. Tạo nhánh test
git checkout -b test-merge-to-main

# 3. Fetch và merge main
git fetch origin
git merge origin/main

# 4. Nếu có conflict, resolve và commit
# (Xem bước 5 ở trên)

# 5. Test ứng dụng
# - Start backend: cd backend && npm start
# - Build Android app
# - Test login với các roles
# - Test database

# 6. Nếu OK, quay về AnAn và push
git checkout AnAn
git push origin AnAn

# 7. Tạo Pull Request trên GitHub
# Vào GitHub -> Pull Requests -> New Pull Request
# Base: main <- Compare: AnAn
# Review changes -> Create Pull Request

# 8. Sau khi merge PR, pull main về local
git checkout main
git pull origin main

# 9. Xóa nhánh AnAn (nếu không cần nữa)
git branch -d AnAn
git push origin --delete AnAn
```

---

## ⚠️ Lưu ý quan trọng

1. **Không bao giờ force push lên main**: `git push -f origin main` ❌
2. **Luôn backup trước khi merge**: Tạo tag hoặc branch backup
3. **Test kỹ sau merge**: Backend + Android + Database
4. **Resolve conflicts cẩn thận**: Đọc kỹ cả 2 phiên bản code
5. **Commit message rõ ràng**: Dùng conventional commits (feat:, fix:, refactor:)

---

## 📚 Tài liệu tham khảo

- [Git Branching Strategy](https://www.atlassian.com/git/tutorials/comparing-workflows)
- [Resolving Merge Conflicts](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/addressing-merge-conflicts)
- [Git Best Practices](https://www.git-tower.com/learn/git/ebook/en/command-line/appendix/best-practices)
