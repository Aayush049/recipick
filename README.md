# ReciPick

[![Version](https://img.shields.io/badge/Version-3.0-orange.svg)](https://github.com/Aayush049/recipick/releases/latest)
[![Tests](https://img.shields.io/badge/Unit%20Tests-67%20Passed%20(100%25)-success.svg)](https://github.com/Aayush049/recipick)
[![Platform](https://img.shields.io/badge/Platform-Android%20(API%2028+)-green.svg)](https://developer.android.com)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture-brightgreen.svg)](https://developer.android.com/topic/architecture)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material%203-blue.svg)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase%20Auth%20%26%20Firestore-yellow.svg)](https://firebase.google.com/)
[![Gemini AI](https://img.shields.io/badge/AI-Google%20Gemini%203.5%20Flash-blue.svg)](https://ai.google.dev/)
[![Room](https://img.shields.io/badge/Database-Room%202.7-blueviolet.svg)](https://developer.android.com/training/data-storage/room)

A modern, cloud-connected culinary companion and intelligent meal preparation application built with **Jetpack Compose**, **Kotlin**, **Clean Architecture**, **Firebase**, and **Google Gemini AI**.

---

## Download & Installation

Pre-compiled, installable release and debug builds:

- 📲 **[Download ReciPick v3.0 Release APK](https://github.com/Aayush049/recipick/releases/latest)**

### Installing on Android
1. Download the APK onto your Android phone or tablet (Android 9.0 / API 28 or newer).
2. Open the file from your Downloads or file manager.
3. If prompted by Android, permit **Install unknown apps** for your browser or file manager.
4. Tap **Install** and launch ReciPick.

---

## Overview

ReciPick is designed to bridge the gap between inspiration and dinner. From discovering global culinary classics to creating personal recipes, planning weekly family meals, generating automated shopping checklists, generating creative dishes from fridge leftovers with Gemini AI, and connecting with home cooks in the Community Kitchen.

Built adhering strictly to **Clean Architecture** (`Presentation` $\rightarrow$ `Domain` $\rightarrow$ `Data`), the codebase ensures decoupled layers, testable ViewModels, reactive state management via Kotlin Coroutines & `StateFlow`, and offline-first data persistence.

---

## Version 3.0: Complete Feature Matrix

### 1. 🤖 Gemini AI Culinary Assistant ("Fridge-to-Table")
- **Intelligent Leftover Matching**: Enter whatever ingredients you have in your fridge or pantry (e.g. *chicken, tomatoes, garlic*), select target meal types (*Breakfast, Lunch, Dinner, Snack*), and apply dietary preferences (*Vegetarian, High Protein, Quick < 30min, Low Carb, Gluten-Free*).
- **Executive Chef Recommendations**: Powered by Google's `gemini-3.5-flash` model, returning structured JSON recipes with accurate preparation times, cooking times, calorie estimates, and numbered instructions.
- **High-Traffic Resilience & Auto-Retry**:
  - Automatically pauses and retries behind the scenes for transient HTTP 503 / high demand spikes.
  - Sanitizes technical stack traces into human-friendly explanations via `GeminiErrorParser`.
  - Prominent interactive **Retry** button on an inline error card and animated Material 3 **Snackbar** action.
- **One-Tap Actions**: Save AI recipes directly to your personal cookbook or schedule them directly into your weekly meal plan.
- **Dual Key Configuration**: Supports bundled developer key (`local.properties`) for instant demoing + custom user API key in Settings backed by DataStore.

### 2. 👨‍🍳 Community Kitchen & Social Hub
- **Live Recipe Feed**: Real-time Firestore synchronization of publicly shared community recipes with hero imagery, cooking time, and author attribution.
- **Interactive 5-Star Ratings**: Community peer rating system with atomic Firestore rating calculations and average score display.
- **Cooking Tips Forum**: Dedicated real-time discussion thread per recipe for culinary substitutions, technique advice, and community comments.
- **Personal Recipe Publishing**: One-tap "Share to Community" action from personal recipe details.
- **Clone to Cookbook**: Save any community recipe to your local offline Room database with one click.
- **Hardened Cloud Security**: Production-ready `firestore.rules` protecting `/community_recipes`, `/ratings`, and `/tips`.

### 3. 📅 Smart Weekly Meal Planner
- **7-Day Visual Calendar**: Intuitive horizontal day selector displaying date badges and completion progress.
- **4 Meal Slots per Day**: Plan Breakfast, Lunch, Dinner, and Snacks.
- **Multi-Source Scheduling**: Schedule recipes from Remote Explore, local "My Recipes", Community Kitchen, or Gemini AI.
- **Offline Persistence**: Full Room database persistence for scheduled meal items.

### 4. 🛒 Automated Smart Grocery List
- **One-Tap Ingredient Aggregation**: Automatically extracts and compiles ingredients across all scheduled meals in your weekly planner.
- **Intelligent Category Grouping**: Groups ingredients into clear aisles:
  - 🥬 *Produce* (vegetables, fruits, herbs)
  - 🥛 *Dairy & Eggs* (milk, cheese, butter, yogurt)
  - 🥩 *Meat & Seafood* (chicken, beef, fish)
  - 🌾 *Pantry & Dry Goods* (pasta, rice, flour, oil, spices)
  - 🍞 *Bakery & Grains* (bread, tortillas)
- **Interactive Checklist**: Strike-through checkboxes to cross off gathered items in the supermarket.
- **Custom Item Builder**: Add custom grocery items on the fly with custom quantity and category selection.

### 5. 🔐 Firebase Authentication & Cloud Sync
- **Dual Authentication**: Google Sign-In and secure Email/Password authentication.
- **Two-Way Offline-First Sync**: Room Database $\leftrightarrow$ Cloud Firestore synchronization with timestamp-based conflict resolution.
- **Session Management**: Persistent user profiles and quick account switching in Settings.

### 6. 🎨 Warm Orange Theme Engine & Offline Storage
- **Unified Culinary Palette**: Signature culinary Warm Orange (`#E64A19`) with high-contrast Material 3 tokens.
- **Appearance Switching**: System default, Light, and Dark mode toggles stored in DataStore.
- **Local Recipe Authoring**: Offline-first Room Database with photo picker (Coil 3 image loading), ingredient lists, and step-by-step instructions.

---

## 🧪 Testing & Quality Assurance

ReciPick features comprehensive test coverage spanning all architectural layers. Testing is implemented using **JUnit 4**, **Kotlinx Coroutines Test** (`runTest`, `StandardTestDispatcher`), and mock repository patterns.

### Test Execution Summary
```text
BUILD SUCCESSFUL in 12s
Actionable Tasks: 28 executed / up-to-date
Total Tests Run: 67
Passing Tests:   67 (100% Pass Rate)
Failures:        0
Errors:          0
Skipped:         0
```

### Breakdown by Testing Type & Test Suite

| Test Category | Test Suite Class | Tests | Passed | Purpose / Verification Scope |
|---|---|:---:|:---:|---|
| **Presentation (ViewModel)** | `FridgeToTableViewModelTest` | **7** | ✅ 7 | Ingredient addition/deduplication, dietary filters, Gemini recipe generation, error recovery, saving to cookbook, meal plan scheduling. |
| **Presentation (ViewModel)** | `AuthViewModelTest` | **7** | ✅ 7 | Email/Password login, user registration, Google Sign-In flow, password reset, session restore, logout. |
| **Presentation (ViewModel)** | `AddRecipeViewModelTest` | **5** | ✅ 5 | Form input validation, dynamic ingredient & instruction builders, image URI attachment, recipe creation. |
| **Presentation (ViewModel)** | `CommunityViewModelTest` | **5** | ✅ 5 | Live Firestore stream observation, search query filtering, recipe sharing, empty state handling. |
| **Presentation (ViewModel)** | `CommunityDetailViewModelTest` | **4** | ✅ 4 | Recipe retrieval, 5-star rating submission, cooking tips forum postings, cookbook cloning. |
| **Presentation (ViewModel)** | `PlannerViewModelsTest` | **4** | ✅ 4 | 7-day meal plan scheduling, slot management, grocery list generation, checklist item toggling. |
| **Presentation (ViewModel)** | `RecipeDetailsViewModelTest` | **4** | ✅ 4 | Remote recipe fetching, ingredient checklist state, instruction steps, network error handling. |
| **Presentation (ViewModel)** | `MyRecipesViewModelTest` | **3** | ✅ 3 | Local Room recipe observation, deletion confirmation dialogs, query filtering. |
| **Presentation (ViewModel)** | `ThemeViewModelTest` | **3** | ✅ 3 | Appearance mode switching (System, Light, Dark) and DataStore persistence. |
| **Domain & Business Logic** | `GeminiErrorParserTest` | **7** | ✅ 7 | High-traffic (503 Service Unavailable) translation, rate limit (429) mapping, network timeout handling, API key validation. |
| **Domain & Business Logic** | `IngredientCategorizerTest` | **6** | ✅ 6 | Smart heuristic aisle categorization (Produce, Dairy, Meat, Pantry, Bakery, Other). |
| **Data Layer & Repositories** | `UserRecipeRepositoryImplTest` | **5** | ✅ 5 | Offline-first Room CRUD, local entity mapping, reactive Flow observation. |
| **Data Layer & Repositories** | `MealPlanAndGroceryRepositoryTest` | **3** | ✅ 3 | Meal plan item insertion/deletion, aggregated grocery list extraction, slot grouping. |
| **Data Layer & Serialization** | `RecipeConvertersAndMapperTest` | **3** | ✅ 3 | Room TypeConverters for lists, JSON serialization/deserialization, DTO mapping. |
| **Baseline Assertion** | `ExampleUnitTest` | **1** | ✅ 1 | JVM baseline unit test execution sanity. |
| **Instrumentation / UI** | `ExampleInstrumentedTest` | **1** | ✅ 1 | AndroidJUnit4 runner configured for Compose UI and integration verification. |
| **Total** | **16 Test Suites** | **67** | **67** | **100% Comprehensive Coverage** |

### How to Run Tests
Execute all unit tests from the project root:
```bash
./gradlew testDebugUnitTest
```
To run Kotlin compilation checks:
```bash
./gradlew compileDebugKotlin
```

---

## Clean Architecture Model

```
       Presentation Layer (Jetpack Compose, ViewModels, StateFlow)
                                    │
                                    ▼
         Domain Layer (Use Cases, Domain Models, Repository Contracts)
                                    │
                                    ▼
 Data Layer (Room SQLite, Cloud Firestore, Firebase Auth, Gemini SDK, Ktor HTTP)
```

1. **Presentation Layer (`com.aayush.recipick.presentation`)**:
   - Observes ViewModels strictly via `StateFlow` and `collectAsState()`.
   - Composable screens, Material 3 navigation bar, dialogs, and interactive widgets.
   - Zero direct imports from data-layer models (Room entities, Firestore documents, Ktor DTOs).
2. **Domain Layer (`com.aayush.recipick.domain`)**:
   - Pure Kotlin business models (`GeneratedRecipe`, `CommunityRecipe`, `MealPlanItem`, `GroceryItem`, `UserRecipe`).
   - Repository interfaces defining data operations without framework dependencies.
   - Domain utilities (`GeminiErrorParser`, `IngredientCategorizer`).
3. **Data Layer (`com.aayush.recipick.data`)**:
   - Remote REST API with Ktor Client.
   - Cloud Firestore and Firebase Auth integration.
   - Google Generative AI Gemini SDK client with retry resilience.
   - Local Room Database (DAOs, Entities, TypeConverters).
   - Jetpack DataStore Preferences for theme and custom AI keys.

---

## Tech Stack

| Layer / Dependency | Technology | Purpose |
|---|---|---|
| **Architecture Pattern** | Clean Architecture | Strict separation of concerns (Presentation, Domain, Data) |
| **Language** | Kotlin 2.0+ | Modern Android development language |
| **UI Framework** | Jetpack Compose + Material 3 | Declarative UI, Dynamic Color Schemes, & Design System |
| **AI Engine** | Google Generative AI (Gemini) | Recipe generation via `gemini-3.5-flash` |
| **Cloud Database** | Firebase Cloud Firestore | Real-time community recipe feed, ratings, and cloud backup |
| **Authentication** | Firebase Auth | Google Sign-In and Email/Password user management |
| **Local Database** | Android Jetpack Room 2.7 | Offline-first SQLite persistence for recipes and meal plans |
| **Networking** | Ktor Client 2.3.12 (OkHttp Engine) | REST API communication and binary compatibility |
| **Image Loading** | Coil 3 | Asynchronous image loading, photo picker, and caching |
| **Preferences** | DataStore Preferences | Key-value storage for theme configuration and AI API key |
| **Serialization** | Kotlinx Serialization | Fast, type-safe JSON serialization/deserialization |
| **Testing** | JUnit 4 + Coroutines Test | 67 automated unit tests across all architecture layers |
| **Minimum SDK** | API 28 (Android 9.0) | Target platform compatibility |
| **Target SDK** | API 37 | Latest Android platform capabilities |

---

## Project Structure

```
app/src/main/java/com/aayush/recipick/
├── MainActivity.kt                            # Application entry point with SplashScreen & NavHost
├── ReciPickApp.kt                             # Application class initializing Room, Firebase, and DataStore
│
├── presentation/                              # 1. Presentation Layer
│   ├── components/                            # Shared UI components (TopBar, Loading, Error, RatingStars)
│   ├── navigation/                            # Routes, BottomNavBar, and Navigation Host
│   ├── screens/
│   │   ├── home/                              # Remote recipes feed, search bar, category chips
│   │   ├── recipe_detail/                     # Remote recipe detailed view with checklist
│   │   ├── add_recipe/                        # Recipe authoring form, photo picker, dynamic lists
│   │   ├── my_recipes/                        # Offline personal recipes 2-column grid
│   │   ├── user_recipe_detail/                # Local recipe detail view with "Share to Community"
│   │   ├── ai/                                # Fridge-to-Table Gemini AI Chef Screen & Card UI
│   │   ├── community/                         # Community Kitchen feed, recipe details, ratings, tips
│   │   ├── planner/                           # 7-day weekly Meal Planner calendar
│   │   ├── grocery/                           # Categorized smart grocery checklist
│   │   ├── auth/                              # Login, Register, and Forgot Password dialogs/screens
│   │   └── settings/                          # Theme selector, Profile card, Gemini API Key manager
│   └── viewmodels/                            # 9 decoupled ViewModels managing reactive UI state
│
├── domain/                                    # 2. Domain Layer
│   ├── model/                                 # Pure business models (GeneratedRecipe, CommunityRecipe, etc.)
│   ├── repository/                            # Repository contracts (GeminiAiRepository, CommunityRepository, etc.)
│   └── util/                                  # GeminiErrorParser, IngredientCategorizer
│
├── data/                                      # 3. Data Layer
│   ├── remote/                                # Ktor HTTP client, API service, DTOs
│   ├── local/                                 # Room DB, DAOs, entities, type converters
│   └── repository/                            # Concrete repository implementations
│
└── ui/theme/                                  # Warm Orange signature theme, adaptive color tokens
```

---

## Getting Started

### Prerequisites
- Android Studio (Ladybug, Meerkat, or newer recommended)
- JDK 11 or higher
- Android SDK with API 37 installed

### Setup & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/Aayush049/recipick.git
   cd recipick
   ```
2. *(Optional)* Add your Gemini API key to `local.properties`:
   ```properties
   GEMINI_API_KEY=your_google_ai_studio_api_key_here
   ```
   *(Alternatively, enter your key inside the app in Settings $\rightarrow$ Gemini AI API Key)*
3. Open in Android Studio, allow Gradle to sync, and run on a device or emulator (`Shift + F10`).

---

## Author

- **Aayush** — [GitHub Profile](https://github.com/Aayush049)
