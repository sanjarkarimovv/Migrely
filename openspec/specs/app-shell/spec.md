# app-shell Specification

## Purpose
Defines how Migrely starts and presents itself before any migraine feature exists: its identity, the screen it opens on,
its default theme, and what it must never do on launch (ask for permissions or send data off the device).

## Requirements

### Requirement: App identity and supported devices
The app SHALL be named "Migrely", SHALL use the application ID `com.migrely.app`, and SHALL install and run on Android 8.0
(API level 26) and newer.

#### Scenario: Install on the oldest supported Android version
- **WHEN** the app is installed on a device running Android 8.0 (API 26)
- **THEN** the installation succeeds and the launcher shows an icon labelled "Migrely"

#### Scenario: Device below the supported version
- **WHEN** a device runs Android 7.1 (API 25) or older
- **THEN** the app cannot be installed on it

### Requirement: Launch opens the home screen
The app SHALL open on the home screen when started from the launcher. The home screen SHALL show today's sky and the
control to start an attack, or attack mode when an attack is in progress, and SHALL survive a configuration change
without returning to a different screen.

#### Scenario: Cold start from the launcher
- **WHEN** the user taps the Migrely icon, the app is not running and no attack is in progress
- **THEN** the home screen is shown with today's sky and "Migraine started"

#### Scenario: Cold start during an attack
- **WHEN** the user taps the Migrely icon while an attack is in progress
- **THEN** the home screen is shown in attack mode

#### Scenario: Rotation on the home screen
- **WHEN** the device is rotated while the home screen is visible
- **THEN** the home screen is still shown after the rotation, in the same mode as before

### Requirement: Dark low-brightness theme by default
The app SHALL use a dark, low-brightness theme by default regardless of the system theme (BRD EC-UX-01). Backgrounds and
large surfaces SHALL NOT use pure white.

#### Scenario: System in light mode
- **WHEN** the device's system theme is light and the user opens the app
- **THEN** the app is shown with the dark theme

#### Scenario: System in dark mode
- **WHEN** the device's system theme is dark and the user opens the app
- **THEN** the app is shown with the dark theme

### Requirement: No permission prompts on launch
The app SHALL NOT show any runtime permission request when it starts. Permissions are requested only at the moment a
feature needs them (BRD EC-REM-01).

#### Scenario: First launch after install
- **WHEN** the user opens the app for the first time after installing it
- **THEN** no system permission dialog is shown

### Requirement: No data leaves the device on launch
The app SHALL NOT send any data off the device when it starts or while the home screen is shown (BRD principle 4).

#### Scenario: Launch with network traffic observed
- **WHEN** the app is started and the home screen is shown while outgoing network traffic from the device is monitored
- **THEN** the app makes no network requests

### Requirement: Times follow a time zone change
When the device's time zone changes, while the app is open or in the background, every screen SHALL show its times
again at once, naming a city only for times in another zone than the new device zone. What the user was doing, the
screen, its scroll position and any message on screen SHALL be kept (BRD EC-TIM-02, D36).

#### Scenario: Back from another time zone during an attack
- **WHEN** an attack was started at 14:33 in Dubai, the phone moves to Tashkent while the app is in the background,
  and the user opens the app again
- **THEN** attack mode shows "Started 2:33 PM (Dubai)" at once

#### Scenario: History open during the change
- **WHEN** History lists a Dubai attack as "Sat, Oct 24, 3:25 PM – 3:26 PM" and the phone's zone changes to Tashkent
- **THEN** the row reads "Sat, Oct 24, 3:25 PM – 3:26 PM (Dubai)" when the user looks at it again, and History stays
  on the same month

### Requirement: Dates and times in English
While the app's words are English only, every name a date or time shows, months, weekdays and AM or PM, SHALL be
English. Only the first day of the week and the system's 12- or 24-hour setting SHALL come from the phone. Dates SHALL be
formatted in the phone's locale when its language is English, otherwise in English with the phone's country, or in
plain English when it has none (BRD EC-L10N-01, EC-L10N-03, EC-TIM-05, D117).

#### Scenario: A phone in Russian
- **WHEN** the phone's locale is Russian (Russia) with the 24-hour setting
- **THEN** home's date names its weekday and month in English, such as "Sunday" and "October", History's weeks start on
  Monday, and times read like "14:30"

#### Scenario: A phone in English (United States)
- **WHEN** the phone's locale is English (United States) with the 12-hour setting
- **THEN** the weeks start on Sunday and times read like "2:30 PM"

#### Scenario: The language changes while the app runs
- **WHEN** the phone's language is changed from English to Russian while Migrely is open
- **THEN** on returning to Migrely its dates are still English, and its weeks start on Monday

#### Scenario: A first day chosen in the phone's settings
- **WHEN** a phone in Russian has Sunday chosen as the first day of the week in its regional preferences
- **THEN** Migrely's weeks start on Sunday

#### Scenario: The CSV on a phone in Russian
- **WHEN** a phone in Russian exports the CSV
- **THEN** its columns are still separated by semicolons, by the phone's own language (D72)

### Requirement: Only the current screen navigates
A navigation event SHALL navigate only while the screen observing it is the current back-stack entry, compared by entry
id. A late result of a screen no longer current SHALL NOT navigate. A screen fading in is current, so its first tap and
what it sends on entering SHALL count, unless that press is ignored as the second tap of a double tap. The check reads the
observer's screen, not the sender's (BRD EC-UX-21, D154, D159).

#### Scenario: Two rows on Home's today list
- **WHEN** the user touches two rows of "Logged today" within a fade, by two taps or a resting finger
- **THEN** one details screen opens, and Delete returns to home with "Undo the deletion"

#### Scenario: The same row twice
- **WHEN** the user double taps one row of "Logged today"
- **THEN** one details screen opens

#### Scenario: A first tap on a screen fading in
- **WHEN** the user taps Back on the editor while it is still fading in
- **THEN** the list shows, unless the press is ignored as the second tap of a double tap (D159)

#### Scenario: A screen that navigates as it enters
- **WHEN** a screen sends a navigation event while it fades in
- **THEN** the event is obeyed

#### Scenario: A late result after leaving
- **WHEN** a screen's async work ends after the user has left that screen for another destination
- **THEN** no navigation happens

#### Scenario: An entry replaced by launchSingleTop
- **WHEN** `launchSingleTop` replaces the current entry's object
- **THEN** it keeps its id and still counts as current

#### Scenario: Two entries of one destination
- **WHEN** a late result from a leaving entry arrives while another entry of the same destination is current
- **THEN** it navigates, because the check knows the observer's screen and not the sender's

#### Scenario: A paywall due on a screen just left
- **WHEN** a paywall is due on home's load and the user has just left home
- **THEN** it does not open, nothing is marked as shown, and home's next load asks again

### Requirement: Touches only on the current screen
The pointer input of this window on a back-stack entry that is not current SHALL be consumed, read at each event, so a
finger already down cancels instead of clicking. Semantics SHALL NOT be touched, so screen readers and switch access keep
working. Dialogs and popups are separate windows and SHALL NOT be covered. Layout SHALL NOT change. A press on the current
entry is not decided here: the second tap of a double tap has its own rule (BRD EC-UX-22, D155, D159).

#### Scenario: A resting finger on Remove
- **WHEN** a finger rests on a row's Remove in My medications, another finger taps another row, and the first lifts during
  the fade
- **THEN** the editor opens and nothing is archived

#### Scenario: A resting finger on Delete migraine
- **WHEN** a finger rests on "Delete migraine", another finger taps Back, and the first lifts during the fade
- **THEN** nothing is deleted

#### Scenario: A resting finger on Delete everything
- **WHEN** DELETE is typed, a finger rests on "Delete everything", another finger taps Back, and the first lifts during the
  fade
- **THEN** nothing is deleted and Settings shows

#### Scenario: A new touch falling through
- **WHEN** a new touch lands during a push where the leaving screen below has a control
- **THEN** it does not reach the leaving screen

#### Scenario: A push
- **WHEN** the user taps a control on a screen fading in after a push
- **THEN** the control answers, unless the press comes under Android's double-tap timeout and within its double-tap distance of
  the last lift, on an entry no touch has lifted on, which is ignored as the second tap of a double tap (D159)

#### Scenario: A pop where the leaving screen takes no touch
- **WHEN** the user taps a control on the screen fading in after a pop, where the leaving screen has no control or scroll
  column
- **THEN** the control answers, unless the press is ignored as the second tap of a double tap (D159), which on a return can only happen on a screen no touch has lifted on

#### Scenario: A pop where the leaving screen has a control
- **WHEN** the user taps during a pop where the leaving screen has a control or a scroll column
- **THEN** the tap does nothing, for at most the length of the fade

#### Scenario: Nothing navigated
- **WHEN** a finger rests on a control and lifts with no navigation in between
- **THEN** the control clicks as usual, unless the press is ignored as the second tap of a double tap (D159)

#### Scenario: Returning to the screen
- **WHEN** a control's screen was left and is current again after the fade
- **THEN** the control answers, unless the press is ignored as the second tap of a double tap (D159), which on a return can only happen on a screen no touch has lifted on

#### Scenario: A screen reader on a leaving screen
- **WHEN** an accessibility service performs the click action on a control of a leaving screen
- **THEN** the action runs, and any navigation it sends is dropped by the rule above

#### Scenario: Size and place
- **WHEN** a screen is shown through this rule
- **THEN** it has the size and place it would have without it

### Requirement: One fade between screens
Every navigation SHALL use one 200 ms linear fade, set in one place, for entering and leaving, on a push and on a pop. The
library's default SHALL NOT be used. With the system "Remove animations" setting on, no fade SHALL play (BRD EC-UX-23, D156).

#### Scenario: Any navigation
- **WHEN** the user moves between any two screens
- **THEN** the screens fade linearly over 200 ms, and the leaving screen is gone after that

#### Scenario: Tapping after Back
- **WHEN** the user presses Back and taps the screen below 300 ms later
- **THEN** the tap answers

#### Scenario: Animations removed
- **WHEN** the system "Remove animations" setting is on
- **THEN** the next screen shows at once, with no fade

### Requirement: The second tap of a double tap does not reach a new screen
A press on a screen just entered SHALL be ignored, as a whole gesture except a text field's long press, when it comes less than Android's double-tap timeout
after a touch lifted on the screen before, and within Android's double-tap distance of that lift. Both values SHALL come from
the platform and the times from the touch events. This comes before the rule that a screen fading in takes touches (BRD
EC-UX-24, D159).

#### Scenario: Add medication on a scrolled details screen
- **WHEN** the user double taps Add medication with a gap under the timeout, with 4 medications, and a Log button lies under
  the finger on the screen that opens
- **THEN** Log medication opens and waits for a choice, and no intake is written

#### Scenario: Add medication on details, not scrolled
- **WHEN** the user double taps Add medication with a gap under the timeout, with 8 medications
- **THEN** Log medication opens and no intake is written

#### Scenario: Log medication on Home
- **WHEN** the user double taps Log medication on Home with a gap under the timeout, with 4 medications in landscape or 14 in
  portrait
- **THEN** Log medication opens and no intake is written

#### Scenario: Medication taken in attack mode
- **WHEN** the user double taps "Medication taken" with a gap under the timeout, with 4 medications in landscape or 8 in
  portrait
- **THEN** Log medication opens in the dark room and no intake is written

#### Scenario: Remove pain level
- **WHEN** the user double taps the right end of Home's first today row with a gap under the timeout, and Remove pain level
  lies under the finger on the details screen
- **THEN** details opens and the pain level stays

#### Scenario: A location chip
- **WHEN** the user double taps a History row with a gap under the timeout, and a location chip lies under the finger on the
  details screen
- **THEN** details opens and no location is added

#### Scenario: The Reminders switch
- **WHEN** the user double taps the Reminders row in Settings with a gap under the timeout, at 100% or 200% font, and "Ask if
  a medication helped" lies under the finger
- **THEN** Reminders opens and the setting stays on

#### Scenario: Any other screen
- **WHEN** a second tap under the timeout lands on any screen opened by the first, including the paywall's buy button
- **THEN** that press does nothing, because the rule is on every destination and not on a list of controls

#### Scenario: A very short gap
- **WHEN** the second press comes under 100 ms after the lift, even under 40 ms, and the new screen is already current
- **THEN** it does nothing, because there is no lower limit on the gap

#### Scenario: Three quick taps
- **WHEN** a third tap follows an ignored second tap, under the timeout and within the distance
- **THEN** it does nothing either, and the next press after a pause of the timeout answers

#### Scenario: Repeated tapping
- **WHEN** the user keeps tapping a control of a new screen, with every gap under the timeout and within the distance
- **THEN** no tap answers, with no upper limit on how many, because a shaky third tap must not write
- **AND** the next tap after a pause of the timeout answers

#### Scenario: The ignored press is held or dragged
- **WHEN** the ignored press is held or becomes a drag
- **THEN** nothing clicks or scrolls for that gesture, and the next gesture answers

#### Scenario: An ignored press held on a text field
- **WHEN** the ignored press is held still on a text field for about half a second, Android's long-press time
- **THEN** text selection, a magnifier and a haptic can show, because consuming the press cannot stop that long press
- **AND** the text does not change and nothing is written, which is accepted as a residue

#### Scenario: A short ignored tap on a text field
- **WHEN** the ignored press is a short tap on a text field
- **THEN** the field does not take focus, and the next tap focuses it

#### Scenario: Remove animations
- **WHEN** "Remove animations" is on and the user double taps a control that opens a screen
- **THEN** the second tap does nothing, because the rule does not depend on the fade

#### Scenario: Slow frames
- **WHEN** frames are slow while the new screen enters
- **THEN** the gap is still read from the touch events, so a slow frame neither lengthens nor shortens it

#### Scenario: The clock moves
- **WHEN** the date, the time zone or the clock changes between the two taps
- **THEN** the rule is unchanged, because touch event times do not follow the clock

### Requirement: Every other press answers
A press SHALL answer when it comes at or after the timeout, when it lies beyond the distance, when the last lift was on the
same screen, or when no touch lifted recently in this window. No message or other visible text SHALL be added (BRD EC-UX-25,
D159).

#### Scenario: A tap from the timeout up
- **WHEN** the user taps a control on the new screen, at the same point, 300 ms or more after the lift of the tap that opened it
- **THEN** the control answers, even if the fade has not ended

#### Scenario: A slower double tap still writes
- **WHEN** the second tap comes 300 ms or more after the lift and lands on a Log button
- **THEN** the intake is written with Done and Undo, because that is a plain tap on a visible control

#### Scenario: A tap elsewhere on the new screen
- **WHEN** a tap lands on the new screen under the timeout but beyond the distance from the first
- **THEN** the control answers

#### Scenario: Two taps with no new screen
- **WHEN** the user taps twice quickly on a control that opens no screen, such as a location chip
- **THEN** both taps count as before

#### Scenario: Opened from a notification
- **WHEN** a screen opens from a notification and the user taps a control straight away
- **THEN** the control answers, because no touch lifted in this window just before it

#### Scenario: Opened from a shortcut
- **WHEN** the "start migraine" shortcut opens attack mode and the user taps a control straight away
- **THEN** this rule does not act on the press, and any wait of attack mode's own (D32) stays as it was

#### Scenario: A first launch and after process death
- **WHEN** the app starts or the process was killed and the user taps a control straight away
- **THEN** the control answers, because the time of the last lift is not kept across them

#### Scenario: A rotation between two taps
- **WHEN** the phone rotates after the first tap, which recreates the activity, and the user taps a control
- **THEN** the control answers, because the time of the last lift is not kept across it

#### Scenario: A screen that opens itself just after a tap
- **WHEN** a screen opens by itself, such as a paywall due on Home, just after a tap, and a press lands under the timeout and
  within the distance of that tap
- **THEN** that press does nothing, and the next one answers

#### Scenario: Another finger
- **WHEN** another finger presses beyond the distance while the ignored press is down
- **THEN** that finger's press answers

#### Scenario: Nothing to see
- **WHEN** a press is ignored and it is not held on a text field
- **THEN** no text, sound, haptic or layout change shows, and the new screen stays as it entered

### Requirement: Back and returns do not change
The rule SHALL NOT cover a return to a screen where a touch has already lifted, by Back, Done, Save or `popUpTo`, also after the
activity was recreated or the process restored. Touches after a return SHALL behave as they did before this change, so the time
before a tap lands after Back SHALL not grow (BRD EC-UX-26, D159).

#### Scenario: A tap after Back
- **WHEN** the user taps Back and then taps a row of the screen below
- **THEN** the tap lands as it did before this change, and #148's measures do not get worse

#### Scenario: A double tap on Back
- **WHEN** the user double taps Back
- **THEN** the second tap does nothing while the leaving screen takes it, for at most the fade, and after that it lands on the
  screen below as it did before this change

#### Scenario: A double tap on Done or Save
- **WHEN** the user double taps a control that returns to the screen before
- **THEN** this rule does not act, and the leaving screen takes the second tap for at most the fade

#### Scenario: A return after the activity was recreated
- **WHEN** the user tapped to leave a screen, the phone rotates or the process is killed and restored, the user taps Back, and
  presses under the timeout and within the distance of that tap
- **THEN** the press answers, because which screens were touched survives the recreation

#### Scenario: A return to a screen never touched
- **WHEN** the user returns to a screen that a touch has never lifted on, such as one under a screen opened from a shortcut
- **THEN** the rule can act on one press under the timeout and within the distance, which is accepted as harmless

### Requirement: The double-tap rule is touch only
The rule SHALL act only on pointer input in this window. Semantics SHALL NOT change, so TalkBack, the click action of switch
access and keyboards SHALL work at any moment. Dialogs and popups are separate windows and SHALL NOT be covered (BRD EC-UX-24,
D155, D159).

#### Scenario: TalkBack on a new screen
- **WHEN** an accessibility service performs the click action on a control of a screen just entered, under the timeout
- **THEN** the action runs

#### Scenario: Keyboard
- **WHEN** a keyboard activates a control of a screen just entered, under the timeout
- **THEN** the control answers

#### Scenario: Nothing added to the tree
- **WHEN** a screen goes through the guard
- **THEN** its semantics tree and its layout are the same as without the guard

#### Scenario: A dialog
- **WHEN** the first tap opens a dialog and the second lands on it
- **THEN** this rule does not act, so the second tap reaches the dialog
- **AND** this rule does not make that path safe: it was measured separately and it writes (#157, #159)

### Requirement: The double-tap check can fail
The check for this rule SHALL include a negative control: with the guard removed, it SHALL fail on every route in #156's table,
on debug and R8. A person SHALL try it by hand on a real phone on the internal track. Reports SHALL say that emulator runs with
injected taps do not prove what a human hand does (BRD D159).

#### Scenario: Guard removed
- **WHEN** QA's table runs on a build with only the guard removed, under the timeout
- **THEN** writes come back on every route in the table, on debug and on R8

#### Scenario: Guard in place
- **WHEN** QA's table runs on the build under test
- **THEN** no route writes under the timeout, and from about 300 ms up the tap still answers

#### Scenario: By hand on the internal track
- **WHEN** 1.1.0 is on the internal track
- **THEN** a person double taps the routes on a real phone, and then taps single deliberate taps on the new screen, and the
  result goes in the Go/No-Go with what it does not prove
- **AND** a false intake from a natural double tap at or above the timeout goes back to `team-lead` before Production
- **AND** after repeated tapping on a control of a new screen, the next tap after a short pause answers, and a screen that feels
  frozen goes back to `team-lead` before Production
