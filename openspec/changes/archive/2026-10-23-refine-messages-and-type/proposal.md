# Proposal

## Why

The owner reviewed change 1 on a device and found two things that make Migrely look unfinished:

- **Messages:** the app uses Material's stock snackbar. Every message looks the same and disappears after 5 seconds. That
  includes "storage full", which means the data was not saved. People who need more time to act, such as TalkBack users
  or anyone who has raised Android's accessibility timeout, get no extra time. Rotating the phone loses the undo, and an
  undo that cannot run fails silently.
- **Type:** every screen uses the system font (Roboto). It reads as a default Android app, not as a calm, trustworthy
  diary (BRD 18.4 G5, 18.5).

## What Changes

- **Brand type (BRD D20, EC-UX-17):** Fraunces (a soft serif) for titles and the one calming line in the dark room, and
  Figtree for all other text, buttons and numbers.
  - Both are bundled variable fonts, so they work offline.
  - The type scale gets generous line height, no weight below 400 for text, and tabular figures for times that tick.
- **Custom snackbar (BRD D21, EC-UX-16)** in `:designsystem`, used by the home screen:
  - Three kinds, each with its own icon shape: confirmation ✓, information i and error !.
  - The undo window stays 5 seconds, or longer when the user's accessibility timeout asks for more. A thin line shows
    the time left; with reduce motion the line is hidden.
  - Information and the generic error stay 8 seconds. "Storage full" stays until the user dismisses it.
  - A new message replaces the current one. A message survives rotation and a short trip to the background with the
    time it has left.
  - Messages can be swiped away or closed with a close button.
  - Undo outcomes are confirmed: "Undone" when it worked, and a short explanation when it no longer can run.
  - TalkBack reads messages politely, and errors at once. Each action has a specific label.
  - At 200% font size the action moves below the text, and no text is ever cut off.
  - Messages enter and leave gently, and appear at once with reduce motion.
  - Errors get a short "reject" haptic. Start and end keep their confirm haptic.
- **Spec change (`attack-logging`):** the start and end undo windows follow the snackbar's accessible timing instead of
  a fixed 5 seconds.

**Out of scope:**
- An "Open-source licences" screen. The licence texts ship in the app now; settings (change 12) will show them.
- Cyrillic, Greek or CJK glyphs. Those scripts fall back to the system font until a language that needs them is added
  (BRD 6.2 lists only Latin-script languages and Japanese).
- A "Retry" action for failures, and a shortcut to the phone's storage settings from "storage full".
- Snackbars on screens other than home. They will reuse the component when those screens are built.

## Capabilities

### New Capabilities

- `in-app-messages`: how short messages are shown, timed, dismissed, announced and kept across rotation.
- `brand-typography`: the bundled typefaces, the roles they play, and the reading-comfort rules for text and numbers.

### Modified Capabilities

- `attack-logging`: "Start an attack in one tap" and "End an attack in one tap" define the undo window through
  `in-app-messages` timing (5 seconds, extended by the accessibility timeout).

## Impact

- **Code:**
  - `:designsystem`: fonts in `res/font`, the new type scale, a `snackbar` package (component, host, tone, icons) and
    the font licences in `assets`.
  - `:features:home:presentation`: two new notifications, `Undone` and `UndoNotPossible`.
  - `:features:home:ui`:
    - parcelable message models and a saveable shown-message model;
    - a mapper from message to behaviour (tone, action, timing);
    - the new host replaces `SnackbarHost`;
    - tabular figures on the elapsed time.
- **Dependencies:** none new. The Compose UI test artifacts are added to `:designsystem` as test-only dependencies.
- **APK size:** about +0.4 MB of fonts (BRD NFR limit 15 MB).
- **Docs:** BRD 9.9 (EC-UX-16, EC-UX-17), 13 (change 1a), 17 (D20, D21), 18.5. `docs/ARCHITECTURE.md` §10 covers
  fonts and the snackbar component.
- **BRD references:** ATK-F1, ATK-F2, EC-UX-01, EC-UX-03, EC-UX-04, EC-UX-08, EC-UX-15, EC-UX-16, EC-UX-17, 18.4 G5,
  18.5, D19, D20, D21.
