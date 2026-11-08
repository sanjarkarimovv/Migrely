# Tasks

The code is on `bugfix/142-leaving-screen-input` at 10261340, stacked on `bugfix/138-stale-navigation` (PR #143). A ticked code
task here means that the code and test exist at that head and the result is the engineer's, from the texts of PR #143 and
PR #147. `product-manager` read the code and did not rerun the tests.

## 1. Plan

- [x] 1.1 BRD: D154 to D157, EC-UX-21 to 23, EC-MED-14, EC-MED-15, EC-STO-05, §13 row 29. Verify: `grep -c "^| D15[4-7] " docs/BRD.md` prints 4, and none of the four is on another remote branch right before the merge. If one is, the second to merge renumbers.
- [x] 1.2 Proposal, design, deltas, tasks. Verify: `openspec validate fix-leaving-screen-input --strict`.

## 2. Navigation and the restart (PR #143)

- [x] 2.1 `BaseComposeHolder.ViewModelObserver` drops a navigation event unless its screen is the current entry, by id. Verify: `./gradlew :architecture:ui:connectedDebugAndroidTest` with `ANDROID_SERIAL=emulator-5570`, `ViewModelObserverNavigationTest` 13/13 in the PR. With no guard, 7 of 12 failed.
- [x] 2.2 `DeleteAllDataObserver` beside the `NavHost`. Verify: `./gradlew :features:settings:ui:testDebugUnitTest --tests "*DeleteAllDataObserverTest"`, 5/5 in the PR. On the old place, 2 of 5 failed.

## 3. The editor (PR #143, #147)

- [x] 3.1 `key(medicationId)`, the id check, a direct collect, and a form winning over NotFound. Verify: `./gradlew :features:medication:ui:testDebugUnitTest --tests "*EditMedicationIdTest"`, 12/12 in the PR. Mutants: no id check 2 of 12 fail, no form check 1 of 12 fail.
- [x] 3.2 `TapGuard` over Kind and Category only, 700 ms. Verify: `./gradlew :features:medication:ui:testDebugUnitTest --tests "*EditMedicationTapGuardTest"`, 8/8 in the PR. Mutants: none, 500 ms and whole form each fail 3 of 8.

## 4. Touches and the fade (PR #147)

- [x] 4.1 `screen()` and `touchesOnlyWhile` on every destination. Verify: `./gradlew :app:testDebugUnitTest --tests "*LeavingScreenInputTest"`, 14/14 in the PR. Mutants: consume off 4 of 14 fail, no shift 1 of 14, no `propagateMinConstraints` 1 of 14.
- [x] 4.2 `ScreenNavHost`, one 200 ms linear fade. Verify: `theFadeIs200MillisecondsAndLinear`. Mutants: 700 ms 3 of 14 fail, default easing 1 of 14.
- [x] 4.3 `docs/ARCHITECTURE.md` section 8. Verify: read at 10261340.

## 5. Still owed

- [x] 5.1 `code-reviewer`'s delta on the fade and guard commits, with fixes recorded here.
- [x] 5.2 Device re-walk on `emulator-5570`, because every walked gap shifts with the 200 ms fade: the #138, #139, #140 and #142 matrix, 200% font and landscape, the legitimate-navigation paths, and the instrumented suites not rerun since #148. Verify: results in the verification record below. Include a tap on the list 50 and 300 ms after Back (#148), and record what each does.
- [x] 5.3 `qa-engineer` retests on an R8 release build: #138, #139, #140, #142, #148 by the reporters' own steps, and confirms that no fade plays with "Remove animations" on. The reporter closes each issue.
- [x] 5.4 Gate on the final head after 5.1. Verify: `./gradlew assembleDebug test ktlintCheck detekt lint` and `./gradlew :app:checkReleasePermissions`, each with its exit code.

## 6. Finish

- [x] 6.1 `android-engineer` archives after 5.3. Verify: `openspec archive fix-leaving-screen-input --yes`, then `openspec validate --specs --strict`.

## Verification record

Final head 2f800c71. Earlier results are in PR #143 and PR #147, and the #147 walk there is from before the 200 ms fade.

- **5.1** `code-reviewer`'s delta on the fade and guard commits: no Critical or Important, as reported by `team-lead`. Its N2 and N5 were fixed at 2f800c71 and 741cc769.
- **5.3** `qa-engineer`: GO at exactly 2f800c71, as reported by `team-lead`. The engineer did not read QA's report for this record. The reporter closes each issue.
- **5.4** Gate at 2f800c71: exit 0, run by the engineer and again by `team-lead`. `:app:checkReleasePermissions`: exit 0, no INTERNET, 5 permissions.
- **5.2** Engineer's walk at 2f800c71: `emulator-5570`, debug, started with `-gpu host`, which `team-lead` has since ruled gives slow frames and does not count as timing evidence, one injected finger, gaps from `getevent`. Instrumented: `:architecture:ui` 13/13, `:features:medication:ui` 14/14, `:designsystem` 48/48, `:datasource:implementation` 112/112, `:app` 1/1.

| Case | Gaps achieved | Result |
|---|---|---|
| #138 resting finger | 45-712 ms | right row 16/16 |
| #138 two taps | 25-276 ms | right row 18/18 |
| #138 at 200% font | rest 177, 327; taps 109, 143 | right row 4/4 |
| #138 in landscape | rest 298; taps 125; plus a single-tap control | right row 2/2 |
| Overuse warning | rest 302 | Home and History warnings stay |
| #139 two rows, taps | 108-141 ms | Home with Undo, one deleted, 4/4 |
| #139 same row, taps | 93-161 ms | Home with Undo, one deleted, 6/6 |
| #139 two rows, resting finger | asked 100-300 ms | Home with Undo, one deleted, 6/6 |
| #142 Delete plus Back, Home and History | 94-319 ms | nothing deleted, 9/9 |
| #142 Remove | 115-313 ms | nothing archived, 6/6 |
| Delete plus Add medication; the second-row gesture (density 160) | 118-323 ms | nothing deleted, 3/3 and 2/2 |
| Delete all data, word typed, resting finger plus Back | 306 ms and one more | nothing deleted, 2/2 |
| #140 on Edit, no-guard APK | click 182-705 ms | Preventive 4/8 |
| #140 on Edit | click 159-694 ms | Acute 8/8 |
| #140 on Add, no-guard APK | click 163-697 ms | Preventive 5/8 |
| #140 on Add | click 198-690 ms | not chosen 8/8 |

**#148, a tap on row 2 of the list after the editor's Back arrow, by press gap.** It did not land reliably at 300 ms in this walk, on a debug build with `-gpu host`, so read this as a result on that setup and not as a timing measure. The reason was not found. Reverted APK at 297-320 ms: 0/3. A later walk with `-no-window -gpu swiftshader_indirect`, on `feature/fix-double-tap-writes`, a different route (a double tap on Back into Settings), landed 2/2 at 273-274 ms and 2/2 at 345-350 ms.

| Press gap | Landed |
|---|---|
| Under 280 ms | 0/9 |
| 280-340 ms | 1/6 |
| 340-420 ms | 2/8 |
| 420-500 ms | 1/4 |
| 500-620 ms | 2/3 |
| 620-1100 ms | 8/8 |

"Remove animations" on the same route, 14 trials from 19 to 519 ms: landed at 197 and 318 ms only. No frame capture was made at that scale, so that walk does not say whether a fade played; 5.3 is QA's.

Layout: 20 of 21 screens identical to #143's APK, and History differs only by develop's overuse wording. Not walked: the merge double tap on this head (9710061b, 5/5) and TalkBack.

Known limits: a real phone, a human hand and TalkBack on a device are not covered by the tasks above.
