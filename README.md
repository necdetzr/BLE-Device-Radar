# BLE Device Radar

[![CI Pipeline](https://github.com/necdetzr/BLE-Device-Radar/actions/workflows/ci.yml/badge.svg)](https://github.com/necdetzr/BLE-Device-Radar/actions/workflows/ci.yml)

BLE Device Radar is an Android application for discovering nearby Bluetooth Low Energy devices, inspecting their advertising data, and saving scans for later review.

The project is built with Kotlin and Jetpack Compose using a modular, layered architecture inspired by the official Now in Android project.

## Features

### BLE radar

- Scan nearby Bluetooth Low Energy devices in real time.
- Display device name, MAC address, RSSI and observed packet count.
- Inspect advertised services, manufacturer data, service data and raw advertising packets.
- Handle unsupported Bluetooth hardware, disabled Bluetooth, denied permissions and scan failures.
- Configure the scan duration and RSSI threshold.
- Save completed scans with a custom name.

### Scan history

- Store scans and discovered devices locally with Room.
- View recent scans and total scan count.
- Open saved scans and inspect their devices.
- Search scans by name.
- Search devices by name or MAC address.
- See which saved scans contained a particular device.
- Delete individual scans or clear the entire scan history.

### Settings

- Choose light, dark or system theme.
- Configure scan duration.
- Configure RSSI filtering.
- View application version information.
- Permanently delete locally saved scan history.

### Privacy

BLE observations, including device addresses and advertising data, are stored locally on the device.

The scan-history database is excluded from Android cloud backup and device transfer. Users can delete all saved scan data from the Settings screen.

## Architecture

The project uses a modular, layered architecture with unidirectional state flow:

```text
Compose UI
    ↓ events
ViewModel
    ↓
Repository interfaces
    ↓
DataStore / Room / Android BLE APIs
```

ViewModels expose immutable `StateFlow` UI state. UI events are passed back to ViewModels through explicit callbacks.

The project does not introduce use cases solely to satisfy an architectural template. Business rules can be moved into dedicated use cases when they become complex enough to justify them.

## Tech stack

- Kotlin
- Jetpack Compose
- Material 3
- Compose Canvas
- Android Navigation 3
- Kotlin Coroutines and Flow
- Dagger Hilt
- Room
- Preferences DataStore
- Kotlin Serialization
- Gradle Kotlin DSL
- Version Catalogs
- Convention Plugins
- Detekt
- GitHub Actions

## Module structure

```text
BLE-Device-Radar
├── app
├── build-logic
├── feature
│   ├── radar
│   ├── history
│   └── settings
└── core
    ├── ble
    ├── common
    ├── data
    ├── database
    ├── datastore
    ├── designsystem
    ├── model
    ├── navigation
    └── ui
```

### Application

- `:app`  
  Application entry point, root navigation and top-level UI composition.

### Feature modules

- `:feature:radar`  
  BLE scan presentation, radar visualization, device list and scan-saving flow.

- `:feature:history`  
  Saved scan history, scan details, device history and search.

- `:feature:settings`  
  Scanner preferences, appearance settings, application information and local-data deletion.

### Core modules

- `:core:ble`  
  Android BLE scanner integration and `ScanResult` mapping.

- `:core:common`  
  Shared coroutine dispatchers and application coroutine scopes.

- `:core:data`  
  Repository contracts, implementations and data mappers.

- `:core:database`  
  Room database, entities, relations, queries and migrations.

- `:core:datastore`  
  Preferences DataStore configuration and user preference persistence.

- `:core:designsystem`  
  Theme, typography, dimensions, icons and shared navigation components.

- `:core:model`  
  Shared application models.

- `:core:navigation`  
  Navigation 3 state and navigation contracts.

- `:core:ui`  
  Reusable device cards, device feeds and detail components.

### Build logic

- `:build-logic`  
  Convention plugins used to share Android, Compose, Hilt, Room and JVM build configuration.

## Requirements

- Android Studio with JDK 17
- Android SDK 36
- Android 12 or newer (API 31+)
- A physical Android device with Bluetooth Low Energy support

A physical device is recommended because Android emulators generally cannot perform real nearby BLE scans.

## Getting started

Clone the repository:

```bash
git clone https://github.com/necdetzr/BLE-Device-Radar.git
cd BLE-Device-Radar
```

Open the project in Android Studio, sync Gradle, and run the `app` configuration on a compatible physical device.

When prompted, grant the Nearby devices permissions required for BLE scanning.

## Build and code quality

Build the debug application:

```bash
./gradlew assembleDebug
```

Run static analysis:

```bash
./gradlew detekt
```

On Windows:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat detekt
```

The GitHub Actions CI workflow runs Detekt and builds the debug application for pushes and pull requests targeting `main`.

## Screenshots

Screenshots and a short radar demonstration will be added before the first public release.

Recommended screenshots:

- Radar while scanning
- Device detail sheet
- Saved scan history
- Device search
- Settings

## License

Licensed under the [Apache License 2.0](LICENSE).
