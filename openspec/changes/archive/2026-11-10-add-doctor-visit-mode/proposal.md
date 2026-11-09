# Proposal

## Why

People bring Migrely to a doctor's appointment. The report is the reason many of them keep a diary at all, and the
moment they need it is fixed: a few days before the visit, when they ask themselves "how have I been since last time?".
Today nothing in the app knows that moment. A person has to remember to make the report, and a free user meets the
paywall only if they go looking for it.

Doctor visit mode (tracker T21, release 1.2.0, BRD D168) lets the person save the date of the next visit once. Two days
before it, the app reminds them and offers the report. A Premium user goes straight to it. A free user meets the
existing paywall at the moment they want the report, by their own tap, never by the app's push. The reminder itself,
the date and the home card are free, like every reminder (BRD §7.1).

## What Changes

- **The date.** The Reminders screen gets a "Doctor visit" section with one row, "Next visit". Tapping it opens a
  full-screen date picker, from today to 365 days ahead. The date is saved at once. One upcoming visit at a time,
  "Remove" with Undo. No time, doctor, clinic or note.
- **After the visit.** At local midnight after the visit day, the date becomes the "last visit" and "Next visit" reads
  "Not set" again. The date is kept because the report period uses it.
- **The reminder.** At about 10:00 local time two days before the visit, one notification names the visit date and
  offers to make a report. It runs as WorkManager one-time work, a new kind of the existing `ReminderWorker`, with no
  exact alarm and no new permission. It is planned again at start, after a reboot and on time zone or clock changes, by
  the existing paths. It is not sent while an attack is in progress (it tries again at 10:00 the next day), and not
  sent when nothing was logged in the period. A late run still posts on the visit day, never after it.
- **A visit too close.** If the reminder time has already passed when the date is saved, no notification is sent and
  the home card shows at once.
- **The home card.** From the reminder time until the visit day ends, home in normal mode shows one card with a report
  button and a close button. It never covers a safety card and never shows in attack mode.
- **The tap.** The notification and the card open History with the doctor report on top. Premium gets the report
  screen. Free gets the existing paywall with "A report for your doctor", and closing it leaves History. The paywall is
  the one History's "Doctor report" opens, and it does not count toward D110's 7-day limit, because the person tapped.
- **No urgency.** No price, "Premium", offer, discount, countdown or visit date in the notification, the card or the
  paywall. The text is the same for Free and Premium.
- **Privacy.** The notification text holds the date and nothing about health. It is `VISIBILITY_PRIVATE`, with a
  public version that reads "Reminder", in its own "Doctor visit" channel. The PDF is made on the phone as today.
- **Notifications off.** Setting a visit asks for permission when notifications are off, at most once in 30 days. The
  date is saved either way. The row says when notifications or the channel are off, with "Open settings". The home
  card still works.
- **Report period.** When the last visit is within 24 months, the Doctor report screen offers "Since last visit" first,
  from that visit's day to today, and chooses it. Otherwise 3 months stays the default.
- **Data.** A new `doctor_visit` table, DB v7 by additive AutoMigration. JSON backups carry `doctorVisits` at
  `schemaVersion` 7. Older files import with no visits. "Add" never brings a second upcoming visit. "Delete all data"
  removes visits.

## Out of scope

- A visit time, doctor, clinic or note; several upcoming visits; entering or editing a past visit.
- Changing the reminder time or the "2 days".
- Setting the date from the Doctor report screen, onboarding or home.
- A "how did the visit go?" question, adding the visit to the system calendar.
- Any change to the PDF's content, the CSV export, or the import preview counts.
- Any change to the paywall, its headlines, products or self-show rules beyond not counting this tap.
- Exact alarms, `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`, `INTERNET` or any other new permission.
- The lock-screen wording of the existing "Did <medication> help?" notification. It names a medication, which this
  change's rule would not allow. Filed as a finding for `team-lead`, not changed here.
- Version bump and release notes for 1.2.0 (the release branch does that).

## Capabilities

### New Capabilities

- `doctor-visit`: the next visit date, its reminder, the home card, the tap into the report or paywall, privacy and
  accessibility.

### Modified Capabilities

- `reminders`: the Reminders screen gets the "Doctor visit" section, and the permission rule covers setting a visit.
- `doctor-report`: the period gets "Since last visit".
- `data-backup`: the JSON backup carries doctor visits at schema 7, with import rules for them.

## Impact

- `features/reminders` (domain, presentation, data, UI): the visit row, picker, remove and Undo, the run, the
  reschedule, the home card composable and its ViewModel.
- `features/report` (domain, presentation, UI): the "Since last visit" period.
- `features/history` (presentation): open the doctor report once when History is opened by the reminder, through the
  existing premium gate.
- `features/backup` and `datasource`: `doctor_visit` table and DAO, DB v7, backup schema 7, the import rules.
- `app`: `ReminderWorker` kind, `NotificationDataSourceImpl` channel and public version, the deep link in
  `MainActivity`, the home card slot, DI.
- `docs/ARCHITECTURE.md` (DB v7, the new table), `docs/BRD.md` (done in this commit).
- Permissions: none added. Data safety: no change.
- BRD: REM-F4, REP-F1, §7.3, EC-REM-07 to EC-REM-19, EC-REP-04, EC-BAK-06, §13 row 37, D168.
