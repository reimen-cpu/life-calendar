# Life Calendar 📅

> **Visualize your entire life as weeks on your phone wallpaper**

Life Calendar is a minimalist Android wallpaper app with two powerful visualization modes:

1.  **Life Calendar:** Transforms your life into a grid of dots, where each dot represents one week.
2.  **Year Tracker:** Visualizes the current year as a grid of days, with passed days turning black.

## ✨ Features

-  **Two Modes:**
    -  **Life Calendar:** Weeks lived (filled) vs. weeks remaining (hollow).
    -  **Year Tracker:** Mint green dots for remaining days, black dots for days passed.
-  **Daily Auto-Update:** Wallpaper updates automatically at midnight to keep your tracker accurate.
-  **Customizable:**
    -  **Life Expectancy:** Adjust from 50 to 120 years.
    -  **Days Position:** Toggle days counter between top and bottom (Year Tracker).
-  **Minimalist Design:** Pure black background optimized for AMOLED screens.
-  **Battery Friendly:** Updates only once per day using efficient scheduling.

---

## 📱 Modes

### 1. Life Calendar
- Grid where **each row = 1 year** (52 weeks)
- **Filled dots** = weeks you've already lived
- **Hollow dots** = weeks remaining
- Year markers every 10 years

### 2. Year Tracker
- Grid where **each dot = 1 day** of the current year
- **Mint Green dots** = days remaining
- **Black dots** = days passed ("lost forever")
- Days remaining counter (toggleable top/bottom position)

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

