# Design

## Context

See proposal.md (Why). The QA tools are `qa/maestro.sh`, `qa/device_scenarios.sh` (it moves the clock and zone, sends
keys and reads the focus), the audit, monkey, the permission audit and the coverage report.

develop d0e0214 has changes 5–7 and #39, which adds `:app:check<Variant>Permissions`.

The reminders run as WorkManager unique work: `daily` and `effect-<intakeId>`, with the REPLACE policy. The worker is
`ReminderWorker`. The app re-plans the work at every start and after a zone or clock change. Notifications are "How was
today?" (actions "Pain-free day" and "Log a migraine") and "Did %1$s help?".

## Goals / Non-Goals

**Goals:**
- every `month-stats`, indicator and `reminders` scenario, and every edge case EC-UX-14, EC-MED-06 and EC-REM-01…06,
  is checked on the device;
- QA tools for notifications and permissions.

**Non-Goals:**
- `data-backup` (change 7), which gets its own QA change;
- onboarding (change 8);
- product fixes.

## Decisions

### D1. Data through long attacks and clock moves

Migraine days are the days an attack touches, and an attack day is also an entry for the progress message (D55). One
attack started on one day and ended N days later therefore gives N migraine days in one start and one end. Each case
is built this way:

| Case | How |
|---|---|
| Chronic: 15 against 14 days | One attack from the 1st to the 15th, against one from the 1st to the 14th |
| Progress: fewer, more, as many | Last month and this month get attacks touching 12 and 10 days, 10 and 12, and 10 and 10 |
| Progress: too few entries | 9 days on one side |
| An attack over two months | From the 30th of last month to the 2nd |
| Average peak | Levels set through the intensity choices, and "—" when none is set |

The clock moves through `qa/device_scenarios.sh` (D60). The long-attack check (EC-ATK, after 72 h) is answered with
"Still going". sqlite is the fallback only if the UI cannot build a case within about 5 minutes.

### D2. Notifications on demand (D64)

| Option | Verdict |
|---|---|
| Wait for the planned time in real time | Rejected: up to 24 h, and WorkManager has a ±15 min window |
| Move the clock past the time | Rejected on its own: JobScheduler delays run on elapsed time, so a wall-clock jump does not fire them |
| Set the clock 2 min before the time, start the app (it re-plans), then `cmd jobscheduler run -f com.migrely.app <id>` | **Chosen**. The job id comes from `dumpsys jobscheduler com.migrely.app` |

The notification is read with `cmd statusbar expand-notifications` and uiautomator, and its actions are tapped by
label. The shade is collapsed afterwards.

### D3. Permission states

- **Grant and revoke:** `pm grant` and `pm revoke com.migrely.app android.permission.POST_NOTIFICATIONS`.
- **First refusal:** a real tap on "Don't allow" in the system dialog.
- **30-day re-ask** (`reminder_settings.permission_asked_epoch_millis`): the clock moves 29 days, where there is no
  dialog, and then 31 days, where the dialog comes back.
- **Restore:** the scripts save the permission state and its flags, and restore them.

### D4. Reboot (EC-REM-04)

`adb reboot` restarts the QA emulator. The check waits for `sys.boot_completed`, then for the app's planned `daily`
job in `dumpsys jobscheduler`, without opening the app.

### D5. Duplicate resource names

`qa/resource_names.py` lists every `<string name>` defined in more than one module's `res/values*/strings.xml`. Only
names on its allowlist, each with a reason, may repeat. The picker-title bug came from such a duplicate (e74055e).

## Risks / Trade-offs

- **A reboot is slow** (about 1 min) and can fail on a loaded host. It runs once per cycle, and a failed boot is
  reported as not run, not as a pass.
- **The shade belongs to SystemUI**, so its layout differs by API level. The notification checks run on API 36 only.
- **Moving the clock by 31 days** also moves WorkManager's own clock. Each scenario restores the real time.
