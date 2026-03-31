![Version](https://img.shields.io/google-play/v/fyi.tono.stroppark?style=flat-square)

![Installs](https://img.shields.io/google-play/installs/fyi.tono.stroppark?style=flat-square)
![Rating](https://img.shields.io/google-play/rating/fyi.tono.stroppark?style=flat-square)
![Build Status](https://img.shields.io/github/actions/workflow/status/detono/stroppark/main.yml?branch=main&label=Android%20CI&style=flat-square)
![Tests](https://img.shields.io/badge/tests-passed-brightgreen?style=flat-square)
![Kotlin](https://img.shields.io/badge/kotlin-1.9.20-blue.svg?logo=kotlin&style=flat-square)
![Compose](https://img.shields.io/badge/UI-Compose%20Multiplatform-orange?logo=jetpackcompose&style=flat-square)
![Android](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&style=flat-square)
![License](https://img.shields.io/github/license/detono/stroppark?style=flat-square)
[![Support Tono on Ko-fi](https://img.shields.io/badge/Support_Tono-Tea-BD8C5E?style=flat-square&logo=ko-fi&logoColor=white)](https://ko-fi.com/detono)

# StropPark 🚗⚡️

StropPark is a modern, cross-platform mobile application designed to help users locate parking facilities and EV charging stations, specifically tailored with data for the city of Ghent.

Built entirely with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**, this project shares business logic, networking, local persistence, and the presentation layer across both Android and iOS, ensuring a native-feeling experience with minimal code duplication.

## ✨ Features

* **Interactive Map:** View parking spots and EV chargers on an interactive map.
* **Real-time Availability:** Check the status and availability of connectors and parking bays.
* **Smart Filtering:** Filter points of interest by type (Parking vs. Chargers).
* **Cross-Platform UI:** A fully shared user interface written in Compose Multiplatform.
* **Offline Caching:** Local database caching to ensure a smooth experience even with patchy network connectivity.

## 🛠 Tech Stack & Architecture

The application strictly adheres to **Clean Architecture** principles and the **Unidirectional Data Flow (MVI)** pattern, modularised by feature (`chargers`, `map`, `parking`).

* **Language:** [Kotlin](https://kotlinlang.org/)
* **UI Framework:** [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
* **Networking:** [Ktor](https://ktor.io/)
* **Local Persistence:** [Room (Multiplatform)](https://developer.android.com/training/data-storage/room)
* **Concurrency:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & Flow
* **Dependency Injection:** Shared DI module implementation
* **Location Services:** Moko Location for handling permissions and location tracking natively across platforms.

## 📁 Project Structure

The shared code is located in the `composeApp/src/commonMain/kotlin` directory, broken down into clear, feature-based packages:

* `core`: Shared utilities, themes, database configurations, location services, and network clients.
* `chargers`: Domain, data, database, and UI logic for EV charging stations.
* `map`: Logic and UI components for the interactive map interface.
* `parking`: Domain, data, database, and UI logic for standard parking facilities.

Platform-specific implementations (such as crash reporting or specific permission handling) are securely abstracted using Kotlin's `expect`/`actual` mechanism within the `androidMain` and `iosMain` source sets.

## 🚀 Getting Started

### Prerequisites

* **Android Studio** (latest stable or Ladybug/Meerkat depending on KMP plugin requirements) or **Fleet**.
* **Xcode** (for iOS development).
* **JDK 17+**

### Building for Android

1. Open the project in Android Studio.
2. Select the `composeApp` run configuration.
3. Click **Run** (or hit `Shift + F10`).

### Building for iOS

1. Open the project in Android Studio or Xcode.
2. Ensure you have the necessary iOS simulators installed.
3. Select the `iosApp` run configuration.
4. Click **Run**.
  * *Alternatively*, you can open `iosApp/iosApp.xcworkspace` directly in Xcode and build from there.

## 🧪 Testing

The codebase maintains a strong focus on testability. Fakes for data sources (e.g., `FakeChargerRepository`, `FakeParkingDao`) are provided in the `commonTest` directory.

To run the shared tests:
```bash
./gradlew clean test