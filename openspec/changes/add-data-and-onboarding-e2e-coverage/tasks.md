# Tasks

## 1. Requirements and baseline

- [x] 1.1 BRD §13 row 12b and §17 D200. Verify: `grep -n "| 12b |\|| D200 " docs/BRD.md`
- [x] 1.2 Retest #52 on 1fbc32b with `message_tap_guard` (D114). Verify: passes twice; #52 is closed with the results
- [ ] 1.3 Baseline on 1fbc32b: full regression and all device scenarios. Triage every failure. Verify: stale checks
  fixed, defects filed

## 2. Your data (BAK-F*, EC-BAK-01…05)

- [ ] 2.1 `qa/backup_files.py`: check a JSON export (format, schemaVersion, epoch millis, counts) and a CSV export (BOM,
  CRLF, header, separator, formula prefix); write the broken, newer and older import files from a real export.
  Verify: it checks a real export and rejects a file with a missing BOM
- [ ] 2.2 Flow `data-backup/open-and-export.yaml`: Opening Your data; Exporting JSON and CSV to Download. Then the device
  scenario `backup_export` pulls and checks the files, including a note `=SUM(1)` and the uz/ru separator. Verify:
  passes twice
- [ ] 2.3 Device scenario `backup_import`:
  - Before importing (the counts dialog);
  - Replace (the automatic copy kept) and Add ("Added N migraines." and "M were already here.");
  - the same attack on two phones;
  - a medication by the same name;
  - a broken file;
  - a newer file;
  - an older schema;
  - restoring the automatic copy (Undoing a Replace).
  Verify: passes twice, and every entry survives a refused import
- [ ] 2.4 Device scenario `backup_android` through `bmgr` (design D3). Verify: run twice; the result is recorded as
  observed

## 3. Onboarding (ONB-F*, D76–D80)

- [ ] 3.1 Flows `onboarding/`:
  - a new user;
  - the answers stored, and Skip;
  - the disclaimer;
  - Restore from a backup;
  - the reminder offer, turned on (with the permission allowed, through `permissions`).
  Verify: each passes 3 times
- [ ] 3.2 Device scenarios `onboarding_states`: an update with data shows no onboarding; the attack shortcut skips it.
  Verify: passes twice

## 4. Settings and safety (D93–D98)

- [ ] 4.1 Flows `app-settings/`:
  - Opening Settings (the sections);
  - Light and "Match the phone", with attack mode dark;
  - Warning signs;
  - Delete all data not typed;
  - the version.
  Verify: each passes 3 times
- [ ] 4.2 Device scenario `delete_all_data`: every kind of entry exists first; after DELETE, the app restarts into
  onboarding in Dark, and History, medications, marks, reminders (no planned job), backups and reports are empty.
  Verify: passes twice

## 5. Documents

- [ ] 5.1 `docs/QA.md` and `.maestro/README.md`: files through the picker (D200), locales, `bmgr`. Verify:
  `grep -n "D200" docs/QA.md`

## 6. Verify

- [ ] 6.1 Smoke, the new flows 3 times, the new scenarios twice, `qa/e2e_coverage.py` exit 0. Record the results and
  the gaps with reasons (D61) in a "Verification record"
- [ ] 6.2 An independent review, its findings fixed
- [ ] 6.3 Gate `./gradlew assembleDebug test ktlintCheck detekt` and `openspec validate
  add-data-and-onboarding-e2e-coverage --strict`
