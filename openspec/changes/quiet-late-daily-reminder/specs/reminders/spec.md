## ADDED Requirements

### Requirement: A late daily reminder posts quietly
A daily reminder that posts more than 60 minutes after its date's set time, taken from the current setting in the phone's
zone at that moment, SHALL post without sound, vibration or heads-up. It SHALL still post, with the same text, actions,
status bar icon and lock screen version, and SHALL mark the date shown. At 60 minutes late or less, or early, it SHALL
alert as the channel says (BRD D183, EC-REM-29).

#### Scenario: Held by Doze for hours
- **WHEN** the daily reminder is on for 8:00 PM, today is not answered, the phone sits untouched in Doze, and the run comes
  at 11:10 PM with Migrely not on screen
- **THEN** "How was today?" posts once at 11:10 PM with "Pain-free day" and "Log a migraine", with no sound, no vibration
  and no pop-up over the screen
- **AND** it shows in the notification shade with Migrely's status bar icon, and today is marked shown

#### Scenario: About on time
- **WHEN** the run for 8:00 PM comes at 8:07 PM
- **THEN** "How was today?" posts with the channel's sound, as before

#### Scenario: The boundary
- **WHEN** the run for 8:00 PM comes at 9:00 PM, exactly 60 minutes late
- **THEN** it posts with the channel's sound
- **AND** a run that comes at 9:01 PM posts quietly

#### Scenario: The first on-screen postpone
- **WHEN** the person was in Migrely when the 8:00 PM run came, left without answering, and the postponed run comes at
  about 8:31 PM
- **THEN** it posts with the channel's sound, as D176 measured

#### Scenario: Postponed past the hour
- **WHEN** the person stays in Migrely until 9:10 PM, so the run is postponed until about 9:40 PM, and they leave without
  answering
- **THEN** the run at about 9:40 PM posts quietly

#### Scenario: Reboot after the time
- **WHEN** the reminder is on for 8:00 PM, today is not shown, and the phone restarts at 11:00 PM
- **THEN** the run that comes after the restart posts quietly, once, if Migrely is not on screen

#### Scenario: Clock moved forward
- **WHEN** the reminder is on for 8:00 PM, today is not shown, and at 7:00 PM the person sets the clock to 11:00 PM
- **THEN** the catch-up run for today posts quietly

#### Scenario: Clock moved back before the run
- **WHEN** the run for 8:00 PM has not come, and the clock is set back from 8:30 PM to 7:00 PM
- **THEN** the reminder comes at about 8:00 PM with the channel's sound

#### Scenario: Clock moved back after a quiet post
- **WHEN** a quiet reminder posted at 11:00 PM, and the clock is set back to 7:00 PM the same date
- **THEN** no second reminder posts for that date

#### Scenario: Time zone change after the time
- **WHEN** the reminder is on for 8:00 PM, today is not shown, and the phone moves to a zone where it is now 11:00 PM
- **THEN** the catch-up run posts quietly, because lateness is read in the new local time

#### Scenario: Time zone change before the time
- **WHEN** the phone moves west at 7:30 PM to a zone where it is now 5:30 PM
- **THEN** the reminder comes at about 8:00 PM local time with the channel's sound

#### Scenario: The person changed the time
- **WHEN** the run was held past 8:00 PM, and at 9:30 PM the person changes the reminder to 10:00 PM
- **THEN** the run at about 10:00 PM posts with the channel's sound, because lateness is read against the new time

#### Scenario: A late reminder time
- **WHEN** the reminder is on for 11:15 PM and Doze holds the run past midnight
- **THEN** nothing posts about the date that ended, as before, so a reminder at 11:00 PM or later never posts quietly

#### Scenario: Daylight saving, a skipped time
- **WHEN** the reminder is on for 2:30 AM on the night the clocks jump from 2:00 AM to 3:00 AM, and the run comes at 3:35 AM
- **THEN** it posts with the channel's sound, because lateness is read from 3:30 AM, where the plan put the run

#### Scenario: Daylight saving, a repeated hour
- **WHEN** the reminder is on for 1:30 AM on the night the clocks fall back from 2:00 AM to 1:00 AM, and the run comes at
  the second 1:30 AM
- **THEN** it is exactly 60 minutes late and posts with the channel's sound

#### Scenario: Free and Premium
- **WHEN** the same late run comes for a free user and a Premium user
- **THEN** both get the same quiet reminder

#### Scenario: The check can fail
- **WHEN** the clock-forward row runs on develop e4b1fbcd
- **THEN** the events log shows a `notification_alert` with sound for the late reminder, which shows the row can fail

### Requirement: Quiet only removes an alert, only for the daily reminder
The quiet rule SHALL only remove an alert, never add one. Do Not Disturb, the person's channel settings and notifications
turned off SHALL keep their effect, and a Do Not Disturb exception for Migrely SHALL NOT make a late reminder sound. "Did
it help?" and the visit reminder SHALL alert as before. No setting, text, channel, permission or stored value SHALL be
added (BRD D183, EC-REM-30).

#### Scenario: Do Not Disturb on
- **WHEN** Do Not Disturb is on and the daily reminder posts on time or late
- **THEN** Android treats it as before, and a late one adds no sound

#### Scenario: Migrely allowed through Do Not Disturb
- **WHEN** the person allowed Migrely through Do Not Disturb and a late reminder posts
- **THEN** it makes no sound and no vibration

#### Scenario: The person's channel settings
- **WHEN** the person set the "daily" channel to silent, or turned it off
- **THEN** an on-time reminder follows that setting as before, and a late one is never louder

#### Scenario: Notifications off
- **WHEN** notifications are off for Migrely and the run comes late
- **THEN** nothing posts and the date is not marked, as before

#### Scenario: Lock screen
- **WHEN** a late reminder posts while the lock screen hides sensitive content
- **THEN** it shows Migrely with "Reminder" only, as every Migrely notification does

#### Scenario: TalkBack
- **WHEN** TalkBack is on and a late reminder posts
- **THEN** the person can reach it in the shade, where TalkBack reads its title, text and both actions as before

#### Scenario: Pain-free day on a quiet reminder
- **WHEN** the person taps "Pain-free day" on a quiet reminder and an attack touches today
- **THEN** the notification reads "Not marked. Open Migrely to see today." without sound, as before

#### Scenario: "Did it help?" late
- **WHEN** the "Did it help?" notification for a 2:00 PM intake comes at 5:30 PM
- **THEN** it posts with its channel's sound, as before

#### Scenario: Visit reminder late
- **WHEN** the visit reminder planned for 10:00 AM comes at 1:00 PM
- **THEN** it posts with its channel's sound, as before

#### Scenario: Nothing new to see or allow
- **WHEN** the person opens Reminders, Android's notification settings for Migrely, or the app's permissions after the
  update
- **THEN** they find the same settings, the same three channels and the same permissions as on develop e4b1fbcd
