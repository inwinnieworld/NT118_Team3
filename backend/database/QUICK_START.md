# 🚀 Quick Start - Database Setup

## Cách nhanh nhất (Recommended)

### Bước 1: Generate SQL file với password hash thực
```bash
cd backend
node database/setup_with_real_hash.js
```

### Bước 2: Chạy SQL file
```bash
mysql -u root -p < backend/database/setup_ready.sql
```

**Xong!** Database đã sẵn sàng với:
- ✅ 9 tables
- ✅ 8 users (1 admin, 2 staff, 5 students)
- ✅ 15 emotions
- ✅ Sample commits và merges
- ✅ Sample severity alert

---

## Test ngay

### 1. Test Login API
```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "account": "21520001",
    "password": "password123"
  }'
```

### 2. Test với Android App
- Email: `an.nguyen@student.uit.edu.vn`
- Student Code: `21520001`
- Password: `password123`

### 3. Test Admin
- Email: `admin@uit.edu.vn`
- Password: `password123`

### 4. Test Staff
- Email: `thangda@uit.edu.vn`
- Password: `password123`

---

## Credentials Summary

| Role | Login | Password | Status |
|------|-------|----------|--------|
| Admin | admin@uit.edu.vn | password123 | ✅ Active |
| Staff | thangda@uit.edu.vn | password123 | ✅ Active |
| Staff | binhle@uit.edu.vn | password123 | ✅ Active |
| Student | 21520001 | password123 | ✅ Active |
| Student | an.nguyen@student.uit.edu.vn | password123 | ✅ Active |
| Student | 21520002 | password123 | ✅ Active |
| Student | 21520003 | password123 | ✅ Active |
| Student | 21520004 | password123 | ✅ Active |
| Student | 21520005 | password123 | 🔒 Locked |

---

## Troubleshooting

### Error: "command not found: mysql"
Install MySQL client:
```bash
# Windows (with Chocolatey)
choco install mysql

# Or download from: https://dev.mysql.com/downloads/mysql/
```

### Error: "Access denied"
Check MySQL credentials in `.env`:
```env
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=your_password
DB_NAME=emotion_debugging
DB_PORT=3306
```

### Error: "Database already exists"
Drop and recreate:
```bash
mysql -u root -p -e "DROP DATABASE IF EXISTS emotion_debugging;"
mysql -u root -p < backend/database/setup_ready.sql
```

---

## What's Included?

### Tables Created:
1. **users** - Core authentication (with `role` column)
2. **students** - Student info
3. **staff** - Staff info
4. **admins** - Admin info
5. **password_reset_tokens** - Password reset
6. **emotions** - 15 emotions (6 negative, 6 positive, 3 neutral)
7. **commits** - Git journal commits
8. **daily_merges** - Daily emotion summaries
9. **severity_alerts** - Alert history

### Sample Data:
- **Student 1 (An)**: 8 commits over 4 days, 1 merge, 1 severity alert
- **Student 2 (Bình)**: 3 positive commits, 1 merge
- **All emotions**: Ready to use in Git Journal

---

## Next Steps

1. ✅ Database setup complete
2. Start backend: `npm start`
3. Test API endpoints
4. Run Android app
5. Test all features:
   - Login/Register
   - Profile management
   - Git Journal (create commits)
   - Admin dashboard (manage students)
   - Staff dashboard

---

## Need Help?

See full documentation: `backend/database/README.md`
