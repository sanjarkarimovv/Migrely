# Design

BRD D183 (T61, ux-designer's follow-up (c) from #213). Code read on develop e4b1fbcd, not run.

## Context

The daily reminder's moment is the evening the person chose. When it posts hours later, its sound no longer matches anything
the person expects, and the person most likely to be hit is the one lying still in a dark room, because a still, untouched
phone is exactly what Doze waits for.

### Where a late daily reminder comes from

| Path | Planned for | Can be hours late |
|---|---|---|
| Normal run, Doze or standby bucket holds the job | that date's set time | yes, AOSP deep-idle windows grow from about 1 h to about 6 h |
| Normal run, JobScheduler batching | set time | minutes (7 min seen in #213) |
| Reboot after the time (WorkManager restores the overdue run; the start keeps it, D176) | set time | yes |
| Clock or zone change after the time, today not shown (`due()` = now, D176) | now | yes, relative to the set time |
| Nothing planned at a start after the time (#197 residual, restore, failed chain) (`due()` = now) | now | yes |
| On-screen postpone (D176), once | run time + 30 min | about 30 min |
| On-screen postpone, repeated while the person stays in the app | + 30 min each | yes |

## Decisions

### D1. Quiet, not missing

A late daily reminder still posts, still shows in the shade with the status bar icon, the same text, both actions and the
lock screen's "Reminder" version, and still marks the date shown (`setDailyShown`). Only sound and vibration go. Every
Migrely channel is `IMPORTANCE_DEFAULT` (`NotificationDataSourceImpl.kt:107`), which never pops up on screen, so "no pop-up"
is not evidence on the default channel and QA does not count it. Only if the person raised "daily" to "Pop on screen" does
the group trick (D4) also stop a late one popping up: an optional QA row (4.6).
The notification stays until the person dismisses or answers it, as today (`setAutoCancel`). So nothing the person can
answer is lost, and D176's work to bring a held day back is kept.

### D2. Lateness = now − that date's set time, in the current zone, from the current setting

`RunDailyReminderUseCase` already has `now` (instant and zone), `date` and `settings.dailyTime`. Lateness is
`Duration.between(at(date, settings.dailyTime, now.zone), now.instant)`, using the same `DailyReminderTime.at()` the plan
uses, so DST behaves identically: a skipped time moves forward, a repeated time takes the earlier offset.

Why not the instant the run was planned for: the catch-up runs (`due()` = now) and postponed runs are planned for "now" or
"now + 30 min", so they would never count as late, and a reboot at 11:00 PM would still ring. The person's expectation is the
time they set, not WorkManager's bookkeeping. This also needs no new WorkManager input, no stored value, and no migration:
runs planned by any shipped version work unchanged, including old runs without a date (`date = today`).

Why the current setting: the run already reads settings fresh. If the person changed the time, the new time is what they
expect. Changing the time REPLACEs the planned run (`SetDailyReminderUseCase`), so a run for the old time cannot arrive.

Why the current zone: the dark room is in local time. The date check already uses `now.zone`.

Negative lateness (clock set back, or a run slightly early) is "not late".

### D3. Threshold: more than 60 minutes

Strictly greater than 60 minutes is quiet; 60:00 and below alert.

- Below it, and alerting: EC-REM-03's ±15 min, JobScheduler batching (7 min in #213), and the first on-screen postpone
  (D176 measured 8:31–8:36 PM), where the person has just left the app and the phone is in their hand.
- Above it, quiet: Doze holds, whose windows start at about an hour and grow; a reboot or clock change hours after the time;
  a second on-screen postpone.
- Rejected 30 min: the first postpone would go quiet. Rejected 2 h: a first Doze window would still ring.
- Consequence: a set time of 11:00 PM or later is never quiet, because a run for a date that has ended does not post (D176
  (4)), except on a night when the clocks fall back at midnight (America/Santiago, Asia/Beirut, Africa/Cairo): 11:00 PM to
  11:59 PM happens twice, so a run held to the second 11:10 PM is 70 minutes late and quiet, which is D2's rule working.
  Accepted: the person chose a late time and lateness under an hour still alerts.
- DST repeated hour: only a run at exactly the second 1:30 AM is 60 minutes late and alerts; a run at the second 1:31 AM or
  later is quiet. Rare, accepted.

### D4. Mechanism: `NotificationCompat.Builder.setSilent(true)` on this one post

Domain: `ShowDailyReminderRepository.showDailyReminder(date, quiet)` (or a two-value domain model; engineer's choice, one
name across layers). Data: `RemindersRepository` passes it. Data source: `NotificationDataSource.showDailyReminder(date,
quiet)`; `NotificationDataSourceImpl` adds `.setSilent(quiet)` to the existing builder. Nothing else in the builder changes.

Why per-post silence and not a second channel:
- a second channel adds a visible channel with a new name string in Android's settings;
- an `IMPORTANCE_LOW` channel can hide the status bar icon and lets "hide silent notifications" remove it from the lock
  screen: closer to missing;
- the person's own choices on the "daily" channel (sound, vibration, off) would not carry over.

How androidx core 1.17.0 implements it (ux-designer read `NotificationCompatBuilder.java:278-297`, 2026-11-16): on every
API from 26 up it is the group trick, not a platform flag. If no group is set it sets the group key "silent" and
`GROUP_ALERT_SUMMARY`, and clears sound and vibration. The daily post sets no group today, so it lands in group "silent"
with no summary. The engineer confirms this in the Robolectric test (task 2.5) and proves on device (task 2.8) that the
shade entry, icon and actions stay, on API 26 and API 36 alike.

### D5. Only the daily reminder

Migrely has no medication-taking reminder (REM-F1 to REM-F4). "Did it help?" (D67) asks about a dose already taken; its
dark-room risk exists on time too, and D69 already stops it during an attack, so a lateness rule would not answer it. The
visit reminder (D168) is a daytime, once-per-visit nudge where being noticed matters more. Both keep their sound. The
"Did it help?" always quiet (ux-designer, task 1.2) is a follow-up backlog row opened by team-lead, not this change.

### D6. Do Not Disturb, channels, TalkBack

The flag only removes an alert. DND, the channel's sound and importance, and notifications turned off keep their effect.
A DND exception for Migrely does not bring a late reminder's sound back, because the post itself carries no sound. TalkBack
reads the notification in the shade as before. Whether TalkBack announces the arrival of a silent notification is Android's;
QA records what it does, and it is not a pass or fail condition.

## Edge cases

| Case | develop e4b1fbcd | After |
|---|---|---|
| Doze holds the 8 PM run to 11:10 PM (EC-REM-29) | posts with sound | posts quietly, marked shown |
| Run at 8:07 PM | sound | sound |
| Exactly 60 min late | sound | sound |
| 61 min late | sound | quiet |
| First on-screen postpone, about 8:31 PM | sound | sound |
| Second postpone, about 9:40 PM | sound | quiet |
| Reboot at 11 PM, today not shown | sound | quiet |
| Clock moved forward past the time | sound | quiet |
| Clock moved back before the run | sound at 8 PM | sound at 8 PM |
| Clock moved back after a quiet post | no second post | no second post |
| Zone change east, now after the time | sound | quiet (new local time) |
| Zone change west, now before the time | sound at local 8 PM | sound at local 8 PM |
| Time changed to later while a run was held | sound at the new time | sound at the new time |
| Time changed to earlier after it passed | today lost, EC-REM-28 | unchanged |
| Set time 11:00 PM or later, held past midnight (no fall-back at midnight) | nothing posts | nothing posts |
| DST gap, set 2:30 AM, run 3:35 AM | sound | sound (read from 3:30) |
| DST overlap, second 1:30 AM | sound | sound (exactly 60 min) |
| DST overlap, second 1:31 AM or later | sound | quiet |
| Clocks fall back at midnight, 11 PM set time, run at the second 11:10 PM | sound | quiet (70 min) |
| Midnight between plan and run | nothing posts | nothing posts |
| Run date not today | nothing posts | nothing posts |
| Today answered, notifications off, reminder off | nothing posts | nothing posts |
| Migrely on screen | postponed 30 min | postponed 30 min (D176) |
| DND on | Android's rules | Android's rules, never louder |
| Migrely allowed through DND | sound | late: quiet |
| "daily" channel silent or off by the person | follows it | follows it, never louder (silent: QA 4.2; off: Android's, not tested) |
| Lock screen hides content | "Reminder" | "Reminder" |
| TalkBack | reads in the shade | reads in the shade; arrival recorded by QA |
| "Pain-free day" with an attack today | update, `setOnlyAlertOnce` | unchanged |
| "Log a migraine" | opens home, clears it | unchanged |
| "Did it help?" late | sound | sound |
| Visit reminder late | sound | sound |
| Upgrade with a run planned by any shipped version, with or without a date | sound | rule applies; no migration |
| Backup and restore | nothing stored | nothing stored |
| Process death, rotation, double tap | not reachable (worker) | not reachable |
| Free and Premium | same | same (proof: `RunDailyReminderUseCase` takes no entitlement input) |
| Offline | no network used | no network used |
| API 26 and API 36 | same code | `setSilent` checked on both |

## Risks

- **R1. `setSilent`'s group trick, API 26 and API 36 alike.** androidx implements it with group "silent" and
  `GROUP_ALERT_SUMMARY` on every API from 26 (D4). If on any API the
  notification loses its shade entry, its icon or its actions, stop and send it back to the product-manager: missing is
  worse than loud.
- **R2. A person who relies on vibration** (hard of hearing) gets no buzz for a late reminder. It still shows. Accepted:
  the same reasoning applies to everyone, and the reminder is not a safety path.
- **R3. Android 14 flash notifications, watches and screen wake** ("wake screen for notifications", always-on display) may
  or may not react to a silent post. Android's, not ours; QA records screen wake (4.3b).
- **R4. Emulator evidence only.** Real Doze windows and vendor behaviour are not covered, as for D176.
- **R5. Clock-forward test path.** QA's deterministic late post uses a clock change (D176's catch-up). The Doze-held path
  uses the same code after the run starts; one Doze row is optional and its cost is time, not risk.
