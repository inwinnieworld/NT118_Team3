# 🚀 Database Setup - FINAL VERSION

## Chạy 2 lệnh này là xong!

### Bước 1: Tạo schema (DROP + CREATE tables)
```bash
mysql -u root -p emotion_debugging < backend/database/complete_schema.sql
```

### Bước 2: Insert seed data (với password hash THẬT)
```bash
mysql -u root -p emotion_debugging < backend/database/seed_with_real_hash.sql
```

---

## ✅ Test Credentials

**Password cho TẤT CẢ accounts: `password123`**

| Role | Email/Student Code | Password | Status |
|------|-------------------|----------|--------|
| **Admin** | admin@uit.edu.vn | password123 | ✅ Active |
| **Staff** | thangda@uit.edu.vn | password123 | ✅ Active |
| **Staff** | binhle@uit.edu.vn | password123 | ✅ Active |
| **Student** | 21520001 | password123 | ✅ Active |
| **Student** | an.nguyen@student.uit.edu.vn | password123 | ✅ Active |
| **Student** | 21520002 | password123 | ✅ Active |
| **Student** | 21520003 | password123 | ✅ Active |
| **Student** | 21520004 | password123 | ✅ Active |
| **Student** | 21520005 | password123 | 🔒 Locked |

---

## 📊 Data Included

- ✅ 8 users (1 admin, 2 staff, 5 students)
- ✅ 15 emotions (6 negative, 6 positive, 3 neutral)
- ✅ 26 sample commits (for testing Git Journal)
- ❌ No daily_merges (will be created when user merges)
- ❌ No severity_alerts (will be created automatically)
- ❌ No password_reset_tokens (will be created when user requests reset)

---

## 🔐 Password Hash Info

**Plain password:** `password123`

**Bcrypt hash (10 rounds):** `$2b$10$w/XSW5yv.3iJsdbNMAaTlujtZdRe5Jo01i3IfaioyqGJBtdk48hQy`

This is a REAL hash that works with bcrypt.compare()!

---

## 🧪 Quick Test

### Test Login API:
```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "account": "21520001",
    "password": "password123"
  }'
```

Expected response:
```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "userId": 4,
    "email": "an.nguyen@student.uit.edu.vn",
    "name": "Nguyễn Văn An",
    "role": "STUDENT",
    "studentCode": "21520001"
  }
}
```

---

## 🔧 Troubleshooting

### "Access denied for user"
Check your MySQL credentials in `.env`:
```env
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=your_mysql_password
DB_NAME=emotion_debugging
```

### "Table doesn't exist"
Run schema first:
```bash
mysql -u root -p emotion_debugging < backend/database/complete_schema.sql
```

### "Duplicate entry"
Database already has data. Drop and recreate:
```bash
mysql -u root -p -e "DROP DATABASE IF EXISTS emotion_debugging; CREATE DATABASE emotion_debugging;"
mysql -u root -p emotion_debugging < backend/database/complete_schema.sql
mysql -u root -p emotion_debugging < backend/database/seed_with_real_hash.sql
```

---

## ✨ What's Next?

1. ✅ Database setup complete
2. Start backend: `cd backend && npm start`
3. Test login with Android app
4. Create commits in Git Journal
5. Test admin/staff dashboards

---

## 📝 Files Overview

- `complete_schema.sql` - DROP + CREATE all tables
- `seed_with_real_hash.sql` - Insert data with REAL password hash ⭐
- `complete_seed.sql` - Old version with fake hash (deprecated)
- `generate_hash_only.js` - Generate new hash if needed
- `SETUP_FINAL.md` - This file

---

**Password: `password123` cho tất cả accounts!** 🎉
