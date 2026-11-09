## ADDED Requirements

### Requirement: Setting the next visit
The Reminders screen SHALL show "Next visit" with the saved date or "Not set". A tap SHALL open a full-screen date
picker from today to 365 days ahead, where other days cannot be picked. "Done" SHALL save the date at once. A new date SHALL
replace only visits dated after today, and the same date SHALL change nothing. No time, doctor or note SHALL be asked
(BRD REM-F4, EC-REM-07, EC-REM-15, D168).

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

#### Scenario: A later date on the visit day
- **WHEN** on November 20, the visit day, the user picks December 18
- **THEN** the row reads December 18, the visit of November 20 is kept, and from November 21 it is the last visit

### Requirement: Removing a visit
When the next visit is after today, "Next visit" SHALL offer "Remove"; on the visit day it SHALL not. Remove SHALL
take the date away at once, cancel its reminder,
remove its notification and card, show "Visit removed" with Undo for 5 seconds or the longer accessibility timeout,
and move focus to "Next visit". Undo SHALL restore the date, plan it again and say "Undone."; for a date now past it
SHALL delete nothing. A removal SHALL stand if the process ends first (BRD EC-REM-10, EC-UX-16, D168).

#### Scenario: Remove
- **WHEN** the user taps "Remove" on a visit set for November 20
- **THEN** the row reads "Not set", no reminder is planned, "Visit removed" shows with "Undo", and keyboard focus is
  on "Next visit"

#### Scenario: Undo
- **WHEN** the user taps "Undo" within 5 seconds
- **THEN** "Undone." shows, the row reads November 20 again, and the reminder for November 18 is planned again

#### Scenario: Undo after the reminder time
- **WHEN** the visit was removed at 9:59:58 on November 18 and Undo is tapped at 10:00:02
- **THEN** no notification is sent, no extra text shows, and the home card shows

#### Scenario: No Remove on the visit day
- **WHEN** it is November 20 and the next visit is November 20
- **THEN** the row shows November 20 and no "Remove" is offered

#### Scenario: Undo across midnight
- **WHEN** a visit for November 20 was removed at 23:59:58 on November 19 and Undo is tapped at 00:00:02
- **THEN** November 20 is restored as today's visit, and no other visit is deleted

#### Scenario: Killed during Undo
- **WHEN** the app's process is killed 2 seconds after "Remove" and the app is opened again
- **THEN** "Next visit" reads "Not set" and no visit work is planned

### Requirement: After the visit
The next visit SHALL be the earliest saved date after today, or today's visit when there is none later; the last
visit SHALL be the latest saved date before today, by the phone's local date. A visit dated today or earlier SHALL
never be deleted by a save or Remove. When the visit day ends, its notification and card SHALL go, and it SHALL be the
last visit (BRD EC-REM-15, D168).

#### Scenario: The day after
- **WHEN** the visit was November 20 and it is now November 21
- **THEN** "Next visit" reads "Not set", no card shows, and the Doctor report offers "Since Nov 20" first

#### Scenario: On the visit day
- **WHEN** it is 11:00 on November 20, the visit day
- **THEN** "Next visit" still reads November 20

#### Scenario: Clock set back before a past visit
- **WHEN** the visit of November 20 has passed and the clock is set back to November 18
- **THEN** November 20 counts as upcoming again, an accepted residual, and nothing is deleted until a date is saved

#### Scenario: A save after the clock was set back
- **WHEN** the clock was set back from November 21 to November 18 and the user then saves December 18
- **THEN** November 20 is replaced, the accepted residual of a clock set back by 2 or more days

#### Scenario: Clock set back by one day
- **WHEN** the clock is set back from November 21 to November 20 and the user saves December 18
- **THEN** November 20 is kept, because it is the visit day

### Requirement: The visit reminder
At about 10:00 local time, two days before the next visit, one notification SHALL arrive for that date. It SHALL run
as WorkManager one-time work, with no exact alarm and no new permission. It SHALL name the visit's date, never a
count of days. If it could not run before, it SHALL still arrive on the visit day, and never after that day ends. It
SHALL go away when the visit day ends (BRD REM-F4, EC-REM-03, EC-REM-14, ADR-05, D168).

#### Scenario: Two days before
- **WHEN** the next visit is Friday, November 20, something was logged in the report period, and the phone is set to
  en-US
- **THEN** at about 10:00 on Wednesday, November 18 one notification reads "Your visit is on Fri, Nov 20" and "Make a
  report to bring.", with no action buttons

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
When the reminder time has already passed as the date is saved or imported, because the visit is today, tomorrow, or
two days ahead and it is after 10:00, no notification SHALL be sent, and home SHALL show the visit card on the next
return (BRD EC-REM-08, EC-BAK-06, D168).

#### Scenario: Tomorrow
- **WHEN** on November 19 the user sets the visit for November 20
- **THEN** no notification is planned or sent, and home shows the visit card

#### Scenario: Two days ahead in the afternoon
- **WHEN** at 15:00 on November 18 the user sets the visit for November 20
- **THEN** no notification is sent, and home shows the visit card

#### Scenario: Imported too close
- **WHEN** on November 19 the user replaces with a backup whose next visit is November 20
- **THEN** no notification arrives after the import, and home shows the visit card

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
during an attack SHALL only bring the app forward in attack mode, leaving its back stack as it was, and open neither
the report nor the paywall (BRD EC-REM-12,
EC-UX-12, EC-UX-13, D69, D168).

#### Scenario: An attack at 10:00
- **WHEN** an attack is in progress at 10:00 on November 18 and ends at 14:00
- **THEN** no notification arrives on November 18, the home card shows after 14:00, and the notification arrives at
  about 10:00 on November 19

#### Scenario: Tapped during an attack
- **WHEN** the user starts an attack and then taps the visit notification
- **THEN** the app opens in attack mode, with no report screen and no paywall

#### Scenario: A form left open
- **WHEN** during an attack the user has a form open from attack mode, leaves the app, and taps the visit notification
- **THEN** the app comes forward with the same form, unchanged

### Requirement: Nothing to report
When nothing was logged in the period the report would cover by default (no attack, no intake and no pain-free mark,
D81's rule), the reminder SHALL not be sent, the date SHALL count as reminded so no later start posts for it, and the
card SHALL show only once something is logged. The date SHALL stay saved (BRD EC-REM-13, EC-REP-01, D168).

#### Scenario: An empty diary
- **WHEN** a new user with nothing logged sets a visit for November 20
- **THEN** no notification arrives on November 18, no card shows, and "Next visit" still reads November 20

#### Scenario: A reboot after the skip
- **WHEN** the run skipped November 20 on November 18 and the phone restarts at 03:00 on November 19
- **THEN** no notification arrives for November 20

#### Scenario: Logged after the skip
- **WHEN** after the skip the user logs a migraine on November 19
- **THEN** home shows the visit card, and no notification arrives

### Requirement: A private notification
The notification SHALL read "Your visit is on <short date>" and "Make a report to bring.", and SHALL NOT show
"migraine", "headache", "doctor", a medication, a symptom, a count, a price, "Premium" or an offer. Its text SHALL be
the same for Free and Premium. It SHALL be private on the lock screen, with a public version titled only "Reminder",
in its own "Doctor visit" channel, whose name SHALL not show on the lock screen (BRD EC-REM-16, EC-REM-21, D168,
D169).

#### Scenario: A lock screen that hides sensitive content
- **WHEN** the phone's lock screen hides sensitive notification content and the reminder arrives
- **THEN** the lock screen shows Migrely with "Reminder", and no date, report text or channel name

#### Scenario: A lock screen that shows all content
- **WHEN** the lock screen shows all notification content and the reminder arrives
- **THEN** it shows "Your visit is on Fri, Nov 20" and "Make a report to bring.", and no health word, number other than
  the date, price, "Premium" or channel name

#### Scenario: Free and Premium
- **WHEN** the same visit's reminder arrives for a free user and for a Premium user
- **THEN** the two notifications read the same

#### Scenario: Its own channel
- **WHEN** the user opens Migrely's notification settings in Android
- **THEN** "Doctor visit" is a channel of its own, described as "A reminder two days before your next visit", apart
  from the daily and medication ones

### Requirement: Notifications off
Setting a visit SHALL ask for notification permission when notifications are off for Migrely, at most once in 30
days, and the date SHALL be saved either way. With a date set: app notifications off SHALL add "This reminder can show
only on Home." under the row, beside the screen's existing notice; only the "Doctor visit" channel off SHALL show its
own notice with "Open settings" for that channel. The home card SHALL still show (BRD EC-REM-01, EC-REM-02, EC-REM-11,
D168).

#### Scenario: Refused
- **WHEN** on Android 13 the user sets a visit and refuses notifications
- **THEN** the date is saved, the screen's notice offers "Open settings", the row has "This reminder can show only on
  Home." under it, and the home card shows on November 18

#### Scenario: Asked recently
- **WHEN** the permission was refused 10 days ago and the user sets a visit
- **THEN** Android does not ask again, and the date is saved

#### Scenario: Only the channel is off
- **WHEN** app notifications are on, the "Doctor visit" channel is off, and a visit is set
- **THEN** a notice reads "Doctor visit notifications are off, so this reminder can show only on Home." with "Open
  settings", which opens that channel's settings

#### Scenario: Both off
- **WHEN** app notifications and the channel are both off
- **THEN** only the app-level notice and the line under the row show, not the channel notice

#### Scenario: No date
- **WHEN** notifications are off and no visit is set
- **THEN** nothing is added under "Next visit"

#### Scenario: Back from settings
- **WHEN** the user turns notifications on in Android and returns to Reminders
- **THEN** the line and notices are gone at once

### Requirement: Asking for permission after the picker
When setting a visit needs the permission, the app SHALL ask only after the picker has closed and 500 ms have passed
since the lift that tapped "Done", and only while Reminders is still the current screen. The date SHALL be saved at
once and not wait. Leaving the screen first SHALL skip the request and not start the 30-day count (BRD EC-REM-20,
D161, D168).

#### Scenario: Two fast taps on Done
- **WHEN** with the permission never asked, the user taps "Done" twice, 150 ms apart
- **THEN** the date is saved once, and the system permission dialog appears only after 500 ms, untouched by the second
  tap

#### Scenario: Back during the wait
- **WHEN** the user taps "Done" and then Back within 500 ms
- **THEN** the date is saved, no permission dialog appears, and the next visit set later may still ask

#### Scenario: Rotated during the wait
- **WHEN** the phone is rotated within 500 ms of "Done"
- **THEN** the wait starts again, and the dialog appears once, after it

### Requirement: The home card
From the reminder time until the visit day ends, home in normal mode SHALL show, below the medication prompt, a card
reading "Your doctor visit is on <short date>." with "Make a report" and a close button. It SHALL name the date, never
"today" or "tomorrow". After a tap on either button it SHALL not return for that date, with no message or Undo. It
SHALL never cover a safety card and SHALL not show in attack mode or the dark room (BRD EC-UX-13, D168).

#### Scenario: Two days before
- **WHEN** a user opens the app at 11:00 on November 18 with a visit on November 20
- **THEN** home shows "Your doctor visit is on Fri, Nov 20." with "Make a report"

#### Scenario: Closed
- **WHEN** the user taps "Close the visit reminder"
- **THEN** the card goes at once with no message, and does not show again for November 20

#### Scenario: A safety card at the same time
- **WHEN** the overuse card or "Did it help?" is due along with the visit card
- **THEN** that card shows in full above, and the visit card is below it

#### Scenario: Across midnight
- **WHEN** the card shows at 23:59 on November 19 and the screen stays open past midnight
- **THEN** it still reads "Your doctor visit is on Fri, Nov 20."

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
"Next visit" SHALL be one Button node, read "Next visit, <spoken date>. Change it." or "Next visit, not set. Set a
date.". The picker SHALL follow D127: pane title "Next visit", a polite line "Pick a day" or the date, two weeks and
"Done" visible at 200% in landscape, "Done" off until a pick, other days disabled. Every button SHALL be labelled and
reachable by keyboard. Text SHALL wrap, never cut; dates follow the locale; RTL mirrors; nothing animates (BRD
EC-REM-18, EC-UX-05, D127, D168).

#### Scenario: TalkBack
- **WHEN** TalkBack focuses the row with a visit on November 20, 2026
- **THEN** it reads "Next visit, Friday, November 20, 2026. Change it." as one item

#### Scenario: Labels
- **WHEN** TalkBack moves over "Remove", the card's text and the card's close button
- **THEN** it reads "Remove the next visit", "Your doctor visit is on Friday, November 20, 2026." and "Close the visit
  reminder"

#### Scenario: The picker opens
- **WHEN** the picker opens with no date set
- **THEN** TalkBack says "Next visit" and then "Pick a day", "Done" is announced as disabled, and so is a day before
  today

#### Scenario: Largest font in landscape
- **WHEN** the font size is 200%, the phone is in landscape, and the user opens the picker
- **THEN** the weekday row and at least two full weeks show without scrolling, and "Done" can be reached

#### Scenario: Keyboard
- **WHEN** the user moves with Tab on the Reminders screen and on home with the card showing
- **THEN** "Next visit", "Remove", "Make a report" and the close button each take focus and work with Enter

#### Scenario: Reduce motion
- **WHEN** animations are off in Android and the card is due
- **THEN** the card appears and goes without any animation

#### Scenario: Right to left
- **WHEN** Android's "Force RTL layout direction" is on
- **THEN** the row and the card mirror, the close button moves to the left, and no text or button is cut or overlapped

#### Scenario: Another year
- **WHEN** the next visit is January 8 of next year
- **THEN** the row shows the short date with the year

### Requirement: Rotation, process death and failures
A picked but unsaved date SHALL survive rotation. A saved date SHALL survive process death and be planned again at
the next start. When saving fails, as on full storage, the app SHALL say so and keep the previous date and reminder.
A late read SHALL never overwrite a newer save, and fast second taps SHALL follow D159, D160 and D161 (BRD EC-REM-19,
EC-STO-02, D168).

#### Scenario: Rotation in the picker
- **WHEN** the user picks November 20 and rotates the phone before "Done"
- **THEN** November 20 is still picked

#### Scenario: Storage full
- **WHEN** saving November 27 fails because storage is full and the visit was November 20
- **THEN** "Couldn't save. Your phone's storage is full." shows, and the row and reminder stay November 20

#### Scenario: Killed after saving
- **WHEN** the app is killed right after a visit is saved
- **THEN** after the next start the row shows the date and its reminder is planned

#### Scenario: A late read
- **WHEN** the screen's first read of the visit arrives after the user has already saved November 27
- **THEN** the row shows November 27, and the reminder is planned for November 25 only

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
