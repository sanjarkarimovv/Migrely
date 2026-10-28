# Tasks

## 1. Fix

- [x] 1.1 `MigrelyDateTimePicker`: `TimePickerLayoutType.Vertical`
- [x] 1.2 Reminders `TimeDialog`: the same, in a dialog that scrolls as a whole (review finding)

## 2. Verify

- [x] 2.1 Gate: `assembleDebug test ktlintCheck detekt`
- [x] 2.2 `openspec validate fix-landscape-time-picker --strict`
- [x] 2.3 On emulator-5570, in landscape:
  - "Add a past migraine" › Ended › Next shows every hour, the minutes and PM;
  - a 3:30 PM end can be picked and confirmed;
  - the same in "Log medication" and in the Reminders time;
  - portrait looks as before;
  - the Reminders time in a 24-hour locale in landscape, where the dial is widest.

## 3. Review

- [x] 3.1 Independent review (`superpowers:code-reviewer`) of 47f4725

## Verification record

- **Gate:** `assembleDebug test ktlintCheck detekt` passes.
- **Emulator (emulator-5570, API 36, 1080×2400, debug build), landscape:**
  - **Add a past migraine › Ended:** the time step shows "Select hour", "Select minutes", AM and PM. After scrolling,
    all 12 hours and Cancel/OK are there. 3:30 PM was picked and shown.
  - **Log medication › Taken at:** the same, with all 12 hours, AM/PM and Cancel/OK.
  - **Reminders, reworked after the review:**
    - In portrait (12-hour), every part fits, and 9:15 PM was saved.
    - In landscape, a fling from the title scrolls to OK, and 10:30 PM was saved.
    - In 24-hour landscape, hours 0–23 show, and 22:45 was saved.
    - No drag on the title changed the time.
  - **Portrait:** the dialogs look as before.
  - 0 FATAL EXCEPTION.
- **Review finding fixed:** the Reminders AlertDialog scrolled only its text slot, which the dial filled in landscape.
  It is now a dialog that scrolls as a whole.
- **Found, not changed:**
  - In landscape, the dial catches drags, so the dialog scrolls from its title, edges or buttons. One slow drag from
    the title may not reach OK; a fling or a second drag does.
  - Material's AM/PM switch exposes no selected state to accessibility services. That belongs to the Material
    component and goes to develop.
- **Not checked:** 200% font in landscape. The fixed 96×80 dp hour and minute boxes may clip, as they already did in
  portrait in 1.0.0.
