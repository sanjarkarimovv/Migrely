# Design

## Context

BRD D168 holds the product decision and its reasons in risk order. This file says how it fits the code that exists on
`origin/develop` 3170865b. I checked each item against the code:

- Reminders are WorkManager one-time unique work with `REPLACE`, run by `app/.../reminder/ReminderWorker.kt` with a
  `kind` input (`daily`, `effect`) (`ReminderScheduleDataSourceImpl`). There are no exact alarms.
- `RECEIVE_BOOT_COMPLETED` and `WAKE_LOCK` come from WorkManager's merged manifest. The app declares only
  `POST_NOTIFICATIONS` and `ACCESS_NETWORK_STATE`. The 1.1.1 release permission list is
  `ACCESS_NETWORK_STATE, POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, WAKE_LOCK, com.android.vending.BILLING,
  com.migrely.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`.
- `TimeChangeReceiver` (`TIMEZONE_CHANGED`, `TIME_SET`) and `MigrelyApplication` start both call
  `RescheduleRemindersUseCase`. Today that use case plans only the daily reminder.
- `NotificationDataSourceImpl` has two channels, `daily` and `effect`, both `IMPORTANCE_DEFAULT`. It sets no
  visibility, and it posts only when `areNotificationsEnabled()`.
- Room is at DB v6, and `reminder_settings` is the last table added. The JSON backup is at `BACKUP_SCHEMA_VERSION = 6`,
  and `BackupRecordsDataModel` leaves device state out on purpose.
- History's premium gate sits in `HistoryViewModel.onReportAction()` and is routed by
  `HistoryNavigationEventDestinationMapper` (report, or paywall with `PaywallOrigin.Report`).
- Home has slots that `:app` fills from other features (`trialBanner`, the medication card).

## Goals and non-goals

Goals: one date and one tap, a reminder that is honest for both Free and Premium, the existing paywall at the
person's own tap, nothing about health on the lock screen, no new permission, and no data lost on upgrade or import.

Non-goals: everything in the proposal's "Out of scope".

## Decisions

### D1. The feature lives in `features/reminders`, with a slot on home

The visit is a reminder. The date entry, the run, the reschedule and the home card's ViewModel all go in
`features/reminders`, so the permission flow is reused and not duplicated. The home card is a composable from
`features/reminders/ui`, which `:app` places in a new home slot, as it does with `trialBanner`. Home does not depend
on reminders. `features/report` reads the last visit through its own repository interface over the shared datasource,
because features do not depend on each other (BRD §12).

### D2. Data

- A `doctor_visit` table: `id TEXT PRIMARY KEY` (UUID), `date TEXT NOT NULL UNIQUE` (ISO `LocalDate`, no zone, like
  `day_mark`). DB v7, `AutoMigration(from = 6, to = 7)`, with a schema export and a migration test.
- In `reminder_settings`, nullable `LocalDate` columns: `visit_reminded_for` and `visit_card_closed_for` hold the
  visit date whose notification was shown, or whose card was tapped or closed, and `visit_postponed_to` (D3). Keying
  by date means a new date resets them on its own. They are device state: the backup leaves them out, and Replace
  keeps them. The same AutoMigration 6 to 7 adds them.
- Next visit = `MIN(date) WHERE date >= today`. Last visit = `MAX(date) WHERE date < today`. "today" is the phone's
  current local date. Saving a new next visit, in one transaction, deletes every row with `date >= today` and inserts
  the new one. That keeps at most one upcoming row, even when an older install had more.
- `DoctorVisitDataSource` in `datasource/source`, with the Room implementation in `datasource/implementation`. It
  offers a Flow of the next and last visits, `setNextVisit(date)`, `removeNextVisit()` (it returns the removed row for
  Undo) and `restore(row)`.

### D3. Planning the reminder

- The reminder time is `ZonedDateTime.of(visit.minusDays(2), 10:00, zone)`, with `zone` the phone's zone at planning
  time. A DST gap resolves forward, as `ZonedDateTime` does. 10:00 is never in a gap in any current zone.
- `RescheduleRemindersUseCase` also plans the visit: one unique work `visit`, `REPLACE`, with input
  `kind = visit, visitDate = <ISO>`. With no next visit, it cancels `visit`. This one path runs at app start (which
  also covers restore), after a reboot (WorkManager's own reschedule), on `TIMEZONE_CHANGED` and `TIME_SET`, after a
  save, a remove, an Undo and an import. It is safe to repeat.
- A third nullable column, `visit_postponed_to` (`LocalDate`), holds the day whose 10:00 the reminder waits for
  after an attack (EC-REM-12). It counts only while it lies between `visit - 2` and `visit`, so a new date ignores it.
- The planned time is `at = 10:00 on max(visit - 2, visit_postponed_to)`. The use case enqueues
  `max(at, now)` when all of these hold: there is a next visit, `visit_reminded_for != date`, and the visit day has not
  ended. Otherwise it cancels `visit`. Because a passed `at` is coerced to zero delay, as today, a phone that was off
  or dozing posts as soon as it can on the visit day or before (EC-REM-14).
- **Too close (EC-REM-08).** When the date is saved and `10:00 on visit - 2` has already passed, the save path sets
  `visit_reminded_for = date` in the same transaction, so that visit never posts. Only the card shows it.
- `RunVisitReminderUseCase`, in order:
  1. The input date is not the next visit: do nothing.
  2. `visit_reminded_for == date`: do nothing.
  3. The visit day has ended: do nothing.
  4. An attack is in progress: when tomorrow is on or before the visit day, set `visit_postponed_to = tomorrow` and
     plan again, else do nothing.
  5. Nothing logged in the default report period (D4): do nothing. A later run after an entry is not planned; the
     card covers that case.
  6. Post, then set `visit_reminded_for = date`.

  A failed read returns `retry`, as the daily run does. An app start or a zone change after an attack keeps
  `visit_postponed_to`, so the notification does not jump to the moment the app opens.
- The notification uses `setTimeoutAfter(<end of visit day in the current zone> - now)`, which the API supports from
  26 (minSdk). A move, remove or Undo cancels the posted notification by its fixed id.

### D4. The default report period, shared by the reminder and the report screen

`DefaultReportPeriod` is one pure domain function in each of the two features that need it. Their inputs differ, and
a shared module would couple the features (Konsist forbids it). Both return:
- `SinceLastVisit(lastVisit, today)` when `lastVisit >= today.minusMonths(24)`;
- `LastMonths(3)` otherwise.

The two copies are small, and each feature's unit test runs the same table of cases (no visit, a visit 24 months ago
to the day, one a day older, the visit day itself, yesterday), so a drift fails a test. "Nothing logged" asks the
datasource the D81 question, any attack, intake or pain-free mark between two dates, through a reminders repository.
Undo goes through the same save path, so its too-close check (D3) applies as well.

### D5. The notification

- A channel `visit`, named "Doctor visit", `IMPORTANCE_DEFAULT`, created like the other two.
- `setVisibility(VISIBILITY_PRIVATE)` and `setPublicVersion(<builder with title "Reminder" and no text>)`.
- Title and text come from `ux-designer`. A draft that meets the rule: "Your visit is on Fri, Nov 20" and "Make a
  report to bring." The date uses `DateTimeFormatter.ofLocalizedDate(MEDIUM)` with the weekday, following the locale
  (EC-TIM-05).
- The content intent opens `MainActivity` with `EXTRA_OPEN_DOCTOR_REPORT = true` and a request code of its own. There
  are no action buttons, because one tap is enough.
- Posting is still guarded by `areNotificationsEnabled()`. A blocked channel is read with
  `getNotificationChannelCompat("visit")?.importance == IMPORTANCE_NONE` for the row's notice.

### D6. From the tap to the report or the paywall

- `MainActivity` (onCreate and onNewIntent) reads the extra. When an attack is in progress, it ignores the extra, and
  the normal attack-mode start runs (EC-REM-12). Otherwise it navigates Home, then History with
  `openReport = true`.
- `HistoryViewModel` runs `onReportAction()` once when `openReport` is set, and the flag is consumed in
  `SavedStateHandle` so rotation and process restore do not repeat it. This is the same gate as the top-bar button, so
  Premium, Free, pending, offline and region-unavailable all behave as they do today (D108, D166). The paywall is
  untouched, which keeps its headline, products and messages.
- A tapped paywall never writes the "last self-shown" time, so it already does not count toward D110's limit. QA
  confirms this (A9). There is no code change for it.
- The card's report button uses the same navigation event, and its tap also sets `visit_card_closed_for`.

### D7. Backup

- `BACKUP_SCHEMA_VERSION = 7`, and `BackupRecordsDataModel` gets `doctorVisits: List<BackupDoctorVisitDataModel>`
  (`id`, `date`). The field is optional when decoding, so schema 1 to 6 files decode to an empty list.
- Replace: the visits are cleared and the file's are inserted, in the import transaction.
- Add: insert where neither the id nor the date exists. Insert a row with `date >= today` only when the phone has no
  row with `date >= today`.
- After commit, call `RescheduleRemindersUseCase`.
- 1.1.x apps reject schema 7 with "Update the app" (EC-BAK-02). That is accepted: the file stays whole, and nothing is
  dropped without a word.

### D8. Home card placement

The card goes below every safety card (overuse, first 10, the 72-hour question) and the "Did it help?" card, and above
the review prompt. It is never shown in attack mode or the dark room. `ux-designer` decides the final order and look
against these rules. The trial banner has its own place and is not affected.

## Risks

- **Discoverability.** Only the Reminders screen sets a date. That is a residual risk for the business goal. It is not
  a defect. `data-analyst` watches it at the 30-day review.
- **A forgotten "in progress" attack** blocks the notification for days. The card waits too. The existing SAF-F3
  question exists for this. This is a residual risk.
- **OEM battery managers** (Xiaomi, Samsung) can delay WorkManager past the visit day. The home card is the fallback.
  The notification never lies, because it names the date.
- **Schema 7** makes a 1.2 backup unreadable on 1.1. That is accepted as above.
- **Two copies of the default-period rule** could drift. The shared test table is the guard.

## Acceptance for QA

The Go/No-Go covers this change only (owner rule). It does not re-verify 1.1.1 areas, apart from the regressions
named here. Each check names a negative control, which shows the check can fail.

| # | Check | How | Negative control |
|---|---|---|---|
| A1 | Reminder at about 10:00, 2 days before, once | Instrumented test with `WorkManagerTestInitHelper` and `TestDriver.setInitialDelayMet`. On a device, set the clock to 09:50 of the reminder day | Visit for a date already reminded (`visit_reminded_for` set): no post |
| A2 | Too close: today, tomorrow, 2 days ahead after 10:00 | Device, 3 dates | 2 days ahead at 09:00: a notification does arrive at about 10:00 |
| A3 | Date moved after the reminder: old one gone, new one planned. Old work does nothing | `dumpsys notification` and the WorkManager test | Same date picked again: the work id and its enqueue time are unchanged |
| A4 | Remove, Undo, killed during Undo | Device, `am kill` within 5 s | Undo after 5 s is not offered |
| A5 | Notifications refused (API 36 and 26), channel off | QA AVDs `_36`, `_26` | Notifications allowed: no notice row |
| A6 | Attack in progress at 10:00: nothing that day, card after the end, notification at 10:00 the next day. Tap during an attack opens attack mode | Device clock | No attack: the notification arrives the same day |
| A7 | Nothing logged: no notification, no card | Fresh install | One intake logged: both appear |
| A8 | Lock screen privacy | Secure lock. Screenshots with "hide sensitive content" and with "show all". Text checked against the word list in the spec | "Show all" must show the date. If neither shows it, the screenshot proves nothing |
| A9 | Free tap opens the paywall with "A report for your doctor", Close leaves History. Premium opens the report. Pending, offline and region messages show. The tapped paywall does not block the 5th-migraine paywall | Debug simulated store (D113) | A tap on History's "Doctor report" gives the same screens |
| A10 | "Since last visit" first and default, the 24-month cut-off, the visit day, totals under 28 days | Seeded backup | No past visit: 3 months is the default |
| A11 | Zone, clock back, DST, reboot, phone off until the visit day, phone off past it | Device scenarios as in `add-stats-and-reminders-e2e-coverage` | No zone change: the time is unchanged |
| A12 | DB 6 to 7 upgrade from 1.0.2, 1.1.0 and 1.1.1 builds, with records kept | Install over each, then compare counts | Room `MigrationTestHelper` fails when the v7 schema JSON is missing |
| A13 | JSON 7 export. Schema 6 import. Add keeps one next visit. Replace | Byte check of the file | Add with no visit on the phone: the file's visit is added |
| A14 | Permissions equal the 1.1.1 list | `:app:checkReleasePermissions` and `diff` | Temporarily add `SCHEDULE_EXACT_ALARM` on a scratch branch: the task fails (shown once, then dropped) |
| A15 | TalkBack, 200% font in landscape, keyboard, RTL, reduce motion | The a11y audit on the Reminders screen, picker and home card | The audit flags an unlabelled control on a scratch build |
| A16 | Delete all data | Device | Before the delete: the visit work is listed |

What these checks do not prove: delivery timing on OEM battery managers, real people finding the feature, and the
real effect on purchases.

## Medical review

None of the planned wording makes a health claim: a date, "make a report", "since last visit". The "2 days" and the
"10:00" are product choices with no medical basis, and no text says otherwise. `medical-reviewer` checks the final copy
from `ux-designer`: the notification, the card, the row, its notice and the period label. The card must not promise an
outcome, such as better treatment or fewer migraines.

## Open for ux-designer

The row and its states (not set, set, notifications off, channel off). The picker title. The "Visit removed" message.
The notification title and text within D5's rule. The public version. The card's line, its buttons and its place
within D8. The "Since last visit" label with its date. TalkBack labels. 200% font and landscape layouts.
