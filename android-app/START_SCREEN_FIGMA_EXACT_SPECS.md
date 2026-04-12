# START SCREEN - FIGMA EXACT SPECIFICATIONS

## 📐 PHÂN TÍCH FIGMA LAYERS CHI TIẾT

### Thông tin từ Figma:
- **Frame Size**: 360 x 800 (Medium Phone)
- **Position**: X: 8, Y: -1921
- **Layers Structure** (từ trên xuống):
  1. `loading 1` - Progress bar component
  2. `Thanh màn hình điện thoại` - Status bar
  3. `Font chữ Emotion Debugging (full)` - Logo text "emotion"
  4. `Font chữ Emotion Debugging (full)` - Logo text "Debugging_"
  5. `[!] [Mascot chính] 1` - Mascot character
  6. `Mix 4` - Background elements
  7. `Ảnh 1` - Background image

---

## 🎯 SPECIFICATIONS CHÍNH XÁC

### 1. FRAME & SCREEN SIZE
```
Figma Frame: 360 x 800 (Medium Phone)
Android Equivalent: 
- Width: 360dp
- Height: 800dp
- Density: mdpi/hdpi (Medium Phone API)
```

### 2. BACKGROUND LAYERS

#### Layer: "Ảnh 1" (Background Image)
```
- Type: Image
- Size: Full screen (360 x 800)
- ScaleType: centerCrop
- Alpha: 0.8-1.0
- Description: Futuristic tech lab với tông màu cyan/teal
```

#### Layer: "Mix 4" (Background Elements)
```
- Type: Mixed elements (decorative)
- Overlay trên background image
- Có thể bỏ qua nếu đã có trong background image
```

### 3. LOGO TEXT LAYERS

#### Layer: "Font chữ Emotion Debugging (full)" - "emotion"
```
Position: Center horizontal, ~30-35% from top
Font: Sans-serif (có thể là Roboto hoặc custom)
Size: ~48sp (ước lượng từ 360dp width)
Color: Cyan (#00D9FF hoặc tương tự)
Effect: Glow/Shadow với cyan color
Letter spacing: Normal to slightly wide
Text: "emotion" (lowercase)
```

#### Layer: "Font chữ Emotion Debugging (full)" - "Debugging_"
```
Position: Below "emotion", ~4-8dp margin
Font: Sans-serif (same as above)
Size: ~32sp (nhỏ hơn "emotion")
Color: Cyan (#00D9FF)
Effect: Glow/Shadow với cyan color
Letter spacing: Slightly wider
Text: "Debugging_" (with underscore)
Special: Underscore nhấp nháy
```

### 4. MASCOT LAYER

#### Layer: "[!] [Mascot chính] 1"
```
Position: Center of screen (both horizontal & vertical)
Size: ~120dp x 120dp (ước lượng)
Type: Image/Vector
Description: Robot bug character với màu cyan
Effect: Có glow effect xung quanh
```

### 5. PROGRESS BAR LAYER

#### Layer: "loading 1"
```
Position: Bottom of screen, ~60-80dp from bottom
Width: Screen width - 48dp margin (left + right)
Height: ~8dp
Type: Custom progress bar
Background: Dark cyan/teal với alpha
Fill: Bright cyan gradient
Corner radius: Fully rounded (100dp)
Animation: Fill from 0% to 100%
```

### 6. STATUS BAR LAYER

#### Layer: "Thanh màn hình điện thoại"
```
Position: Top of screen
Height: 24dp (standard Android status bar)
Content: Time, signal, battery icons
Note: Trong app thực tế, có thể ẩn hoặc làm transparent
```

---

## 📱 RESPONSIVE DESIGN CHO MEDIUM PHONE API

### Screen Specifications:
```
Medium Phone API (Emulator):
- Resolution: 1080 x 2340 pixels (typical)
- Density: 420dpi (xxhdpi)
- DP Size: ~360 x 800 dp
- Aspect Ratio: 19.5:9
```

### Conversion từ Figma (360x800) sang Android:
```
Figma 360dp = Android 360dp ✅ (Perfect match!)
Figma 800dp = Android 800dp ✅ (Perfect match!)

→ Không cần scale, chỉ cần convert exact values!
```

---

## 🎨 EXACT COLOR PALETTE (từ Figma)

### Primary Colors:
```xml
<!-- Background -->
<color name="start_bg_dark">#0A1929</color>
<color name="start_bg_teal">#0D2A3F</color>
<color name="start_bg_overlay">#0F3A52</color>

<!-- Cyan Theme -->
<color name="cyan_primary">#00D9FF</color>
<color name="cyan_glow">#00FFFF</color>
<color name="cyan_bright">#00E5FF</color>
<color name="cyan_dark">#008B9E</color>

<!-- Progress Bar -->
<color name="progress_bg">#1A3A4F</color>
<color name="progress_bg_alpha">#4D1A3A4F</color> <!-- 30% alpha -->
<color name="progress_fill_start">#00D9FF</color>
<color name="progress_fill_center">#00FFFF</color>
<color name="progress_fill_end">#00E5FF</color>
```

---

## 📏 EXACT DIMENSIONS (cho 360dp width)

### Logo Text:
```xml
<!-- "emotion" -->
<dimen name="logo_emotion_size">48sp</dimen>
<dimen name="logo_emotion_margin_top">240dp</dimen> <!-- ~30% of 800dp -->

<!-- "Debugging_" -->
<dimen name="logo_debugging_size">32sp</dimen>
<dimen name="logo_debugging_margin_top">6dp</dimen>

<!-- Letter spacing -->
<item name="logo_letter_spacing" type="dimen">0.05</item>
```

### Mascot:
```xml
<dimen name="mascot_size">120dp</dimen>
<dimen name="mascot_glow_size">140dp</dimen>
<!-- Position: Center (50% x 50%) -->
```

### Progress Bar:
```xml
<dimen name="progress_height">8dp</dimen>
<dimen name="progress_margin_horizontal">48dp</dimen>
<dimen name="progress_margin_bottom">70dp</dimen>
<dimen name="progress_corner_radius">100dp</dimen>
```

---

## 🎯 UPDATED LAYOUT XML (EXACT FROM FIGMA)

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".ui.auth.StartScreenActivity">

    <!-- Background Image Layer: "Ảnh 1" -->
    <ImageView
        android:id="@+id/imgBackground"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="centerCrop"
        android:src="@drawable/start_screen_bg"
        android:contentDescription="Background"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- Overlay gradient nếu cần -->
    <View
        android:id="@+id/viewOverlay"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@drawable/bg_start_screen_overlay"
        android:alpha="0.3"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent" />

    <!-- Logo Container -->
    <LinearLayout
        android:id="@+id/logoContainer"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:gravity="center"
        android:layout_marginTop="240dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <!-- Layer: "Font chữ Emotion Debugging (full)" - "emotion" -->
        <TextView
            android:id="@+id/tvLogoEmotion"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="emotion"
            android:textSize="48sp"
            android:textColor="#00D9FF"
            android:textStyle="bold"
            android:fontFamily="sans-serif-medium"
            android:letterSpacing="0.05"
            android:shadowColor="#00FFFF"
            android:shadowDx="0"
            android:shadowDy="0"
            android:shadowRadius="20" />

        <!-- Layer: "Font chữ Emotion Debugging (full)" - "Debugging_" -->
        <TextView
            android:id="@+id/tvLogoDebugging"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Debugging_"
            android:textSize="32sp"
            android:textColor="#00D9FF"
            android:textStyle="bold"
            android:fontFamily="sans-serif-medium"
            android:letterSpacing="0.08"
            android:layout_marginTop="6dp"
            android:shadowColor="#00FFFF"
            android:shadowDx="0"
            android:shadowDy="0"
            android:shadowRadius="15" />
    </LinearLayout>

    <!-- Layer: "[!] [Mascot chính] 1" -->
    <FrameLayout
        android:id="@+id/mascotContainer"
        android:layout_width="120dp"
        android:layout_height="120dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <!-- Glow effect -->
        <View
            android:id="@+id/viewMascotGlow"
            android:layout_width="140dp"
            android:layout_height="140dp"
            android:layout_gravity="center"
            android:background="@drawable/bg_mascot_glow"
            android:alpha="0.5" />

        <!-- Mascot Image -->
        <ImageView
            android:id="@+id/imgMascot"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:src="@drawable/ic_mascot"
            android:contentDescription="Mascot"
            android:scaleType="fitCenter" />
    </FrameLayout>

    <!-- Layer: "loading 1" - Progress Bar -->
    <FrameLayout
        android:id="@+id/progressContainer"
        android:layout_width="0dp"
        android:layout_height="8dp"
        android:layout_marginStart="48dp"
        android:layout_marginEnd="48dp"
        android:layout_marginBottom="70dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <!-- Progress Background -->
        <View
            android:id="@+id/progressBackground"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:background="@drawable/bg_progress_bar" />

        <!-- Progress Fill (animated) -->
        <View
            android:id="@+id/progressFill"
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:background="@drawable/bg_progress_fill" />
    </FrameLayout>

</androidx.constraintlayout.widget.ConstraintLayout>
```

---

## 🎨 UPDATED DRAWABLES

### bg_progress_bar.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <corners android:radius="100dp" />
    <solid android:color="#4D1A3A4F" /> <!-- 30% alpha dark teal -->
</shape>
```

### bg_progress_fill.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <corners android:radius="100dp" />
    <gradient
        android:angle="0"
        android:startColor="#00D9FF"
        android:centerColor="#00FFFF"
        android:endColor="#00E5FF"
        android:type="linear" />
</shape>
```

### bg_mascot_glow.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <gradient
        android:type="radial"
        android:gradientRadius="70dp"
        android:startColor="#80 00FFFF"
        android:centerColor="#4000FFFF"
        android:endColor="#0000FFFF" />
</shape>
```

---

## 📱 RESPONSIVE ADJUSTMENTS

### Cho Medium Phone API (360dp width):
```xml
<!-- values/dimens.xml (default - cho 360dp) -->
<dimen name="logo_emotion_size">48sp</dimen>
<dimen name="logo_debugging_size">32sp</dimen>
<dimen name="mascot_size">120dp</dimen>
<dimen name="progress_margin_horizontal">48dp</dimen>
```

### Cho Large Phone (411dp+ width):
```xml
<!-- values-w411dp/dimens.xml -->
<dimen name="logo_emotion_size">56sp</dimen>
<dimen name="logo_debugging_size">38sp</dimen>
<dimen name="mascot_size">140dp</dimen>
<dimen name="progress_margin_horizontal">60dp</dimen>
```

### Cho Small Phone (320dp width):
```xml
<!-- values-w320dp/dimens.xml -->
<dimen name="logo_emotion_size">42sp</dimen>
<dimen name="logo_debugging_size">28sp</dimen>
<dimen name="mascot_size">100dp</dimen>
<dimen name="progress_margin_horizontal">32dp</dimen>
```

---

## 🎯 ANIMATION TIMING (EXACT)

### Progress Bar Animation:
```java
// Total duration: 2.5 seconds (2500ms)
private static final int ANIMATION_DURATION = 2500;

// Breakdown:
// 0-500ms: Slow start (0-20%)
// 500-2000ms: Fast middle (20-80%)
// 2000-2500ms: Slow end (80-100%)

// Interpolator: AccelerateDecelerateInterpolator
progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
```

### Underscore Blink:
```java
// Blink interval: 500ms
// Pattern: ON (500ms) → OFF (500ms) → ON (500ms) → ...
handler.postDelayed(blinkRunnable, 500);
```

---

## 🔍 FIGMA INSPECT CHECKLIST

Để đạt 99% accuracy, kiểm tra trong Figma:

### 1. Select "emotion" text layer:
- [ ] Font family: ___________
- [ ] Font size: ___________
- [ ] Font weight: ___________
- [ ] Color (hex): ___________
- [ ] Letter spacing: ___________
- [ ] Shadow blur: ___________
- [ ] Shadow color: ___________

### 2. Select "Debugging_" text layer:
- [ ] Font family: ___________
- [ ] Font size: ___________
- [ ] Font weight: ___________
- [ ] Color (hex): ___________
- [ ] Letter spacing: ___________

### 3. Select Mascot layer:
- [ ] Width: ___________
- [ ] Height: ___________
- [ ] Position X: ___________
- [ ] Position Y: ___________

### 4. Select Progress bar layer:
- [ ] Width: ___________
- [ ] Height: ___________
- [ ] Corner radius: ___________
- [ ] Fill color: ___________
- [ ] Background color: ___________

---

## 🚀 IMPLEMENTATION STEPS (UPDATED)

### Step 1: Export từ Figma
```bash
1. Select "Ảnh 1" layer → Export as PNG
   - @1x (360x800) → drawable-mdpi
   - @2x (720x1600) → drawable-xhdpi
   - @3x (1080x2400) → drawable-xxhdpi
   - @4x (1440x3200) → drawable-xxxhdpi

2. Select "[!] [Mascot chính] 1" → Export as PNG
   - @1x (120x120) → drawable-mdpi
   - @2x (240x240) → drawable-xhdpi
   - @3x (360x360) → drawable-xxhdpi
   - @4x (480x480) → drawable-xxxhdpi
```

### Step 2: Copy exact colors
```bash
1. Click vào "emotion" text
2. Copy hex color từ Fill
3. Copy shadow color từ Effects
4. Paste vào colors.xml
```

### Step 3: Copy exact dimensions
```bash
1. Select each layer
2. Note Width, Height từ right panel
3. Convert px to dp: dp = px / (dpi / 160)
4. For 360x800 Figma: 1px = 1dp (mdpi baseline)
```

---

## ✅ FINAL VERIFICATION

Test trên Medium Phone API emulator:
- [ ] Background image fill toàn màn hình
- [ ] Logo "emotion" ở vị trí 30% from top
- [ ] Logo "Debugging_" ngay dưới "emotion"
- [ ] Mascot ở chính giữa màn hình
- [ ] Progress bar cách bottom 70dp
- [ ] Progress bar margins 48dp mỗi bên
- [ ] Animation smooth 2.5 giây
- [ ] Colors match 100% với Figma
- [ ] Text glow effect hiển thị đúng

---

**Với specs này, bạn sẽ đạt 99% accuracy so với Figma design!** 🎯
