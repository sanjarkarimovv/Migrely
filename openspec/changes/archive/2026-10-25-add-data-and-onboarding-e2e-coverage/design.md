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

`backup_android` runs on demand, not in the default run. It uses the local transport (`bmgr enable true`, `bmgr
transport com.android.localtransport/.LocalTransport`). The app disables cloud backup without end-to-end encryption, so
the transport is told to claim encryption (`backup_local_transport_parameters fake_encryption_flag=true`). Then the
scenario backs up, runs `pm clear` and `bmgr restore`, and prints what came back. The transport, its enabled state and
the parameter are restored, also by the exit trap. A real "new phone" restore needs a Google account backup, so it is
a D61 gap.

### D4. Onboarding states

- **New user:** a clean install.
- **Answers:** the answers are read back from the debug build's preferences (`run-as … cat shared_prefs/…`, read only).
- **An update:** an older version with entries and no onboarding answer is simulated. Entries are logged, the
  preferences file is deleted, and the app is reinstalled over itself with `adb install -r`. Home must open, because a
  record exists.
- **Shortcut:** `am start -a com.migrely.app.action.START_ATTACK -n <launcher activity>` on a clean install. The
  launcher shortcut names the activity too.

### D5. Delete all data

The check covers each kind of entry: attacks, medications and intakes, marks, reminders, backups and reports. Afterwards
the app is in onboarding, in Dark, and nothing comes back on the next launch.

## Risks / Trade-offs

- **DocumentsUI changes between Android versions.** These checks run on API 36 only.
- **`bmgr` on the emulator may not reflect a real phone's Google backup.** The result is reported as observed.
