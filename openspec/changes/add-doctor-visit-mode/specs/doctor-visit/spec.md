## ADDED Requirements

### Requirement: Setting the next visit
The Reminders screen SHALL show "Next visit" with the saved date or "Not set". A tap SHALL open a full-screen date
picker from today to 365 days ahead, where other days cannot be picked. "Done" SHALL save the date at once. There SHALL
be at most one next visit: a new date replaces it, and the same date changes nothing. No time, doctor or note SHALL be
asked (BRD REM-F4, EC-REM-07, D168).

#### Scenario: Setting a date
- **WHEN** on November 10 the user taps "Next visit", picks November 20 and taps "Done"
- **THEN** the row reads the date of November 20 and the reminder is planned for about 10:00 on November 18

#### Scenario: A past day
- **WHEN** the picker is open on November 10
- **THEN** November 9 and earlier, and days after November 10 of next year, cannot be picked, and TalkBack says so

#### Scenario: Changing the date before the reminder
- **WHEN** the next visit is November 20 and the user picks November 27
- **THEN** the row reads November 27, and only one reminder is planned, for about 10:00 on November 25

#### Scenario: The same date again
- **WHEN** the next visit is November 20 and the user picks November 20 again
- **THEN** nothing is saved again and the planned reminder is unchanged

### Requirement: Removing a visit
When a date is set, "Next visit" SHALL offer "Remove". It SHALL take the date away at once, cancel its reminder,
remove its notification and home card, and show "Visit removed" with Undo for 5 seconds or the longer accessibility
timeout. Undo SHALL restore the same date and plan it again. If the process ends before Undo, the removal SHALL stand
(BRD EC-REM-10, EC-UX-16, D168).

#### Scenario: Remove
- **WHEN** the user taps "Remove" on a visit set for November 20
- **THEN** the row reads "Not set", no reminder is planned, and "Visit removed" shows with Undo

#### Scenario: Undo
- **WHEN** the user taps Undo within 5 seconds
- **THEN** the row reads November 20 again and the reminder for November 18 is planned again

#### Scenario: Undo after the reminder time
- **WHEN** the visit was removed at 9:59:58 on November 18 and Undo is tapped at 10:00:02
- **THEN** no notification is sent and the home card shows

#### Scenario: Killed during Undo
- **WHEN** the app's process is killed 2 seconds after "Remove" and the app is opened again
- **THEN** "Next visit" reads "Not set" and no visit work is planned

### Requirement: After the visit
The next visit SHALL be the earliest saved date on or after today, and the last visit the latest saved date before
today, both by the phone's current local date. When the visit day ends at local midnight, "Next visit" SHALL read "Not
set", the date SHALL be kept as the last visit, and its notification and home card SHALL go (BRD EC-REM-15, D168).

#### Scenario: The day after
- **WHEN** the visit was November 20 and it is now November 21
- **THEN** "Next visit" reads "Not set", no card shows, and the Doctor report offers "Since last visit" from November 20

#### Scenario: On the visit day
- **WHEN** it is 11:00 on November 20, the visit day
- **THEN** "Next visit" still reads November 20

### Requirement: The visit reminder
At about 10:00 local time, two days before the next visit, one notification SHALL arrive for that date. It SHALL run
as WorkManager one-time work, with no exact alarm and no new permission. It SHALL name the visit's date, never a
count of days. If it could not run before, it SHALL still arrive on the visit day, and never after that day ends. It
SHALL go away when the visit day ends (BRD REM-F4, EC-REM-03, EC-REM-14, ADR-05, D168).

#### Scenario: Two days before
- **WHEN** the next visit is Friday, November 20 and something was logged in the report period
- **THEN** at about 10:00 on Wednesday, November 18 one notification names Friday, November 20 and offers a report

#### Scenario: The phone was off
- **WHEN** the phone is off from November 17 until 8:00 on November 20, the visit day
- **THEN** the notification arrives once soon after the phone starts, still naming November 20

#### Scenario: Off until after the visit
- **WHEN** the phone is off from November 17 until November 21
- **THEN** no notification arrives for November 20

#### Scenario: Once per date
- **WHEN** the notification for November 20 was shown and the phone restarts on November 19
- **THEN** no second notification arrives for November 20

#### Scenario: No exact alarm
- **WHEN** the release build's merged manifest is checked
- **THEN** its permissions equal those of 1.1.1, with no `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM` or `INTERNET`

### Requirement: A visit too close to remind
When the reminder time has already passed as the date is saved, because the visit is today, tomorrow, or two days
ahead and it is after 10:00, no notification SHALL be sent, and home SHALL show the visit card on the next return
(BRD EC-REM-08, D168).

#### Scenario: Tomorrow
- **WHEN** on November 19 the user sets the visit for November 20
- **THEN** no notification is planned or sent, and home shows the visit card

#### Scenario: Two days ahead in the afternoon
- **WHEN** at 15:00 on November 18 the user sets the visit for November 20
- **THEN** no notification is sent, and home shows the visit card

### Requirement: Changing the date after the reminder
When the date changes after its reminder arrived, the old notification and card SHALL go, and the new date SHALL get
its own reminder by the same rules. Work planned for a date that is no longer the next visit SHALL do nothing when it
runs (BRD EC-REM-09, D168).

#### Scenario: Moved a week later
- **WHEN** the reminder for November 20 was shown, and the user moves the visit to November 27
- **THEN** that notification and the card go, and a reminder is planned for about 10:00 on November 25

#### Scenario: Work for an old date
- **WHEN** work planned for November 20 runs after the visit was moved to November 27
- **THEN** it posts nothing

### Requirement: Not during an attack
While an attack is in progress at the reminder time, no notification SHALL be sent. The reminder SHALL try again at
about 10:00 the next day, up to the visit day, so it never arrives at night. A tap on the notification or the card
during an attack SHALL open the app in attack mode and open neither the report nor the paywall (BRD EC-REM-12,
EC-UX-12, EC-UX-13, D69, D168).

#### Scenario: An attack at 10:00
- **WHEN** an attack is in progress at 10:00 on November 18 and ends at 14:00
- **THEN** no notification arrives on November 18, the home card shows after 14:00, and the notification arrives at
  about 10:00 on November 19

#### Scenario: Tapped during an attack
- **WHEN** the user starts an attack and then taps the visit notification
- **THEN** the app opens in attack mode, with no report screen and no paywall

### Requirement: Nothing to report
When nothing was logged in the period the report would cover by default (no attack, no intake and no pain-free mark,
D81's rule), the reminder SHALL not be sent and the card SHALL not show. The date SHALL stay saved (BRD EC-REM-13,
EC-REP-01, D168).

#### Scenario: An empty diary
- **WHEN** a new user with nothing logged sets a visit for November 20
- **THEN** no notification arrives on November 18, no card shows, and "Next visit" still reads November 20

### Requirement: A private notification
The notification SHALL show the visit's date and an offer to make a report, and SHALL NOT show "migraine",
"headache", "doctor", a medication, a symptom, a count, a price, "Premium" or an offer. Its text SHALL be the same for
Free and Premium. It SHALL be private on the lock screen, with a public version that reads only "Reminder", in its own
"Doctor visit" channel (BRD EC-REM-16, D168).

#### Scenario: A lock screen that hides sensitive content
- **WHEN** the phone's lock screen hides sensitive notification content and the reminder arrives
- **THEN** the lock screen shows "Reminder" and no date or report text

#### Scenario: A lock screen that shows all content
- **WHEN** the lock screen shows all notification content and the reminder arrives
- **THEN** it shows the visit's date and the offer to make a report, and no health word, number, price or "Premium"

#### Scenario: Free and Premium
- **WHEN** the same visit's reminder arrives for a free user and for a Premium user
- **THEN** the two notifications read the same

#### Scenario: Its own channel
- **WHEN** the user opens Migrely's notification settings in Android
- **THEN** "Doctor visit" is a channel of its own, apart from the daily and medication ones

### Requirement: Notifications off
Setting a visit SHALL ask for notification permission when notifications are off for Migrely, at most once in 30
days. The date SHALL be saved whether it is allowed or not. While notifications or the "Doctor visit" channel are
off, the row SHALL say so and offer "Open settings", and the home card SHALL still show (BRD EC-REM-01, EC-REM-02,
EC-REM-11, D168).

#### Scenario: Refused
- **WHEN** on Android 13 the user sets a visit and refuses notifications
- **THEN** the date is saved, the row says notifications are off with "Open settings", and the home card shows on
  November 18

#### Scenario: Asked recently
- **WHEN** the permission was refused 10 days ago and the user sets a visit
- **THEN** Android does not ask again, and the date is saved

#### Scenario: Only the channel is off
- **WHEN** the "Doctor visit" channel is turned off in Android and a visit is set
- **THEN** the row says its notifications are off, and "Open settings" opens that channel's settings

### Requirement: The home card
From the reminder time until the visit day ends, home in normal mode SHALL show a card with one short line, a button
that opens the report and a close button. After a tap on either, it SHALL not return for that date. It SHALL never
cover or replace a safety card (overuse, 72 hours, first 10) and SHALL not show in attack mode or the dark room (BRD
EC-UX-13, D168).

#### Scenario: Two days before
- **WHEN** a user opens the app at 11:00 on November 18 with a visit on November 20
- **THEN** home shows the visit card

#### Scenario: Closed
- **WHEN** the user closes the card
- **THEN** it does not show again for November 20

#### Scenario: A safety card at the same time
- **WHEN** the overuse card and the visit card are both due
- **THEN** the overuse card shows in full, and the visit card does not cover it

### Requirement: Opening the report from the reminder
A tap on the notification or on the card's report button SHALL open History with the doctor report on top, through
History's own premium gate: the report screen for Premium, else the paywall with the doctor-report headline, whose
close leaves History. After a purchase the report screen SHALL open. Pending, offline and unavailable billing SHALL
show the paywall's own messages (BRD EC-REM-17, D108, D166, D168).

#### Scenario: Premium
- **WHEN** a Premium user taps the notification
- **THEN** the Doctor report screen opens above History, and Back returns to History

#### Scenario: Free
- **WHEN** a free user taps the notification and then closes the paywall
- **THEN** the paywall showed "A report for your doctor", and History is on screen

#### Scenario: Premium ended before the visit
- **WHEN** Premium ended on November 15 and the user taps the card on November 18
- **THEN** the paywall opens, and every record and free feature stays as it was

#### Scenario: Offline
- **WHEN** a free user with no internet taps the card, then taps the buy button
- **THEN** the paywall shows its offline message, and nothing else changes

#### Scenario: Payment pending
- **WHEN** a user whose payment is pending taps the notification
- **THEN** the paywall opens showing "Payment pending", and the report stays closed until the payment is confirmed

#### Scenario: Billing not available
- **WHEN** Google Play offers no Migrely products and a free user taps the card
- **THEN** the paywall says "Premium isn't available in your region yet", and closing it leaves History

#### Scenario: Bought from the reminder
- **WHEN** a free user taps the notification and buys on the paywall
- **THEN** the Doctor report screen opens with the same period it would have from History

### Requirement: No urgency
The reminder SHALL never open the paywall by itself. A paywall opened by its tap SHALL be the one History's "Doctor
report" opens, with no visit date, countdown, discount or urgency, and SHALL not count toward the limit of one
self-shown paywall in 7 days (BRD §7.3, D110, D168).

#### Scenario: The reminder arrives
- **WHEN** the reminder arrives and the user does not tap it
- **THEN** no paywall opens, now or on the next app start

#### Scenario: The 7-day limit
- **WHEN** a free user opened the paywall from the visit card yesterday and logs their 5th migraine today
- **THEN** the 5th-migraine paywall may still show itself by its own rule

### Requirement: Time zone, clock and restart
The reminder time SHALL be worked out again by the new local time when the app starts, after a reboot, and when the
time zone or clock changes. The visit date SHALL not move with the zone. A reminder shown for a date SHALL not be
shown again for it (BRD EC-REM-14, EC-REM-04, EC-REM-05, EC-DAY-04, D168).

#### Scenario: Travel
- **WHEN** the visit is November 20 and on November 16 the phone moves from Tashkent to New York
- **THEN** the reminder arrives at about 10:00 New York time on November 18, and the visit is still November 20

#### Scenario: Clock moved back
- **WHEN** the visit November 20 has passed, and the clock is set back to November 19
- **THEN** November 20 is the next visit again, and its reminder, already shown, is not shown again

#### Scenario: Daylight saving
- **WHEN** the clocks change on the night before the reminder day
- **THEN** the reminder still arrives at about 10:00 local time

### Requirement: Accessible everywhere
"Next visit" SHALL be one labelled node read with the full date. The picker SHALL follow the custom range rules: two
weeks visible in landscape and at 200% font, "Done" on only once a day is picked, days that cannot be picked
announced. "Remove" and closing the card SHALL be buttons a keyboard reaches. Text SHALL wrap, never cut; dates
follow the locale; layouts mirror right to left; the card appears without motion (BRD EC-REM-18, EC-UX-05, D127,
D168).

#### Scenario: TalkBack
- **WHEN** TalkBack focuses the row with a visit on November 20, 2026
- **THEN** it reads "Next visit" with "Friday, November 20, 2026" as one item, and announces it can be changed

#### Scenario: Largest font in landscape
- **WHEN** the font size is 200%, the phone is in landscape, and the user opens the picker
- **THEN** at least two weeks show, a day can be picked, and "Done" can be reached

#### Scenario: Keyboard
- **WHEN** the user moves with Tab on the Reminders screen and on home with the card showing
- **THEN** "Next visit", "Remove", the card's report button and its close button each take focus and work with Enter

#### Scenario: Reduce motion
- **WHEN** animations are off in Android and the card is due
- **THEN** the card appears without any animation

#### Scenario: Right to left
- **WHEN** Android's "Force RTL layout direction" is on
- **THEN** the row and the card mirror, and no text or button is cut or overlapped

### Requirement: Rotation, process death and failures
A picked but unsaved date SHALL survive rotation. A saved date SHALL survive process death and be planned again at
the next start. When saving fails, as on full storage, the app SHALL say so and keep the previous date and reminder.
A late read SHALL never overwrite a newer save, and fast second taps SHALL follow D159 and D160 (BRD EC-REM-19,
EC-STO-02, D168).

#### Scenario: Rotation in the picker
- **WHEN** the user picks November 20 and rotates the phone before "Done"
- **THEN** November 20 is still picked

#### Scenario: Storage full
- **WHEN** saving November 27 fails because storage is full and the visit was November 20
- **THEN** the storage message shows, and the row and reminder stay November 20

#### Scenario: Killed after saving
- **WHEN** the app is killed right after a visit is saved
- **THEN** after the next start the row shows the date and its reminder is planned

#### Scenario: A late read
- **WHEN** the screen's first read of the visit arrives after the user has already saved November 27
- **THEN** the row shows November 27, and the reminder is planned for November 25 only

#### Scenario: Two fast taps on Done
- **WHEN** the user taps "Done" twice quickly
- **THEN** the date is saved once, and the second tap does not reach the Reminders screen below

### Requirement: Delete all data and upgrade
"Delete all data" SHALL delete every visit and cancel its reminder and notification. Upgrading from any shipped
version SHALL keep every record, add no visit, and plan no visit reminder (BRD EC-BAK-06, EC-STO-01, EC-STO-03,
D168).

#### Scenario: Delete all data
- **WHEN** a visit is set and the user deletes all data
- **THEN** after the restart "Next visit" reads "Not set" and no visit work is planned

#### Scenario: Upgrade from 1.1.1
- **WHEN** 1.2.0 is installed over 1.1.1 with 40 attacks
- **THEN** the 40 attacks remain, "Next visit" reads "Not set", and no visit work is planned
