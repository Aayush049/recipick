# ReciPick

[![Version](https://img.shields.io/badge/Version-2.0-orange.svg)](https://github.com/Aayush049/recipick/releases/latest)
[![Download APK](https://img.shields.io/badge/Download-ReciPick%20v2.0%20APK-success?logo=android)](https://github.com/Aayush049/recipick/releases/latest/download/ReciPick-v2.0.apk)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture-brightgreen.svg)](https://developer.android.com/topic/architecture)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material%203-blue.svg)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Database-Room%202.7-blueviolet.svg)](https://developer.android.com/training/data-storage/room)
[![Ktor](https://img.shields.io/badge/Network-Ktor%203.6.0-black.svg)](https://ktor.io/)

A modern recipe discovery and meal preparation application built with Jetpack Compose, Kotlin, and Clean Architecture.

---

## Download & Installation

You can download the pre-compiled, installable release APK:

- 📲 **[Download ReciPick v2.0 Release APK](https://github.com/Aayush049/recipick/releases/latest/download/ReciPick-v2.0.apk)**

### Installing on Android
1. Download `ReciPick-v2.0.apk` onto your Android phone or tablet (Android 9.0 / API 28 or newer).
2. Open the file from your Downloads or file manager.
3. If prompted by Android, permit **Install unknown apps** for your browser or file manager.
4. Tap **Install** and enjoy cooking with ReciPick!

---

## Overview

ReciPick is a native Android application designed to help users discover, browse, author, and prepare a wide range of culinary recipes. Built adhering strictly to the **Clean Architecture** pattern, the codebase enforces clear boundaries, separation of concerns, and maintainable data flow across presentation, domain, and data layers.

---

## Version 2.0 (Current Release)

Version 2.0 introduces offline-first local storage, user recipe creation, a unified signature theme engine, and polished Material 3 navigation:

### Shipped Features

- **Offline-First Local Storage (Room Database)**:
  - Robust SQLite persistence for user-created recipes using Android Jetpack Room (Entities, DAOs, Database, and TypeConverters).
  - Reactive Flow observation across repositories and ViewModels.
- **User Recipe Creation ("Add Recipe" UI)**:
  - Comprehensive creation form with photo picker (Coil 3 image preview & deletion).
  - Dynamic list builders for ingredients and cooking instructions.
  - Form validation for required fields, prep/cook times, servings, and calorie counts.
- **"My Recipes" & Personal Detail View**:
  - Dedicated screen listing all user-authored local recipes in a responsive 2-column grid with empty states.
  - Interactive personal recipe detail view featuring hero imagery, metadata chips, interactive ingredient check-lists, numbered instruction steps, and delete confirmation dialogs.
- **Unified Warm Orange Theme Engine**:
  - DataStore-backed theme preferences (`theme_preferences`).
  - Seamless System, Light, and Dark appearance mode switching.
  - Signature culinary Warm Orange theme (`#E64A19`) designed with adaptive high-contrast color tokens for optimal legibility in both light and dark themes.
- **Material 3 Bottom Navigation**:
  - Seamless tab navigation between **Explore** (Home), **My Recipes**, and **Settings**, hidden automatically on detail and creation screens.
  - Active tab highlighting and intuitive backstack state restoration.
- **Settings & Appearance Screen**:
  - Intuitive vertical layout for appearance mode selection (System, Light, Dark).
  - Version 3.0 roadmap profile placeholder card prepared for future Firebase Auth integration.

---

## Version 3.0 (Planned Roadmap)

- **Firebase Authentication**: Google Sign-In and Email/Password authentication.
- **Cloud Firestore Sync**: Cloud backup and synchronization of custom recipes across devices.
- **Community & Social Sharing**: Public recipe sharing, user ratings, and community reviews.
- **Smart Meal Planning**: Weekly meal planner calendar and automated grocery list export.

---

## Clean Architecture Model

The application follows the **Clean Architecture** software design pattern, organizing the codebase into three decoupled layers:

### 1. Presentation Layer
- **Compose UI**: Declarative screens, composable components, Material 3 bottom navigation, and responsive grids.
- **ViewModels (State Holders)**: Manage reactive UI state (`StateFlow`), handle user interactions, and coordinate with repositories.

### 2. Domain Layer
- **Business Logic**: Core domain models (`Recipe`, `UserRecipe`, `AppThemeConfig`, `ThemeMode`, `ColorPalette`) independent of frameworks.
- **Repository Interfaces**: Clean contracts (`RecipeRepository`, `UserRecipeRepository`, `ThemePreferencesRepository`) defining data access rules.

### 3. Data Layer
- **Remote Data Sources**: REST API communication via Ktor Client (`RecipeAPIService`, DTOs).
- **Local Data Sources**: Room Database DAOs, entities, and kotlinx.serialization-powered type converters.
- **Preferences DataStore**: Asynchronous preference storage for theme configuration.
- **Repository Implementations**: Concrete orchestrators mapping data layer objects into clean domain models.

---

## Tech Stack

| Layer / Dependency | Technology | Purpose |
|---|---|---|
| **Architecture Pattern** | Clean Architecture | Strict separation of concerns (Presentation, Domain, Data) |
| **Language** | Kotlin | Primary programming language |
| **UI Framework** | Jetpack Compose + Material 3 | Declarative UI, Dynamic Color Schemes, & Design System |
| **Networking** | Ktor Client (Android Engine) | Asynchronous HTTP requests and JSON content negotiation |
| **Serialization** | Kotlinx Serialization | Type-safe JSON serialization/deserialization |
| **Image Loading** | Coil 3 | Image caching, asynchronous rendering, and photo picking |
| **Navigation** | Navigation Compose | Type-safe in-app screen navigation and Bottom Navigation |
| **Local Persistence** | Room Database (SQLite) | Offline-first storage for user recipes with KSP |
| **Preferences** | DataStore Preferences | Key-value storage for theme mode and color palettes |
| **Minimum SDK** | API 28 (Android 9.0) | Target platform compatibility |
| **Target SDK** | API 37 | Latest Android platform capabilities |

---

## Project Structure

```
app/src/main/java/com/aayush/recipick/
├── MainActivity.kt                       # Application entry point with SplashScreen & NavHost
├── ReciPickApp.kt                        # Application class initializing Room DB and DataStore
│
├── presentation/                         # 1. Presentation Layer
│   ├── components/                       # Shared UI components (TopBar, Loading, Error)
│   ├── navigation/                       # Route declarations, BottomNavBar, and Navigation Host
│   ├── screens/
│   │   ├── home/                         # Home feed, category selector, recipe cards
│   │   ├── recipe_detail/                # Remote recipe detail view
│   │   ├── add_recipe/                   # Recipe creation form, photo picker, dynamic lists
│   │   ├── my_recipes/                   # Local personal recipes grid
│   │   ├── user_recipe_detail/           # Personal recipe detail view with checklist
│   │   └── settings/                     # Appearance mode toggle & culinary palette selector
│   └── viewmodels/                       # Home, Details, AddRecipe, MyRecipes, and Theme ViewModels
│
├── domain/                               # 2. Domain Layer
│   ├── model/                            # UserRecipe, AppThemeConfig, ThemeMode, ColorPalette
│   └── repository/                       # RecipeRepository, UserRecipeRepository, ThemePreferencesRepository
│
├── data/                                 # 3. Data Layer
│   ├── remote/                           # Ktor HTTP client, API service, and DTOs
│   ├── local/                            # Room DB, DAOs, entities, converters, and mappers
│   └── repository/                       # Concrete repository implementations
│
└── ui/theme/                             # Warm Orange signature theme, adaptive color schemes, and MaterialTheme
```

---

## Getting Started

### Prerequisites

- Android Studio (Ladybug, Meerkat, or newer recommended)
- JDK 11 or higher
- Android SDK with API 37 installed

### Build and Run

1. Clone the repository:
   ```bash
   git clone https://github.com/Aayush049/recipick.git
   cd recipick
   ```

2. Open the project in Android Studio.
3. Allow Gradle to synchronize dependencies.
4. Select a connected device or emulator (API 28+) and run the project (`Shift + F10`).

---

## API Reference

ReciPick consumes public endpoints provided by the [DummyJSON Recipes API](https://dummyjson.com/docs/recipes):
- `GET /recipes` — Retrieves a paginated list of remote recipes.
- `GET /recipes/{id}` — Retrieves detailed data for a specific remote recipe.

---

## Author

- **Aayush** — [GitHub Profile](https://github.com/Aayush049)
