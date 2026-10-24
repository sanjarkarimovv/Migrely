# Tasks

## 1. UI (`:features:home:ui`, `:features:history:ui`, `:designsystem`)

- [x] 1.1 Add the mode switch tap guard to `HomeContents` (design D1) with a unit test for `guardsTaps`; verify `./gradlew :features:home:ui:test`
- [x] 1.2 Use the full attack-mode text colour for the start-time line (D3); verify `./gradlew :features:home:ui:assembleDebug`
- [x] 1.3 Key `rememberNowMillis` on the attack's start (D4); verify `./gradlew :features:home:ui:assembleDebug`
- [x] 1.4 Merge semantics instead of clearing them (D2) in `AttackSummaryRow`, `MonthSkyCalendar`'s day cell, the History rows and the month card title, and extend the calendar Compose test to require focus semantics; verify `ANDROID_SERIAL=emulator-5570 ./gradlew :designsystem:connectedDebugAndroidTest`

## 2. Docs

- [x] 2.1 BRD EC-UX-18, EC-UX-19, D32 and row 2a; verify the BRD reads consistently

## 3. Verify

- [x] 3.1 Run the gate `./gradlew assembleDebug test ktlintCheck detekt` and the designsystem instrumented tests; verify all pass
- [x] 3.2 Walk on `emulator-5570`, restoring every setting afterwards:
  - the QA repros for #12 (both cases, at y = 2100 px, with gaps of 0.15 to 0.7 s);
  - the #13 colour;
  - #14 through `uiautomator dump` (`focusable="true"`) and Tab with Enter;
  - #15 with "1 h".

  Record the results here
- [ ] 3.3 Run an independent review of `git diff develop...feature/fix-home-qa-findings`, fix the real findings and validate strictly

## Verification record

**Gate and tests (2026-10-24)**
- `./gradlew assembleDebug test ktlintCheck detekt`: BUILD SUCCESSFUL, 575 unit tests, including 6 new ones for
  `guardsTaps`.
- designsystem instrumented 13/13 on emulator-5570, including a new one: a selectable day is a focusable button.

**Walk on emulator-5570** (Migrely_API_36, 1080×2400 px, the same hit areas as QA: "Migraine started"
`[42,1979][1038,2168]`, "Migraine ended" `[42,2088][1038,2273]`):

| Repro | Gap | Result |
|---|---|---|
| #12 A: double tap on "Migraine started" at y = 2100 px | 0.15 s, 0.25 s, 0.4 s, 0.7 s | One attack in progress, 4/4 ✅ |
| #12 B: double tap on "Migraine ended" at y = 2100 px | 0.15 s, 0.25 s, 0.5 s | The attack ended and no new one started, 3/3 ✅ |
| Deliberate end 3 s after a start | — | Ended ✅ |
| Tap at once after launch | — | Started ✅ (case A's first tap) |

- **One B run at 0.5 s changed nothing:** both taps landed while the app was still loading, under a host load of 18.
  Re-run with a longer wait, it passed. No wrong data was stored.
- **#13:** the start-time line measured #8A909A on #11151F, which is **5.68:1** (4.05:1 before).
- **#14:**
  - `uiautomator dump`: the row, the month title and the days are now a clickable, focusable node, like a Compose
    `Button`, with the description on its child;
  - Tab moved focus row → month title → 1 October → 2 October, each reported `focused="true"`;
  - Shift+Tab back to the row and Enter opened its details.
- **#15:** after "1 h", the line read "Started 10:00 AM · 1 h 0 min" 1.5 s later (before: "59 min" for up to 30 s).
- **Settings:** unchanged throughout (font 1.0, rotation 0 with auto-rotate on, animations 1, auto time zone on).
  The walk's attacks were removed.

