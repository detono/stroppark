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

# StropPark

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
