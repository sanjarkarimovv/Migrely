# Proposal

## Why

Issue #178 (S3). The "Did it help?" notification's title is `Did %1$s help?`, so it names the medication, for example
"Did Sumatriptan help?". `NotificationDataSourceImpl` sets no visibility and no public version. On a phone that shows
all notification content on the lock screen (often the default), anyone who sees the locked phone can read which
migraine medication the person took.

`ux-designer` added two points:
- **F1.** A public version alone does not fix this. Android shows the public version only when the person chose
  "Hide sensitive content". Under "Show all content", the full text shows.
- **F2.** The daily reminder's update after "Pain-free day", "A migraine is logged today.", tells the lock screen
  that the person had a migraine today.

BRD D169 sets one rule for every notification. Text that can show on the lock screen does not reveal the person's
own health data. Generic text that is the same for everyone every day is allowed, because it says only that Migrely is
installed, and the app name already says that. D168 applies the strict form of this rule to the doctor visit reminder.

## What Changes

- "Did it help?" notification title: "Did it help?", with no medication name. The text stays "Tap to answer in
  Migrely.". The in-app card still names the medication.
- The daily reminder's update after "Pain-free day" on a day with an attack: "Not marked. Open Migrely to see today."
  in place of "A migraine is logged today.". `ux-designer` chose it and the lead agreed. `medical-reviewer` passed it
  by the same criteria. The first proposal, "Today already has an entry", was dropped because it hinted at an attack.
- Every channel (`daily`, `effect`, and `visit` once T21 lands) posts with `VISIBILITY_PRIVATE` and a public version
  titled "Reminder", with no text, from one shared string, `notification_public_title`.
- Unchanged: the daily reminder's "How was today?", "Mark a pain-free day, or log a migraine.", and the "Pain-free
  day" and "Log a migraine" actions. They are generic, the same every day for everyone, and say nothing about the
  person. Neutral wording ("log an entry") would be less clear to someone in pain (D169 (3)).

## Out of scope

- Channels, importance, timing, permission, actions, and the in-app cards.
- The channel names, which show only in Android's settings.
- OEM lock screens that ignore the public version. They are recorded by QA, not fixed.
- Any new permission.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `reminders`:
  - the "Did it help?" notification's title;
  - the update after "Pain-free day";
  - a new lock-screen requirement for every notification.

## Impact

- `app`: `NotificationDataSourceImpl` (visibility and public version), and `app/src/main/res/values/strings.xml`
  (`notification_effect_title`, `notification_attack_logged`, new `notification_public_title`).
- QA assets that assert the old text: `qa/device_scenarios.sh` lines asserting "A migraine is logged today." and
  "Did Ibuprofen help?". `qa-engineer` owns these.
- No data, permission or Data safety change.
- BRD: D169, EC-REM-21, EC-REM-06 amended, §13 row 38. Refs #178.
- Order: implemented on `feature/add-doctor-visit-mode` before T21's notification task, in its own commits with
  `Refs #178`. If it has to ship before 1.2.0, `bugfix/178-notification-privacy` from `origin/develop` can take this
  change alone.
