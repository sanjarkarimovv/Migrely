# Design

## Context

After `add-attack-logging`:
- the theme in `:designsystem` uses `FontFamily.Default` for every style;
- the home screen shows messages with Material's `SnackbarHost` and `Snackbar`, all with the same look and the same
  5-second timeout;
- `HomeMessageUiModel` is a plain sealed interface; the shown message lives in a `SnackbarHostState` that is lost on
  rotation;
- `UndoResultDomainModel.NotPossible` reloads the screen without telling the user.

See proposal.md for motivation and the specs for the required behaviour. Modules touched, in checklist order:
`:features:home:presentation`, `:designsystem`, `:features:home:ui`. Domain, data, datasources and `:app` do not change.

## Goals / Non-Goals

**Goals:**
- One snackbar component in `:designsystem` that every feature can reuse.
- Message timing that respects the user's accessibility timeout and survives rotation.
- A type system that looks deliberate and stays comfortable for people with photophobia and pattern glare.

**Non-Goals:**
- No change to how the ViewModel emits notifications (one-off, replay 0) beyond two new cases.
- No screenshot-test framework; the component is checked with Compose instrumented tests and on the emulator.

## Decisions

### D1. Typefaces: Fraunces for titles, Figtree for text

The candidates were rendered with the app's real strings on the real palette and measured (x-height, average advance,
tabular figures, weights, scripts, file size):

| Font | Result |
|---|---|
| **Figtree** (text) | Clean and friendly. Has tabular figures and weights 300–900. Its average advance (0.499 em) matches Roboto. Only 61 KB. **Chosen.** |
| **Fraunces** (display) | With Soft 100 and Wonky 0 the serifs are rounded and the stroke contrast is low at small optical sizes. It sets a warm diary voice and holds up on dark backgrounds. 351 KB. **Chosen.** |
| Atkinson Hyperlegible Next | Built for low vision, but it slashes the zero in every time and date ("10:05", "30 min") and has no alternate to turn that off. Its I/l/1 shapes rarely matter in short labels. |
| Lexend | Pleasant and widely spaced, but has no tabular figures at all. |
| Roboto, Inter | Neutral. This is the "default Android" look the owner rejected. |
| Plus Jakarta Sans, Manrope | Tight spacing makes denser stripes of text (pattern glare). |
| Newsreader | Thin hairlines bloom or vanish on the dark theme. |
| Nunito Sans | Low x-height (0.484 em), so it reads smaller. |
| Literata | Heavy and bookish at title size. |

Both fonts are bundled as variable TTF files in `designsystem/src/main/res/font` (`fraunces.ttf`, `figtree.ttf`).
Compose's `Font(resId, weight, variationSettings = FontVariation.Settings(...))` creates one entry per weight:
- Fraunces sets `wght`, `SOFT` = 100, `WONK` = 0 and `opsz` (24 for titles, 36 for headlines, 72 for display);
- Figtree sets `wght` only.

`minSdk` 26 supports font variations.

### D2. Type scale

| Style | Family | Size / line height (sp) | Weight | Used for |
|---|---|---|---|---|
| displayLarge, displayMedium, displaySmall | Fraunces, opsz 72 | 57/64, 45/52, 36/44 | 400 | not used yet |
| headlineLarge, headlineMedium, headlineSmall | Fraunces, opsz 36 | 32/40, 28/36, 24/32 | 500 | future screen titles |
| titleLarge | Fraunces, opsz 24 | 24/31 | 500 | sky card title, the calming line in attack mode |
| titleMedium, titleSmall | Figtree | 18/25, 15/21 | 600 | card titles such as the 72-hour check |
| bodyLarge, bodyMedium, bodySmall | Figtree | 17/25, 15/22, 13/19 | 400 | text (line height ≥ 1.45×) |
| labelLarge, labelMedium, labelSmall | Figtree | 18/24, 14/20, 12/16 | 600 | buttons, actions, chips |

Body letter spacing stays at 0.15 sp. The elapsed and start times add `fontFeatureSettings = "tnum"` at the call
site, because tabular figures in running text would look gappy.

### D3. Font licences

`designsystem/src/main/assets/licenses/fonts/OFL-Fraunces.txt` and `OFL-Figtree.txt` are copied from the
`google/fonts` repository. Assets are not removed by resource shrinking. The settings change will show them.

### D4. Snackbar component (`:designsystem`, package `com.migrely.app.designsystem.snackbar`)

- **`SnackbarTone`:** `Confirmation`, `Information`, `Error`. Each tone has an icon drawn as an `ImageVector` in code
  (D19): check mark, "i" in a circle, "!" in a circle. Each tone also has a colour: `Positive`, `Secondary` or `Error`.
- **`MigrelySnackbar(text, tone, action, onDismiss, timeLeft, modifier)`** is stateless:
  - container `SurfaceContainerHigh`, 1 dp `OutlineVariant` border, 20 dp corners, no shadow;
  - text `bodyMedium` in `OnSurface` (#C8CCD2), with no line limit;
  - `action` is `SnackbarAction(label, spokenLabel, onClick)` or null. It is not named `*UiModel`, because the
    Konsist rule keeps `*Model` classes in `model` packages;
  - without an action it shows a 48 dp close button labelled "Dismiss";
  - `timeLeft: (() -> Float)?` draws a 2 dp line in `Primary` at 45% alpha along the bottom edge, read in the draw
    phase so the countdown does not recompose.
- **Layout:** a custom `Layout` puts the action beside the text when the text fits in two lines next to it. Otherwise
  the action goes below, aligned to the end. Measurement uses the text's max intrinsic width.
- **`SwipeToDismissBox`** wraps the component; a sideways swipe calls `onDismiss`.
- **Semantics:** `liveRegion` is `Polite`, or `Assertive` for `Error`. The action's content description is the spoken
  label.
- **`MigrelySnackbarHost(message, reduceMotion, content)`:**
  - places the component at the top within `WindowInsets.safeDrawing` (top and horizontal), at most 560 dp wide and
    centred;
  - wraps it in `AnimatedContent`: slide and fade in over 250 ms, fade out over 200 ms, and `None` with reduce motion.

### D5. Message behaviour mapping (`:features:home:ui`)

`HomeMessageBehaviourUiMapper.toBehaviour(message): HomeMessageBehaviourUiModel`, which holds the tone, the undo
target and the base duration in milliseconds (null means it stays until dismissed). It is a pure class with an
exhaustive `when`.

| Message | Tone | Undo | Base duration |
|---|---|---|---|
| `AttackStarted` | Confirmation | start | 5 000 ms |
| `AttackEnded` | Confirmation | end | 5 000 ms |
| `Undone`, `UndoNotPossible`, `StartMoveRejected`, `EndTimeOutOfRange` | Information | — | 8 000 ms |
| `SomethingWentWrong` | Error | — | 8 000 ms |
| `StorageFull` | Error | — | until dismissed |

The screen extends the base duration with
`LocalAccessibilityManager.current?.calculateRecommendedTimeoutMillis(base, containsIcons = true, containsText = true,
containsControls = true)`. That reads Android's "Time to take action" setting.

### D6. Shown message survives rotation

`ShownHomeMessageUiModel(message, shownAtEpochMillis)` is `@Parcelize`, so `HomeMessageUiModel` becomes `Parcelable`.
- The screen keeps it in `rememberSaveable`.
- A `LaunchedEffect(shown)` waits for the time left, `shownAt + timeout - now`, then clears it.
- `remainingMillis(now, timeout)` is a function on the model and is unit tested.
- When the app returns after the time has passed, the remainder is 0 or less and the message is cleared at once.
- The presenter flow still drops messages while no collector is active, as before; a message arriving while the screen
  is away is not needed, because the reload shows the state.

### D7. Undo outcomes (`:features:home:presentation`)

`HomePresentationNotification` gains `Undone` and `UndoNotPossible`. `presentUndo` notifies them, and still reloads
today.

### D8. Haptics

`HapticFeedbackType.Confirm` stays for start and end. `HapticFeedbackType.Reject` is added for `StorageFull` and
`SomethingWentWrong`.

## Risks / Trade-offs

- **Fraunces is a display face.** If it is used for long text it would tire the eye, so D2 limits it to titles and the
  one calming line.
- **The fonts cover only Latin scripts.** Cyrillic or Greek text falls back to the system font, which is acceptable
  until such a language is planned (BRD 6.2).
- **Wall-clock deadlines.** A manual clock change during the 5 seconds can shorten or lengthen the window. This is
  harmless; the domain undo guards still apply.
- **Some tones rarely appear.** "Storage full" and the generic error cannot be triggered safely on the emulator. The
  Compose instrumented test renders every tone instead.
