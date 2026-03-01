# YearByWeeks 📅

A minimal, elegant Android app to visualize your year's progress by weeks and days. Built with Kotlin and Jetpack Compose.

![Min SDK](https://img.shields.io/badge/Min%20SDK-26-green)
![Target SDK](https://img.shields.io/badge/Target%20SDK-36-blue)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-purple)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-orange)

## 📱 Download

<a href="https://github.com/YOUR_USERNAME/YearByWeeks/releases/latest/download/YearByWeeks.apk">
  <img src="https://img.shields.io/badge/Download-APK-brightgreen?style=for-the-badge&logo=android" alt="Download APK">
</a>

**[⬇️ Download Latest APK](https://github.com/YOUR_USERNAME/YearByWeeks/releases/latest/download/YearByWeeks.apk)**

> Replace `YOUR_USERNAME` with your actual GitHub username after pushing to GitHub.

## ✨ Features

- **Year Progress Tracking** - Visualize how much of the year has passed
- **Weeks View** - Track progress by weeks remaining in the year
- **Days View** - Track progress by days remaining in the year
- **Home Screen Widgets** - Three beautiful widgets for your home screen:
  - 📊 **Weeks Progress Widget** - Shows weeks elapsed/remaining
  - 📅 **Days Progress Widget** - Shows days elapsed/remaining
  - ⏰ **Event Countdown Widget** - Custom countdown to any event
- **Dark Theme** - Beautiful dark mode design
- **Material 3 Design** - Modern Material You design language
- **Glance Widgets** - Built with Jetpack Glance for modern widget experience

## 📸 Screenshots

<!-- Add your screenshots here -->
| Main Screen | Weeks Widget | Days Widget | Countdown Widget |
|-------------|--------------|-------------|------------------|
| ![Main](screenshots/main.png) | ![Weeks](screenshots/weeks_widget.png) | ![Days](screenshots/days_widget.png) | ![Countdown](screenshots/countdown_widget.png) |

## 🛠️ Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Design System:** Material 3
- **Widgets:** Jetpack Glance
- **Background Work:** WorkManager
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 36

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 36

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/YearByWeeks.git
   ```

2. Open the project in Android Studio

3. Sync Gradle and run the app on your device/emulator

### Building APK

To build a release APK:

```bash
./gradlew assembleRelease
```

The APK will be generated at `app/build/outputs/apk/release/`

## 📦 Project Structure

```
app/
├── src/main/
│   ├── java/com/example/yearbyweeks/
│   │   ├── MainActivity.kt          # Main app entry point
│   │   ├── data/                     # Data models
│   │   ├── ui/                       # UI components & theme
│   │   ├── util/                     # Utility classes
│   │   └── widget/                   # Widget implementations
│   │       ├── YearProgressWidget.kt
│   │       ├── DaysProgressWidget.kt
│   │       ├── CustomCountdownWidget.kt
│   │       └── CustomCountdownConfigActivity.kt
│   └── res/
│       ├── layout/                   # Widget layouts
│       └── xml/                      # Widget configurations
```

## 🎨 Widgets

### Adding Widgets to Home Screen

1. Long press on your home screen
2. Select "Widgets"
3. Find "YearByWeeks" in the widget list
4. Choose from:
   - **Weeks Progress** - Shows week-based progress
   - **Days Progress** - Shows day-based progress
   - **Event Countdown** - Customizable countdown to any date

### Widget Customization

The Event Countdown widget allows you to:
- Set a custom event name
- Choose any target date
- Reconfigure anytime by tapping the widget

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**Bhavesh Manoj Mankar**

- GitHub: [@YOUR_USERNAME](https://github.com/YOUR_USERNAME)

## ⭐ Show Your Support

Give a ⭐️ if you like this project!

---

<p align="center">Made with ❤️ using Kotlin & Jetpack Compose</p>
