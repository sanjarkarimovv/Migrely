# Tasks

Base: `bugfix/pref-save-rollback` from `origin/develop` 82df348f (team-lead decision 2026-11-23: its own branch; T72 merges
`develop` afterwards). BRD D197, EC-STO-13, §13 row 60.

## 1. Plan

- [x] 1.1 BRD D197, EC-STO-13, §13 row 60; this change. Verify: `openspec validate fix-pref-save-rollback --strict`.

## 2. Datasource

- [x] 2.1 Red first: `PreferencesFailedWriteTest` (7, on `MemoryFirstPreferences`, a fake that changes memory before the
  file as Android does). Verify: 6 of 7 red on develop's helper, then `./gradlew :datasource:implementation:testDebugUnitTest`.
- [x] 2.2 Fix in `PreferencesDataSourceImpl.write`: on a failed commit, put back each key the write changed, then throw.
- [x] 2.3 Instrumented: `PreferencesFailedWriteFilesTest` (2, real preferences, read-only folder), with a negative control
  (red without 2.2). Verify: `ANDROID_SERIAL=emulator-5570 ./gradlew :datasource:implementation:connectedDebugAndroidTest`
  (waits for a 5570 slot).
  Done 2026-11-23 on emulator-5570 (API 36): 2/2 with the fix (`migrely-work/loops/278-connected-fix-20261123-1929.log`);
  negative control with the fix reverted locally, never committed: 2/2 red
  (`migrely-work/loops/278-connected-control-20261123-2002.log`).

## 3. Presentation

- [x] 3.1 `SettingsViewModelTest`: after a failed Track periods save the stored value is read again and shown.

## 3b. Code review cr-278 of 841aade9

- [x] 3b.1 Important, the paywall loop on a full disk: a paywall or rating-request mark whose write fails counts as set
  for the process (`UnsavedMarksDataSource`, one for the app; the rule in `SubscriptionRepository` and
  `ReviewRepository`, which also reads the paywall's time). Tests: 5 repository tests (3 review + 2 subscription), 4 red before the fix, including the subscription
  restart control; the review restart control passed before and after.
- [x] 3b.2 Minor, overlapping writes: one write per preferences file at a time. Red first: the overlap test without the
  lock.
- [x] 3b.3 Minor: EC-STO-13 names the marks that go through the helper; D197's sentence on the accepted window replaced.

## 4. Verify

- [ ] 4.1 Gate `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exits 0.
- [x] 4.2 Device: full disk on 5570, Track periods and the theme: message, stored value shown, the same after a restart and
  after a later successful write.
  Done 2026-11-23 (`migrely-work/walks/278/row.log`, shots): full disk, Track periods shows "Couldn't save that. Try
  again." and stays on (row R1b; the first R1 dump came before the message); the theme stays Dark with the message (R2);
  the same after freeing and after a restart (R3, R4); a later successful theme write leaves `period_tracking` stored on
  (R5).
- [ ] 4.3 `code-reviewer`, then QA's check of the diff's reach, then archive and PR into `develop` (`Refs #278`).
