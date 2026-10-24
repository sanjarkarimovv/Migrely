# Proposal

## Why

The MVP promises reminders (BRD §6.1 item 6). Two are the core of the habit loop:
- A daily "How was today?" gives the user a chance to mark a pain-free day. Without that mark, a month without entries
  looks the same as a month without pain (D55).
- "Did it help?" after a medication reaches users who never open the app at the right moment (MED-F4).

## What Changes

- **A "Reminders" screen** in a new `reminders` feature, opened from a bell button in home's header in normal mode
  (D65). It holds:
  - "Daily reminder" (off until turned on, default 20:00) with its time;
  - "Ask if a medication helped" (on once notifications are allowed).
- **The daily reminder** (D66) is WorkManager one-time work for the next occurrence, about ±15 minutes.
  - It is rescheduled after each run, after a reboot, and on a change of time zone or clock.
  - It is skipped when today is already marked pain-free or has an attack.
  - Its actions are "Pain-free day", which follows History's rule and so refuses a day with an attack (EC-REM-06), and
    "Log a migraine", which opens home.
- **"Did it help?"** (D67) is a notification 2 hours after an acute intake, if the question is still open. Tapping it
  opens home, where the card asks. It is cancelled when the question is answered, skipped or undone.
- **Permission** (EC-REM-01, EC-REM-02, D68): it is asked for only when a reminder is turned on. When it is refused,
  the screen explains and links to system settings, and the app asks again at most once in 30 days.
- **Storage:** DB v6 adds `reminder_settings`.

**Out of scope:** the trial reminder (REM-F3), which comes with subscription in change 9.

## Capabilities

### New Capabilities

- `reminders`: the Reminders screen, the daily reminder and the "Did it help?" notification.

### Modified Capabilities

None.

## Impact

- **New** `:features:reminders:{domain,presentation,ui,data}`.
- **Datasource:**
  - `reminder` for the settings table;
  - `notification` and `schedule` interfaces in `:datasource:source`, with Android implementations over
    NotificationManager and WorkManager.
- **`:app`:**
  - the worker and the receivers: notification actions, time zone and clock changes;
  - DI and navigation, and home's header button event.
- **Manifest:** `POST_NOTIFICATIONS`. There is no exact alarm and no INTERNET.
