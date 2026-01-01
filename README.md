# Life Calendar 📅

> **Visualize your entire life as weeks on your phone wallpaper**

Life Calendar is a minimalist Android wallpaper app that transforms your life into a beautiful grid of dots. Each dot represents one week of your life—filled dots show weeks you've lived, hollow dots show weeks remaining based on your life expectancy.

![Android](https://img.shields.io/badge/Android-26%2B-green?logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple?logo=kotlin)
![License](https://img.shields.io/badge/License-MIT-blue)

---

##  Features

-  **Beautiful Minimalist Design** — Pure black wallpaper with elegant white dots
-  **Life Statistics** — See weeks lived, weeks remaining, and life progress percentage
-  **Weekly Auto-Update** — Automatically updates your wallpaper every Sunday
-  **Customizable** — Adjust life expectancy from 50 to 120 years
-  **Battery Friendly** — Uses WorkManager for efficient background updates
-  **AMOLED Optimized** — Perfect for OLED/AMOLED displays

---

## 📱 Screenshots

The wallpaper displays:
- A grid where **each row = 1 year** (52 weeks)
- **Filled dots** = weeks you've already lived
- **Hollow dots** = weeks remaining in your life
- Year markers every 10 years for reference

---

## 📥 Installation

### Option 1: Download APK (Recommended)

1. **Download** the latest APK from the [Releases](../../releases) section
2. **Enable** installation from unknown sources:
   - Go to **Settings → Security → Install unknown apps**
   - Allow your browser or file manager to install apps
3. **Open** the downloaded APK file to install
4. **Launch** Life Calendar and set your birthdate!

### Option 2: Build from Source

```bash
# Clone the repository
git clone https://github.com/mddragon18/life-calendar.git
cd life-calendar

# Build debug APK
./gradlew assembleDebug

# The APK will be at:
# app/build/outputs/apk/debug/app-debug.apk
```

### Option 3: Build Release APK

```bash
# Build unsigned release APK
./gradlew assembleRelease

# The APK will be at:
# app/build/outputs/apk/release/app-release-unsigned.apk
```



##  Inspiration

Inspired by the concept of "Your Life in Weeks" by Tim Urban ([Wait But Why](https://waitbutwhy.com/2014/05/life-weeks.html)), which visualizes a human life as a grid of 52 × 90 weeks — a powerful reminder of time's passage.

