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
- Channel description: "A reminder two days before your next visit".
- `setVisibility(VISIBILITY_PRIVATE)` and `setPublicVersion(<builder with title "Reminder" and no text>)`, using the
  shared `notification_public_title` that `fix-notification-privacy` adds for all three channels (D169).
- Title "Your visit is on %1$s" and text "Make a report to bring." (copy table below). Note: the public version shows
  only under "Hide sensitive content". Under "Show all content", often the default, the full text shows, which is why
  the full text itself carries no health word.
- Dates: never a hard-coded pattern and not `ofLocalizedDate(MEDIUM)`, which has no weekday in en-US. Short date =
  `DateFormat.getBestDateTimePattern(locale, "EEEMMMd")` ("Fri, Nov 20"; en-GB "Fri 20 Nov"), and `"EEEMMMdy"` when
  the year is not the current one. Spoken date = `"EEEEMMMMdy"` ("Friday, November 20, 2026"), used in every
  contentDescription (EC-TIM-05).
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

On Home Today, in normal mode only: day header, trial banner, sky card, Period, the medication prompt slot (overuse
warning or "Did it help?"), **the visit card**, today's migraines, then the month calendar. In compact height
(landscape) the order is the same in the left column. The 72-hour question and the first-10 card live in attack mode
and Attack details, where the card never shows, so it cannot sit above them. The review prompt is Google's sheet, not
a card. A new `visitCard` slot is filled by `:app`, like `trialBanner`.

Look: the medication prompt's container (`Surface`, 20 dp corners, `surfaceContainer`), the decorative
`MigrelyIcons.Report` in `onSurfaceVariant`, a "Make a report" `TextButton` (at least 48 dp), and `MigrelyIcons.Close`
at the top end (48 dp target). Never the tertiary safety colour and no warning icon: this is a reminder, not a
warning. The card appears and goes without animation. When it goes, content below moves up at once. Residual: a fast
second tap on Close may land on what moved up (a migraine row opens its details, Back returns). It writes nothing,
and QA records it.

### D9. Reminders screen and picker (from `ux-designer`)

- **Order.** The app-level "notifications off" notice (unchanged), then the **Doctor visit** section first, then a
  24 dp gap and the existing daily and medication rows. Reason: only this screen sets a date, and discoverability is
  the plan's main residual risk.
- **Row.** Reuse the daily "Time" row pattern: one merged Button node, at least 56 dp. The value is in `primary` with
  tabular figures. At 130% and 200% the value moves under the label, start-aligned, and is never ellipsised. "Remove"
  is an end-aligned `TextButton` on its own line, at least 48 dp. After Remove, keyboard focus moves to the row,
  because the focused control is gone. QA records where TalkBack focus lands.
- **Notices.** With a date, when app notifications are off: one line, "This reminder can show only on Home.", with no
  button, because the app-level notice right above already has "Open settings". When only the channel is off: reuse
  `NotificationsOffNotice` with "Doctor visit notifications are off, so this reminder can show only on Home." and
  "Open settings" for the `visit` channel. When both are off, the app-level case wins. With no date, nothing shows.
  The notice is read again on resume.
- **Picker.** A new `MigrelyDatePicker` in `designsystem/picker`, a sibling of `MigrelyDateRangePicker` (D127). It
  is full screen, with a top row of Cancel, the title "Next visit" (the pane title) and Done. Done is off until a day
  is picked. A saved date is pre-picked, with its month shown. The days from today to today + 365 can be picked;
  others are shown disabled. Earlier months can't be reached. Later months can be browsed with every day disabled
  (Material's year range, accepted). Under the top row is one headline, a polite live region: "Pick a day", then the
  short date, spoken as the full date. Then Material's grid with its arrows. The calendar or text-input toggle stays.
  In compact height, everything under the top row scrolls and the top row never does, so the weekday row and two full
  weeks show at 200% in landscape (about 411 dp). The keyboard rules (`ROOM_FOR_HEADER_WHILE_TYPING`) and the resize
  rules come from D127.
- **"Visit removed".** Information kind, "Undo" (contentDescription "Undo removing the visit"), 5 s or the
  accessibility timeout, with the time line (hidden with reduced motion). Polite, and a body tap does nothing (D158).
  Then "Undone." (8 s). A failed Undo shows the existing error and leaves "Not set". The message area lies over the
  section heading and summary, which are not controls. Whether it reaches the row at 200% in landscape is not
  measured; QA records it.

### D10. Asking for permission after the picker (ux F3, EC-REM-20)

Order after Done:
1. The picker closes.
2. The date is saved at once, and the row shows it.
3. If a request is due (API 33 and later, never granted, and none in 30 days), a request is marked pending in
   `SavedStateHandle`.
4. The system dialog launches when **both** of these hold: the picker's window has gone, **and** 500 ms (D161) have
   passed since the lift on Done.

The request also needs the Reminders entry to be the current, resumed destination. When the person leaves first, the
pending flag is dropped: there is no dialog, and the 30-day count is not stamped. Rotation and process restore start
the 500 ms again.

Reason: the system dialog is another app's window. D160's wait cannot cover it, so a second quick tap on Done could
answer "Allow" or "Don't allow" for the person. This is a permission decision, so it ranks under privacy, ahead of
UX. Cost: the dialog comes half a second later.

Residual: a deliberate tap after 500 ms reaches the dialog, and that is the person's choice.

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
| A9 | Free tap opens the paywall with "A report for your doctor", with no price or offer before it and the same terms as any paywall. Close leaves History. Premium opens the report. Pending, offline and region messages show. The tapped paywall does not block the 5th-migraine paywall | Debug simulated store (D113) | A tap on History's "Doctor report" gives the same screens |
| A10 | "Since last visit" first and default, the 24-month cut-off, the visit day, totals under 28 days | Seeded backup | No past visit: 3 months is the default |
| A11 | Zone, clock back, DST, reboot, phone off until the visit day, phone off past it | Device scenarios as in `add-stats-and-reminders-e2e-coverage` | No zone change: the time is unchanged |
| A12 | DB 6 to 7 upgrade from 1.0.2, 1.1.0 and 1.1.1 builds, with records kept | Install over each, then compare counts | Room `MigrationTestHelper` fails when the v7 schema JSON is missing |
| A13 | JSON 7 export. Schema 6 import. Add keeps one next visit. Replace | Byte check of the file | Add with no visit on the phone: the file's visit is added |
| A14 | Permissions equal the 1.1.1 list | `:app:checkReleasePermissions` and `diff` | Temporarily add `SCHEDULE_EXACT_ALARM` on a scratch branch: the task fails (shown once, then dropped) |
| A15 | TalkBack, 200% font in landscape, keyboard, RTL, reduce motion | The a11y audit on the Reminders screen, picker and home card | The audit flags an unlabelled control on a scratch build |
| A16 | Delete all data | Device | Before the delete: the visit work is listed |
| A17 | Row TalkBack labels exactly as in the copy table, set and not set. Heading navigation lands on "Doctor visit" | TalkBack log | A build without the heading semantics: no heading stop |
| A18 | 100%, 130% and 200% font, portrait and landscape: the row value is never cut; Remove, the line and the notice never overlap | Screenshots | The same at 100% for comparison |
| A19 | Notices: app off gives the line with no button; channel off gives the notice, and "Open settings" opens the channel page; both off, the app-level case wins; back from settings, gone on resume | Device | Everything on: no line |
| A20 | Picker at 200% in landscape: weekday row and two full weeks with no scrolling, Done visible, off before a pick and on after. Days before today and after +365 are announced disabled. Rotation keeps the pick | Device and TalkBack | Material's plain `DatePickerDialog` on a scratch build shows fewer than 2 weeks (shown once) |
| A21 | F3: two taps on Done at 150 and 300 ms, with the permission never asked, API 36: the dialog shows after the wait and the second tap does not answer it. Back within 500 ms gives no dialog and no 30-day stamp | Device, `adb shell input tap` timing, `dumpsys package` permission flags | A scratch build that asks at once: record what the second tap does |
| A22 | "Visit removed" placement at 100% and 200% portrait and at 200% landscape: record what lies under it | Screenshots | |
| A23 | Card order with an overuse warning, and with "Did it help?" pending: the card is below. Hidden in attack mode and in the dark room. No animation in or out | 60 fps screen recording | Animations on in a debug build: the recording shows frames between states |
| A24 | Card TalkBack: the full spoken date; buttons labelled; the keyboard reaches both | TalkBack log | |
| A25 | Lock screen: "Hide sensitive content" shows "Migrely" and "Reminder" only; "Show all" shows the title and text exactly as in the copy table. In neither is the channel name "Doctor visit" shown (medical note 1) | Secure lock, screenshots | As A8 |
| A26 | Report chip "Since Aug 20" first and chosen; covers line "Since your last visit: …"; switching chips speaks the new line; a visit last year shows the year | Seeded backup, TalkBack | No past visit: no chip |

What these checks do not prove: delivery timing on OEM battery managers, real people finding the feature, and the
real effect on purchases.

## Copy (final)

Source: `ux-designer`'s spec (`migrely-work/t21/ux-spec.md`, section 7). `medical-reviewer` passed all 33 lines on
2026-11-09. The two "Home only" lines take the reviewer's optional accuracy fix, "can show only on Home": the card
does not show when nothing was logged (EC-REM-13), so "will show" would be too sure. English only; keys are
suggestions.

| Where | Key | Text |
|---|---|---|
| Reminders | `reminders_visit_title` | Doctor visit |
| Reminders | `reminders_visit_summary` | A reminder two days before, to make a report for your doctor |
| Reminders | `reminders_visit_next` | Next visit |
| Reminders | `reminders_visit_not_set` | Not set |
| Reminders | `reminders_visit_description` | Next visit, %1$s. Change it. |
| Reminders | `reminders_visit_description_not_set` | Next visit, not set. Set a date. |
| Reminders | `reminders_visit_remove` | Remove |
| Reminders | `reminders_visit_remove_description` | Remove the next visit |
| Reminders | `reminders_visit_home_only` | This reminder can show only on Home. |
| Reminders | `reminders_visit_channel_off` | Doctor visit notifications are off, so this reminder can show only on Home. |
| Reminders | `reminders_open_settings` (existing) | Open settings |
| Reminders | `reminders_open_settings_description` | Open notification settings |
| Reminders | `reminders_message_visit_removed` | Visit removed |
| Reminders | `reminders_snackbar_undo` | Undo |
| Reminders | `reminders_snackbar_undo_visit_description` | Undo removing the visit |
| Reminders | `reminders_message_undone` | Undone. |
| Picker | `reminders_visit_picker_title` | Next visit |
| Picker | `reminders_visit_picker_none` | Pick a day |
| Picker | `reminders_visit_picker_done` | Done |
| Picker | `reminders_visit_picker_cancel` | Cancel |
| Notification | `notification_channel_visit` | Doctor visit |
| Notification | `notification_channel_visit_description` | A reminder two days before your next visit |
| Notification | `notification_visit_title` | Your visit is on %1$s |
| Notification | `notification_visit_text` | Make a report to bring. |
| Notification | `notification_public_title` (all channels, D169) | Reminder |
| Notification (#178, D169) | `notification_effect_title` (changed) | Did it help? |
| Home card | `visit_card_text` | Your doctor visit is on %1$s. |
| Home card | `visit_card_text_description` | Your doctor visit is on %1$s. (spoken date) |
| Home card | `visit_card_report` | Make a report |
| Home card | `visit_card_close_description` | Close the visit reminder |
| Report | `report_since_visit` | Since %1$s |
| Report | `report_since_visit_description` | Since your last visit, %1$s |
| Report | `report_covers_since_visit` | Since your last visit: %1$s to %2$s |

The card may say "doctor" because the lock-screen rule does not apply in the app. The notification says "visit".

## Medical review

`medical-reviewer`, 2026-11-09: PASS on all 33 lines, with no health claim and no outcome promise. Its notes are
folded in:
1. The channel name "Doctor visit" must not show on the lock screen under either setting (A9, A8).
2. For a free user, "Make a report to bring." leads to the existing paywall. That paywall shows the price and the
   terms with no pressure, and it is an ordinary tap on a premium feature (D108). The store-policy reasoning is in
   D168 (4): the text is the same for Free and Premium, makes no promise that the report is free or already made, and
   carries no price, offer or countdown.
3. The "can show only on Home" wording, as above.
4. Accepted residual: a visit that was cancelled but left in place still becomes the last visit, and "Since your last
   visit" starts from it. The person can pick another period (EC-REP-04).

"2 days" and "10:00" are product choices with no medical basis, and no text says otherwise.
