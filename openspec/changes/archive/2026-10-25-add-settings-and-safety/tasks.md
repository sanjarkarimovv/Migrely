# Tasks

## 1. Datasources

- [x] 1.1 Preferences: theme and `clear()`. Backup: `deleteAll()`. Backup file: `newestAutomatic()` and `deleteAll()`.
  Reports: `deleteAll()`. Reminders: `cancelAll()` for work and notifications. `AppInfoDataSource`. Tests.

## 2. Theme

- [x] 2.1 A light scheme in `:designsystem`, `MigrelyTheme(dark)`, and the start-up read and live apply in `:app`.

## 3. Settings

- [x] 3.1 Domain and data: theme, delete all, version; tests.
- [x] 3.2 Presentation and UI: sections, the theme dialog, Warning signs, About, Privacy, licences, version, and the
  Delete all data confirmation; tests.

## 4. Your data

- [x] 4.1 Restore the automatic copy: domain, data, presentation, UI; tests.

## 5. Verify

- [x] 5.1 Gate, and instrumented tests.
- [x] 5.2 Emulator walk:
  - every screen in Light and Dark;
  - attack mode stays dark;
  - Match the phone;
  - delete all, then onboarding;
  - restore the copy after a Replace;
  - 200% font, TalkBack.
- [x] 5.3 Independent review; fix and record.

## Verification record

**Gate:** 1262 unit tests, 0 failures. **Instrumented** on the final code:
- datasource 92/92, including the table-clearing round trip with `sqlite_sequence` and the in-progress flow;
- designsystem 33/33, including `MigrelyDarkRoomTest`. `MigrelySnackbarTest.actionMovesBelowTheTextAtDoubleFontSize`
  timed out once in `captureToImage` while another build loaded the machine, and passed on its own; no designsystem code
  had changed;
- app 1/1, the notifications' `cancelAll`.

**Emulator walk** on emulator-5570 (587ee70), with the app data restored to the baseline afterwards (dump md5 unchanged).
All 7 scenarios passed:
- the Settings list and its pages;
- Light on every screen, with no pure white, readable rings and success text, and light-icon bars;
- attack mode, its pickers and snackbars, and the dark room staying dark;
- the theme at cold start, through rotation, and following the phone;
- restoring the copy after a Replace: 16 back, then the copy of 3 offered, at most 3 files kept;
- Delete all: exact DELETE only; every table, preference, copy, report, job and notification gone; onboarding in Dark;
- 200% font, and the radio group.

**Fixed after it:**
- **B1, a light flash at cold start during an attack:** home's Loading is dark.
- **B2, screens opened from attack mode were light:** every screen is dark while an attack is in progress, which
  amends D94.
- **B3, the DELETE field below the fold at 200%:** the field follows a one-line warning.
- **B4, the progress closing before the restart:** it now stays until the restart.

**Re-walk** (3b1aa0b): 6 of 7 checks passed:
- no light frame at a cold start during an attack, twice;
- every screen dark during an attack, and light again at its end;
- dark-to-dark fades;
- the field visible at 200% in portrait;
- the data reset;
- the copy;
- the outline on light.

**Fixed after it:**
- "Delete all data" became a screen of its own instead of a dialog, so the field and the pinned button show together in
  landscape at 200%;
- its progress, drawn dark, stays as the last frame until the restart, rotation included;
- the delete also empties `sqlite_sequence`.

**Final check** (e4f9675):
- in landscape at 200% the field and the button both show without scrolling;
- Back and close return with nothing deleted;
- no Settings frame shows between the confirm and onboarding in Dark, with a rotation too;
- Back does nothing while deleting;
- every table and `sqlite_sequence` is empty afterwards.

Its one finding, the top bar fading from light for about 200 ms, is fixed in 2db4f9e: a fresh bar when deleting starts.
Checked on the device: no light bar frame in 5 recordings, with rotations 0.1–0.5 s after the tap too.

**Accepted:**
- a rotation within about 0.1 s of the tap, before the first dark frame is drawn, makes Android's own rotation
  animation freeze the snapshot of the light confirmation for a moment; the delete and restart still complete;
- the status-bar icons stay dark on the dark screen for under a second before turning light; nothing bright shows.

## Review (5.3)

An independent `superpowers:code-reviewer` pass:

| Finding | Outcome |
|---|---|
| **I1** light frames around attack mode (home's Loading, fades over a light surface) | fixed: the root surface is dark while a dark room shows, and home's Loading is drawn dark |
| **I2** Android's cloud backup still holds the last snapshot after Delete all | fixed in the copy: the Delete dialog and Privacy say it is replaced at the next backup (D96, D97) |
| M1 preferences cleared before files | fixed: records, files, reminders and notifications, then preferences last |
| M2 a failed delete lost "Did it help?" questions | fixed by the same order: nothing is cancelled before the records are gone |
| M3 an import or report still working during the delete | accepted: Room serializes the transactions, the confirm needs typing, and the restart ends in-flight work; a `.partial` report is cleaned at start |
| M4 a failure lost on rotation | fixed: kept until the screen takes it |
| M5 which copy is restored | fixed: the newest copy that decodes is offered, and exactly the shown copy is restored. Accepted: retention by file time when the clock goes back |
| M6 nothing-logged outline under 3:1 on light | fixed: `outline`, 3.8:1. Accepted: dark stays dim (1.8:1) by design for light-sensitive eyes; TalkBack reads each day's state |
| M7 `:app` read a domain model | fixed: mapped to `:app`'s `AppearanceUiModel` |
| M8 the DELETE check only in the composable | fixed: `DeleteConfirmationResolver` in presentation, unit-tested |
| M9 no end-to-end encryption required on Android 9–11 | fixed: `requireFlags="clientSideEncryption"`, with a device-to-device twin so phone transfer keeps working |
| Test gaps | fixed: `MigrelyDarkRoomTest`, `NotificationDataSourceImplTest`; delete-then-onboarding covered by the walks |
