# in-app-review Specification

## Purpose
Defines how Migrely asks for a Google Play rating: only through Google Play's own flow, with no permission and no
question of its own, when the app asks, how home must settle first, and what counts as a request (BRD §14.4,
D129–D132, EC-REV-01…12).

## Requirements

### Requirement: Google Play's review flow only
The app SHALL ask for a rating only through Google Play's in-app review flow. It SHALL ask no question of its own
before or during it, such as whether the user likes Migrely, and SHALL show no stars, rating or button of its own.
The flow SHALL add no permission, and INTERNET SHALL stay forbidden (BRD §14.4, D129).

#### Scenario: The flow opens
- **WHEN** the rules for asking all hold on home
- **THEN** Google Play's review flow is launched over home, and Migrely shows nothing before, around or after it

#### Scenario: No new permission
- **WHEN** the debug and release apps are built with the review library
- **THEN** the build's permission check passes, and neither merged manifest declares INTERNET or any network
  permission

### Requirement: When the app asks
On a home load in today's mode, the app SHALL ask only when all of these hold (D130):
- at least 3 migraines are logged and not deleted;
- no attack is in progress;
- no paywall showed itself in the last 3 days;
- the app's previous exit was not a crash or ANR, where Android tells it;
- it never asked, or it asked fewer than 3 times and the last time at least 120 days ago.

#### Scenario: The 3rd migraine ended
- **WHEN** a user with no requests yet ends their 3rd migraine, and home returns to today's mode
- **THEN** the review flow is launched once home has settled

#### Scenario: Never during an attack
- **WHEN** the 3rd migraine is started and is still in progress, in attack mode or the dark room
- **THEN** nothing is asked until the attack has ended and home shows today's mode (EC-REV-01)

#### Scenario: Soon after a paywall
- **WHEN** the onboarding paywall or the paywall after the 5th migraine showed itself less than 3 days ago
- **THEN** nothing is asked, and the next home load at least 3 days after that paywall may ask (EC-REV-02, EC-REV-03)

#### Scenario: No paywall on day one
- **WHEN** a new user, to whom no paywall showed itself because billing is unavailable, adds 3 past migraines
- **THEN** the review flow may be launched the same day (EC-REV-02)

#### Scenario: After a crash
- **WHEN** Android 11 or later reports that the app's previous process ended with a crash, a native crash or an ANR
- **THEN** nothing is asked while the app runs this time, and the next start after a normal exit may ask; on
  Android 8–10, which give no reason, this rule holds (EC-REV-04)

#### Scenario: Spacing and limit
- **WHEN** the app asked 119 days ago, or has asked 3 times already
- **THEN** nothing is asked, whatever the migraines, until 120 days have passed and fewer than 3 requests were made
  (EC-REV-06)

#### Scenario: Deleted migraines
- **WHEN** a user had 3 migraines and deleted one, or one is in its Undo window
- **THEN** only 2 count and nothing is asked (EC-REV-11)

#### Scenario: Premium or free
- **WHEN** the user has Premium
- **THEN** the same rules apply as for a free user (EC-REV-12)

#### Scenario: Unknown storage
- **WHEN** the migraines or the attack in progress cannot be read
- **THEN** nothing is asked on that load

### Requirement: Letting home settle
The app SHALL ask only after home has shown today's mode for 2 seconds while on screen with no message showing; a message SHALL
drop a wait under way. After an error during a visit to home, it SHALL NOT ask until the next visit. It SHALL have Google Play
prepare the flow and launch it only if home is still on screen, no attack runs and no paywall showed; at most once per process
(D130, D165, D178).

#### Scenario: Leaving within 2 seconds
- **WHEN** within 2 seconds of home showing today's mode the user starts an attack, opens another screen or sends the
  app to the background
- **THEN** nothing is asked, and the rules are checked again on the next home load in today's mode (EC-REV-07)

#### Scenario: Leaving while Google Play prepares
- **WHEN** after home has settled the user starts an attack, opens another screen or a paywall opens before Google
  Play has prepared the flow
- **THEN** the flow is not launched, nothing counts, and the next settled home load in today's mode tries again
  (EC-REV-01, EC-REV-03, EC-REV-07)

#### Scenario: Rotation and reloads
- **WHEN** the screen rotates or home reloads several times after the flow was launched in this process
- **THEN** the flow is not launched again in this process (EC-REV-08)

#### Scenario: An error on Home Today
- **WHEN** a request is due and "Couldn't save" or "Something went wrong" shows on Home Today, or appears during the 2-second
  wait or while Google Play prepares the flow
- **THEN** nothing is asked and nothing counts (EC-REV-13)

#### Scenario: The rest of the visit after an error
- **WHEN** an error has shown on Home Today and has gone, by "Saved.", Dismiss or a swipe, and the user stays on home
- **THEN** nothing is asked for the rest of that visit, including after rotation or a restored process (EC-REV-13)

#### Scenario: The next visit
- **WHEN** after such an error the user opens Settings and comes back, or starts the app again, and home then shows today's mode
  for 2 seconds with no message and the other rules met
- **THEN** the request may run (EC-REV-13)

#### Scenario: Undo or information on Home Today
- **WHEN** a request is due and "Migraine ended" with "Undo", or an information message, shows on Home Today
- **THEN** nothing is asked while it shows, and the 2 seconds start when it leaves (EC-REV-13)

### Requirement: Counting a request
A request SHALL count, with its time, when Google Play's flow completes, since Play says neither whether a card showed
nor whether a rating was given. When Google Play cannot run the flow, nothing SHALL count, nothing SHALL show and the
app SHALL NOT try again in that process. The marks SHALL be kept with the settings (D131).

#### Scenario: Completed flow
- **WHEN** Google Play's flow completes, whether or not the user rated or a card appeared
- **THEN** one request is counted with its time (EC-REV-06)

#### Scenario: Play cannot run it
- **WHEN** the Play Store is missing, the app was not installed from Play and Play fails to prepare or launch the
  flow, no activity is on screen, or Play does not answer in time
- **THEN** no error shows, the app goes on, no request counts, and none is tried again in that process (EC-REV-05)

#### Scenario: Delete all data
- **WHEN** the user deletes all data
- **THEN** the review marks are erased with the settings, and 3 new migraines may lead to a request (EC-REV-09)

#### Scenario: Restored on a new phone
- **WHEN** the app's data is restored by Android's backup
- **THEN** the review marks come back with the settings, so the 120-day spacing and the limit of 3 still apply
  (EC-REV-10)

### Requirement: A rating request that is not saved still counts
When the mark of a rating request cannot be written to the phone, Migrely SHALL count that request, with its time, until
the process ends, so Home does not ask again on its next loads. The paywall's time that could not be saved SHALL keep the
rating away as a stored one does. After a restart only the stored marks SHALL count (BRD D130, D131, D197, EC-STO-13).

#### Scenario: Storage full when a rating is asked
- **WHEN** the phone's storage is full and the rating request opens
- **THEN** the next Home loads in this process do not ask again

### Requirement: Quiet after a safety card
The app SHALL not ask for a rating within 3 days after the first-10 safety card, the "Still going?" card or the
medication overuse card appeared. The moment each appears SHALL be kept with the review marks: moved by Android's backup,
removed by "Delete all data", left out of the JSON export. A moment dated after now SHALL count as recent until the clock
passes it (BRD D188, EC-REV-14).

#### Scenario: After the first 10
- **WHEN** the first-10 safety card appeared on an attack, the attack ended, and home returns to today's mode within
  3 days with every D130 condition met
- **THEN** nothing is asked (EC-REV-14)

#### Scenario: After "Still going?"
- **WHEN** "Still going?" appeared on a long attack, and the attack is ended within the next 3 days
- **THEN** nothing is asked until 3 days after that card appeared (EC-REV-14)

#### Scenario: The overuse card on home
- **WHEN** the medication overuse card appears on home
- **THEN** no review flow starts on that visit or in the next 3 days (EC-REV-14)

#### Scenario: Three days later
- **WHEN** the last safety card appeared at least 3 days ago and every D130 condition holds
- **THEN** the next home load in today's mode may ask, as before

#### Scenario: Clock moved back
- **WHEN** the clock is moved back so that the last safety card's moment is in the future
- **THEN** nothing is asked until the clock passes that moment and 3 more days have gone by

#### Scenario: Storage full when a safety card appears
- **WHEN** the phone's storage is full when a safety card appears, so its mark cannot be saved
- **THEN** the mark counts until the process ends, and no rating is requested within 3 days of the card in that process
  (BRD D197)

#### Scenario: Delete all data and restore
- **WHEN** the user deletes all data, or restores Android's backup on a new phone
- **THEN** the mark is gone after deleting, as a successful delete starts the app again in a new process; one this
  process could not save is also cleared just before (unit-tested, not visible on a device); it travels with the other
  review marks after a restore, and the JSON export has no such field
