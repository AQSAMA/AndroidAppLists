# App List Manager - Todo Checklist

**Last Updated:** January 2026

---

## Task 1: Modify AppListItem Component
- [ ] Add `onIconClick` callback parameter
- [ ] Add `onInfoClick` callback parameter  
- [ ] Remove `showPlayStoreButton` parameter
- [ ] Remove Play Store IconButton from the component
- [ ] Make app icon area clickable with `onIconClick`
- [ ] Make app info area (name, package, details) clickable with `onInfoClick`
- [ ] Preserve long click behavior for selection mode

## Task 2: Update AppsScreen
- [ ] Update `AppsList` to pass new click handlers
- [ ] Wire `onIconClick` to open app detail bottom sheet
- [ ] Wire `onInfoClick` to open Play Store

## Task 3: Update ListDetailScreen
- [ ] Modify `ListDetailAppItem` composable
- [ ] Remove Play Store IconButton
- [ ] Add separate icon click handler for app detail
- [ ] Add info area click handler for Play Store
- [ ] Keep remove button unchanged

## Task 4: Update SearchScreen
- [ ] Update `AppListItem` usage with new click handlers
- [ ] Add app detail bottom sheet support
- [ ] Wire icon click to app detail, info click to Play Store

## Task 5: Create Theme Preference Management
- [ ] Create `ThemePreferences.kt` with DataStore
- [ ] Create `PreferencesModule.kt` for Hilt DI
- [ ] Define theme preference keys and defaults

## Task 6: Wire Up Theme to App
- [ ] Update `Theme.kt` to accept external darkTheme control
- [ ] Update `MainActivity.kt` with theme state management
- [ ] Update `SettingsScreen.kt` to use and persist theme preference
- [ ] Test theme switching works correctly

---

## Verification Checklist

- [ ] Tapping app icon opens app detail in all screens
- [ ] Tapping app info area opens Play Store in all screens
- [ ] Selection mode still works via long press
- [ ] Night mode toggle persists across app restarts
- [ ] Night mode applies immediately when changed
- [ ] No compile errors
- [ ] No runtime crashes

---

## Review Section

_To be filled after implementation_

### Summary of Changes
- 

### Files Modified
- 

### Files Created
- 

### Testing Notes
- 

### Known Issues
- 
