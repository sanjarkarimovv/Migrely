## ADDED Requirements

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
The app SHALL ask only after home has shown today's mode for 2 seconds while the app is on screen. It SHALL first have
Google Play prepare the flow, and SHALL launch it only if home is still on screen, no attack is in progress and no
paywall has shown itself. It SHALL launch the flow at most once per process, whatever the rotations or home loads
(D130).

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
