# BLE-Device-Radar
BLE Device Radar Repository
![CI Status](https://img.shields.io/badge/CI-Passing-brightgreen?style=flat-square&logo=githubactions)
![Detekt](https://img.shields.io/badge/Detekt-Linted-blue?style=flat-square&logo=kotlin)
![Min SDK](https://img.shields.io/badge/Min%20SDK-31%20(Android%2012)-orange?style=flat-square&logo=android)
![Tech Stack](https://img.shields.io/badge/Tech-Compose%20%7C%20Coroutines%20%7C%20Hilt-cyan?style=flat-square)

BLE Device Radar is a modern, professional-grade Android application designed to scan, visualize, and interact with Bluetooth Low Energy (BLE) devices in real-time. Built with a heavy focus on architectural purity, this project serves as a showcase for state-of-the-art Android development practices.

## ✨ Features
* **Real-time BLE Scanning:** Modern and clean Bluetooth permission handling (API 31+).
* **Custom UI Components:** A fully custom, neon-themed dynamic Radar view built entirely with **Jetpack Compose Canvas**.
* **Reactive State Management:** Unidirectional Data Flow (UDF) utilizing MVI architecture.
* **Strict Code Quality:** Enforced Detekt rules and a fully automated GitHub Actions CI pipeline.

## 🏗️ Architecture & Tech Stack

This project strictly adheres to the **Clean Architecture** principles and a highly scalable **Multi-Module** structure, heavily inspired by the official *Now in Android* repository.

### 🛠️ Core Technologies
* **UI:** Jetpack Compose, Material 3, Custom Canvas.
* **Architecture:** MVI (Model-View-Intent), ViewModel, UDF.
* **Concurrency:** Kotlin Coroutines & Flows (StateFlow/SharedFlow).
* **Dependency Injection:** Dagger Hilt.
* **Build System:** Gradle Kotlin DSL (`build.gradle.kts`), Version Catalogs (`libs.versions.toml`), Convention Plugins.
* **Static Analysis:** Detekt.

### 📦 Module Structure
The app is heavily modularized to ensure fast build times, strict visibility control, and high reusability:

* `:app` - The main application module bringing everything together.
* `:feature:scanner` - Contains the UI and presentation logic for the BLE scanning screen.
* `:core:ble` - Encapsulates all Bluetooth Low Energy logic, scanning, and byte-array parsing.
* `:core:designsystem` - Centralized theme, typography, colors (Neon Cyan), and custom Compose components (Dimensions, Icons).

## 🚀 Getting Started

### Prerequisites
* Android Studio (Latest version recommended)
* JDK 17
* A physical Android device running Android 12 (API 31) or higher for testing BLE features (Emulators do not support BLE scanning).

### Installation
1. Clone the repository: git clone https://github.com/necdetzr/BLE-Device-Radar.git
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Build and run the :app module on your physical device.

### Screenshots
Note: Screenshots and GIFs of the custom Canvas radar will be added here as the UI development progresses.

### License
This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

