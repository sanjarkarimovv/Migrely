# Tasks

## 1. Presentation (`:features:home:presentation`)

- [ ] 1.1 Add `Undone` and `UndoNotPossible` to `HomePresentationNotification` and notify them from `presentUndo` (design D7); extend `HomeViewModelTest` for undo succeeded, not possible and failed; verify `./gradlew :features:home:presentation:test` passes

## 2. Design system (`:designsystem`)

- [ ] 2.1 Add `fraunces.ttf` and `figtree.ttf` to `res/font` and the two OFL texts to `assets/licenses/fonts/` (design D1, D3); verify `./gradlew :designsystem:assembleDebug` packages them
- [ ] 2.2 Rewrite `Type.kt` with the full type scale from design D2 (Fraunces roles with `SOFT`, `WONK` and `opsz`, Figtree for the rest); add a unit test that checks every body style's line height is at least 1.4× its size and every weight is 400 or more; verify `./gradlew :designsystem:testDebugUnitTest` passes
- [ ] 2.3 Add the `snackbar` package: `SnackbarTone`, the three tone icons as `ImageVector`s, `SnackbarActionUiModel`, `MigrelySnackbar` (custom action layout, close button, time-left line, swipe to dismiss, live region) and `MigrelySnackbarHost` (top placement in safe-drawing insets, 560 dp max width, 250/200 ms motion, none with reduce motion), with previews for every tone, a long text and 200% font (design D4); verify `./gradlew :designsystem:build`
- [ ] 2.4 Add Compose instrumented tests for `MigrelySnackbar`: text and icon per tone, action click, close click, the spoken undo label, live region modes, and the action moving below the text at font scale 2.0; save a screenshot of each case for review; verify `ANDROID_SERIAL=emulator-5570 ./gradlew :designsystem:connectedDebugAndroidTest` passes

## 3. Home UI (`:features:home:ui`)

- [ ] 3.1 Make `HomeMessageUiModel` `Parcelable`, add `Undone` and `UndoNotPossible`, and map the new presentation notifications in `HomeNotificationUiMapper` with tests; verify `./gradlew :features:home:ui:testDebugUnitTest` passes
- [ ] 3.2 Add `HomeMessageBehaviourUiModel` and `HomeMessageBehaviourUiMapper` (tone, undo target, base duration from design D5), and `ShownHomeMessageUiModel` with `remainingMillis`, with parameterized tests; verify UI unit tests pass
- [ ] 3.3 Replace `SnackbarHost` in `HomeScreen` with `MigrelySnackbarHost`. Keep the shown message in `rememberSaveable`, apply the accessibility timeout, clear the message on time, undo, close and swipe, and add the reject haptic for errors and the new strings (design D5, D6, D8); verify `./gradlew :features:home:ui:build`
- [ ] 3.4 Use `titleLarge` (Fraunces) for the calming line in attack mode and `tnum` for the start and elapsed times; check the today card, attack mode and 72-hour card previews; verify `./gradlew :features:home:ui:build`

## 4. Docs

- [ ] 4.1 Update `docs/ARCHITECTURE.md` §10 with the bundled fonts, the type roles and the snackbar component; verify the section names both fonts and the `snackbar` package

## 5. Verify

- [ ] 5.1 Run `./gradlew assembleDebug test ktlintCheck detekt`; verify it ends with BUILD SUCCESSFUL and record the test count
- [ ] 5.2 Walk the `in-app-messages` and `brand-typography` scenarios on the `Migrely_API_36` emulator and record each result:
  - start, end and undo with the countdown line;
  - undo "Undone", and the rejected move message;
  - font scale 2.0, landscape, rotation during the undo window, return after the window;
  - reduce motion, and the accessibility timeout (`adb shell settings put secure accessibility_interactive_ui_timeout_ms 30000`);
  - swipe to dismiss, offline first launch, and the licence files in the APK.

  Restore every setting afterwards.
- [ ] 5.3 Run `openspec validate refine-messages-and-type --strict`; verify it reports the change as valid

## Workflow follow-up

- Commit per task group with the session's attribution, push `feat/refine-messages-and-type` and open a PR stacked on PR #2.
- Archive after review, so `in-app-messages` and `brand-typography` become main specs.
