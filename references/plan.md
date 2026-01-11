# App List Manager - UI Improvements and Night Mode Fix Plan

**Created:** January 2026

---

## Overview

This plan addresses the following user requests:
1. Remove the Play Store button from app items
2. Make tapping the app display rectangle (name, package, info) open Google Play
3. Make tapping the app icon/image open the app information window
4. Apply these changes to all app lists (main list, list detail, search)
5. Fix the night mode feature

---

## Current State Analysis

### App List Item Behavior (Current)
- `AppListItem.kt`: Has a separate Play Store button (`showPlayStoreButton`)
- Tapping the whole card opens the app detail bottom sheet
- `ListDetailScreen.kt`: Has its own `ListDetailAppItem` with similar Play Store button
- `SearchScreen.kt`: Uses `AppListItem`, clicking opens Play Store directly

### Night Mode (Current)
- `SettingsScreen.kt`: Has `darkMode` state variable but it's not persisted or connected
- `Theme.kt`: Only uses `isSystemInDarkTheme()`, no external preference support
- `MainActivity.kt`: Uses `MyApplicationTheme` without passing dark mode preference

---

## Implementation Plan

### Task 1: Modify AppListItem Component
**File:** `ui/components/AppListItem.kt`
- Add separate click handlers: `onIconClick` and `onInfoClick`
- Remove `showPlayStoreButton` parameter
- Remove the Play Store IconButton
- Make the icon area clickable (for app detail)
- Make the info area clickable (for Play Store)
- Keep long click for selection mode

### Task 2: Update AppsScreen
**File:** `ui/screens/apps/AppsScreen.kt`
- Update `AppsList` composable to pass new click handlers
- `onIconClick` → open app detail bottom sheet
- `onInfoClick` → open Play Store

### Task 3: Update ListDetailScreen
**File:** `ui/screens/lists/ListDetailScreen.kt`
- Modify `ListDetailAppItem` composable with same pattern
- Remove the Play Store button
- Make icon clickable → open app detail
- Make info area clickable → open Play Store
- Keep the remove button as-is

### Task 4: Update SearchScreen
**File:** `ui/screens/search/SearchScreen.kt`
- Add `onIconClick` handler to open app detail
- Update click behavior to open Play Store from info area

### Task 5: Create Theme Preference Management
**Files:** 
- Create `data/preferences/ThemePreferences.kt` - DataStore for theme
- Create `ui/theme/ThemeViewModel.kt` - ViewModel for theme state

### Task 6: Wire Up Theme to App
**Files:**
- Update `di/AppModule.kt` - Add DataStore dependency
- Update `MainActivity.kt` - Use ThemeViewModel to control theme
- Update `Theme.kt` - Accept darkTheme parameter from outside
- Update `SettingsScreen.kt` - Use ThemeViewModel and persist changes

---

## Simplicity Principles

1. Minimal code changes - only touch what's necessary
2. Reuse existing patterns in the codebase
3. No breaking changes to existing functionality
4. Simple DataStore for theme preference (one value)

---

## Files to Modify

1. `ui/components/AppListItem.kt`
2. `ui/screens/apps/AppsScreen.kt`
3. `ui/screens/lists/ListDetailScreen.kt`
4. `ui/screens/search/SearchScreen.kt`
5. `ui/theme/Theme.kt`
6. `MainActivity.kt`
7. `ui/screens/settings/SettingsScreen.kt`

## Files to Create

1. `data/preferences/ThemePreferences.kt`
2. `di/PreferencesModule.kt`

---

## Risk Assessment

- **Low Risk:** UI click handler changes - straightforward refactor
- **Medium Risk:** Theme persistence - requires careful wiring with Hilt
- **Mitigation:** Test each change incrementally