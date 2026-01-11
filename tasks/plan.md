# App List Manager - UI Improvements and Night Mode Fix Plan

**Created:** January 2026
**Status:** ✅ COMPLETED

---

## Overview

This plan addressed the following user requests:
1. Remove the Play Store button from app items
2. Make tapping the app display rectangle (name, package, info) open Google Play
3. Make tapping the app icon/image open the app information window
4. Apply these changes to all app lists (main list, list detail, search)
5. Fix the night mode feature

---

## Implementation Plan

### Task 1: Modify AppListItem Component ✅
**File:** `ui/components/AppListItem.kt`
- Added separate click handlers: `onIconClick` and `onInfoClick`
- Removed `showPlayStoreButton` parameter
- Removed the Play Store IconButton
- Made the icon area clickable (for app detail)
- Made the info area clickable (for Play Store)
- Kept long click for selection mode

### Task 2: Update AppsScreen ✅
**File:** `ui/screens/apps/AppsScreen.kt`
- Updated `AppsList` composable to pass new click handlers
- `onIconClick` → open app detail bottom sheet
- `onInfoClick` → open Play Store

### Task 3: Update ListDetailScreen ✅
**File:** `ui/screens/lists/ListDetailScreen.kt`
- Modified `ListDetailAppItem` composable with same pattern
- Removed the Play Store button
- Made icon clickable → open app detail
- Made info area clickable → open Play Store
- Kept the remove button as-is

### Task 4: Update SearchScreen ✅
**File:** `ui/screens/search/SearchScreen.kt`
- Added `onIconClick` handler to open app detail
- Updated click behavior to open Play Store from info area
- Added app detail bottom sheet support

### Task 5: Create Theme Preference Management ✅
**Files:** 
- Created `data/preferences/ThemePreferences.kt` - DataStore for theme
- Created `di/PreferencesModule.kt` - Hilt DI module

### Task 6: Wire Up Theme to App ✅
**Files:**
- Updated `Theme.kt` to accept external darkTheme control via ThemeMode enum
- Updated `MainActivity.kt` with theme state management
- Updated `SettingsScreen.kt` to use SettingsViewModel and persist theme preference

---

## Files Modified

1. `ui/components/AppListItem.kt`
2. `ui/screens/apps/AppsScreen.kt`
3. `ui/screens/lists/ListDetailScreen.kt`
4. `ui/screens/search/SearchScreen.kt`
5. `ui/theme/Theme.kt`
6. `MainActivity.kt`
7. `ui/screens/settings/SettingsScreen.kt`

## Files Created

1. `data/preferences/ThemePreferences.kt`
2. `di/PreferencesModule.kt`

---

## Design Decisions

### Click Behavior
- **Icon tap** → Opens app detail bottom sheet (shows full app info, add to list options)
- **Info area tap** → Opens Google Play Store (primary action for discovering more about the app)
- **Long press** → Enters selection mode (unchanged from original behavior)

### Theme Persistence
- Used Android DataStore Preferences (modern replacement for SharedPreferences)
- Created `ThemeMode` enum with three values: SYSTEM, LIGHT, DARK
- SettingsViewModel handles the business logic and coroutine scope
- Changes apply immediately without app restart

---

## Verification Results

- ✅ No compile errors
- ✅ All diagnostics passed
- ✅ Consistent behavior across all three screens
- ✅ Theme persistence works correctly
- ✅ Selection mode preserved