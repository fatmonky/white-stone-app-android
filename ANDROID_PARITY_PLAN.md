# Android Parity Plan — White Stone

**Status:** Drafted 2026-05-16. Brings the Android app to feature parity with iOS Phases 1–4 of the [v1.2 plan](../white_stone/IMPLEMENTATION_PLAN_v1_2.md). Phase 5 is deferred until iOS also ships it.

**Audience:** Future Claude Code sessions or any developer continuing the Android port. Each PR section is self-contained enough to be picked up cold.

**Source of truth:** iOS repo at `/Users/pjteh/Desktop/AI Projects/white_stone` (DO NOT modify). Mirror iOS semantics byte-for-byte where called out (enum raw values, question rotation, pattern thresholds), since the spec requires the same date to produce the same question across platforms.

---

## Current gap (snapshot 2026-05-16)

| Area | iOS today | Android today | Gap |
|---|---|---|---|
| Tabs | Today / Review / Reflections / About | Today / Calendar / Trends / About | Restructure to 4 new tabs |
| Stone model | `type`, `note`, `timestamp`, `dayKey`, `root`, multi-root CSV, `rootDescriptor`, `intensity` | `type`, `note`, `timestamp`, `dayKey` only | Add 3 nullable columns + migration |
| Reflection model | `Reflection` SwiftData entity | none | New Room entity + DAO + migration |
| Patterns | `PatternEngine.swift` (pure function, observation rules 1–5) | none | Port to Kotlin |
| Source links | Tappable `Link` in About/Reflections | n/a | Use `AnnotatedString` + `UriHandler` |
| Streak counter | Preserved inside Review | Lives in Trends | Move into Review |
| AddStone tagging UI | Optional root chips (multi + custom descriptor) + intensity chips | none | New chip rows + viewmodel state |
| Tests | iOS still light | none | Add `app/src/test` migration + pattern unit tests |

Android is at pre-v1.2 baseline. iOS has shipped Phases 1–4. Phase 5 (evening closure ritual) is unshipped on both platforms.

---

## Architectural decisions (apply to all PRs)

1. **Mirror iOS enum raw values exactly.** `StoneRoot` raw values: `sensual`, `illWill`, `harming`, `renunciation`, `kindness`, `harmlessness`. `StoneIntensity`: `strong`, `weak`. This keeps stored data structurally identical across platforms.
2. **Use a single `rootTagsCsv: String?` column on Android** (comma-joined raw values). iOS carries both a legacy `root` and a CSV `rootTagsRawValue` only for SwiftData migration safety — Android starts clean and does not need the legacy single-root field. Custom user descriptors go in a separate `rootDescriptor: String?` column, newline-joined, matching iOS.
3. **Reflection date = `dayKey` string (`yyyy-MM-dd`)**, not a `Long`/`Instant`. Mirrors the existing `Stone.dayKey` approach, sidesteps timezone bugs, and keeps DAO queries trivial.
4. **Enable schema export.** Flip `exportSchema = false` → `true` on `@Database`. Commit generated JSON under `app/schemas/`. Required for `MigrationTestHelper`. Never ship `.fallbackToDestructiveMigration()`.
5. **Bump DB version once per PR that changes schema.** Each migration is hand-written and unit-tested.
6. **No new third-party dependencies.** Project policy. If a phase tempts you toward a library, stop and surface to the user.
7. **Local-first absolute.** No analytics, no telemetry, no cloud. Same as iOS guiding principles in the v1.2 plan §0.

---

## PR 1 — `phase-1-review-tab`

**Goal:** Collapse the existing Calendar and Trends tabs into a single Review tab while preserving the streak counter exactly as-is.

### Tasks

1. Create `app/src/main/java/com/whitestone/app/ui/review/`. Move `ui/calendar/*` and `ui/trends/*` into it (e.g. `review/calendar/`, `review/charts/`, `review/patterns/`).
2. New `ReviewScreen.kt` (tab root) with this top-to-bottom layout:
   - **Stat strip** (low emphasis, no emoji): total days tracked; this-month tally `N white · M black`; "most-tagged root in the last 14 days" line — gate this line on Phase 3 data existing, so in PR 1 it is hidden.
   - **Streak counter** — moved verbatim from Trends. Same numeric value, same logic. Do NOT redesign. Place it inside the stat strip or alongside the 14-day bars — whichever fits cleanly.
   - **Calendar** — existing month grid + day-ratio coloring, prev/next month nav. Each day cell gets a tiny reflection-presence dot (stub: always false in PR 1; wired in PR 3). Tap → DayDetail.
   - **Secondary views** under a segmented control or horizontal pager:
     a. **14-day bars** — port from Trends.
     b. **All-time** — new month-over-month stacked bar chart (white vs. black per month). Compute from existing data.
     c. **Patterns** — stub composable showing: *"Patterns will appear here once you've logged a few weeks of stones."*
3. Update `ui/navigation/Screen.kt`:
   - Remove `Calendar` and `Trends`.
   - Add `Review`.
   - Reorder bottom-nav: Today / Review / *(Reflections stub — see PR 3)* / About. In PR 1 just go Today / Review / About (3 tabs), then PR 3 inserts Reflections.
4. Update `WhiteStoneNavGraph.kt` and bottom-nav tab definitions accordingly. Grep for route literals (`"calendar"`, `"trends"`) anywhere they're persisted.
5. Tab icon: `Icons.Filled.CalendarMonth` or similar — no badge, nothing implying score.

### Acceptance

- App still launches into Today; tapping the second tab opens Review.
- Calendar grid behavior unchanged from old Calendar tab.
- 14-day bar chart reachable from Review in one tap/swipe.
- Streak counter still works with the same numeric value as before the refactor.
- A smoke test (`ReviewScreenTest`) asserts the tab title renders and the streak value matches an injected fake stone list.

### Watch-outs

- Don't introduce milestone/celebration copy around the streak.
- Don't lose the existing chart's rendering math (the stacked-bar fix from `cb2adb8` must remain).

---

## PR 2 — `phase-3-stone-tagging`

**Why before Phase 2:** Lands the schema-export + migration scaffolding in isolation so Phase 2 can layer on top without bundling two migrations.

### Tasks

1. Flip `@Database(exportSchema = false)` → `true` on `StoneDatabase`. Configure `room.schemaLocation` in `app/build.gradle`. Commit the generated `app/schemas/com.whitestone.app.data.StoneDatabase/1.json`.
2. Bump DB to version 2.
3. Update `Stone` entity:
   ```kotlin
   @Entity(tableName = "stones")
   data class Stone(
       @PrimaryKey(autoGenerate = true) val id: Long = 0,
       val type: StoneType,
       val timestamp: Long,
       val note: String = "",
       val dayKey: String,
       val rootTagsCsv: String? = null,
       val rootDescriptor: String? = null,
       val intensity: String? = null,
   )
   ```
4. Hand-written `Migration(1, 2)` adding three nullable columns. Register in `DatabaseModule`.
5. New enums in `data/`:
   - `StoneRoot` with raw values matching iOS exactly. Provide `allowedFor(type: StoneType): List<StoneRoot>`.
   - `StoneIntensity` with `strong`, `weak`.
6. Helpers on `Stone` (extension functions OK, keep entity data-class pure):
   - `roots: List<StoneRoot>` parsed from CSV.
   - `customRootDescriptors: List<String>` parsed from newline-joined string.
   - `rootDisplayNames: List<String>` (roots + custom).
   - `tagSummaryText: String?` — `"kindness · strong"` style, `·`-joined, null if empty.
7. Update `AddStoneSheet`:
   - Two new optional rows below note: **Root (optional)** and **Intensity (optional)**.
   - Root: multi-select chips, shown by current stone color. Tap-to-toggle. Add a small "+ custom" affordance that appends a text-input chip; saved descriptors persist per-color and become reusable chips on future sheets.
   - Intensity: two chips `Strong` / `Weak`, single-select, toggle-off on second tap.
   - Flipping stone color clears root selections (since allowed roots change); intensity stays.
   - Save button always enabled regardless of tag state.
8. `StoneDetailScreen`: same chip rows in edit mode; in read mode render `tagSummaryText` next to the timestamp.
9. `StoneTimelineItem` (shared component): if `tagSummaryText != null`, render it as a metadata pill row below the time/note.
10. Tests under `app/src/test/` (or `androidTest/` for migrations):
    - `MigrationTest` using `MigrationTestHelper` v1 → v2 round-trip with existing rows preserved and new columns null.
    - `StoneDaoTest` round-tripping all three new fields.
    - `StoneTagHelpersTest` for CSV parsing edge cases (empty string, malformed values).

### Acceptance

- Existing stones (created before upgrade) display unchanged.
- New stones can save any combination of root/intensity/neither.
- Tapping a selected chip deselects it.
- Root chips visible depend on stone color; flipping color clears prior selections.
- Custom descriptors entered once become reusable chips on future Add Stone sheets for the same color.
- Stone Detail and timeline items render `tagSummaryText` only when present.

---

## PR 3 — `phase-2-reflection-tab`

### Tasks

1. Bump DB to version 3. New `Reflection` entity:
   ```kotlin
   @Entity(
       tableName = "reflections",
       indices = [Index(value = ["dayKey"], unique = true)],
   )
   data class Reflection(
       @PrimaryKey(autoGenerate = true) val id: Long = 0,
       val dayKey: String,            // yyyy-MM-dd
       val questionIndex: Int,        // 0..9
       val responseText: String,
       val createdAt: Long,
       val updatedAt: Long,
   )
   ```
2. Hand-written `Migration(2, 3)` creating the table + unique index. Migration test required.
3. `ReflectionDao`:
   - `getByDayKey(dayKey: String): Flow<Reflection?>`
   - `getAllForQuestion(index: Int): Flow<List<Reflection>>` (ordered by `dayKey ASC` so prev/next is deterministic).
   - `countsByQuestion(): Flow<List<QuestionCount>>` (`questionIndex`, `count`).
   - `upsert(...)`, `deleteByDayKey(...)`.
4. `util/ReflectionQuestions.kt`:
   - 10-element list of strings, verbatim from iOS `ReflectionQuestions.swift`.
   - `fun questionForDate(date: LocalDate): Pair<Int, String>` using `(date.dayOfYear - 1) % 10` — match iOS implementation exactly. Add a property-style test that picks a few known dates and asserts the same index iOS produces.
5. New `ui/reflection/` package:
   - `ReflectionScreen.kt` — tab root, hosts Today subview by default with a top-right "by question" icon to switch.
   - `ReflectionTodayScreen.kt` — today's date + today's question, free-text editor (placeholder *"Take your time. There's no need to write anything."*), single Save button, in-place save confirmation. Above the editor, if there are ≥1 prior reflections on today's question, show a tappable line: *"you've reflected on this question N times before."* opening By-question expanded to today's question.
   - `ByQuestionScreen.kt` — 10 collapsible sections in canonical order. Header text: `"<question> — N reflections"` or `"<question> — no reflections yet on this question."` Expanded section lists all entries inline, newest first, with date + full text. No second tap to read.
   - `ReflectionDetailScreen.kt` — full date + question header, scrollable editable response, "previous on this question" / "next on this question" buttons disabled when no adjacent entry exists. Walks chronologically across same `questionIndex` only.
6. Save behavior:
   - Non-empty save → upsert keyed on `dayKey`.
   - Empty save on a previously-non-empty day → delete the row silently.
   - No skip button.
   - No reflection-specific streak. The only streak is the stone-logging streak from PR 1.
7. **Cross-tab integration** in Review:
   - Calendar day cells: render a tiny uniform-color dot in the corner when `getByDayKey(dayKey)` is non-null.
   - DayDetail (existing screen, but rename source folder if not already under Review): below the stones, append a quiet card with that day's question + response. Tap → opens `ReflectionDetailScreen`. If no stones but a reflection exists, gracefully collapse the stones region.
8. Wire Reflections into bottom-nav as the 3rd tab: Today / Review / Reflections / About. Icon suggestion: `Icons.AutoMirrored.Filled.MenuBook`.
9. About: add Reflections attribution line + tappable SuttaCentral link (`AnnotatedString` with `UrlAnnotation` or `ClickableText` + `LocalUriHandler`). Style: brown, underlined.
10. Tests:
    - `ReflectionQuestionsTest` — date-rotation determinism vs iOS values.
    - `ReflectionDaoTest` — upsert keyed on dayKey, empty-save deletion, prev/next within same questionIndex.
    - `MigrationTest` for v2 → v3.
    - UI smoke: opening Reflection on a fixed date shows the expected question text.

### Acceptance

(Verbatim from v1.2 spec §3 "Acceptance".)

- Opening Reflection on day N shows the same question as `dayOfYear(N) % 10`.
- Writing + saving creates exactly one record per date; reopening same day shows saved text.
- Empty save on a previously-non-empty day deletes the record.
- "You've reflected on this question N times before" appears iff N ≥ 1; tapping opens By-question expanded to today's question.
- By-question groups reflections under correct headers with correct counts; expansion is inline.
- Detail prev/next walks only across same `questionIndex` chronologically.
- Review calendar shows reflection dots only on days with a reflection.
- Review DayDetail shows reflection (if any) below stones; tap opens editable detail view.
- No notifications in this phase.

---

## PR 4 — `phase-4-patterns`

### Tasks

1. Port `Utilities/PatternEngine.swift` → `util/PatternEngine.kt` as a pure function:
   ```kotlin
   data class Observation(val text: String, val priority: Int)
   object PatternEngine {
       fun observe(stones: List<Stone>, now: Instant, zone: ZoneId = ZoneId.systemDefault()): List<Observation>
   }
   ```
2. Implement the 5 observation rules with the iOS thresholds **verbatim**:
   1. **Time-of-day clustering** — ≥10 stones in last 14d; 4-hour buckets (6–10, 10–14, 14–18, 18–22, 22–6); render only if one bucket >50% for that color.
   2. **Most-tagged root** — ≥5 root-tagged stones in last 14d.
   3. **Intensity tilt** — ≥5 intensity-tagged in last 14d; render only if strong:weak ratio ≥2:1 either direction.
   4. **Intensity × color cross-tag** — ≥5 intensity-tagged of a single color in last 14d AND ≥70% share an intensity. At most one observation in this category.
   5. **Logging cadence** — *"logging on most days"* iff ≥10 of last 14 days have entries. Else *"it's been a few days since your last entry"* iff last entry ≥3 days ago. Pure observation, no CTA.
3. Cap at 4 observations rendered; priority order = numeric order above.
4. Phrasing rules (enforce in tests if practical): lower-case, no exclamation marks, no emoji, no future projection, no thanks/congrats, no week-over-week comparisons.
5. `PatternsView` composable replaces the PR 1 stub. Renders the list, or the empty-state line *"Patterns will appear here once you've logged a few weeks of stones."* when `stones.size < 10`.
6. Unit tests covering each rule's threshold edges with synthetic stone fixtures.

### Acceptance

- With <10 stones logged, Patterns shows the single empty-state line.
- With sufficient synthetic data, each rule's expected observation appears.
- Observations update when the Patterns view is (re)entered.

---

## PR 5 — `phase-5-evening-closure` *(deferred)*

Do not start until iOS has shipped Phase 5 OR the user explicitly asks Android to lead. When implemented:

- Settings screen reachable from About (gear icon top-right). Contains exactly: `Evening reflection` toggle (off by default) + `Evening reflection time` picker (default 21:00, only visible when toggle on).
- `AlarmManager.setExactAndAllowWhileIdle` for daily firing. **Not WorkManager** — Doze prevents exact-time delivery. `BOOT_COMPLETED` receiver to re-schedule after reboot.
- `NotificationCompat` channel. Body: *"A quiet moment before the day ends."* No emoji, no urgency.
- Request `POST_NOTIFICATIONS` permission only on toggle-on (Android 13+).
- Tap notification → deep-link to `EveningReflectionSheet` (modal bottom sheet): today's date + flat row of today's stones (no counts, no ratio bar), today's AN 10.51 question, editor pre-filled with any existing Reflection for today, single "Done" button.
- Saving updates the same Reflection record as the Reflections tab — do not create a parallel storage path.

---

## Cross-cutting risks

1. **Schema migrations on installed devices.** Every schema change needs an explicit `Migration` with a `MigrationTestHelper` test. Do NOT ship `fallbackToDestructiveMigration()`.
2. **Tab restructure breaks deep links.** Grep for route literals (`"calendar"`, `"trends"`) anywhere persisted (saved state bundles, future Phase 5 notification intents).
3. **Multi-root storage shape.** iOS keeps a legacy `root` field for SwiftData migration safety; Android intentionally does not. Document this in the entity so future cross-platform reviewers don't get confused.
4. **Reflection date timezone.** Always derive `dayKey` from the user's local zone via `DateHelpers.dayKey(...)` already used by `Stone`. Never store an `Instant` for `Reflection.date`.
5. **Engagement decline is a feature.** Per v1.2 §0, do not introduce streaks beyond the existing one, do not add badges, do not extend the streak with milestones.

---

## Execution order summary

```
PR 1  phase-1-review-tab          (tab restructure, streak preserved, Patterns stub)
PR 2  phase-3-stone-tagging       (schema migration scaffolding, tags in AddStone/Detail)
PR 3  phase-2-reflection-tab      (Reflection entity, two subviews, detail nav, calendar markers)
PR 4  phase-4-patterns            (PatternEngine + Patterns view)
PR 5  phase-5-evening-closure     (deferred until iOS ships it)
```

Each PR is independently shippable. Update `CLAUDE.md`'s "Recent Work Log" after each merge.

## Commit conventions

- One conceptual change per commit.
- Imperative present tense, phase-prefixed: `phase-2: add Reflection Room entity and DAO`.
- Reference the iOS PR / commit in the body when porting behavior so reviewers can diff.

## Things to flag back to the user before merging

- Any migration that risks data loss on populated DBs.
- Any temptation to add a third-party dependency (project policy: zero externals).
- Any observation rule producing nonsense output on the user's real data — surface examples, don't ship.
