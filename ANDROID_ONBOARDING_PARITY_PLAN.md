# Android Onboarding Parity Plan

**Status:** Revised 2026-05-23 after iOS onboarding update.

**Goal:** Bring Android onboarding back to parity with the current iOS first-run flow while keeping the app local-first and avoiding new engagement mechanics.

**Updated iOS source of truth:**

- `/Users/peijingteh/Desktop/AI Projects/white-stone-app/WhiteStone/Views/ContentView.swift`
- `/Users/peijingteh/Desktop/AI Projects/white-stone-app/WhiteStone/Views/Today/TodayView.swift`
- `/Users/peijingteh/Desktop/AI Projects/white-stone-app/WhiteStone/Views/Review/ReviewView.swift`
- `/Users/peijingteh/Desktop/AI Projects/white-stone-app/WhiteStone/Views/Reflection/ReflectionView.swift`

**Android target repo:** `/Users/peijingteh/Desktop/AI Projects/white-stone-app-android`

---

## Current iOS Flow

iOS now has a five-step onboarding sequence:

1. Fresh install with no stones shows a welcome sheet.
2. User starts the tour and sees a two-step Today coaching overlay.
3. User logs the first stone and sees a post-first-entry sheet.
4. User continues to Review and sees a Review tour overlay.
5. User continues to Reflections and sees a Reflections tour overlay.

The Android implementation currently matches the earlier four-step flow and completes onboarding from the Review overlay. It does not yet support the new `REFLECTIONS_TOUR` step, the updated Review overlay primary action, or the updated welcome/post-first-entry copy.

---

## Required Behavior

### Fresh user bootstrap

Keep the existing explicit Room load-state behavior:

```kotlin
sealed interface StonesLoadState {
    data object Loading : StonesLoadState
    data class Loaded(val stones: List<Stone>) : StonesLoadState
}
```

Rules:

- If there are no stones and no saved onboarding state, set onboarding to `WELCOME`.
- If there are existing stones and no active onboarding state, set onboarding to `COMPLETED`.
- If onboarding is already `COMPLETED`, never restart it just because all stones are deleted later.
- If the app restarts mid-flow, resume from the persisted onboarding step.
- Do not run bootstrap from `collectAsState(initial = emptyList())`; only evaluate after `StonesLoadState.Loaded`.
- Preserve active flow when step is `FIRST_LOG`, `REVIEW_TOUR`, `REFLECTIONS_TOUR`, or the post-first-entry sheet is visible.

### Persisted steps

Update the Android enum to include the new Reflections tour step:

```kotlin
enum class OnboardingStep {
    WELCOME,
    TODAY_COACH,
    FIRST_LOG,
    REVIEW_TOUR,
    REFLECTIONS_TOUR,
    COMPLETED
}
```

Continue storing under `onboarding.step` in `SharedPreferences`.

Critical writes must be deterministic:

- `FIRST_LOG` -> `REVIEW_TOUR`: use `commit()`.
- `REVIEW_TOUR` -> `REFLECTIONS_TOUR`: use `commit()`.
- Any step -> `COMPLETED`: use `commit()`.

---

## User Flow Updates

### 1. Welcome sheet

Show when onboarding step is `WELCOME`.

Updated iOS copy:

- Navigation title: `Welcome`
- Title: `Track your thoughts, one stone at a time.`
- Body: `Log a white stone for a wholesome thought, or log a black stone if the thought was unskillful. Start with logging one stone today, then take a short tour of Review and Reflections.`
- Primary button: `Start Tour`
- Secondary button: `Skip`

Behavior:

- `Start Tour` sets onboarding to `TODAY_COACH` and navigates/selects Today.
- `Skip` sets onboarding to `COMPLETED`.
- Sheet must not dismiss on outside tap.
- Keep this as a root-level sheet above the app scaffold.
- Do not render the welcome sheet over `Screen.Splash.route`. Start onboarding presentation after the splash exits to Today, while keeping the state bootstrap itself independent of the splash.

Android delta:

- `WelcomeOnboardingSheet` currently says only `Review`; update body to `Review and Reflections`.
- Current Android does not need to force Today for `WELCOME`, but `Start Tour` should route to Today if not already there, matching iOS `selectedTab = 0`.

### 2. Today coaching overlay

No copy change from the previous Android plan.

Show only when:

- onboarding step is `TODAY_COACH`
- current nav destination is `Screen.Today.route`

Step 0:

- Title: `Swipe to switch stone`
- Body: `Swipe left or right on the stone to switch between White and Black.`
- Primary: `Next`
- Secondary: `Not now`

Step 1:

- Title: `Hold to log this stone`
- Body: `Press and hold the stone for a moment to open the log sheet.`
- Primary: `Try it now`
- Secondary: `Not now`

Behavior:

- Step 0 `Next` advances to step 1.
- Step 1 `Try it now` sets onboarding to `FIRST_LOG`.
- `Not now` sets onboarding to `COMPLETED`.
- Re-showing the overlay resets to step 0.
- Only `OnboardingStep` is persisted. The internal `coachStep` is process-local, matching iOS `@State`, and resets to step 0 after process death or when the overlay is shown again.
- While visible, block interaction with Today content underneath.

Android delta:

- Existing Android implementation already mostly matches this. Keep current tests.

### 3. Post-first-entry sheet

Show only when a stone is actually saved while onboarding step is `FIRST_LOG`.

Updated iOS copy:

- Navigation title: `Nice Start`
- Title: `Great. You’ve started today’s record.`
- Body: `Next, take a quick look at Review, then finish with Reflections for the daily question and saved responses.`
- Primary button: `Continue Tour`
- Secondary button: `Finish Without Tour`

Behavior:

- `Continue Tour` sets onboarding to `REVIEW_TOUR`, hides the sheet, and navigates/selects Review.
- `Finish Without Tour` sets onboarding to `COMPLETED`.
- The save callback must fire after a successful insert, through the `TodayViewModel` one-shot save-success event.

Android delta:

- `FirstStoneSuccessSheet` currently says `Continue to Review` and mentions only Review. Update copy and rename callback/action wording as needed.

### 4. Review tour overlay

Show only when:

- onboarding step is `REVIEW_TOUR`
- current nav destination is `Screen.Review.route`

Updated iOS copy/actions:

- Title: `Review`
- Body: `Review combines the calendar and trends into one place. Start with the month grid, then use the sections below it for recent bars, all-time totals, and quiet pattern notes.`
- Primary button: `Continue to Reflections`
- Secondary button: `Skip Tour`

Behavior:

- `Continue to Reflections` sets onboarding to `REFLECTIONS_TOUR` and navigates/selects Reflections.
- `Skip Tour` sets onboarding to `COMPLETED`.
- While visible, block interaction with Review content underneath.

Android delta:

- `ReviewTourOverlay` currently uses `Finish Tour` and completes onboarding. Change primary label and callback semantics.
- `WhiteStoneNavGraph` currently maps Review finish to `dismissOnboarding`; it must call a new `continueToReflections()` transition.

### 5. Reflections tour overlay

New Android work.

Show only when:

- onboarding step is `REFLECTIONS_TOUR`
- current nav destination is `Screen.Reflections.route`

iOS copy/actions:

- Title: `Reflections`
- Body: `Reflections gives you one daily question and a quiet place to save a response. Use Questions to revisit past answers by prompt; saved reflections also appear as markers in Review.`
- Primary button: `Finish Tour`
- Secondary button: `Skip Tour`

Behavior:

- `Finish Tour` sets onboarding to `COMPLETED`.
- `Skip Tour` sets onboarding to `COMPLETED`.
- While visible, block interaction with Reflections content underneath.

Android implementation shape:

```kotlin
fun ReflectionScreen(
    showTourOverlay: Boolean,
    onFinishTour: () -> Unit,
    onSkipTour: () -> Unit,
    ...
)

fun ReflectionsTourOverlay(
    visible: Boolean,
    onFinishTour: () -> Unit,
    onSkipTour: () -> Unit,
    modifier: Modifier = Modifier
)
```

Use the same overlay pattern as Today and Review: root `Box`, scrim that consumes pointer input, centered surface, calm copy, no badges/celebration/streak language.

---

## Files To Change

### Existing Android files

- `app/src/main/java/com/whitestone/app/data/OnboardingPreferences.kt`
  - Add `REFLECTIONS_TOUR` enum value.

- `app/src/main/java/com/whitestone/app/ui/onboarding/OnboardingCoordinator.kt`
  - Preserve `REFLECTIONS_TOUR` during bootstrap.
  - Add `continueToReflectionsTransition()`.
  - Keep `completeOnboardingTransition()` for Reflections finish/skip and all skip paths.

- `app/src/main/java/com/whitestone/app/ui/onboarding/OnboardingViewModel.kt`
  - Add `continueToReflections()`.
  - Ensure transition uses committed persistence.

- `app/src/main/java/com/whitestone/app/ui/navigation/WhiteStoneNavGraph.kt`
  - Sync `REVIEW_TOUR` to `Screen.Review.route`.
  - Sync `REFLECTIONS_TOUR` to `Screen.Reflections.route`.
  - Route Welcome `Start Tour` to Today.
  - Route post-first-entry `Continue Tour` to Review.
  - Route Review `Continue to Reflections` to Reflections.
  - Pass Reflections tour flags/callbacks into `ReflectionScreen`.

- `app/src/main/java/com/whitestone/app/ui/onboarding/WelcomeOnboardingSheet.kt`
  - Update body copy.

- `app/src/main/java/com/whitestone/app/ui/onboarding/FirstStoneSuccessSheet.kt`
  - Update body copy.
  - Update primary label to `Continue Tour`.

- `app/src/main/java/com/whitestone/app/ui/onboarding/ReviewTourOverlay.kt`
  - Update primary label to `Continue to Reflections`.
  - Rename callback from `onFinishTour` to `onContinueToReflections` if useful.

- `app/src/main/java/com/whitestone/app/ui/reflection/ReflectionScreen.kt`
  - Accept `showTourOverlay`, `onFinishTour`, and `onSkipTour`.
- Render the new Reflections overlay above both Today and Questions modes.
- Disable underlying interaction while visible.
- Disable mode switching while the Reflections tour is visible, or make the overlay consume all interaction above both modes so the toolbar action cannot change modes underneath it.

### New Android file

- `app/src/main/java/com/whitestone/app/ui/onboarding/ReflectionsTourOverlay.kt`

---

## Acceptance Criteria

- Fresh install with no stones shows updated Welcome sheet copy mentioning `Review and Reflections`.
- Welcome sheet does not appear over the splash screen; it appears after the app has entered the main scaffold destination.
- Welcome `Start Tour` enters Today coach.
- Today coach appears only on the Today route and does not appear on Review, Reflections, About, or detail routes.
- Today coach still has two steps and resets to step 0 when re-shown.
- Today coach sub-step is not persisted across process death.
- Completing Today coach sets `FIRST_LOG` and waits for a real first-stone insert.
- Saving the first stone shows updated `Nice Start` sheet copy and `Continue Tour` button.
- `Continue Tour` navigates to Review and shows the Review tour overlay.
- Review tour primary button says `Continue to Reflections`.
- Review tour appears only on the Review route.
- Tapping Review primary navigates to Reflections and shows the Reflections tour overlay.
- Reflections tour shows the exact updated iOS copy.
- Reflections tour appears only on the Reflections route.
- Reflections tour blocks interaction with both the daily reflection content and the Questions/Daily mode toggle.
- `Finish Tour` or `Skip Tour` from Reflections persists `COMPLETED`.
- `Skip Tour` from Review persists `COMPLETED`.
- `Finish Without Tour` from the first-stone sheet persists `COMPLETED`.
- Existing users with stones do not see onboarding.
- Completed onboarding does not restart after app relaunch.
- Completed onboarding does not restart if the user later deletes all stones.
- A saved `REFLECTIONS_TOUR` state survives process death/relaunch and resumes on the Reflections tab.
- No analytics, telemetry, badges, achievements, streak milestones, or notification prompts are added.

---

## Tests

Update focused unit tests in `app/src/test/java/com/whitestone/app/ui/onboarding/OnboardingCoordinatorTest.kt`:

- Empty DB + no saved step -> `WELCOME`.
- Existing stones + no saved step -> `COMPLETED`.
- Loading state -> no bootstrap.
- Completed + empty DB remains `COMPLETED`.
- Completed user deletes all stones -> remains `COMPLETED`.
- `FIRST_LOG` with existing stone preserves flow.
- `REVIEW_TOUR` with existing stone preserves flow.
- `REFLECTIONS_TOUR` with existing stone preserves flow.
- Welcome `Start Tour` -> `TODAY_COACH`.
- Today coach step 0 `Next` -> step 1.
- Today coach step 1 `Try it now` -> `FIRST_LOG`.
- Today coach re-show/relaunch starts at step 0.
- First stone saved during `FIRST_LOG` -> post-first-entry sheet visible.
- Continue Tour -> `REVIEW_TOUR`, committed.
- Continue to Reflections -> `REFLECTIONS_TOUR`, committed.
- Review skip -> `COMPLETED`, committed.
- Reflections finish/skip -> `COMPLETED`, committed.

Keep `TodayViewModelTest` coverage proving save-success event fires after insert.

Add a Compose or emulator smoke test if practical:

1. Clear app data.
2. Launch app and assert Welcome copy includes `Review and Reflections`.
3. Tap `Start Tour`.
4. Assert Today coach step 0 and step 1.
5. Save first stone and assert `Nice Start`.
6. Tap `Continue Tour`.
7. Assert Review tour.
8. Tap `Continue to Reflections`.
9. Assert Reflections tour.
10. Attempt to switch Reflections into Questions mode while the tour is visible; assert the overlay remains and underlying mode does not change.
11. Tap `Finish Tour` and relaunch; assert onboarding does not reappear.

---

## Verification Commands

Run:

```bash
./gradlew testDebugUnitTest assembleDebug
```

If an emulator is available, also run a manual smoke pass with:

```bash
./gradlew installDebug
adb shell pm clear com.whitestone.app
adb shell monkey -p com.whitestone.app 1
```

Capture screenshots or UI hierarchy dumps for Welcome, Today coach step 0, Today coach step 1, first-stone success, Review tour, and Reflections tour.
