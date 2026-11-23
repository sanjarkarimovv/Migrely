# Tasks

Base: `bugfix/pref-save-rollback` from `origin/develop` 82df348f (team-lead decision 2026-11-23: its own branch; T72 merges
`develop` afterwards). BRD D197, EC-STO-13, §13 row 60.

## 1. Plan

- [x] 1.1 BRD D197, EC-STO-13, §13 row 60; this change. Verify: `openspec validate fix-pref-save-rollback --strict`.

## 2. Datasource

- [x] 2.1 Red first: `PreferencesFailedWriteTest` (7, on `MemoryFirstPreferences`, a fake that changes memory before the
  file as Android does). Verify: 6 of 7 red on develop's helper, then `./gradlew :datasource:implementation:testDebugUnitTest`.
- [x] 2.2 Fix in `PreferencesDataSourceImpl.write`: on a failed commit, put back each key the write changed, then throw.
- [ ] 2.3 Instrumented: `PreferencesFailedWriteFilesTest` (2, real preferences, read-only folder), with a negative control
  (red without 2.2). Verify: `ANDROID_SERIAL=emulator-5570 ./gradlew :datasource:implementation:connectedDebugAndroidTest`
  (waits for a 5570 slot).

## 3. Presentation

- [x] 3.1 `SettingsViewModelTest`: after a failed Track periods save the stored value is read again and shown.

## 4. Verify

- [ ] 4.1 Gate `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exits 0.
- [ ] 4.2 Device: full disk on 5570, Track periods and the theme: message, stored value shown, the same after a restart and
  after a later successful write.
- [ ] 4.3 `code-reviewer`, then QA's check of the diff's reach, then archive and PR into `develop` (`Refs #278`).
