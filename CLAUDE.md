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

### Blocked / In Progress
- [ ] **Build verification** — Android Studio needs updating (was running AGP 7.3.1 max, project uses AGP 8.7.3). User is updating Android Studio now.
- [ ] Java 17 installed via Homebrew and symlinked to `/Library/Java/JavaVirtualMachines/openjdk-17.jdk`

### Remaining Work
- [ ] Fix any compilation errors after Android Studio update
- [ ] Test on emulator/device
- [ ] Verify all navigation flows end-to-end
- [ ] Test data persistence across app restarts
- [ ] Test 3D stone gestures (swipe left/right to flip, up/down to spin, long-press to add)
- [ ] Test haptic feedback on all interactions
- [ ] Test empty states on all screens
- [ ] Test midnight day rollover
- [ ] Generate app icon (can reuse iOS SVG asset)
- [ ] Configure ProGuard rules for release build
- [ ] Build and test release APK
- [ ] Verify the stacked bar chart tap detection and label alignment
- [ ] Test on different screen sizes

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
