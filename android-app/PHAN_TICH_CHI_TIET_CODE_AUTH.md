# PHÂN TÍCH CHI TIẾT CODE AUTHENTICATION

## MỤC LỤC
1. Backend Analysis (NodeJS + MySQL)
2. Frontend Analysis (Android Java)
3. Data Flow Diagram
4. Security Analysis

---

## 1. BACKEND ANALYSIS

### 1.1. Database Schema (MySQL)

Dựa vào code backend, database có các bảng:

**USERS Table:**
- user_id (PK, AUTO_INCREMENT)
- name (VARCHAR)
- email (VARCHAR, UNIQUE)
- password_hash (VARCHAR)
- phone (VARCHAR, nullable)
- is_locked (BOOLEAN, default FALSE)
- created_at (TIMESTAMP)

**STUDENTS Table:**
- student_id (PK, AUTO_INCREMENT)
- user_id (FK -> USERS.user_id)
- student_code (VARCHAR, UNIQUE)
- major (VARCHAR, nullable)
- faculty (VARCHAR, nullable)
- year_of_study (INT, nullable)

**ADMINS Table:**
- admin_id (PK)
- user_id (FK -> USERS.user_id)
- admin_role (VARCHAR)

**STAFF Table:**
- staff_id (PK)
- user_id (FK -> USERS.user_id)
- position (VARCHAR)

**PASSWORD_RESET_TOKENS Table:**
- reset_id (PK, AUTO_INCREMENT)
- user_id (FK -> USERS.user_id)
- reset_token (VARCHAR, UNIQUE)
- expires_at (DATETIME)
- is_used (BOOLEAN, default FALSE)
- created_at (TIMESTAMP)

### 1.2. Backend Services

#### auth.service.js - Business Logic

**registerStudent():**
```javascript
// Input: name, email, password, phone, studentCode, major, faculty, yearOfStudy
// Process:
// 1. Check email exists
// 2. Check studentCode exists
// 3. Hash password with bcrypt (10 rounds)
// 4. Begin transaction
// 5. Insert into USERS table
// 6. Insert into STUDENTS table with user_id
// 7. Commit transaction
// Output: {success, status, message}
```

**login():**
```javascript
// Input: account (email or studentCode), password
// Process:
// 1. Find user by email OR studentCode (JOIN USERS + STUDENTS + ADMINS + STAFF)
// 2. Check user exists
// 3. Check is_locked = FALSE
// 4. Compare password with bcrypt
// 5. Determine role (STUDENT/ADMIN/STAFF)
// 6. Generate JWT token (expires 7 days)
// 7. Return token + user info
// Output: {success, status, message, data: {token, user}}
```

**forgotPasswordRequest():**
```javascript
// Input: email
// Process:
// 1. Find user by email
// 2. Generate random token (32 bytes hex)
// 3. Mark old tokens as used
// 4. Insert new token (expires 10 minutes)
// 5. Send email with deep link
// Output: {success, status, message}
```

**validateResetToken():**
```javascript
// Input: token
// Process:
// 1. Find token in PASSWORD_RESET_TOKENS
// 2. Check is_used = FALSE
// 3. Check expires_at > NOW()
// Output: {success, status, message, data: {email}}
```

**resetPassword():**
```javascript
// Input: token, newPassword
// Process:
// 1. Validate token (same as validateResetToken)
// 2. Hash new password
// 3. Begin transaction
// 4. Update USERS.password_hash
// 5. Mark token as used
// 6. Commit transaction
// Output: {success, status, message}
```

### 1.3. API Endpoints

**POST /api/auth/register**
- Request Body: {name, email, password, phone, studentCode, major, faculty, yearOfStudy}
- Response: {success, message, data: null}
- Status Codes: 201 (success), 409 (conflict), 400 (bad request), 500 (error)

**POST /api/auth/login**
- Request Body: {account, password}
- Response: {success, message, data: {token, user}}
- Status Codes: 200 (success), 401 (unauthorized), 403 (locked), 500 (error)

**POST /api/auth/forgot-password**
- Request Body: {email}
- Response: {success, message, data: null}
- Status Codes: 200 (always, for security)

**POST /api/auth/validate-reset-token**
- Request Body: {token}
- Response: {success, message, data: {email}}
- Status Codes: 200 (valid), 400 (invalid/expired)

**POST /api/auth/reset-password**
- Request Body: {token, newPassword}
- Response: {success, message, data: null}
- Status Codes: 200 (success), 400 (invalid token)

**GET /api/auth/open-reset-password?token=xxx**
- Query Param: token
- Response: HTML page with deep link
- Purpose: Bridge từ email → app

---

## 2. FRONTEND ANALYSIS

### 2.1. Architecture Pattern: MVVM

```
View (Activity) ←→ ViewModel ←→ Repository ←→ API Service
                                      ↓
                                 Local Database
```

### 2.2. Data Layer

#### RetrofitClient.java
```java
// Singleton pattern
// BASE_URL = "http://10.0.2.2:3000/" (Android emulator localhost)
// Features:
// - HttpLoggingInterceptor (log request/response)
// - GsonConverterFactory (JSON parsing)
// - Provides AuthApiService instance
```

#### AuthApiService.java (Interface)
```java
// Retrofit interface định nghĩa API calls
@POST("api/auth/register")
Call<ApiResponse<Object>> register(@Body RegisterRequest request);

@POST("api/auth/login")
Call<ApiResponse<LoginResponse>> login(@Body LoginRequest request);

@POST("api/auth/forgot-password")
Call<ApiResponse<Object>> forgotPassword(@Body ForgotPasswordRequest request);

@POST("api/auth/validate-reset-token")
Call<ApiResponse<Object>> validateResetToken(@Body ValidateResetTokenRequest request);

@POST("api/auth/reset-password")
Call<ApiResponse<Object>> resetPassword(@Body ResetPasswordRequest request);
```

#### AuthRepository.java
```java
// Pattern: Repository Pattern
// Purpose: Trung gian giữa ViewModel và API
// Features:
// - Callback interface: onSuccess(data, message), onError(message)
// - Handle Retrofit response
// - Parse ApiResponse wrapper
// - Error handling

// Methods:
login(LoginRequest, RepositoryCallback<LoginResponse>)
register(RegisterRequest, RepositoryCallback<Object>)
forgotPassword(ForgotPasswordRequest, RepositoryCallback<Object>)
```

### 2.3. Model Layer

#### Request Models

**LoginRequest.java:**
```java
class LoginRequest {
    String account;  // email hoặc studentCode
    String password;
}
```

**RegisterRequest.java:**
```java
class RegisterRequest {
    String name;
    String email;
    String password;
    String phone;
    String studentCode;
    String major;
    String faculty;
    Integer yearOfStudy;
}
```

**ForgotPasswordRequest.java:**
```java
class ForgotPasswordRequest {
    String email;
}
```

**ResetPasswordRequest.java:**
```java
class ResetPasswordRequest {
    String token;
    String newPassword;
}
```

#### Response Models

**ApiResponse<T>.java:**
```java
// Generic wrapper cho tất cả API responses
class ApiResponse<T> {
    boolean success;
    String message;
    T data;
    Object errors;
}
```

**LoginResponse.java:**
```java
class LoginResponse {
    String token;
    UserResponse user;
}
```

**UserResponse.java:**
```java
class UserResponse {
    int userId;
    String name;
    String email;
    String phone;
    String role;  // "STUDENT", "ADMIN", "STAFF"
    Integer studentId;
    String studentCode;
    String adminRole;
    String staffPosition;
}
```

### 2.4. ViewModel Layer

#### LoginViewModel.java
```java
// Extends ViewModel (Android Architecture Components)
// LiveData observables:
// - message: MutableLiveData<String>
// - loading: MutableLiveData<Boolean>
// - loginResponse: MutableLiveData<LoginResponse>

// Method: login(account, password)
// Flow:
// 1. Validate input (not empty)
// 2. Set loading = true
// 3. Call repository.login()
// 4. On success: post loginResponse, post message
// 5. On error: post message
// 6. Set loading = false
```

#### RegisterViewModel.java
```java
// LiveData observables:
// - message: MutableLiveData<String>
// - loading: MutableLiveData<Boolean>
// - success: MutableLiveData<Boolean>

// Method: register(fullName, email, password, studentCode)
// Flow:
// 1. Set loading = true
// 2. Create RegisterRequest (phone, major, faculty = empty)
// 3. Call repository.register()
// 4. On success: set success = true, post message
// 5. On error: set success = false, post message
// 6. Set loading = false
```

#### ForgotPasswordViewModel.java
```java
// LiveData observables:
// - message: MutableLiveData<String>
// - loading: MutableLiveData<Boolean>
// - success: MutableLiveData<Boolean>

// Method: forgotPassword(email)
// Flow:
// 1. Set loading = true
// 2. Call repository.forgotPassword()
// 3. On success: set success = true
// 4. On error: set success = false
// 5. Set loading = false
```

### 2.5. View Layer (Activities)

#### LoginActivity.java
```java
// Layout: activity_login.xml
// Views:
// - etAccount: EditText (email or studentCode)
// - etPassword: EditText (password)
// - imgTogglePassword: ImageView (show/hide password)
// - btnLogin: Button
// - tvForgotPassword: TextView (navigate to ForgotPasswordActivity)
// - tvRegister: TextView (navigate to RegisterActivity)

// ViewModel observers:
// - message → Toast
// - loading → disable button, change text
// - loginResponse → handleLoginSuccess()

// Methods:
// - togglePassword(): Show/hide password
// - handleLoginSuccess(): Navigate to MainActivity (chưa lưu token)
```

#### RegisterActivity.java
```java
// Layout: activity_register.xml
// Views:
// - etFullName: EditText
// - etStudentCode: EditText
// - etEmail: EditText
// - etPassword: EditText
// - imgTogglePassword: ImageView
// - btnRegister: Button
// - tvLogin: TextView (back to login)

// ViewModel observers:
// - message → Toast
// - loading → disable button
// - success → finish() (quay về LoginActivity)

// Validation:
// - Check all fields not empty
// - Call viewModel.register()
```

#### ForgotPasswordActivity.java
```java
// Layout: activity_forgot_password.xml
// Views:
// - etEmail: EditText
// - btnSendRequest: Button
// - tvBackToLogin: TextView

// ViewModel observers:
// - message → Toast
// - loading → disable button
// - success → (chưa có action cụ thể)

// Validation:
// - Check email not empty
```

#### ResetPasswordActivity.java
```java
// Layout: activity_reset_password.xml
// Views:
// - edtNewPassword: EditText
// - edtConfirmPassword: EditText
// - btnResetPassword: Button
// - txtBackToLogin: TextView

// Deep Link Handler:
// - Scheme: emotedebugging://reset-password?token=xxx
// - Read token from Intent.getData()

// Validation:
// - Token not empty
// - Password not empty
// - Password >= 6 characters
// - Password == confirmPassword

// API Call:
// - Direct Retrofit call (không dùng ViewModel)
// - Call AuthApiService.resetPassword()
// - On success: navigate to LoginActivity
```

---

## 3. DATA FLOW DIAGRAM

### 3.1. Register Flow
```
[User nhập form]
    ↓
[RegisterActivity validates input]
    ↓
[RegisterViewModel.register()]
    ↓
[AuthRepository.register()]
    ↓
[Retrofit → POST /api/auth/register]
    ↓
[Backend: auth.controller.register()]
    ↓
[auth.service.registerStudent()]
    ↓
[MySQL: INSERT USERS, INSERT STUDENTS]
    ↓
[Response: {success: true, message: "Đăng ký thành công"}]
    ↓
[Repository callback.onSuccess()]
    ↓
[ViewModel posts success = true]
    ↓
[Activity observes success → finish()]
    ↓
[Quay về LoginActivity]
```

### 3.2. Login Flow
```
[User nhập account + password]
    ↓
[LoginActivity validates input]
    ↓
[LoginViewModel.login()]
    ↓
[AuthRepository.login()]
    ↓
[Retrofit → POST /api/auth/login]
    ↓
[Backend: auth.controller.login()]
    ↓
[auth.service.login()]
    ↓
[MySQL: SELECT USERS JOIN STUDENTS/ADMINS/STAFF]
    ↓
[bcrypt.compare(password, password_hash)]
    ↓
[jwt.sign({userId, email, role}, secret, {expiresIn: "7d"})]
    ↓
[Response: {success: true, data: {token, user}}]
    ↓
[Repository callback.onSuccess(LoginResponse)]
    ↓
[ViewModel posts loginResponse]
    ↓
[Activity observes loginResponse → handleLoginSuccess()]
    ↓
[Toast "Đăng nhập thành công"]
    ↓
[CHƯA LƯU TOKEN - CẦN BỔ SUNG]
```

### 3.3. Forgot Password Flow
```
[User nhập email]
    ↓
[ForgotPasswordActivity validates]
    ↓
[ForgotPasswordViewModel.forgotPassword()]
    ↓
[AuthRepository.forgotPassword()]
    ↓
[Retrofit → POST /api/auth/forgot-password]
    ↓
[Backend: auth.service.forgotPasswordRequest()]
    ↓
[crypto.randomBytes(32).toString("hex")]
    ↓
[MySQL: INSERT PASSWORD_RESET_TOKENS]
    ↓
[nodemailer.sendMail() với link reset]
    ↓
[Response: {success: true, message: "Email đã gửi"}]
    ↓
[ViewModel posts success = true]
    ↓
[Activity shows Toast]
```

### 3.4. Reset Password Flow
```
[User click link trong email]
    ↓
[Browser mở: http://10.0.2.2:3000/api/auth/open-reset-password?token=xxx]
    ↓
[Backend trả HTML với deep link]
    ↓
[JavaScript redirect: emotedebugging://reset-password?token=xxx]
    ↓
[Android OS mở ResetPasswordActivity]
    ↓
[Activity đọc token từ Intent.getData()]
    ↓
[User nhập newPassword + confirmPassword]
    ↓
[Activity validates]
    ↓
[Direct Retrofit call → POST /api/auth/reset-password]
    ↓
[Backend: auth.service.resetPassword()]
    ↓
[MySQL: UPDATE USERS.password_hash, UPDATE tokens.is_used]
    ↓
[Response: {success: true}]
    ↓
[Activity navigates to LoginActivity]
```

---

## 4. SECURITY ANALYSIS

### 4.1. Backend Security

**✅ Good Practices:**
- Password hashing với bcrypt (10 rounds)
- JWT token với expiry (7 days)
- Reset token expires (10 minutes)
- Transaction cho database operations
- Mark old reset tokens as used
- Check is_locked trước khi login

**⚠️ Potential Issues:**
- JWT_SECRET nên được lưu trong .env (đã có dotenv)
- Không có rate limiting cho login/register
- Không có CAPTCHA cho forgot password
- Token không được refresh (7 days fixed)
- Không có email verification sau register

### 4.2. Frontend Security

**✅ Good Practices:**
- HTTPS required (usesCleartextTraffic chỉ cho dev)
- Password không hiển thị mặc định
- Deep link validation (check token exists)

**⚠️ Potential Issues:**
- Token lưu trong SharedPreferences (plain text)
  - Nên dùng EncryptedSharedPreferences
- Không validate token expiry ở client
- Không có biometric authentication
- BASE_URL hardcoded (nên dùng BuildConfig)

### 4.3. Recommendations

**High Priority:**
1. Lưu token vào SharedPreferences sau login
2. Implement logout (clear token)
3. Add token to API headers (Authorization: Bearer)
4. Encrypt SharedPreferences
5. Validate email format

**Medium Priority:**
1. Add refresh token mechanism
2. Implement email verification
3. Add rate limiting backend
4. Add password strength indicator
5. Add biometric login option

**Low Priority:**
1. Add CAPTCHA
2. Add 2FA
3. Add session management
4. Add device tracking

---

## 5. MISSING IMPLEMENTATIONS

### 5.1. LoginActivity - Chưa lưu token
```java
// CẦN THÊM trong handleLoginSuccess():
SharedPrefsHelper prefsHelper = new SharedPrefsHelper(this);
prefsHelper.saveToken(response.getToken());
prefsHelper.saveUserInfo(
    response.getUser().getUserId(),
    response.getUser().getName(),
    response.getUser().getEmail(),
    response.getUser().getRole()
);
```

### 5.2. MainActivity - Chưa có logout
```java
// CẦN TẠO method logout():
public void logout() {
    SharedPrefsHelper prefsHelper = new SharedPrefsHelper(this);
    prefsHelper.clearAll();
    
    Intent intent = new Intent(this, LoginActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
}
```

### 5.3. API Calls - Chưa có Authorization header
```java
// CẦN THÊM Interceptor trong RetrofitClient:
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(chain -> {
        Request original = chain.request();
        String token = new SharedPrefsHelper(context).getToken();
        
        if (token != null) {
            Request request = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
            return chain.proceed(request);
        }
        return chain.proceed(original);
    })
    .build();
```

---

## 6. TESTING CHECKLIST

### Backend Tests
- [ ] Register với email đã tồn tại → 409
- [ ] Register với studentCode đã tồn tại → 409
- [ ] Login với email → 200
- [ ] Login với studentCode → 200
- [ ] Login với sai password → 401
- [ ] Login với account bị khóa → 403
- [ ] Forgot password với email không tồn tại → 200 (security)
- [ ] Reset password với token hợp lệ → 200
- [ ] Reset password với token đã dùng → 400
- [ ] Reset password với token hết hạn → 400

### Frontend Tests
- [ ] Register form validation
- [ ] Login form validation
- [ ] Toggle password visibility
- [ ] Navigate giữa các màn hình
- [ ] Deep link reset password
- [ ] Loading states
- [ ] Error messages
- [ ] Success messages

---

**Kết luận:** Code authentication đã được implement khá đầy đủ về mặt chức năng cơ bản. Tuy nhiên cần bổ sung:
1. Lưu token sau login
2. Implement logout
3. Add Authorization header cho API calls
4. Tạo StartScreenActivity để check authentication status
