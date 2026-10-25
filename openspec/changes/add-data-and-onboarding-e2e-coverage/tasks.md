# Tasks

## 1. Requirements and baseline

- [x] 1.1 BRD §13 row 12b and §17 D200. Verify: `grep -n "| 12b |\|| D200 " docs/BRD.md`
- [x] 1.2 Retest #52 on 1fbc32b with `message_tap_guard` (D114). Verify: passes twice; #52 is closed with the results
- [x] 1.3 Baseline on 1fbc32b: full regression and all device scenarios. Triage every failure. Verify: stale checks
  fixed, defects filed

## 2. Your data (BAK-F*, EC-BAK-01…05)

- [x] 2.1 `qa/backup_files.py`: check a JSON export (format, schemaVersion, epoch millis, counts) and a CSV export (BOM,
  CRLF, header, separator, formula prefix); write the broken, newer and older import files from a real export.
  Verify: it checks a real export and rejects a file with a missing BOM
- [x] 2.2 Flow `data-backup/open-and-export.yaml`: Opening Your data; Exporting JSON and CSV to Download. Then the device
  scenario `backup_export` pulls and checks the files, including a note `=SUM(1)` and the uz/ru separator. Verify:
  passes twice
- [x] 2.3 Device scenario `backup_import`:
  - Before importing (the counts dialog);
  - Replace (the automatic copy kept) and Add ("Added N migraines." and "M were already here.");
  - the same attack on two phones;
  - a medication by the same name;
  - a broken file;
  - a newer file;
  - an older schema;
  - restoring the automatic copy (Undoing a Replace).
  Verify: passes twice, and every entry survives a refused import
- [x] 2.4 Device scenario `backup_android` through `bmgr` (design D3). Verify: run twice; the result is recorded as
  observed

## 3. Onboarding (ONB-F*, D76–D80)

- [x] 3.1 Flows `onboarding/`:
  - a new user;
  - the answers stored, and Skip;
  - the disclaimer;
  - Restore from a backup;
  - the reminder offer, turned on (with the permission allowed, through `permissions`).
  Verify: each passes 3 times
- [x] 3.2 Device scenarios `onboarding_states`: an update with data shows no onboarding; the attack shortcut skips it.
  Verify: passes twice

## 4. Settings and safety (D93–D98)

- [x] 4.1 Flows `app-settings/`:
  - Opening Settings (the sections);
  - Light and "Match the phone", with attack mode dark;
  - Warning signs;
  - Delete all data not typed;
  - the version.
  Verify: each passes 3 times
- [x] 4.2 Device scenario `delete_all_data`: every kind of entry exists first; after DELETE, the app restarts into
  onboarding in Dark, and History, medications, marks, reminders (no planned job), backups and reports are empty.
  Verify: passes twice

## 5. Documents

- [x] 5.1 `docs/QA.md` and `.maestro/README.md`: files through the picker (D200), locales, `bmgr`. Verify:
  `grep -n "D200" docs/QA.md`

## 6. Verify

- [x] 6.1 Smoke, the new flows 3 times, the new scenarios twice, `qa/e2e_coverage.py` exit 0. Record the results and
  the gaps with reasons (D61) in a "Verification record"
- [x] 6.2 An independent review, its findings fixed
- [x] 6.3 Gate `./gradlew assembleDebug test ktlintCheck detekt` and `openspec validate
  add-data-and-onboarding-e2e-coverage --strict`

## Scenarios without a check (D61)

| Scenario | Reason | Follow-up |
|---|---|---|
| data-backup › Android backup › A new phone | On the emulator the app's backup is refused ("Backup is not allowed"), even with the local transport claiming encryption. That is correct: cloud backup needs end-to-end encryption, which needs a screen lock (D74). A real restore needs a phone with a screen lock and a Google account | Release QA, on the user's phone |
| reminders › Offered during onboarding › Permission refused | Not written yet; "Turn on" with the permission allowed is covered | Release QA |

## Verification record (2026-10-25, emulator-5580, AVD `Migrely_QA_API_36`)

Base: develop 1fbc32b (up to PR #54).

| Check | Result |
|---|---|
| Baseline 1fbc32b | ✅ regression 38/38 + 9/9; every device scenario. One run was cut by an emulator crash at host memory pressure, and its remaining scenarios then passed |
| #52 retest (`message_tap_guard`) | ✅ 2/2: taps at 5.2 s and 5.5 s land on Undo; at 5.8 s the guard swallows the tap (History, migraine deleted); at 7.5 s Doctor report opens. Doctor report's centre (1006,212) is measured, not fixed |
| New flows (onboarding, app-settings, data-backup) | ✅ 3/3 each |
| `backup_export` | ✅ 2/2. JSON: v6, 2 attacks, 1 medication, 1 day mark. CSV in en, uz and ru: BOM, CRLF, header, "," or ";", 2 rows, `'=SUM(1)`, "Голова" kept |
| `backup_import` | ✅ 2/2. Covered: broken and newer files refused with their messages; an older file with optional fields left out imported; Add with an overlap under another ID (skipped), a new migraine, and "ibuprofen" under another ID (one Ibuprofen, 2 days with attack medication); Replace, then the copy restored and a copy kept; onboarding's restore on an empty phone. #58 reproduces |
| `theme_check` | ✅ 2/2. Luminance 16 dark; Light at once in Settings and at the next start (243); attack mode 9 and its details 16 (dark); light again after the attack; Match the phone follows the system |
| `onboarding_states` | ✅ 2/2. Answers read back (triggers, 4 to 14, no period tracking, finished); the 8:00 PM reminder planned; the shortcut skips onboarding; an update with entries opens home |
| `delete_all_data` | ✅ 2/2. A copy, a reminder and Light first; afterwards dark, no registered job, no copy, onboarding, and an empty History after a relaunch |
| `backup_android` (on demand) | OBSERVED: "Backup is not allowed" (correct without encryption, D74); the transport and its settings restored |
| Smoke | ✅ 14/14 + 3/3 |
| Coverage | ✅ exit 0. 213 of 456 scenarios (47%); onboarding 8/8, app-settings 7/7, data-backup 13/14 |
| `qa/resource_names.py` | ✅ passed |

**Found and filed:**
- #56 (S3, accessibility): Your data's messages last about 4 s and ignore the accessibility timeout; the spec says 8 s, extended.
- #58 (S3): Add does not restore a soft-deleted migraine; the hidden row counts as "already here" until a purge.

**Retested and closed:** #52.

**Disproved by measurement:**
- the daily reminder unplanned: a parser that skipped a second job;
- the shortcut not resolving: it needs the explicit component, as the launcher shortcut has;
- several picker and message timings: test-side.

**Infrastructure:** the emulator dropped off adb several times under host memory pressure (about 70 MB free). It was also hidden from Maestro while the user's phone was attached unauthorised (resolved when it was authorised).

**Review** (`superpowers:code-reviewer`): 1 critical (a `grep -q` under `pipefail` that could pass with a job left), 5 important and 5 minor findings, all fixed (e71def5).
