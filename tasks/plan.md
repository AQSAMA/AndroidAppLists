# App List Manager - Development Plan

## Overview
Build an advanced Android application for managing installed apps through custom Lists and Collections, following Material Design 3 principles with Jetpack Compose.

---

## Architecture

### Tech Stack
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM + Clean Architecture
- **DI:** Hilt
- **Database:** Room
- **Navigation:** Navigation Compose
- **Image Loading:** Coil (with caching)
- **Serialization:** Kotlin Serialization
- **Async:** Kotlin Coroutines + Flow

### Package Structure
```
com.example.myapplication/
├── di/                          # Hilt modules
├── data/
│   ├── local/
│   │   ├── database/
│   │   │   ├── dao/
│   │   │   └── entity/
│   │   └── cache/
│   ├── repository/
│   └── model/
├── ui/
│   ├── navigation/
│   ├── screens/
│   │   ├── main/
│   │   ├── listdetail/
│   │   ├── collections/
│   │   └── search/
│   ├── components/
│   └── theme/
└── util/
```

---

## Phase 1: Foundation & Architecture

### 1.1 Project Setup
- Add Room, Navigation, Hilt, Coil, Material Icons Extended, Kotlin Serialization
- Add QUERY_ALL_PACKAGES permission
- Configure Hilt application class

### 1.2 Data Layer
- Room entities: ListEntity, CollectionEntity, AppListCrossRef, TagEntity
- DAOs with Flow-based queries
- Repository pattern implementation
- JSON serialization models

### 1.3 App Discovery Service
- PackageManager queries for installed apps
- Metadata extraction (version, sizes, SDKs, timestamps)
- Icon caching with Coil + LRU cache

---

## Phase 2: Core Features

### 2.1 Main App List Screen
- StateFlow-based ViewModel
- Filter: System/User/All apps
- Sort: Name, Package, Install Date, Update Date, Size (+ reverse)
- Exclusion toggle for assigned apps
- Pull-to-refresh
- Efficient LazyColumn with cached icons

### 2.2 Search
- Global search across all apps
- Search inside specific Lists
- Result highlighting

### 2.3 App Detail Bottom Sheet
- Full app metadata display
- Play Store redirect
- Add to List action
- Tag management

---

## Phase 3: List & Collection Management

### 3.1 List Operations
- Create/Rename/Delete Lists
- Add/Remove apps
- Batch multi-select
- Duplicate detection

### 3.2 Collection Operations
- Create/Rename/Delete Collections
- Add/Remove Lists from Collections
- Nested management

### 3.3 Advanced Features
- Merge multiple lists
- Custom tags/labels
- Tag filtering

---

## Phase 4: Import/Export

### 4.1 Single List JSON
```json
{
  "version": 1,
  "title": "list_name",
  "date": 1766833107364,
  "apps": [...]
}
```

### 4.2 Collection JSON
Nested format with multiple lists maintaining individual schema.

---

## Phase 5: UI Polish

### Material 3 Compliance
- ModalBottomSheet for ALL context menus
- TopAppBar with search
- NavigationBar for sections
- Dynamic color theming
- Empty state illustrations
- Status badges (Installed/Missing/System)
- List/Collection membership indicators

---

## Data Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │ Main List   │  │ List Detail │  │ Collections │     │
│  │   Screen    │  │   Screen    │  │   Screen    │     │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘     │
│         └────────────────┼────────────────┘             │
│                   ┌──────▼──────┐                       │
│                   │  ViewModels │                       │
│                   └──────┬──────┘                       │
└──────────────────────────┼──────────────────────────────┘
                           │
┌──────────────────────────┼──────────────────────────────┐
│                   Domain Layer                          │
│                   ┌──────▼──────┐                       │
│                   │ Repositories│                       │
│                   └──────┬──────┘                       │
└──────────────────────────┼──────────────────────────────┘
                           │
┌──────────────────────────┼──────────────────────────────┐
│                    Data Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ PackageMan   │  │  Room DB     │  │ File System  │  │
│  │  + Cache     │  │              │  │              │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```
