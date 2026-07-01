# 📋 BACKEND ARCHITECTURE - EMOTION DEBUGGING

## 🎯 Tổng quan
Backend được xây dựng theo kiến trúc **MVC (Model-View-Controller)** với **Layered Architecture** rõ ràng:
- **Routes Layer**: Định nghĩa API endpoints
- **Middlewares**: Authentication, Authorization, File upload
- **Controllers**: Xử lý HTTP requests/responses
- **Services**: Business logic
- **Config**: Database connection, Mailer setup
- **Utils**: Helper functions

---

## 🌐 Server Configuration

### Localhost Setup
```
Backend URL: http://localhost:3000
Database: MySQL localhost:3306
```

### Android Emulator Mapping
```
Backend URL cho Android: http://10.0.2.2:3000
Giải thích: Android emulator map localhost của máy host là 10.0.2.2
```

---

## 📦 Tech Stack

### Core Dependencies
- **Express 5.2.1**: Web framework
- **MySQL2 3.22.0**: Database driver (promise-based)
- **JWT (jsonwebtoken 9.0.3)**: Authentication
- **bcryptjs 3.0.3**: Password hashing
- **multer 1.4.5**: File upload (avatars)
- **cors 2.8.6**: Cross-origin requests
- **nodemailer 8.0.5**: Email sending
- **node-cron 3.0.3**: Scheduled tasks
- **dotenv 17.4.1**: Environment variables

### Dev Dependencies
- **nodemon 3.1.14**: Auto-restart server

---

## 🗂️ Cấu trúc thư mục

```
backend/
├── src/
│   ├── config/              # Cấu hình
│   │   ├── db.js            # MySQL connection pool
│   │   └── mailer.js        # Nodemailer setup
│   │
│   ├── middlewares/         # Middleware functions
│   │   ├── auth.middleware.js      # JWT verification
│   │   ├── admin.middleware.js     # Role-based access control
│   │   └── upload.middleware.js    # Multer file upload
│   │
│   ├── routes/              # API route definitions
│   │   ├── auth.routes.js           # /api/auth/*
│   │   ├── profile.route.js         # /api/profile/*
│   │   ├── admin.route.js           # /api/admin/*
│   │   ├── gitjournal.routes.js     # /api/gitjournal/*
│   │   ├── emergency.routes.js      # /api/emergency/*
│   │   ├── staff.routes.js          # /api/staff/*
│   │   └── community.route.js       # /api/community/*
│   │
│   ├── controllers/         # Request handlers
│   │   ├── auth.controller.js
│   │   ├── profile.controller.js
│   │   ├── admin.controller.js
│   │   ├── gitjournal.controller.js
│   │   ├── emergency.controller.js
│   │   ├── staff.controller.js
│   │   └── community.controller.js
│   │
│   ├── services/            # Business logic
│   │   ├── auth.service.js
│   │   ├── gitjournal.service.js
│   │   ├── emergency.service.js
│   │   └── staff.service.js
│   │
│   ├── utils/               # Helper functions
│   │   ├── response.js      # Standardized API responses
│   │   └── validate.js      # Input validation
│   │
│   ├── jobs/                # Scheduled tasks (empty hiện tại)
│   │
│   └── server.js            # Entry point
│
├── uploads/                 # User uploaded files (avatars)
├── database/                # SQL scripts
├── .env                     # Environment variables
├── package.json
└── package-lock.json
```

---

## 🔗 API Endpoints

### 1. Authentication (`/api/auth`)
```
POST   /register          # Đăng ký tài khoản
POST   /login             # Đăng nhập
POST   /forgot-password   # Gửi OTP qua email
POST   /reset-password    # Reset password với OTP
```

### 2. Profile (`/api/profile`)
```
GET    /                  # Lấy thông tin profile
PUT    /update            # Cập nhật profile
POST   /avatar            # Upload avatar
POST   /change-password   # Đổi mật khẩu
```

### 3. Admin (`/api/admin`)
```
GET    /students          # Danh sách sinh viên
POST   /students          # Thêm sinh viên
PUT    /students/:id      # Sửa sinh viên
DELETE /students/:id      # Xóa sinh viên
GET    /staff             # Danh sách nhân viên
```

### 4. Git Journal (`/api/gitjournal`)
```
GET    /commits           # Danh sách commits
POST   /commits           # Tạo commit mới
GET    /commits/:id       # Chi tiết commit
GET    /graph             # Graph visualization data
POST   /merges            # Tạo merge
```

### 5. Emergency (`/api/emergency`)
```
GET    /contacts          # Danh bạ khẩn cấp
POST   /contacts          # Thêm contact
GET    /resources         # Hotline hỗ trợ
```

### 6. Staff (`/api/staff`)
```
GET    /                  # Danh sách staff
POST   /                  # Thêm staff
PUT    /:id               # Cập nhật staff
DELETE /:id               # Xóa staff
```

### 7. Community (`/api/community`)
```
GET    /posts             # Danh sách bài viết
POST   /posts             # Tạo bài viết mới
GET    /posts/:id         # Chi tiết bài viết
POST   /posts/:id/like    # Like bài viết
POST   /posts/:id/comment # Comment bài viết
```

---

## 🔐 Authentication Flow

### 1. Register/Login
```
Client → POST /api/auth/login
       → Body: { email, password }
       
Backend → Verify credentials
        → Generate JWT token
        → Response: { token, user_info }
```

### 2. Protected Routes
```
Client → GET /api/profile
       → Header: Authorization: Bearer <JWT_TOKEN>
       
Backend → auth.middleware.js validates token
        → Extract user_id from token
        → Controller processes request
```

### 3. Role-based Access
```
Admin routes → auth.middleware + admin.middleware
Student routes → auth.middleware only
```

---

## 💾 Database Connection

### MySQL Pool Configuration
```javascript
// config/db.js
const pool = mysql.createPool({
  host: 'localhost',
  port: 3306,
  user: 'root',
  password: 'Admin123',
  database: 'emotion_debugging',
  connectionLimit: 10,
  charset: 'utf8mb4'
});
```

### Query Pattern
```javascript
// Execute query
const [rows] = await pool.execute('SELECT * FROM users WHERE id = ?', [userId]);
```

---

## 📧 Email System

### Nodemailer Configuration
```
SMTP: smtp.gmail.com:587
Email: emotiondebugging.app@gmail.com
Sử dụng: Gửi OTP reset password
```

---

## 🚀 Cách chạy Backend

### Development Mode (auto-restart)
```cmd
cd backend
npm run dev
```

### Production Mode
```cmd
cd backend
npm start
```

### Kiểm tra server đang chạy
```
Truy cập: http://localhost:3000
Response: "Emote Debugging backend is running"
```

---

## ⚠️ Lưu ý quan trọng

### 1. Table Names (UPPERCASE vs lowercase)
- Hiện tại code backend dùng **UPPERCASE** table names (VD: `STUDENTS`, `COMMITS`)
- Windows MySQL: Không phân biệt hoa/thường ✅
- Linux MySQL: Phân biệt hoa/thường ❌
- **Action required**: Nếu deploy lên Linux, phải đổi tất cả queries sang lowercase

### 2. Android Emulator Connection
- **KHÔNG được dùng** `localhost:3000` trong Android code
- **PHẢI dùng** `10.0.2.2:3000` (Android emulator localhost mapping)

### 3. JWT Secret
- Đang dùng hardcoded secret: `emote_debugging_secret_key`
- Nên đổi thành random string mạnh hơn khi deploy production

### 4. File Upload
- Avatars được lưu ở thư mục `backend/uploads/`
- Serve static files qua route `/uploads`
- Truy cập: `http://localhost:3000/uploads/avatar-xxxxx.jpg`

---

## 🐛 Known Issues (Đã fix)

### Issue #1: Git Commit Failed
- **Lỗi**: Foreign key constraint fails khi insert vào bảng `commits`
- **Nguyên nhân**: Backend dùng `user_id` thay vì `student_id`
- **Giải pháp**: Thêm helper function `getStudentIdFromUserId()` trong `gitjournal.service.js`
- **Status**: ✅ Fixed

### Issue #2: Profile Load Failed
- **Lỗi**: Query bảng `emergency_contacts` không tồn tại
- **Nguyên nhân**: Database thiếu bảng
- **Giải pháp**: Dùng field `students.emergency_phone` có sẵn thay vì tạo bảng mới
- **Status**: ✅ Fixed

---

## 📊 Database Schema (Summary)

### Core Tables
- `users` - Thông tin đăng nhập chung
- `students` - Thông tin sinh viên (extends users)
- `staff` - Thông tin nhân viên (extends users)
- `admins` - Thông tin admin (extends users)

### Feature Tables
- `commits` - Git commits của sinh viên
- `merges` - Git merge history
- `journals` - Journal entries
- `community_posts` - Bài viết cộng đồng
- `community_comments` - Comments
- `community_likes` - Likes

### Reference Tables
- `emotions` - Danh sách cảm xúc
- `moods` - Danh sách tâm trạng
- `error_types` - Loại lỗi

---

## 🔄 Request/Response Flow

### Ví dụ: Push Git Commit

```
1. Android App
   ↓ POST http://10.0.2.2:3000/api/gitjournal/commits
   ↓ Header: Authorization: Bearer <token>
   ↓ Body: { message, branch, files, emotion_id }

2. Routes Layer (gitjournal.routes.js)
   ↓ Route matching: POST /commits
   ↓ Apply middleware: auth.middleware

3. Middleware Layer
   ↓ Verify JWT token
   ↓ Extract user_id from token
   ↓ Attach to req.user

4. Controller Layer (gitjournal.controller.js)
   ↓ Get student_id from user_id
   ↓ Call service method
   ↓ Handle response/error

5. Service Layer (gitjournal.service.js)
   ↓ Business logic
   ↓ Database query execution
   ↓ Return result

6. Database
   ↓ INSERT INTO commits (...)
   ↓ Return inserted row

7. Response
   ↓ { success: true, data: commit_data }
   ↓ HTTP 201 Created
```

---

## 📝 Coding Conventions

### Response Format
```javascript
// Success
{ 
  success: true, 
  data: { ... },
  message: "Success message"
}

// Error
{ 
  success: false, 
  message: "Error message" 
}
```

### Error Handling
```javascript
try {
  // Database operations
} catch (error) {
  console.error('[Controller] Error:', error);
  res.status(500).json({ 
    success: false, 
    message: 'Internal server error' 
  });
}
```

---

## 🎓 Kiến trúc MVC được áp dụng

### Model (implicit)
- Không có model classes riêng
- SQL queries được viết trực tiếp trong Services

### View
- Không có view (RESTful API backend only)
- Frontend (Android) là View layer

### Controller
- Nhận request từ routes
- Validate input
- Gọi services
- Trả về response

### Service Layer (Business Logic)
- Xử lý logic nghiệp vụ
- Thao tác database
- Reusable functions

---

## 🚦 Next Steps / TODO

1. ✅ Fix Git Commit foreign key constraint
2. ✅ Fix Profile emergency_contacts query
3. ⏳ Implement scheduled jobs (jobs/ folder đang empty)
4. ⏳ Add error logging system
5. ⏳ Add API rate limiting
6. ⏳ Add request validation middleware
7. ⏳ Write API documentation (Swagger/Postman)
8. ⏳ Add unit tests
9. ⏳ Setup CI/CD pipeline

---

**Last Updated**: June 21, 2026  
**Version**: 1.0.0  
**Maintainer**: NT118 Team
