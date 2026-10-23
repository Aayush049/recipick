# ReciPick

[![Version](https://img.shields.io/badge/Version-1.0-orange.svg)](https://github.com/Aayush049/recipick)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture-brightgreen.svg)](https://developer.android.com/topic/architecture)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material%203-blue.svg)](https://developer.android.com/jetpack/compose)
[![Ktor](https://img.shields.io/badge/Network-Ktor%203.6.0-black.svg)](https://ktor.io/)

A modern recipe discovery and meal preparation application built with Jetpack Compose, Kotlin, and Clean Architecture.

---

## Overview

ReciPick is a native Android application designed to help users discover, browse, and prepare a wide range of culinary recipes. Built adhering strictly to the **Clean Architecture** pattern, the codebase enforces clear boundaries, separation of concerns, and maintainable data flow across distinct layers.

---

## Version 1.0 (Current Release)

The initial release establishes the core networking, data mapping, and presentation layers for remote recipe browsing:

### Core Features

- **Remote Recipe Catalog**: Fetches real-time recipe collections via REST endpoints from DummyJSON using the Ktor HTTP client.
- **Cuisine and Category Filtering**: Dynamically extracts and presents cuisine categories (e.g., Italian, Asian, Mexican) as interactive filter chips.
- **Responsive Recipe Grid**: Two-column responsive layout showcasing dish imagery, cuisine tags, calorie counts, cook times, and user ratings.
- **Detailed Recipe View**:
  - Full-width hero image loaded asynchronously with Coil 3.
  - Nutritional and preparation metrics (prep time, cook time, servings, difficulty level, calories per serving).
  - Categorization badges (meal types and tags).
  - Structured ingredient checklist.
  - Numbered, step-by-step cooking instructions.
- **Type-Safe Navigation**: Built on Jetpack Navigation Compose using Kotlinx Serialization route objects.
- **State and Error Handling**: Reactive UI state management with dedicated loading indicators, empty states, and error recovery actions.
- **Splash Screen Integration**: Implements the official AndroidX Core SplashScreen API using the application logo from drawable resources for seamless startup.

---

## Version 2.0 (Planned Roadmap)

The upcoming release expands functionality with offline-first capabilities, user contributions, and customized theming:

1. **User Recipe Creation (Local Storage with Room Database)**
   - Allow users to author and save custom recipes directly on their device.
   - Offline-first persistence integrated into the Data Layer using Android Jetpack Room (Entity, DAO, and Database architecture).
   - Fields for custom titles, prep/cook times, servings, ingredients list, and instruction steps.

2. **Theme Change Control**
   - In-app toggle action allowing users to switch themes dynamically without restarting the application.

3. **Multi-Color Palettes**
   - Dedicated color palettes customized for different themes (light, dark, and specialized culinary palettes).

4. **Dedicated "My Recipes" Screen**
   - Independent screen designed specifically to list, organize, and manage user-created local recipes separate from the remote catalog.

---

## Clean Architecture Model

The application follows the **Clean Architecture** software design pattern, organizing the codebase into three decoupled layers with specific responsibilities:

### 1. Presentation Layer
- **Compose UI**: Declarative screens, composable components, top app bars, and responsive grids.
- **ViewModels (State Holders)**: Manage screen state, handle UI events, and communicate with the domain/data layer.
- **User Interaction**: Handles user actions such as category selection, navigation clicks, and retry requests.

### 2. Domain Layer
- **Business Logic**: Core domain definitions and validation rules independent of frameworks.
- **Repository Interfaces**: Clean contracts (e.g., `RecipeRepository`) that define data access rules without exposing implementation details.
- **Use Cases / Interactors**: Encapsulate specific business rules and operations (as features scale).

### 3. Data Layer
- **Data Sources & DTOs**: Models representing external network payloads (`Recipe`, `RecipeResponse`, `AddRecipeRequest`).
- **API Services**: Network communication powered by Ktor Client (`RecipeAPIService`, `KtorClient`).
- **Repository Implementations**: Concrete implementation (`RecipeRepositoryImplementation`) orchestrating network calls and returning clean domain models.
- **Local Database (v2.0)**: Room database integration for offline caching and local user-created recipes.

---

## Tech Stack

| Layer / Dependency | Technology | Purpose |
|---|---|---|
| **Architecture Pattern** | Clean Architecture | Separation of concerns (Presentation, Domain, Data) |
| **Language** | Kotlin | Primary programming language |
| **UI Framework** | Jetpack Compose + Material 3 | Declarative UI and design system |
| **Networking** | Ktor Client (Android Engine) | Asynchronous HTTP requests and content negotiation |
| **Serialization** | Kotlinx Serialization | Type-safe JSON serialization/deserialization |
| **Image Loading** | Coil 3 | Image caching and asynchronous rendering |
| **Navigation** | Navigation Compose | Type-safe in-app screen navigation |
| **Persistence (v2.0)** | Room Database | Local SQLite persistence for user recipes |
| **Minimum SDK** | API 28 (Android 9.0) | Target platform compatibility |
| **Target SDK** | API 37 | Latest Android platform capabilities |

---

## Project Structure

```
app/src/main/java/com/aayush/recipick/
├── MainActivity.kt                       # Application entry point with SplashScreen & NavHost
│
├── presentation/                         # 1. Presentation Layer
│   ├── components/                       # Shared UI components (TopBar, Loading, Error)
│   ├── navigation/                       # Route declarations and Navigation Host
│   ├── screens/
│   │   ├── home/                         # Home feed, category selector, recipe cards
│   │   └── recipe_detail/                # Detail views, metadata chips, instruction steps
│   └── viewmodels/                       # HomeViewModel, RecipeDetailsViewModel (State Holders)
│
├── domain/                               # 2. Domain Layer
│   └── repository/
│       └── RecipeRepository.kt           # Repository abstraction interface
│
├── data/                                 # 3. Data Layer
│   ├── remote/
│   │   ├── KtorClient.kt                 # Ktor HTTP client configuration
│   │   ├── RecipeAPIService.kt           # API endpoint declarations
│   │   └── dto/                          # Remote Data Transfer Objects (DTOs)
│   └── repository/
│       └── RecipeRepositoryImplementation.kt # Concrete repository implementation
│
└── ui/theme/                             # Color definitions, typography, MaterialTheme
```

---

## Getting Started

### Prerequisites

- Android Studio (Hedgehog, Ladybug, or newer recommended)
- JDK 11 or higher
- Android SDK with API 34+ installed

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
- `GET /recipes` — Retrieves a paginated list of recipes with nutritional and instructional data.
- `GET /recipes/{id}` — Retrieves detailed data for a specific recipe.

---

## Author

- **Aayush** — [GitHub Profile](https://github.com/Aayush049)
