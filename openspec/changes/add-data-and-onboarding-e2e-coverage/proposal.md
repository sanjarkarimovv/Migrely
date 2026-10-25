## Why

Changes 7 (`add-data-export-import`, PR #38), 8 (`add-onboarding`, PR #40) and 12 (`add-settings-and-safety`, PR #48) are
merged without QA. They have 29 scenarios and the edge cases EC-BAK-01…05: `data-backup` 14, `onboarding` 8 and
`app-settings` 7.

Two kinds of behaviour are new to QA. Files go through the system file picker, and destructive actions ("Replace",
"Delete all data") must never lose data by surprise. The flows only skip onboarding today; they never check it.

This is the first QA change of the release cycle. The second one, `add-report-insights-and-release-qa`, covers changes 10,
11 and 13 and the full Go/No-Go (D44).

## What Changes

- **Baseline:** develop 1fbc32b. #52 has been retested (`message_tap_guard`, D114).
- **Your data** (BAK-F*, D70–D75):
  - JSON export, checked by its bytes: `format` "migrely-backup", `schemaVersion` 6, times in epoch millis;
  - CSV export:
    - the BOM, CRLF and the header;
    - ";" in a locale with a decimal comma (Uzbek and Russian, through the app language);
    - a note that starts with `=+-@` gets a `'`;
  - import:
    - the confirmation "It holds N migraines, M medications and K marked days." with Add, Replace and Cancel, or
      Import and Cancel on an empty phone;
    - Replace and Add, with their messages;
    - the same attack on two phones; a medication by the same name;
  - EC-BAK-01: a broken file ("This file isn't a Migrely backup. Nothing was changed.");
  - EC-BAK-02: a newer file ("Update Migrely to open this file.");
  - EC-BAK-03: an older schema;
  - restoring the automatic copy after a Replace;
  - Android backup to a new phone, through `bmgr`, if the emulator allows it.
- **Onboarding** (ONB-F*, D76–D80):
  - a new user sees it once, and an update with data does not;
  - the attack shortcut skips it;
  - the answers are stored and Skip works;
  - the disclaimer;
  - "Restore from a backup file" opens Your data (EC-BAK-05);
  - the daily-reminder offer turns the reminder on.
- **Settings** (D93–D98):
  - the sections;
  - the theme ("Light", "Match the phone"), with attack mode always dark;
  - Warning signs;
  - Delete all data: nothing happens until DELETE is typed, and then everything is emptied and onboarding restarts;
  - the version.
- **Spec `quality-gates`:** files through the system picker are checked by their bytes (D200).
- **BRD:** §13 row 12b and D200.

## Capabilities

### New Capabilities
None.

### Modified Capabilities
- `quality-gates`: files exchanged through the system picker are checked by their bytes (ADDED, D200).

## Impact

- **Files:**
  - `.maestro/`: new `data-backup/`, `onboarding/` and `app-settings/` folders, plus `device/` and `subflows/`;
  - `qa/device_scenarios.sh`;
  - a new `qa/backup_files.py`, which checks exported files and writes import fixtures;
  - `docs/QA.md` and `docs/BRD.md`.
- **BRD IDs:** BAK-F*, EC-BAK-01…05, ONB-F*, D70–D80, D93–D98, D114 and D200.

**Out of scope:**
- Product code.
- Changes 10, 11 and 13, and change 9 (billing), which is not merged.
