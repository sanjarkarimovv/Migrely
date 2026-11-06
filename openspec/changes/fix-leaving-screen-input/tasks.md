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

- [ ] 5.1 `code-reviewer`'s delta on the fade and guard commits, with fixes recorded here.
- [ ] 5.2 Device re-walk on `emulator-5570`, because every walked gap shifts with the 200 ms fade: the #138, #139, #140 and #142 matrix, 200% font and landscape, the legitimate-navigation paths, and the instrumented suites not rerun since #148. Verify: results in the verification record below. Include a tap on the list 50 and 300 ms after Back (#148), and record what each does.
- [ ] 5.3 `qa-engineer` retests on an R8 release build: #138, #139, #140, #142, #148 by the reporters' own steps, and confirms that no fade plays with "Remove animations" on. The reporter closes each issue.
- [ ] 5.4 Gate on the final head after 5.1. Verify: `./gradlew assembleDebug test ktlintCheck detekt lint` and `./gradlew :app:checkReleasePermissions`, each with its exit code.

## 6. Finish

- [ ] 6.1 `android-engineer` archives after 5.3. Verify: `openspec archive fix-leaving-screen-input --yes`, then `openspec validate --specs --strict`.

## Verification record

To be filled in by 5.1 to 5.4. Earlier results are in PR #143 and PR #147; the #147 walk is from before the 200 ms fade.

Known limits: a real phone, a human hand and TalkBack on a device are not covered by the tasks above.
