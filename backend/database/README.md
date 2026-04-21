# Database Setup Guide

## 📋 Overview

Database được thiết kế lại hoàn toàn từ đầu với tất cả các chức năng:
- ✅ Authentication (Register, Login, Forgot/Reset Password)
- ✅ Role-based Access Control (Student, Staff, Admin)
- ✅ Profile Management
- ✅ Admin Dashboard (Manage Students, Staff)
- ✅ Staff Dashboard (Quest, Reports, Trace Error)
- ✅ Git Commit Journal (Emotions, Commits, Merges, Alerts)

---

## 🗂️ Database Structure

### Core Tables:
1. **users** - Core user authentication (with `role` column)
2. **students** - Student-specific data
3. **staff** - Staff-specific data
4. **admins** - Admin-specific data
5. **password_reset_tokens** - Password reset tokens

### Git Journal Tables:
6. **emotions** - 15 emotions (POSITIVE, NEGATIVE, NEUTRAL)
7. **commits** - Daily emotion commits
8. **daily_merges** - Daily emotion summaries (WEA algorithm)
9. **severity_alerts** - Alert history for high negative emotions

---

## 🚀 Setup Instructions

### Step 1: Generate Password Hash

```bash
cd backend
node database/generate_seed_with_hash.js
```

Copy the generated hash and replace all `password_hash` values in `complete_seed.sql`.

### Step 2: Create Database

```bash
mysql -u root -p
```

```sql
CREATE DATABASE IF NOT EXISTS emotion_debugging CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE emotion_debugging;
```

### Step 3: Run Schema

```bash
mysql -u root -p emotion_debugging < backend/database/complete_schema.sql
```

### Step 4: Run Seed Data

**IMPORTANT:** First update the password hashes in `complete_seed.sql` with the hash from Step 1!

```bash
mysql -u root -p emotion_debugging < backend/database/complete_seed.sql
```

---

## 🧪 Test Credentials

| Role | Email/Student Code | Password | Status |
|------|-------------------|----------|--------|
| **Admin** | admin@uit.edu.vn | password123 | Active |
| **Staff** | thangda@uit.edu.vn | password123 | Active |
| **Staff** | binhle@uit.edu.vn | password123 | Active |
| **Student** | an.nguyen@student.uit.edu.vn | password123 | Active |
| **Student** | 21520001 | password123 | Active |
| **Student** | binh.tran@student.uit.edu.vn | password123 | Active |
| **Student** | 21520002 | password123 | Active |
| **Student** | cuong.le@student.uit.edu.vn | password123 | Active |
| **Student** | dung.pham@student.uit.edu.vn | password123 | Active |
| **Student** | em.hoang@student.uit.edu.vn | password123 | **LOCKED** |

---

## 📊 Sample Data Included

### Users:
- 1 Admin
- 2 Staff members
- 5 Students (1 locked)

### Git Journal Data:
- **Student 1 (An)**: 
  - 16 commits over 4 days
  - Mixed emotions (positive, negative, neutral)
  - 2 daily merges
  - 1 severity alert (Day 2 had high negative emotions)
  
- **Student 2 (Bình)**:
  - 5 commits (all positive)
  - 1 daily merge
  
- **Student 3 (Cường)**:
  - 4 commits (neutral - studying)

### Emotions:
- 15 emotions total
- 6 NEGATIVE: Ác Quỷ, Buồn Một Chút, Buồn Nhiều Chút, Hối Lỗi, Hơi Quạo, Khinh Bỉ
- 6 POSITIVE: Chúa Hề, Háo Hức, LMAO, Thiên Thần, Vui Vẻ, Yêu Thương
- 3 NEUTRAL: Buồn Ngủ, Suy Ngẫm, Ý Kiến

---

## 🔍 Test Scenarios

### 1. Test Login
```bash
# Admin login
POST /api/auth/login
{
  "account": "admin@uit.edu.vn",
  "password": "password123"
}

# Student login with email
POST /api/auth/login
{
  "account": "an.nguyen@student.uit.edu.vn",
  "password": "password123"
}

# Student login with student code
POST /api/auth/login
{
  "account": "21520001",
  "password": "password123"
}
```

### 2. Test Git Journal
```bash
# Get all emotions
GET /api/gitjournal/emotions

# Get student commits
GET /api/gitjournal/commits?student_id=1&branch_type=main

# Create new commit
POST /api/gitjournal/commits
{
  "student_id": 1,
  "emotion_id": 8,
  "branch_type": "main",
  "intensity_level": 75,
  "message": "Test commit"
}

# Get severity alerts
GET /api/gitjournal/alerts?student_id=1
```

### 3. Test Admin Functions
```bash
# Get all students (paginated)
GET /api/admin/students?page=1&limit=10

# Update student
PUT /api/admin/students/1
{
  "name": "Updated Name",
  "major": "New Major"
}

# Lock/Unlock student
PUT /api/admin/students/1/toggle-lock
```

### 4. Test Password Reset
```bash
# Request reset
POST /api/auth/forgot-password
{
  "email": "an.nguyen@student.uit.edu.vn"
}

# Validate token
POST /api/auth/validate-reset-token
{
  "token": "valid_token_67890"
}

# Reset password
POST /api/auth/reset-password
{
  "token": "valid_token_67890",
  "newPassword": "newpassword123"
}
```

---

## 🔧 Maintenance Scripts

### Reset Database (Clean slate)
```bash
mysql -u root -p emotion_debugging < backend/database/complete_schema.sql
mysql -u root -p emotion_debugging < backend/database/complete_seed.sql
```

### Backup Database
```bash
mysqldump -u root -p emotion_debugging > backup_$(date +%Y%m%d_%H%M%S).sql
```

### Check Database Status
```sql
USE emotion_debugging;

-- Check all tables
SHOW TABLES;

-- Check user counts
SELECT role, COUNT(*) as count FROM users GROUP BY role;

-- Check commit counts
SELECT s.student_code, COUNT(c.commit_id) as commits
FROM students s
LEFT JOIN commits c ON s.student_id = c.student_id
GROUP BY s.student_id;

-- Check severity alerts
SELECT * FROM severity_alerts WHERE is_acknowledged = FALSE;
```

---

## 📝 Notes

1. **Password Hashing**: All passwords are hashed with bcrypt (10 rounds)
2. **Role Column**: Added to `users` table for easy role-based access control
3. **Indexes**: Optimized indexes for common queries
4. **Foreign Keys**: CASCADE delete for user-related data
5. **JSON Fields**: `emotion_stats` in `daily_merges` stores WEA algorithm results
6. **Timestamps**: All tables have `created_at` and `updated_at` where applicable

---

## 🐛 Troubleshooting

### Error: "Table already exists"
```bash
# Drop all tables first
mysql -u root -p emotion_debugging < backend/database/complete_schema.sql
```

### Error: "Cannot add foreign key constraint"
```bash
# Make sure to run schema before seed
# Schema creates tables in correct order
```

### Error: "Duplicate entry"
```bash
# Clear all data first
mysql -u root -p emotion_debugging -e "SET FOREIGN_KEY_CHECKS=0; TRUNCATE TABLE users; SET FOREIGN_KEY_CHECKS=1;"
```

---

## 📚 Related Files

- `complete_schema.sql` - Full database schema (DROP + CREATE)
- `complete_seed.sql` - Test data for all features
- `generate_seed_with_hash.js` - Generate bcrypt hash for passwords
- `add_role_to_users.sql` - Migration to add role column (deprecated, use complete_schema.sql)
- `git_journal_schema.sql` - Old Git Journal schema (deprecated)
- `git_journal_seed.sql` - Old Git Journal seed (deprecated)

---

## ✅ Verification Checklist

After setup, verify:
- [ ] 9 tables created successfully
- [ ] 8 users inserted (1 admin, 2 staff, 5 students)
- [ ] 15 emotions inserted
- [ ] 25+ commits inserted
- [ ] 3 daily merges inserted
- [ ] 1 severity alert inserted
- [ ] Can login with test credentials
- [ ] Role-based routing works (student → MainActivity, admin → AdminDashboard, staff → StaffDashboard)
