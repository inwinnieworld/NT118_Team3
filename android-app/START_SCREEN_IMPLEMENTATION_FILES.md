# START SCREEN - CÁC FILE CODE THỰC TẾ CẦN TẠO

## 📁 DANH SÁCH FILES CẦN TẠO/CẬP NHẬT

### 1. Layout XML
- ✅ `res/layout/activity_start_screen.xml` - Layout chính

### 2. Drawable Resources
- ✅ `res/drawable/bg_start_screen.xml` - Background gradient
- ✅ `res/drawable/bg_progress_bar.xml` - Progress bar background
- ✅ `res/drawable/bg_progress_fill.xml` - Progress bar fill

### 3. Values Resources
- ✅ `res/values/colors_start_screen.xml` - Colors cho Start Screen
- ✅ `res/values/dimens_start_screen.xml` - Dimensions
- ✅ `res/values/strings_start_screen.xml` - Strings
- ✅ `res/values/styles_start_screen.xml` - Styles

### 4. Java Code
- ✅ `ui/auth/StartScreenActivity.java` - Activity chính
- ✅ `utils/SharedPrefsHelper.java` - Helper lưu token

### 5. Manifest
- ✅ Cập nhật `AndroidManifest.xml`

---

## 📝 NỘI DUNG CÁC FILE

Tất cả nội dung chi tiết đã được viết trong file `HUONG_DAN_CODE_START_SCREEN_TU_FIGMA.md`

---

## 🚀 HƯỚNG DẪN TRIỂN KHAI

### Bước 1: Tạo Resources (15 phút)

```bash
# Tạo các file trong res/values/
1. colors_start_screen.xml
2. dimens_start_screen.xml  
3. strings_start_screen.xml
4. styles_start_screen.xml

# Tạo các file trong res/drawable/
5. bg_start_screen.xml
6. bg_progress_bar.xml
7. bg_progress_fill.xml
```

### Bước 2: Export Assets từ Figma (10 phút)

```bash
# Export từ Figma:
1. Background image → start_screen_bg.png (4 resolutions)
2. Mascot image → ic_mascot.png (4 resolutions)

# Copy vào:
res/drawable-hdpi/
res/drawable-xhdpi/
res/drawable-xxhdpi/
res/drawable-xxxhdpi/
```

### Bước 3: Tạo Layout (10 phút)

```bash
# Tạo file:
res/layout/activity_start_screen.xml

# Copy nội dung từ hướng dẫn
```

### Bước 4: Tạo Activity (15 phút)

```bash
# Tạo file:
ui/auth/StartScreenActivity.java

# Copy nội dung từ hướng dẫn
```

### Bước 5: Implement SharedPrefsHelper (10 phút)

```bash
# Tạo/cập nhật file:
utils/SharedPrefsHelper.java

# Copy nội dung từ hướng dẫn
```

### Bước 6: Cập nhật Manifest (5 phút)

```bash
# Cập nhật:
AndroidManifest.xml

# Thay đổi LAUNCHER activity từ LoginActivity → StartScreenActivity
```

### Bước 7: Test (10 phút)

```bash
# Build và run app
# Kiểm tra:
1. Start Screen hiển thị đúng
2. Progress bar animate từ 0-100%
3. Navigate đến LoginActivity (lần đầu)
4. Navigate đến MainActivity (khi đã login)
```

---

## 🎯 ĐIỂM QUAN TRỌNG

### Animation Progress Bar
```java
// Progress bar animate trong 2.5 giây
private static final int ANIMATION_DURATION = 2500;

// Sử dụng AccelerateDecelerateInterpolator cho smooth animation
progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
```

### Underscore Blinking
```java
// "Debugging_" → "Debugging" → "Debugging_"
// Blink every 500ms
handler.postDelayed(blinkRunnable, 500);
```

### Authentication Check
```java
// Sau khi progress bar đầy, check token
String token = prefsHelper.getToken();
if (token != null && !token.isEmpty()) {
    navigateToMain(); // Đã login
} else {
    navigateToLogin(); // Chưa login
}
```

### Fullscreen Mode
```java
// Ẩn ActionBar và Status Bar
getWindow().getDecorView().setSystemUiVisibility(
    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    | View.SYSTEM_UI_FLAG_FULLSCREEN
    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
);
```

---

## 🔧 TROUBLESHOOTING

### Vấn đề 1: Progress bar không animate
**Nguyên nhân:** View chưa được layout
**Giải pháp:** Sử dụng `progressFill.post()` để đợi layout xong

### Vấn đề 2: Background image bị stretch
**Nguyên nhân:** ScaleType không đúng
**Giải pháp:** Dùng `android:scaleType="centerCrop"`

### Vấn đề 3: Text glow không hiển thị
**Nguyên nhân:** Hardware acceleration
**Giải pháp:** Thêm `android:layerType="software"` vào TextView

### Vấn đề 4: App crash khi mở
**Nguyên nhân:** Thiếu SharedPrefsHelper
**Giải pháp:** Đảm bảo đã implement SharedPrefsHelper.java

---

## 📊 TIMELINE DỰ KIẾN

| Bước | Thời gian | Tích lũy |
|------|-----------|----------|
| 1. Tạo Resources | 15 phút | 15 phút |
| 2. Export Assets | 10 phút | 25 phút |
| 3. Tạo Layout | 10 phút | 35 phút |
| 4. Tạo Activity | 15 phút | 50 phút |
| 5. SharedPrefsHelper | 10 phút | 60 phút |
| 6. Cập nhật Manifest | 5 phút | 65 phút |
| 7. Test & Debug | 10 phút | 75 phút |

**Tổng thời gian: ~75 phút (1 giờ 15 phút)**

---

## ✅ VERIFICATION CHECKLIST

Sau khi hoàn thành, kiểm tra:

- [ ] Start Screen hiển thị đúng design Figma
- [ ] Background image/gradient hiển thị đẹp
- [ ] Logo "emotion" và "Debugging_" hiển thị với glow effect
- [ ] Underscore nhấp nháy
- [ ] Mascot hiển thị ở giữa màn hình
- [ ] Progress bar animate smooth từ 0-100%
- [ ] Thời gian loading ~2.5 giây
- [ ] Navigate đến LoginActivity khi chưa login
- [ ] Navigate đến MainActivity khi đã login
- [ ] Không crash khi rotate màn hình
- [ ] Back button bị disable
- [ ] Transition smooth giữa các màn hình

---

## 🎨 CUSTOMIZATION

Nếu muốn điều chỉnh:

### Thay đổi thời gian loading:
```java
private static final int ANIMATION_DURATION = 3000; // 3 giây
```

### Thay đổi màu progress bar:
```xml
<!-- res/drawable/bg_progress_fill.xml -->
<gradient
    android:startColor="#YOUR_COLOR"
    android:endColor="#YOUR_COLOR" />
```

### Thay đổi vị trí logo:
```xml
<!-- activity_start_screen.xml -->
app:layout_constraintVertical_bias="0.4" <!-- 0.0 = top, 1.0 = bottom -->
```

### Thêm fade in animation cho logo:
```java
logoContainer.setAlpha(0f);
logoContainer.animate()
    .alpha(1f)
    .setDuration(1000)
    .start();
```

---

**Tất cả code đã sẵn sàng để copy-paste! Chỉ cần tạo files và test thôi!** 🚀
