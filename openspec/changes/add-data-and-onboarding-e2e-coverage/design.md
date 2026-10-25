# Design

## Context

See proposal.md (Why).
- **Tools:** the existing ones, plus D60 (clock), D62 (keys), D63 (verification depth) and D64 (notifications).
- **Base:** develop 1fbc32b, with DB v6.
- **File access:** Your data goes through DocumentsUI, via the Storage Access Framework, and Download works.
- **Automatic copies:** a Replace keeps `files/backups/migrely-auto-<epochMillis>.json`, the newest 3.
- **Onboarding:** it shows on a fresh install, with 4 steps and Skip, and a 0.8 s tap guard per step.
- **Settings:** the screen has sections. Delete all data restarts into onboarding.

## Goals / Non-Goals

**Goals:**
- every scenario of `data-backup`, `onboarding` and `app-settings` is checked;
- EC-BAK-01…05 are checked;
- no data is lost by surprise on Replace or Delete.

**Non-Goals:**
- changes 10, 11 and 13 (the next QA change);
- billing (change 9);
- product fixes.

## Decisions

### D1. Files through DocumentsUI (D200)

| Option | Verdict |
|---|---|
| Read the app's database after an export | Rejected: it shows what is stored, not the file a person gets |
| Save to Download through the picker, `adb pull`, check the bytes in `qa/backup_files.py` | **Chosen** for exports |
| Push prepared files to `/sdcard/Download`, pick them in the picker | **Chosen** for imports. `qa/backup_files.py` writes the broken, newer and older files from a real export |

The picker's own controls (file name field, "Save", the file list) are SystemUI/DocumentsUI. Flows tap them by text on
API 36 only.

### D2. A locale with a decimal comma

The app language is set with `cmd locale set-app-locales` (uz, then ru), as `keyboard_rtl` does. It is restored on
exit.

### D3. Android backup

| Step | Command |
|---|---|
| Use the local transport | `bmgr enable true`; `bmgr transport com.android.localtransport/.LocalTransport` |
| Back up and restore | `bmgr backupnow com.migrely.app`, then `pm clear` and `bmgr restore` |

The app disables cloud backup without client-side encryption. The local transport may therefore back up nothing. That
is the correct behaviour, and it is recorded as a D61 gap with the reason, not as a pass. The transport is restored.

### D4. Onboarding states

- **New user:** a clean install.
- **An update:** data is seeded through the UI first, then `pm clear` is *not* used; the app is reinstalled over itself
  with `adb install -r` after the onboarding preference is removed. Only with the app stopped, in the D60 sqlite/prefs
  fallback; otherwise the scenario uses "a record exists → no onboarding" directly.
- **Shortcut:** `am start -a com.migrely.app.action.START_ATTACK` on a clean install.

### D5. Delete all data

The check covers each kind of entry: attacks, medications and intakes, marks, reminders, backups and reports. Afterwards
the app is in onboarding, in Dark, and nothing comes back on the next launch.

## Risks / Trade-offs

- **DocumentsUI changes between Android versions.** These checks run on API 36 only.
- **`bmgr` on the emulator may not reflect a real phone's Google backup.** The result is reported as observed.
