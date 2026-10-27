# Tasks

## 1. Fix

- [x] 1.1 `MigrelyDateTimePicker`: `TimePickerLayoutType.Vertical`
- [x] 1.2 Reminders `TimeDialog`: the same, in a dialog that scrolls as a whole (review finding)

## 2. Verify

- [ ] 2.1 Gate: `assembleDebug test ktlintCheck detekt`
- [ ] 2.2 `openspec validate fix-landscape-time-picker --strict`
- [ ] 2.3 On emulator-5570, in landscape:
  - "Add a past migraine" › Ended › Next shows every hour, the minutes and PM;
  - a 3:30 PM end can be picked and confirmed;
  - the same in "Log medication" and in the Reminders time;
  - portrait looks as before;
  - the Reminders time in a 24-hour locale in landscape, where the dial is widest.
