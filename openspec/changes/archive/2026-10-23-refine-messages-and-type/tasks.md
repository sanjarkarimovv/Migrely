# Tasks

## 1. Presentation (`:features:home:presentation`)

- [x] 1.1 Add `Undone` and `UndoNotPossible` to `HomePresentationNotification` and notify them from `presentUndo` (design D7); extend `HomeViewModelTest` for undo succeeded, not possible and failed; verify `./gradlew :features:home:presentation:test` passes

## 2. Design system (`:designsystem`)

- [x] 2.1 Add `fraunces.ttf` and `figtree.ttf` to `res/font` and the two OFL texts to `assets/licenses/fonts/` (design D1, D3); verify `./gradlew :designsystem:assembleDebug` packages them
- [x] 2.2 Rewrite `Type.kt` with the full type scale from design D2 (Fraunces roles with `SOFT`, `WONK` and `opsz`, Figtree for the rest); add a unit test that checks every body style's line height is at least 1.4× its size and every weight is 400 or more; verify `./gradlew :designsystem:testDebugUnitTest` passes
- [x] 2.3 Add the `snackbar` package: `SnackbarTone`, the three tone icons as `ImageVector`s, `SnackbarAction` (not `*UiModel`: Konsist keeps `*Model` classes in `model` packages), `MigrelySnackbar` (custom action layout, close button, time-left line, swipe to dismiss, live region) and `MigrelySnackbarHost` (top placement in safe-drawing insets, 560 dp max width, 250/200 ms motion, none with reduce motion), with previews for every tone, a long text and 200% font (design D4); verify `./gradlew :designsystem:build`
- [x] 2.4 Add Compose instrumented tests for `MigrelySnackbar`: text and icon per tone, action click, close click, the spoken undo label, live region modes, and the action moving below the text at font scale 2.0; save a screenshot of each case for review; verify `ANDROID_SERIAL=emulator-5570 ./gradlew :designsystem:connectedDebugAndroidTest` passes

## 3. Home UI (`:features:home:ui`)

- [x] 3.1 Make `HomeMessageUiModel` `Parcelable`, add `Undone` and `UndoNotPossible`, and map the new presentation notifications in `HomeNotificationUiMapper` with tests; verify `./gradlew :features:home:ui:testDebugUnitTest` passes
- [x] 3.2 Add `HomeMessageBehaviourUiModel` and `HomeMessageBehaviourUiMapper` (tone, undo target, base duration from design D5), and `ShownHomeMessageUiModel` with `remainingMillis`, with parameterized tests; verify UI unit tests pass
- [x] 3.3 Replace `SnackbarHost` in `HomeScreen` with `MigrelySnackbarHost`. Keep the shown message in `rememberSaveable`, apply the accessibility timeout, clear the message on time, undo, close and swipe, and add the reject haptic for errors and the new strings (design D5, D6, D8); verify `./gradlew :features:home:ui:build`
- [x] 3.4 Use `titleLarge` (Fraunces) for the calming line in attack mode and `tnum` for the start and elapsed times; check the today card, attack mode and 72-hour card previews; verify `./gradlew :features:home:ui:build`

## 4. Docs

- [x] 4.1 Update `docs/ARCHITECTURE.md` §10 with the bundled fonts, the type roles and the snackbar component; verify the section names both fonts and the `snackbar` package

## 5. Verify

- [x] 5.1 Run `./gradlew assembleDebug test ktlintCheck detekt`; verify it ends with BUILD SUCCESSFUL and record the test count
- [x] 5.2 Walk the `in-app-messages` and `brand-typography` scenarios on the `Migrely_API_36` emulator and record each result:
  - start, end and undo with the countdown line;
  - undo "Undone", and the rejected move message;
  - font scale 2.0, landscape, rotation during the undo window, return after the window;
  - reduce motion, and the accessibility timeout (`adb shell settings put secure accessibility_interactive_ui_timeout_ms 30000`);
  - swipe to dismiss, offline first launch, and the licence files in the APK.

  Restore every setting afterwards.
- [x] 5.3 Run `openspec validate refine-messages-and-type --strict`; verify it reports the change as valid

## Verification record (2026-10-23)

- 5.1 Gate: BUILD SUCCESSFUL. 170 unit tests (debug variants), 0 failures:

  | Module | Tests |
  |---|---|
  | domain | 48 |
  | presentation | 28 |
  | data | 22 |
  | UI | 36 |
  | datasource | 18 |
  | app with Konsist | 16 |
  | designsystem | 2 |

  Instrumented: `MigrelySnackbarTest` 7/7 and `AttackDaoTest` 7/7 on `Migrely_API_36`.
- 5.2 Emulator walk, all as specified:
  - **Undo countdown:** start and end show a confirmation with "Undo"; the line shrinks over 5 s.
  - **Undo outcomes:** tapping "Undo" on an end reopens the attack and shows "Undone."; a rejected start move shows "Your previous migraine ended at 4:37 PM." with a close button.
  - **Rotation:** rotating 2 s into the undo window keeps the message with its remaining line, and it is gone by the next check.
  - **Landscape:** the message is centred and capped at 560 dp.
  - **Accessibility timeout:** with `accessibility_interactive_ui_timeout_ms` = 30000 the undo was present at 14 s and 24 s and gone at 35 s.
  - **Reduce motion:** no countdown line.
  - **Font scale 2.0:** "Migraine started" and "Undo" fit on one row; the instrumented test covers the action moving below a long error.
  - **Swipe:** swiping sideways dismissed "Migraine ended" without undoing it.
  - **Offline first launch:** airplane mode and cleared data showed the sky title in Fraunces and the rest in Figtree.
  - **Licences:** `assets/licenses/fonts/OFL-Fraunces.txt` and `OFL-Figtree.txt` are in the APK.
  - **Settings restored:** animator 1, font 1.0, rotation 0, airplane off, accessibility timeout unset.
- Also fixed: library instrumented test APKs now set `testOptions.targetSdk`, so Android no longer shows the "built for an older version of Android" dialog during test runs.
- Not checked on the emulator: the reject haptic (the emulator has no vibration motor). "Storage full" and "something went wrong" cannot be triggered safely; they are covered by the mapper tests and the instrumented tone tests.
- 5.3 `openspec validate refine-messages-and-type --strict`: valid.

## Workflow follow-up

- Commit per task group with the session's attribution, push `feat/refine-messages-and-type` and open a PR stacked on PR #2.
- Archive after review, so `in-app-messages` and `brand-typography` become main specs.
