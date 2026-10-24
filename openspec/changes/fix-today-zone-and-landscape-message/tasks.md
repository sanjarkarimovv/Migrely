# Tasks

## 1. Domain (#19, D36, design D1–D2)

- [x] 1.1 `GetTodayRepository.today(from, to)`; `TodaySnapshotDomainModel` with `endedAttacks` and no clock;
  `GetTodayUseCase` reads `GetCurrentTimeRepository`, asks for today ± 2 days in the device zone and keeps the attacks
  whose start-zone dates include today's device date. Tests: flying east, flying west, another zone on the same date,
  across midnight, ending exactly at midnight, the window passed to the repository, the 72-hour check on the use
  case's clock, failure.
  Verify: `./gradlew :features:home:domain:test`

## 2. Data

- [x] 2.1 `HomeRepository.today(from, to)` reads the window it is given; tests updated.
  Verify: `./gradlew :features:home:data:test`

## 3. App wiring

- [x] 3.1 `HomeModule` passes `GetCurrentTimeRepository` to `GetTodayUseCase`.
  Verify: `./gradlew :app:assembleDebug`

## 4. UI

- [x] 4.1 Home `TimeUiFormatter` takes `withCity`; a "Logged today" row names the city once, after the end (D3).
  Tests in `TimeUiFormatterTest`.
  Verify: `./gradlew :features:home:ui:test`
- [x] 4.2 History `HistoryTimeUiFormatter` takes `withCity`; an ended row names the city once, an attack in progress
  keeps it on its start (D3). Tests in `HistoryTimeUiFormatterTest`.
  Verify: `./gradlew :features:history:ui:test`
- [x] 4.3 `HomeContents` keeps messages over the left column in landscape attack mode (`messagesBesideControls`, D4),
  with a unit test.
  Verify: `./gradlew :features:home:ui:test`

## 5. Docs

- [x] 5.1 BRD 9.9 EC-UX-20, 13 row 2c, 17 D36–D38 (done with the plan).
  Verify: `grep -n "D36\|D37\|D38\|EC-UX-20\| 2c " docs/BRD.md`

## 6. Verify

- [x] 6.1 Gate: `./gradlew assembleDebug test ktlintCheck detekt`.
- [x] 6.2 Emulator walk on emulator-5570: travel east and west with the zone and clock set by adb (restored after);
  the city once on home and in History; landscape attack mode with the message at 100% and 200% font and with a
  30 s "Time to take action"; rotation while the message shows; landscape normal mode; portrait unchanged; TalkBack
  labels with `uiautomator dump`. Record the results below.
- [x] 6.3 Independent review of `git diff develop...feature/fix-today-zone-and-landscape-message`; fix and record
  the findings.

## Verification record

**Gate** `./gradlew assembleDebug test ktlintCheck detekt`: BUILD SUCCESSFUL, 705 unit tests (690 before), 0 failures.
New tests: `GetTodayUseCaseTest` +5 (the window, flying east, flying west, another zone on the same date, ending
exactly at midnight), `TimeUiFormatterTest` +2, `HistoryTimeUiFormatterTest` +2, `MessagesBesideControlsTest` 6.

**Instrumented tests:** not run. The datasource and the design system are unchanged.

**Emulator walk** on emulator-5570 (2400×1080, 420 dpi), debug build of this branch. Only the time zone was changed
(`cmd alarm set-timezone`), never the clock.

| Scenario | Result |
|---|---|
| #20 landscape, "Migraine started" | message over the left column, `[297..1147]` px; Mild `[1295..]`, Moderate, Severe, "+ Medication taken" and "Migraine ended" listed by uiautomator and visible |
| #20 tap "Moderate" while the message shows | selected; the message stays |
| #20 "Time to take action" 30 s | the message stays 20 s and more with its time-left line, the controls stay clear |
| #20 200% font in landscape | the chips stack, the message stays over the left column, nothing covered on the right |
| #20 rotation while the message shows | portrait: centred at the top, as before; back to landscape: the left column again; the message is kept |
| #20 Undo from the left-column message | "Undone.", back to normal mode |
| #20 landscape normal mode ("Migraine ended") | centred at the top, as before |
| #19 Pago Pago, 23 Oct 23:28 | the card counts only the Tashkent attack of 22–23 Oct; the 24 Oct Tashkent attacks are not "today" |
| #19 log in Pago Pago, fly east to Kiritimati (25 Oct 00:30) | "Calm so far today", nothing under "Logged today"; the calendar shows 23 Oct with 2 migraines, 24 Oct with 9, today nothing. Card and calendar agree (QA's case) |
| #19 log in Kiritimati (25 Oct), fly west to Pago Pago (23 Oct) | the card shows 2 (22–23 Oct Tashkent, 23 Oct Pago Pago), not the 25 Oct attack; "Today, Friday, October 23: 2 migraines" in the calendar |
| #19 Dubai, same date as the Tashkent attacks | "9 migraines today"; rows "12:59 PM – 1:02 PM (Tashkent)", city once |
| City once in History (Dubai) | "Sat, Oct 24, 3:25 PM – 3:26 PM (Tashkent)", "Fri, Oct 23, 11:29 PM – 11:29 PM (Pago Pago)"; spoken "…3:25 PM to 3:26 PM (Tashkent)…" |
| Attack started in Dubai, phone back in Tashkent | "Started 2:33 PM (Dubai)" 2–3 s after the app opens (see the walk fix) |

**Found in the walk:** after a zone change while the app was away, attack mode showed "Started 2:33 PM" without
"(Dubai)" until the next 30 s tick, because nothing composed the line again on resume. A first fix restarted home's
clock on resume. The review (6.3, I2) showed that History has the same cause, so it was replaced by one fix for every
screen (design D5).

**Not walked:**
- an in-progress History row in another zone: attack mode hides the calendar, so History cannot be opened during an
  attack. The row passes `withCity = !ended`, and `HistoryTimeUiFormatterTest` covers both forms;
- a display cutout on the left in landscape: this AVD has none. Design D4 shows the geometry for any inset.

**Noted:** this host was under heavy load, and a `uiautomator dump` took up to 18 s at 200% font. One early run
looked like the message vanished, but the start tap had been handled late. Two repeats without dumps and one with
dumps showed the message staying.

**Settings restored:** zone Asia/Tashkent, auto_time_zone 1, font_scale 1.0, user_rotation 0, accelerometer_rotation 1,
`accessibility_interactive_ui_timeout_ms` deleted (was null), adbd unrooted. The walk added attacks on 23–25 October to
this emulator's test data.

## Review (6.3)

An independent `superpowers:code-reviewer` pass on `git diff develop...feature/fix-today-zone-and-landscape-message`
found no Critical findings. It confirmed:
- the window and the day rule, including attacks that end at midnight;
- the D4 geometry in LTR and RTL;
- that the message state survives in its `Box`.

Each finding was checked against the code:

| Finding | Outcome |
|---|---|
| I1 the #20 Maestro flow keeps its `known-bug` tag, so a regression would not fail the run | `.maestro` belongs to QA (D40, D41: the tag goes when QA has retested the fix). Handed to QA with the PR number, together with the `# Covers:` lines and the "+ Medication taken" check |
| I2 History keeps a stale city after a zone change, the same cause as the walk find | fixed for every screen: `MainActivity` recreates itself when the device zone differs from the one it was created with, on `onStart` and on `ACTION_TIMEZONE_CHANGED` (design D5, delta `app-shell` › *Times follow a time zone change*); the home-only resume restart was removed |
| M1 a today row got a weekday when the attack's zone was already on the next date | fixed: the weekday compares the start's date with the device's date (D36); test "Given the attack zone is a day ahead of the device then no weekday" |
| M2 westward travel across a month boundary hides an attack for a few hours | accepted and recorded in design Risks; adding it again is caught by the overlap check (EC-ATK-04) |
| M3 tests: mixed list, an attack spanning today, a start at midnight, a DST day; "city once" assembled at the call sites | added the four `GetTodayUseCaseTest` cases; the range moved into `formatRange` on both formatters (`AttackTimeRangeUiModel`, `HistoryTimeRangeUiModel`) with tests; a Compose width test for the message is not added, the geometry is in D4 and walked |
| M4 the half width depends on the equal columns | comments in `HomeContents` and `SideBySideAttackLayout` point to each other |
| M5 docs drift | proposal lists `app-shell`; design D5; Follow-ups below; `GetTodayRepository` KDoc says half-open |
| M6 the left column under the message at 200% | checked on the 200% screenshots: the message covers "Rest now. We'll keep", while "time.", "Started …", "Details" and "Started earlier?" stay clear |

**After the review:** gate BUILD SUCCESSFUL, 711 unit tests, 0 failures.

Walk of the zone fix on emulator-5570:
- History open in Dubai lists "2:33 PM to 2:38 PM". After the zone changes to Tashkent with History open, the row
  reads "2:33 PM to 2:38 PM (Dubai)" at once, on the same month and scroll position.
- Attack mode with "Migraine started" on screen, zone changed to Dubai: "Started 4:01 PM (Tashkent)" at once, and the
  message with Undo and its time-left line is kept.

## Follow-ups

- Change 5 (`add-basic-stats`), the third user of the "attack days" rule, moves it from History and home into a shared
  pure-Kotlin module (design D2).
- QA: remove `known-bug` from `landscape-message-clear.yaml` after the retest, add `# Covers:` for the new
  `in-app-messages` and `today-sky` scenarios, and assert "+ Medication taken" there.
