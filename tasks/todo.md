# App List Manager - Todo Checklist

**Last Updated:** January 2026

---

## Task 1: Modify AppListItem Component
- [x] Add `onIconClick` callback parameter
- [x] Add `onInfoClick` callback parameter  
- [x] Remove `showPlayStoreButton` parameter
- [x] Remove Play Store IconButton from the component
- [x] Make app icon area clickable with `onIconClick`
- [x] Make app info area (name, package, details) clickable with `onInfoClick`
- [x] Preserve long click behavior for selection mode

## Task 2: Update AppsScreen
- [x] Update `AppsList` to pass new click handlers
- [x] Wire `onIconClick` to open app detail bottom sheet
- [x] Wire `onInfoClick` to open Play Store

## Task 3: Update ListDetailScreen
- [x] Modify `ListDetailAppItem` composable
- [x] Remove Play Store IconButton
- [x] Add separate icon click handler for app detail
- [x] Add info area click handler for Play Store
- [x] Keep remove button unchanged

## Task 4: Update SearchScreen
- [x] Update `AppListItem` usage with new click handlers
- [x] Add app detail bottom sheet support
- [x] Wire icon click to app detail, info click to Play Store

## Task 5: Create Theme Preference Management
- [x] Create `ThemePreferences.kt` with DataStore
- [x] Create `PreferencesModule.kt` for Hilt DI
- [x] Define theme preference keys and defaults

## Task 6: Wire Up Theme to App
- [x] Update `Theme.kt` to accept external darkTheme control
- [x] Update `MainActivity.kt` with theme state management
- [x] Update `SettingsScreen.kt` to use and persist theme preference
- [x] Test theme switching works correctly

---

## PR Review Fixes (CodeRabbit/Qodo Feedback)

### Swallowed Exceptions & Code Duplication
- [x] Create `PlayStoreUtils.kt` with shared `Context.openPlayStore()` extension
- [x] Add proper exception logging with `Log.w` and `Log.e`
- [x] Add nested try-catch for web fallback to prevent crashes
- [x] Update `AppsScreen.kt` to use shared utility
- [x] Update `ListDetailScreen.kt` to use shared utility
- [x] Update `SearchScreen.kt` to use shared utility

### DataStore I/O Exception Handling
- [x] Add `IOException` catch to `ThemePreferences.themeMode` Flow
- [x] Emit `emptyPreferences()` on IOException for graceful degradation

---

## Verification Checklist

- [x] Tapping app icon opens app detail in all screens
- [x] Tapping app info area opens Play Store in all screens
- [x] Selection mode still works via long press
- [x] Night mode toggle persists across app restarts
- [x] Night mode applies immediately when changed
- [x] No compile errors
- [x] No runtime crashes
- [x] Play Store errors are properly logged
- [x] DataStore errors are handled gracefully

---

## Review Section

### Summary of Changes

1. **App Item Click Behavior Overhaul:**
   - Removed the dedicated Play Store button from app list items
   - Tapping the app **icon** now opens the app detail bottom sheet
   - Tapping the app **info area** (name, package name, version info) now opens Google Play Store
   - Long press still triggers selection mode

2. **Night Mode Fix:**
   - Created `ThemePreferences` class using DataStore for persistent storage
   - Created `PreferencesModule` for Hilt dependency injection
   - Created `SettingsViewModel` to manage theme state
   - Updated `Theme.kt` to accept `ThemeMode` enum (SYSTEM, LIGHT, DARK)
   - Updated `MainActivity` to inject and observe theme preferences
   - Updated `SettingsScreen` to use ViewModel and persist theme changes

3. **PR Review Fixes:**
   - Created `PlayStoreUtils.kt` with shared `openPlayStore()` extension function
   - Added proper exception logging instead of swallowing exceptions
   - Added IOException handling to DataStore flow
   - Reduced code duplication across 3 screen files

### Files Modified

- `ui/components/AppListItem.kt` - New click handlers, removed Play Store button
- `ui/screens/apps/AppsScreen.kt` - Updated to use new AppListItem API + shared utility
- `ui/screens/lists/ListDetailScreen.kt` - Updated ListDetailAppItem with new click pattern + shared utility
- `ui/screens/search/SearchScreen.kt` - Updated AppListItem usage + shared utility
- `ui/theme/Theme.kt` - Accept ThemeMode parameter
- `MainActivity.kt` - Inject ThemePreferences, observe theme state
- `ui/screens/settings/SettingsScreen.kt` - Added SettingsViewModel, persist theme
- `data/preferences/ThemePreferences.kt` - Added IOException handling

### Files Created

- `data/preferences/ThemePreferences.kt` - DataStore-based theme preference storage
- `di/PreferencesModule.kt` - Hilt module for preferences
- `util/PlayStoreUtils.kt` - Shared Play Store navigation utility with logging

### Testing Notes

- Theme changes should apply immediately without app restart
- Theme preference persists across app restarts
- All three screens (Apps, List Detail, Search) have consistent click behavior
- Selection mode via long press is unaffected by the changes
- Play Store navigation failures are logged for debugging
- DataStore I/O errors gracefully fall back to system theme

### Known Issues

- SearchScreen's AppDetailBottomSheet is view-only (cannot add to lists from search) - this is intentional as the search context doesn't have list management scope