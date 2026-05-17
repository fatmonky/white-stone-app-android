# White Stone Android

## Project Overview
Android port of the White Stone iOS app — a mental action tracker inspired by Upagupta's practice of tracking good (white) and bad (black) thoughts with pebbles.

- **iOS source**: `/Users/pjteh/Desktop/AI Projects/white_stone` (DO NOT modify)
- **Android project**: `/Users/pjteh/desktop/AI Projects/white_stone_android`
- **Remote**: `git@github.com:fatmonky/white-stone-app-android.git`
- **Package**: `com.whitestone.app`

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Database**: Room (SQLite)
- **DI**: Hilt (Dagger)
- **Navigation**: Compose Navigation
- **Min SDK**: 26 (Android 8.0), Target SDK: 35
- **Build**: Gradle with version catalog (`gradle/libs.versions.toml`)
- **Zero third-party dependencies** — Jetpack/Google libraries only

## Architecture
```
Single Activity (MainActivity) → Hilt → NavHost → Screens
Room DAO (Flow) → ViewModel → collectAsState() → Composable
```

## Project Structure
```
app/src/main/java/com/whitestone/app/
├── WhiteStoneApp.kt              # @HiltAndroidApp
├── MainActivity.kt               # Single Activity, setContent {}
├── data/
│   ├── Stone.kt                  # @Entity (id, type, timestamp, note, dayKey)
│   ├── StoneType.kt              # enum WHITE/BLACK
│   ├── StoneDao.kt               # @Dao with Flow-based queries
│   ├── StoneDatabase.kt          # @Database + TypeConverters
│   └── DatabaseModule.kt         # Hilt @Module providing Room
├── ui/
│   ├── theme/                    # Material 3 theme, brown accent Color(0xFF876138)
│   ├── navigation/
│   │   ├── WhiteStoneNavGraph.kt # NavHost + bottom nav bar
│   │   └── Screen.kt            # Sealed class of routes
│   ├── splash/SplashScreen.kt    # 2s delay + fade
│   ├── today/                    # 3D flippable stone, gestures, haptics, timeline
│   ├── addstone/AddStoneSheet.kt # ModalBottomSheet with time picker
│   ├── calendar/                 # Month grid, day selection, inline stone list
│   ├── daydetail/                # Full day view with timeline
│   ├── stonedetail/              # Read/edit stone with date picker
│   ├── trends/                   # Stats, custom Canvas bar chart (14 days), expandable detail
│   ├── about/AboutScreen.kt     # Static Upagupta story text
│   └── components/
│       ├── StoneIcon.kt          # Small (circle) + large (Canvas gradients, 3D look)
│       ├── RatioBar.kt           # Proportional white/black bar
│       ├── StoneTimelineItem.kt  # Reusable timeline row with connector lines
│       └── EmptyStateView.kt
└── util/
    ├── DateHelpers.kt            # java.time date utilities (dayKey, formatting, calendar math)
    └── ColorHelpers.kt           # Ratio-to-grayscale mapping
```

## Current Status

### Completed
- [x] Full project scaffold (Gradle, manifests, theme)
- [x] Data layer (Room entity, DAO, database, Hilt module)
- [x] All utility functions (DateHelpers, ColorHelpers)
- [x] All shared components (StoneIcon, RatioBar, StoneTimelineItem, EmptyStateView)
- [x] Navigation shell (NavHost + bottom bar with 4 tabs)
- [x] All screens implemented:
  - SplashScreen (2s delay + fade transition)
  - TodayScreen (3D stone with drag-to-flip, long-press-to-add, pulsing arrows, haptics, midnight rollover)
  - AddStoneSheet (ModalBottomSheet with time picker + note)
  - CalendarScreen (month grid with ratio-colored cells, day selection, inline stone list)
  - DayDetailScreen (summary + timeline list)
  - StoneDetailScreen (read/edit mode with date/time pickers)
  - TrendsScreen (stat cards, custom Canvas stacked bar chart, tap-to-expand day detail)
  - AboutScreen (Upagupta story)
- [x] Git repo initialized and pushed to remote
- [x] Android debug build/install verification on emulator (`Pixel_5_API_33`)
- [x] iOS-parity fixes merged:
  - [x] Today gesture handling now triggers one flip per swipe gesture end (no multi-flip from drag deltas)
  - [x] Trends stacked bar chart rendering fixed so white segments stack above black segments correctly
  - [x] Today long-press visual feedback restored (press scale animation)
  - [x] Removed unused Calendar day-detail callback wiring
- [x] Kept Add Stone as partial `ModalBottomSheet` interaction (full-screen variant tested and reverted)

### Blocked / In Progress
- [ ] Add automated unit/UI test coverage (`app/src/test` and/or `app/src/androidTest`)

### Remaining Work
- [ ] Expand emulator/device regression testing beyond core flows
- [ ] Verify all navigation flows end-to-end
- [ ] Test data persistence across app restarts
- [ ] Test 3D stone gestures (swipe left/right to flip, up/down to spin, long-press to add)
- [ ] Test haptic feedback on all interactions
- [ ] Test empty states on all screens
- [ ] Test midnight day rollover
- [ ] Configure ProGuard rules for release build
- [ ] Build and test release APK
- [ ] Test on different screen sizes

## Recent Work Log
- 2026-05-17:
  - Ported iOS Phase 4 pattern surfacing to Android with local `PatternEngine`.
  - Wired Review > Patterns to render observations or the under-10-stones empty state.
  - Added unit coverage for iOS-compatible pattern thresholds, ordering, tie-breaks, and four-observation cap.
  - Verified with `testDebugUnitTest`, debug build/androidTest compile, `connectedDebugAndroidTest`, install, launch, and UI hierarchy check on `Pixel_5_API_33`.
- 2026-02-28:
  - Verified repeated emulator deploy/run with `:app:installDebug` and `adb shell am start`.
  - Fixed gesture parity regression in Today screen (single flip per swipe).
  - Fixed Trends chart bar stacking math.
  - Restored Today long-press press-state scale feedback.
  - Removed unused Calendar day-detail callback.
  - Tested full-screen Add Stone flow and reverted to bottom sheet by request.

## Key iOS → Android Mappings Used
| iOS | Android |
|---|---|
| SwiftUI | Jetpack Compose |
| SwiftData @Model | Room @Entity + @Dao |
| @Query reactive binding | Room Flow + collectAsState() |
| NavigationStack + TabView | NavHost + Scaffold + NavigationBar |
| .sheet() | ModalBottomSheet |
| Swift Charts | Custom Canvas composable |
| DragGesture | Modifier.pointerInput { detectDragGestures() } |
| rotation3DEffect | Modifier.graphicsLayer { rotationY/rotationX } |
| UIImpactFeedbackGenerator | VibrationEffect |
| RadialGradient / AngularGradient | Brush.radialGradient() / Brush.sweepGradient() |

## Risk Areas to Watch
1. **3D stone rendering** — gradient layering may need visual tuning on Android
2. **Combined gestures (drag + long-press)** — using separate pointerInput + combinedClickable; may need adjustment
3. **Custom bar chart** — tap detection uses simple x-position division; label alignment uses native Canvas text drawing

## Environment Notes
- Java 17 at: `/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home`
- Symlinked to: `/Library/Java/JavaVirtualMachines/openjdk-17.jdk`
- Gradle wrapper: 8.9
- macOS on Apple Silicon (arm64)
