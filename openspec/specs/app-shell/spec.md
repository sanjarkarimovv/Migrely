# app-shell Specification

## Purpose
Defines how Migrely starts and presents itself before any migraine feature exists: its identity, the screen it opens on,
its default theme, what it must never do on launch (ask for permissions or send data off the device), and the storage
housekeeping every process start does so that an app update never empties the diary (BRD D170).

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
same screen, or when no touch lifted recently in this window, unless the rule for a lift on a dialog's card ignores it. No
message or other visible text SHALL be added (BRD EC-UX-25, D159, D161).

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
activity was recreated or the process restored. Touches after such a return SHALL behave as they did before this change, so the
time before a tap lands after Back SHALL not grow. A return from a dialog's button is the one exception, under D161's rule (BRD
EC-UX-26, D159, D161).

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

#### Scenario: A return from a dialog's button
- **WHEN** a dialog's button returns to a touched screen, and a press lands there under the dialog lift rule's limit and within the
  distance of that lift
- **THEN** the press does nothing, as D161 says, while a return by Back or by a control of the main window stays as above

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
- **AND** the dialog's own wait ignores it (D160), and a lift on the dialog's card counts for what is beneath it (D161)

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

### Requirement: A dialog waits before it takes a touch
Every dialog SHALL ignore touches on its content for 500 ms from when it appears, and a press begun in that time SHALL stay
ignored until it lifts. One shared helper and one constant SHALL set this, so a new dialog has it by default. Nothing SHALL
change on screen during the wait or when it ends, and no text SHALL be added (BRD EC-UX-27, D160).

#### Scenario: Restore in landscape
- **WHEN** an automatic copy exists, the phone is in landscape, and the user double taps "Restore the copy" over where Restore
  will appear, with the second tap inside the wait
- **THEN** the dialog opens and waits, and nothing on the phone is replaced

#### Scenario: How well did it help
- **WHEN** the user double taps an intake's row, at 100% or 200% font or in landscape, with the second tap inside the wait
- **THEN** the dialog opens and waits, and no effect is written

#### Scenario: Add pain level at 200% font
- **WHEN** the user double taps Add pain level at 200% font, with the second tap inside the wait
- **THEN** the dialog opens and waits, and no pain level is added

#### Scenario: Theme
- **WHEN** the user double taps the Theme row, at 100% or 200% font or in landscape, with the second tap inside the wait
- **THEN** the dialog opens and waits, and the theme stays

#### Scenario: Add your own symptom
- **WHEN** the user double taps "Add your own", with the second tap inside the wait on Cancel
- **THEN** the dialog stays open

#### Scenario: From the date to the time
- **WHEN** the user double taps Next on the date step, with the second tap inside the wait
- **THEN** the time step opens and waits, and no time is picked

#### Scenario: Any other dialog
- **WHEN** a new or changed dialog is added, or another font size or layout puts a button under the finger
- **THEN** it waits too, because a check fails if a dialog does not use the helper

#### Scenario: A press held past the wait
- **WHEN** a press begins inside the wait and lifts after it
- **THEN** it does nothing, and the next press answers

#### Scenario: A tap just after the wait
- **WHEN** the user taps a control in the dialog just after the wait
- **THEN** the control answers, on the first tap

#### Scenario: A tap just after the wait on a dialog opened from a dialog
- **WHEN** a dialog opens from a lift on another dialog, as the time step does from Next, a second tap is ignored by its wait, and a
  tap comes just after the wait
- **THEN** the control answers, because a dialog that appeared after the lift is not beneath it and its own wait alone decides
- **AND** the wait counts from when the dialog appears, which is after the lift, so its end is later than the lift plus 500 ms, and
  later still under load: on an emulator the time step attached 62 to 113 ms after the lift, and one tap at 587 ms after the first
  lift did nothing at load 11

#### Scenario: A person who is really fast
- **WHEN** a person who has read nothing yet, or knows the dialog, taps a control inside the wait
- **THEN** that tap does nothing, with no ripple, text or sound, and the next tap works

#### Scenario: A slower double tap
- **WHEN** the second tap comes after the wait and lands on a dialog's button
- **THEN** the button runs, because from there it is a plain tap on a visible control, which is the residual risk

#### Scenario: Nothing to see
- **WHEN** a dialog is in its wait, or the wait ends
- **THEN** it looks the same: no dimming, flicker, change of colour or change of layout

#### Scenario: A drag inside the wait
- **WHEN** a drag begins inside the wait on a dialog that scrolls
- **THEN** that gesture does nothing, and the next one scrolls

#### Scenario: Controls that read a press themselves
- **WHEN** a press inside the wait lands on a slider, a clock dial or a day in a calendar
- **THEN** nothing is written, and any of these that can still move is recorded with what it did

#### Scenario: A short tap on a text field
- **WHEN** a short tap inside the wait lands on the name field of "Add your own"
- **THEN** the field takes no focus and no keyboard opens, and the next tap focuses it

#### Scenario: A press held on a text field
- **WHEN** a press inside the wait is held still on a text field for about half a second
- **THEN** long-press selection can still start, with a haptic, which is accepted as in D159, and the text does not change

#### Scenario: Remove animations
- **WHEN** "Remove animations" is on
- **THEN** the wait is the same, because it does not depend on an animation

#### Scenario: A slow frame
- **WHEN** the phone stalls while a dialog opens or during the wait
- **THEN** the wait counts from when the dialog appears, and a press is judged by the time on its own touch event

#### Scenario: The clock moves
- **WHEN** the date, the time zone or the clock changes during the wait
- **THEN** the wait is unchanged, because it does not follow the clock

### Requirement: A dialog with no first tap waits the same
The wait SHALL be the same whatever opened a dialog, with a tap or without, because it gives a person a moment to see it. It
SHALL start again when a dialog is shown again by a rotation or a restored process. It SHALL NOT start again on a return from
Home or Recents to a dialog still open. What was typed or chosen SHALL be kept as it is today (BRD EC-UX-29, D160).

#### Scenario: After the file picker
- **WHEN** the system file picker returns a file, the import dialog appears, and a tap lands on it inside the wait
- **THEN** that tap does nothing, so a double tap on the file cannot run Replace

#### Scenario: A rotation with a dialog open
- **WHEN** the phone rotates while a dialog is open and a tap lands inside the new wait
- **THEN** that tap does nothing, and the next tap answers

#### Scenario: A restored process
- **WHEN** the process is killed and restored with a dialog open and a tap lands inside the wait
- **THEN** that tap does nothing, and the next tap answers

#### Scenario: What was typed
- **WHEN** a name is typed in "Add your own" and the phone rotates
- **THEN** the name stays, and the wait reaches only the card
- **AND** nothing in that dialog requests focus, so whether a keyboard is still open after the rotation is what QA records

#### Scenario: A return from Recents
- **WHEN** the user leaves a dialog open, comes back from Home or Recents, and taps a control straight away
- **THEN** the control answers, because the dialog was already seen, and a double tap on a Recents card landing on a dialog's
  button is residual risk

### Requirement: Leaving a dialog during the wait
During the wait a tap outside the dialog's card SHALL NOT close it. Back SHALL close it at once. After the wait a tap outside
SHALL close it as before, where the dialog allows that. A press begun outside inside the wait SHALL NOT close it when it lifts
after the wait (BRD EC-UX-28, D160).

#### Scenario: A second tap outside the card
- **WHEN** the user double taps a control that opens a dialog, and the second tap lands outside the card inside the wait
- **THEN** the dialog stays open

#### Scenario: A press outside held past the wait
- **WHEN** a press begins outside the card inside the wait and lifts after it
- **THEN** the dialog stays open

#### Scenario: A tap outside after the wait
- **WHEN** the user taps outside the card after the wait
- **THEN** the dialog closes as before

#### Scenario: Back during the wait
- **WHEN** the user presses Back, by gesture or button, inside the wait
- **THEN** the dialog closes at once and nothing is written

#### Scenario: A full-screen dialog
- **WHEN** the date range picker shows, which has no outside
- **THEN** its handling of the keyboard is unchanged, in portrait and in landscape

### Requirement: The dialog wait is touch only
The wait SHALL act only on pointer input in the dialog's window. Semantics SHALL NOT change, so TalkBack, the click action of
switch access and a keyboard SHALL work at once (BRD EC-UX-27, D160).

#### Scenario: TalkBack inside the wait
- **WHEN** an accessibility service performs the click action on a control of a dialog inside the wait
- **THEN** the action runs

#### Scenario: A keyboard inside the wait
- **WHEN** a keyboard activates a control of a dialog inside the wait
- **THEN** the control answers

#### Scenario: Nothing added to the tree
- **WHEN** a dialog goes through the helper
- **THEN** its semantics tree and its size and place are the same as without it

### Requirement: A lift in a dialog counts for what is beneath it
A touch that lifts on a dialog's card, once the wait has let it through, SHALL count as the first tap of a double tap for every
window already there. A press in another window, the main screen or an older dialog, SHALL be ignored as a whole gesture under the
500 ms constant and within Android's double-tap distance of that lift, on a screen touched or not. The distance SHALL come from the
platform and the times from the touch events. Every other press SHALL answer (BRD EC-UX-30, D161, D164).

#### Scenario: The OK of a picker over a Log button
- **WHEN** the user double taps OK in the "Taken at" picker on Log medication, with a gap under the limit, in landscape with 4
  medications or wherever a Log button lies under the finger
- **THEN** the time is picked and no intake is written

#### Scenario: The OK of a picker over "Add migraine"
- **WHEN** the user double taps OK in the "Change when it started" or "Change when it ended" picker on Add past, in landscape, with a
  gap under the limit, and "Add migraine" lies under the finger
- **THEN** the time is picked, no migraine is created, and Add past stays open

#### Scenario: An option over a location chip
- **WHEN** the user double taps "Not at all" on the effect question with a gap under the limit, at 100% or 200% font, and a
  location chip lies under the finger on the screen beneath
- **THEN** the effect is written and no location is added

#### Scenario: A theme over a setting
- **WHEN** the user double taps a choice on the Theme dialog with a gap under the limit, and the "Track periods" switch or
  another row lies under the finger
- **THEN** the theme changes, and the setting stays on and no screen opens

#### Scenario: Restore over its own row
- **WHEN** the user double taps Restore with a gap under the limit, and "Restore the copy" lies under the finger
- **THEN** the copy is restored once, and the dialog does not open again

#### Scenario: Cancel
- **WHEN** the user double taps Cancel with a gap under the limit
- **THEN** the dialog closes and the second press does nothing, because a lift on Cancel is a lift on the card like any other

#### Scenario: A screen just opened from a dialog
- **WHEN** a dialog's button opens a new screen and the second press lands on it under the limit and within the distance
- **THEN** that press does nothing

#### Scenario: A return from a dialog's button
- **WHEN** a dialog's button returns to a touched screen and the second press lands on it under the limit and within the
  distance
- **THEN** that press does nothing, which is the one case where a press on a touched screen is ignored, and Back and returns by a
  control of the main window stay as they are

#### Scenario: A whole gesture beneath
- **WHEN** a drag, a hold or a fling starts beneath under the limit and within the distance
- **THEN** it is dead until the finger lifts, with no time limit, and writes nothing

#### Scenario: A tap after the limit
- **WHEN** the second tap comes at or after the limit and lands on a chip, a switch or a Log button
- **THEN** it writes, including a false intake with its Undo, because that is two taps, which is the residual risk

#### Scenario: A deliberate tap just after the limit
- **WHEN** the user taps a control beneath, at the same point, just after the limit, with no press ignored in between
- **THEN** the control answers, on the first tap

#### Scenario: A tap farther away
- **WHEN** a press lands beneath under the limit but beyond the distance from the lift
- **THEN** the control answers

#### Scenario: Taps inside one dialog
- **WHEN** the user taps twice quickly inside one dialog, such as two days in a calendar, or a lift closes nothing
- **THEN** both taps count as before, because the rule asks only about another window

#### Scenario: A picker over a dialog
- **WHEN** the user double taps OK or Cancel on `MigrelyDateTimePicker` opened from "Add pain level", and the second press lands
  on the older dialog under the limit and within the distance, at 100% or 200% font or in landscape
- **THEN** that press does nothing, and the older dialog's control does not run

#### Scenario: Between the date step and the time step
- **WHEN** the date step closes and a press lands on the older dialog before the time step's window is up, under the limit and
  within the distance of the lift on Next
- **THEN** that press does nothing

#### Scenario: A press ignored by the wait does not count
- **WHEN** a press is ignored by the dialog's wait, lifts, and Back then closes the dialog, and the user presses a control beneath
- **THEN** this rule does not act, because no lift was let through, and the control answers once the dialog's window has closed

#### Scenario: A dialog closed during its wait
- **WHEN** a dialog opened by a tap is closed by Back, and the user then presses a control under it in the main window
- **THEN** this rule does not act, because no lift on the card was let through, and the control answers once the dialog's window
  has closed

#### Scenario: Back
- **WHEN** a dialog is closed by Back after its wait, and the user presses a control beneath
- **THEN** this rule does not act, because Back lifts no finger on the card, and the tap lands once the dialog's window has closed:
  on an emulator 0 of 5 at 0 ms after Back's command, 2 of 5 at 100 ms, and 15 of 15 from 200 ms, with and without the rule alike

#### Scenario: A tap outside the card
- **WHEN** a tap outside the card closes the dialog after its wait, and a second tap lands beneath under the limit
- **THEN** this rule does not act, because that tap never reaches the card, and what it does is measured and is not called safe

#### Scenario: Screen readers and keyboards
- **WHEN** an accessibility service's click action or a keyboard closes a dialog, and a touch presses a control beneath
- **THEN** this rule does not act, because no finger lifted on the card, and semantics are not touched

#### Scenario: A rotation or a restored process
- **WHEN** the activity is recreated after a lift on a dialog's card
- **THEN** the lift is not kept, and the next press answers

#### Scenario: A menu on a custom tag
- **WHEN** the user long presses a custom tag and double taps Remove in its menu, with a gap under the limit
- **THEN** the second press does nothing, because a lift on a popup menu's card counts the same as a lift on a dialog's card (D164)

### Requirement: What the lift rule asks of a later press
A dialog that appeared after the lift SHALL NOT be covered: its own wait alone decides, and an older dialog still asks. The lift of an
ignored press SHALL move the time and the place, and the next press SHALL be judged by the platform's double-tap timeout, not 500 ms,
so re-tapping at an ordinary pace answers. A third tap from that timeout up SHALL act, and that can write (BRD EC-UX-30, D161).

#### Scenario: A burst's next press under the burst limit
- **WHEN** a third tap follows an ignored second tap, under the platform's double-tap timeout, 300 ms today, after the second's lift
  and within the distance of it
- **THEN** it does nothing either, and the next press after that timeout answers
- **AND** a burst goes on only while every gap stays under that timeout, with no limit on how many taps

#### Scenario: A burst's next press over the burst limit
- **WHEN** a third tap comes at or after the platform's double-tap timeout after the second's lift, at the same place
- **THEN** it acts, even if under 500 ms from the first lift, because a tap that does nothing invites another
- **AND** that can write, which is accepted: a false location at about 350 ms, 4 of 4 on an emulator

#### Scenario: Re-tapping twice a second
- **WHEN** a person re-taps a control beneath at about two taps a second
- **THEN** the tap after the first ignored one answers

#### Scenario: Tapping along a row
- **WHEN** a first ignored press has come under 500 ms, and the user goes on tapping along a row of chips with each tap under the
  platform's double-tap timeout after the last and within the distance of it
- **THEN** every one does nothing, because each ignored lift moves the time and the place, until a pause of that timeout

#### Scenario: A press that reached the older dialog first
- **WHEN** a dialog stays up under a new one, a second tap reaches the older one before the new window is up, and a third lands on the
  new one under the platform's double-tap timeout, inside or just after its wait
- **THEN** it does nothing, because the lift moved after the new window appeared, so that window asks too

### Requirement: The dialog check can fail
The check SHALL include a negative control: with the guard removed, the tests on the guard SHALL fail, and QA's dialog rows SHALL
write again on debug and on R8. A check SHALL fail if a dialog is opened other than through the helper. A person SHALL try it by
hand on the internal track. Reports SHALL say what emulator runs with injected taps do not prove (BRD D160).

#### Scenario: Guard removed
- **WHEN** QA's dialog rows run on a build with only the guard removed, inside the wait
- **THEN** writes come back on every row that wrote in #157

#### Scenario: Guard in place
- **WHEN** QA's dialog rows run on the build under test
- **THEN** no row writes inside the wait, and a tap just after it answers

#### Scenario: A raw dialog
- **WHEN** a production file outside the helper names a dialog from Compose or Material, by import or fully qualified
- **THEN** the check fails and names the file

#### Scenario: By hand on the internal track
- **WHEN** 1.1.0 is on the internal track
- **THEN** a person double taps the rows on a real phone, double taps a dialog's option, and taps a dialog's control as soon as it
  can be read, and a dialog that feels dead or a write from a natural double tap goes back to `team-lead` before Production

### Requirement: A lift on a popup menu counts for what is beneath it
A touch that lifts on a popup menu's card SHALL count as the first tap of a double tap for every other window, under the same
rule, 500 ms constant, burst timeout and distance as a lift on a dialog's card. The lift SHALL be recorded before the item's
action runs. What lies beneath SHALL NOT change the rule, which asks only about the place on the screen and the time (BRD
EC-UX-31, D164).

#### Scenario: Remove over a chip, at the measured gaps
- **WHEN** the user long presses a custom tag and double taps Remove, with about 150, 300 or 450 ms from the first lift to the
  second press, and a chip such as "Sensitivity to smell" lies under the finger on the details screen
- **THEN** the tag is removed from the list, the chip stays as it was, and no message shows

#### Scenario: A single tap on Remove
- **WHEN** the user taps Remove once
- **THEN** the tag is removed from the list and nothing else changes

#### Scenario: A chip that slides under the finger
- **WHEN** removing the tag moves the next chip into the place under the finger, and the second press lands on it under the limit
- **THEN** that press does nothing, because the rule asks about the place, not the control

#### Scenario: A switch or a button beneath
- **WHEN** the second press under the limit lands on a switch, or on a button that writes or opens something
- **THEN** the switch does not change, nothing is written, and nothing opens

#### Scenario: Nothing beneath
- **WHEN** the second press under the limit lands on empty space, or a drag starts there
- **THEN** nothing happens, the drag is dead until the finger lifts, and the next drag scrolls

#### Scenario: The screen changes while the menu closes
- **WHEN** the screen beneath recomposes, moves or scrolls between the lift on Remove and the second press
- **THEN** the press is judged by the place on the screen and the event time of that lift, and does nothing under the limit

#### Scenario: Landscape and 200% font
- **WHEN** the phone is in landscape, at 200% font, or both, so the menu opens above or below the tag and Remove lies over another
  chip
- **THEN** the second press under the limit does nothing, as in portrait at 100%

#### Scenario: A burst under the burst timeout
- **WHEN** a third tap follows the ignored second one under the platform's double-tap timeout, 300 ms today, after the second's lift
  and within the distance of it
- **THEN** it does nothing either, and the next press after that timeout answers

#### Scenario: A burst over the burst timeout
- **WHEN** a third tap comes at or after the platform's double-tap timeout after the second's lift, at the same place
- **THEN** it acts, even under 500 ms from the first lift, and it can change a chip, which is accepted as in D161

#### Scenario: A deliberate tap after the limit
- **WHEN** the user taps the chip beneath, at the same point, at or after the limit, such as 600 ms or more after the lift on
  Remove, with no press ignored in between
- **THEN** the chip answers on the first tap

#### Scenario: A slower double tap still changes a chip
- **WHEN** the second tap comes at or after the limit and lands on a chip
- **THEN** the chip changes, which is the residual risk: the change shows, and one tap puts it back

#### Scenario: A tap farther away
- **WHEN** a press lands beneath under the limit but beyond the distance from the lift
- **THEN** the control answers

#### Scenario: A rotation or a restored process with the menu open
- **WHEN** the activity is recreated while the menu is open, or just after a lift on it
- **THEN** the menu is closed, nothing is written, the lift is not kept, and the next press answers

#### Scenario: Nothing to see
- **WHEN** a press beneath is ignored
- **THEN** no text, sound, haptic or layout change shows, and the menu looks and moves as before

### Requirement: A popup menu answers at once
A popup menu SHALL have no wait: an item SHALL answer the first tap however soon after the menu appears. The guard SHALL act
only on pointer input, so semantics stay, and TalkBack, the click action of switch access and a keyboard SHALL work at once and
SHALL NOT count as a lift. A tap outside and Back SHALL close the menu as before (BRD EC-UX-32, D164).

#### Scenario: Remove straight after the long press
- **WHEN** the user lifts the finger of the long press and taps Remove at once
- **THEN** the tag is removed, on the first tap

#### Scenario: TalkBack removes a tag
- **WHEN** TalkBack runs the chip's "Remove from my list" action, and then activates another chip at once
- **THEN** the tag is removed, and the other chip answers at once

#### Scenario: A click action or a keyboard on Remove
- **WHEN** an accessibility service's click action, switch access's click or a keyboard activates Remove, and a touch then presses a
  chip beneath
- **THEN** the tag is removed, and the chip answers, because no finger lifted on the menu

#### Scenario: Switch access point scan
- **WHEN** switch access's point scan taps Remove, which injects a real touch
- **THEN** that lift counts as any touch does, and a later point scan tap answers once the limit has passed

#### Scenario: A tap outside the menu
- **WHEN** the user taps outside the menu
- **THEN** the menu closes and the control under that tap does not change
- **AND** a second fast tap beneath is not covered, because the tap outside never reaches the card, and what it does is measured
  and is not called safe

#### Scenario: Back
- **WHEN** the user presses Back with the menu open, and then taps a control beneath
- **THEN** the menu closes, this rule does not act, and the tap lands once the menu's window has closed

#### Scenario: A second tap on the closing menu
- **WHEN** the second tap lands on Remove while the menu is still fading out
- **THEN** Remove may run again, the tag is removed once in effect, no message shows, and nothing else changes

#### Scenario: Nothing added to the tree
- **WHEN** a menu goes through the helper
- **THEN** its semantics tree and its size and place are the same as without it

#### Scenario: Remove animations
- **WHEN** "Remove animations" is on, so the menu closes at once
- **THEN** a press beneath under the limit still does nothing, because the rule counts from the lift and not from an animation

#### Scenario: A slow frame or a moved clock
- **WHEN** the phone stalls, or the date, the time zone or the clock changes, between the two taps
- **THEN** the rule is unchanged, because it reads the touch events' own times

### Requirement: Every popup goes through a helper
One helper in `designsystem` SHALL open the app's popup menus and apply the lift rule. A check in the gate SHALL fail, naming
the file, when a production file outside the helper names a raw popup: Material's `DropdownMenu`, `ExposedDropdownMenuBox` or
`TooltipBox`, Compose's `Popup`, or a View popup. `DropdownMenuItem` and `PopupProperties` SHALL stay free (BRD EC-UX-32, D164).

#### Scenario: The custom tag menu
- **WHEN** the gate runs on this change
- **THEN** the custom tag menu opens through the helper and the check passes

#### Scenario: A new raw popup
- **WHEN** a production file outside the helper names a raw popup, by import or fully qualified
- **THEN** the check fails and names the file

#### Scenario: A menu item or popup properties
- **WHEN** a production file names `DropdownMenuItem` or `PopupProperties`
- **THEN** the check passes, because neither is a window

### Requirement: The menu check can fail
The check SHALL include a negative control: with only the menu's lift removed, the tests on the helper SHALL fail and QA's menu
rows SHALL write again on debug and on R8. QA SHALL read each trial by its measured gap. Reports SHALL say what emulator runs
with injected taps do not prove (BRD D164).

#### Scenario: Lift removed
- **WHEN** QA's menu rows run on a build with only the menu's lift removed, at about 150, 300 and 450 ms
- **THEN** the chip beneath changes again on each gap, and if it does not, the run proves nothing and goes back to `team-lead`

#### Scenario: Lift in place
- **WHEN** QA's menu rows run on the build under test
- **THEN** no row changes a chip under the limit, and a tap from 600 ms answers on the first tap

#### Scenario: A raw popup added locally
- **WHEN** a raw `DropdownMenu` is added to a feature file, locally and never committed
- **THEN** the popup check fails and names that file

#### Scenario: What is not proved
- **WHEN** the report is written
- **THEN** it says that a human hand, a real phone and how often this happens are not proved

### Requirement: The diary survives an update after Clear storage
At every start of the app's process, whatever component starts it, the app SHALL make sure its `code_cache` folder exists and that installd's
`user.inode_code_cache` mark, when present, holds that folder's inode. A later app update SHALL then clear only `code_cache`, never `databases/`,
`no_backup/`, `files/` or another folder. This SHALL hold on every Android version, with no version branch, and SHALL show nothing to the user
(BRD EC-STO-06, EC-STO-08, EC-STO-09, D170, #193, #189).

#### Scenario: Clear storage, reopen, then an update keeps the diary (Android 8.0)
- **WHEN** on API 26 the user has a diary, clears storage, one other folder is created on the phone (QA's churn1 method), the user opens Migrely, and
  the app is updated to the fix build
- **THEN** `databases/migrely.db` is kept and the diary opens with its entries, 3 of 3 runs
- **AND** before the update the mark equals the inode of `code_cache`

#### Scenario: Clear storage, reopen, then an update keeps the reminders (Android 8.0)
- **WHEN** on API 26 the user turns the daily reminder on, clears storage, opens Migrely, turns the reminder on again, and the app is updated to the fix
  build
- **THEN** WorkManager's database in `no_backup/` and the planned reminder jobs are kept, and the daily reminder arrives at its time

#### Scenario: Negative control without the fix
- **WHEN** the same steps run with 1.2.0 R8 (`r8-9d453bb5`) as the build before the update
- **THEN** the update deletes `migrely.db` (churn1) or WorkManager's database (plain clear), as QA saw on 2026-11-10

#### Scenario: The first start after the clear comes from a receiver or a job
- **WHEN** after Clear storage the process is first started by a broadcast receiver (for example a clock change), a WorkManager job or a provider,
  not by an activity
- **THEN** the check runs in that start too, and the mark equals the inode of `code_cache` before any update

#### Scenario: The folder exists and the mark is right
- **WHEN** `code_cache` exists and the mark already holds its inode
- **THEN** nothing is created or written, and the folder's contents are untouched

#### Scenario: The folder is missing
- **WHEN** `code_cache` is missing at a process start, with or without a mark
- **THEN** the app re-creates it and the mark holds the new folder's inode

#### Scenario: No mark and the folder exists
- **WHEN** the data folder has no `user.inode_code_cache` mark and `code_cache` exists
- **THEN** the app writes no mark, because installd then finds `code_cache` by its name

#### Scenario: An update with no clear before it
- **WHEN** the app is updated and storage was never cleared since the last update
- **THEN** nothing changes: `migrely.db`, WorkManager's database, settings and automatic backups are kept, on API 26 and API 36

#### Scenario: Android 16 control
- **WHEN** QA runs the same arms on API 36 (Android 16), with and without a clear, before and after the fix
- **THEN** the diary and WorkManager's database are kept in every run, and after a reopen on the fix build the mark equals the inode of `code_cache`

#### Scenario: A work profile or a second user
- **WHEN** Migrely is installed for the owner and in a work profile or for a second user, and storage is cleared in one of them
- **THEN** a process start in that user repairs only that user's data folder, the other user's data is not touched, and a user that never reopened
  the app after its clear has no new folder at risk

#### Scenario: Delete all data in the app
- **WHEN** the user deletes all data from Settings and the app is later updated
- **THEN** nothing is deleted by the update, because "Delete all data" empties tables and removes no folder (BRD EC-STO-03)

#### Scenario: The documented residual, a clear made before 1.2.1
- **WHEN** on API 26 storage was cleared and the app reopened while 1.1.1 was installed, and the app is then updated to the fix build
- **THEN** the update may still delete `migrely.db` or WorkManager's database, because installd deletes before any new code runs; QA records this as a
  known residual, not a failure (BRD EC-STO-10, D170 (2))
- **AND** after that update the mark is right, so later updates delete nothing

### Requirement: The code-cache check is safe
The `code_cache` check SHALL never crash the app or stop its start, whether a folder cannot be created, the mark cannot be read or written, or the
platform throws. It SHALL run without network, account or new permission, SHALL not wait on disk beyond a few file-system calls, and SHALL log at most
the kind of failure, never diary content (BRD EC-STO-07, D170).

#### Scenario: Writing the mark is denied
- **WHEN** writing the mark fails, for example by SELinux or a file system without user xattrs
- **THEN** the app starts normally, the reminders are re-planned, and at most a warning with the failure's kind is logged

#### Scenario: The folder cannot be created
- **WHEN** `code_cache` cannot be created, for example while credential-encrypted storage is locked or storage is full
- **THEN** the app starts normally, and the next process start tries again

#### Scenario: The platform throws
- **WHEN** any call inside the check throws
- **THEN** the exception is caught inside the check, and the rest of `onCreate` runs

#### Scenario: Nothing private in the log
- **WHEN** the check logs a failure
- **THEN** the line holds no diary content, no medication or attack data, and no setting value

### Requirement: The window does not pan for the keyboard
`MainActivity` SHALL declare `adjustResize`. With the on-screen keyboard open on any screen, the window SHALL NOT pan: the top
bar and Back SHALL stay where they are, never scroll away, and answer a first tap, at every font size and orientation. The
one exception is a top bar that steps aside while the line being typed cannot fit (requirement "The line being typed always
shows"). Nothing SHALL draw under the status bar (BRD EC-UX-40, D180, D192).

#### Scenario: The note on Attack details
- **WHEN** the user opens a migraine's details, scrolls to Note and taps it, at 100% or 200% font, in portrait or landscape
- **THEN** the keyboard opens, the top bar with Back keeps its place and size, and a first tap on Back leaves the screen
- **AND** no trigger chip or other content draws under the status bar

#### Scenario: The tightest window
- **WHEN** the keyboard is open on the note at 200% font in landscape on a window 360 dp tall
- **THEN** where the line being typed fits under the top bar (API 26, 156 px), the top bar with Back stays whole and fixed;
  where it does not (API 36 with Gboard, 61 px for a 112 px line), the top bar steps aside while the keyboard is up and the
  window still does not pan; nothing draws under the status bar in either case

#### Scenario: TalkBack with the keyboard open
- **WHEN** TalkBack is on and the keyboard is open on the note
- **THEN** swiping back from the note reaches the content above it, then the message if one shows, then Back, and activating
  Back leaves the screen
- **AND** where the top bar has stepped aside, Back is not in the tree until the keyboard closes (requirement "An aside top
  bar is gone for assistive technology and messages"); the system Back gesture still closes the keyboard first

#### Scenario: The merged manifest
- **WHEN** the debug and the release build are assembled
- **THEN** `MainActivity`'s merged manifest entry declares `adjustResize`, and the permission report equals 1.2.1's

#### Scenario: The checks can fail
- **WHEN** the note row runs on develop ea41869a on API 36, which has no `adjustResize` and pans
- **THEN** Back's bounds are `[0,0][0,0]` with the keyboard open, as in #83, which shows the row can fail
- **AND** a test of `MainActivity`'s window fails without `adjustResize`, and on Android 8 the copy rows that pass on this
  change stay stale on develop ea41869a

### Requirement: A focused field stays above the keyboard
On Attack details, Edit medication and Log medication the scroll's visible part SHALL end at the keyboard's top. A focused
single-line field SHALL show its box and its supporting line when they fit; if not, its input line first, then the supporting
line, then the label. A field SHALL follow a growing keyboard only while its line was in view before. Controls below SHALL be
reachable by scrolling (BRD EC-UX-40, D180).

#### Scenario: Edit medication at 200% in landscape
- **WHEN** the user taps the name or the dose field on Edit medication, at 200% font in landscape
- **THEN** the field's input line shows at least 8 dp above the keyboard, its supporting line too when it fits, and scrolling
  down reaches Save while the keyboard stays open

#### Scenario: A medication error while typing
- **WHEN** a name or dose error appears in the field's supporting line while the keyboard is open
- **THEN** the input line keeps priority, the error line shows if it fits, and it shows again when the keyboard closes

#### Scenario: A field 3 dp under the scroll's edge
- **WHEN** the user taps the dose field on Edit medication while a few dp of it lie under the scroll's lower edge, at 100% font
  in portrait
- **THEN** the whole field with its supporting line shows above the keyboard

#### Scenario: Log medication's name field
- **WHEN** the user types a name on Log medication, at 100% or 200% font, in portrait or landscape, on API 36 or API 26
- **THEN** the input line shows above the keyboard, and the buttons below it can be scrolled to

#### Scenario: Without the change on Edit medication
- **WHEN** the Edit medication row runs on a build with `adjustResize` and with the keyboard padding still inside the scroll
- **THEN** the field or Save can end under the keyboard, which shows the check can fail

### Requirement: Delete all data with the keyboard open
On Delete all data the Delete row SHALL ride above the keyboard only while the input line, with 8 dp above and below it, still
shows above the row.
Otherwise the row SHALL stay at the window's bottom, behind the keyboard, and Done or Back shows the field and the row together.
This SHALL be decided once per keyboard opening, from the keyboard's final height (BRD EC-UX-40, D180).

#### Scenario: A window with room
- **WHEN** the user types in the confirmation field at 100% or 200% font in portrait, or at 200% in landscape on a 411 dp tall
  window where the input line with 8 dp above and below it fits above the Delete row
- **THEN** the input line and the Delete row both show above the keyboard

#### Scenario: A short window
- **WHEN** the user types in the confirmation field at 200% font in landscape on a window 360 dp tall, where the input line
  would not show above the Delete row
- **THEN** the input line shows above the keyboard, the Delete row stays behind it, and after Done the field and "Delete
  everything" show together

#### Scenario: No jump while the keyboard moves
- **WHEN** the keyboard animates open or closed on Delete all data
- **THEN** the Delete row takes its place once, from the keyboard's final height, and does not jump midway

#### Scenario: API 26
- **WHEN** the user types in the confirmation field on Android 8, with no text toolbar shown
- **THEN** the input line shows above the keyboard, and the Delete row follows the same rule, never left under the keyboard
  with the field hidden

#### Scenario: TalkBack order
- **WHEN** TalkBack is on at 100% font in portrait on Delete all data
- **THEN** it reads the warning, then the field, then "Delete everything"

### Requirement: A screen without a text field ignores the keyboard
Home, History, Insights, Settings and its pages, the paywall, Doctor report, My medications, Your data, Reminders and
onboarding SHALL pad only for the system bars and the display cutout, never for the keyboard. One shared inset SHALL serve them
and the message's fit rule (BRD EC-UX-42, D180).

#### Scenario: A keyboard inset reaches Home
- **WHEN** a test gives Home Today and History a keyboard inset of 250 dp
- **THEN** their layout is the same as with no keyboard: Home shows every day cell, and History's list ends at the same place

#### Scenario: Back from the note with the keyboard open
- **WHEN** the keyboard is open on the note and the user presses Back until Home or History shows, at 200% font, in portrait
  or landscape
- **THEN** Home shows its whole month and History its whole list, with no blank lower part

#### Scenario: Android 8 after a copy
- **WHEN** on Android 8 the user long-presses a word in the note, in an Edit medication field or in the Delete all data field,
  taps COPY in the toolbar and presses Back until Home shows, then opens History
- **THEN** Home shows its day cells and History its whole list, with no blank lower half, and no restart is needed (#145)

#### Scenario: Android 8 after a copy, back on a field screen
- **WHEN** on Android 8 the user copies from a field's toolbar, closes the keyboard and stays on that screen or opens another
  screen with a field
- **THEN** the screen keeps no space for a keyboard that is closed

#### Scenario: Without the change on Android 8
- **WHEN** the copy rows run on the build of develop ea41869a
- **THEN** Home loses the keyboard's height and History ends early, as QA measured for #145, which shows the check can fail

#### Scenario: Short window on Home
- **WHEN** the keyboard state changes while Home Today is in a short window
- **THEN** Home keeps the same layout choice, because the choice reads the window's height and not the keyboard

### Requirement: Closing the keyboard and other keyboards
When the keyboard closes, the page SHALL NOT move or scroll to the field, and focus SHALL stay in the field. Near the page's
end the area the keyboard leaves SHALL show the page's own background until the person scrolls back (the keyboard room).
With a hardware or floating keyboard, split screen or a full-screen editor, Migrely SHALL follow Android's inset and add
nothing. "Remove animations" SHALL make every scroll a jump. Nothing SHALL be announced (BRD EC-UX-43, D180, D189).

#### Scenario: The keyboard closes
- **WHEN** the keyboard is open on a field and the user closes it with Back, the keyboard's hide key or Done
- **THEN** the page does not scroll back and does not settle down: its scroll position is the same, and near the page's end
  the keyboard room shows below the content instead
- **AND** the first Back closes only the keyboard, and the second leaves the screen

#### Scenario: Closing after scrolling away
- **WHEN** the keyboard is open on the note, or on Edit medication's name, and the user scrolls the page away from the field,
  then closes the keyboard, with or without "Remove animations"
- **THEN** the page stays where the user left it and does not scroll back to the field

#### Scenario: The keyboard grows after scrolling away
- **WHEN** the user has scrolled the field's line out of view and the keyboard then grows, as when it reopens or changes height
- **THEN** the page does not scroll back to the field, and the next tap, edit or cursor move in the field brings its line into
  view again

#### Scenario: The strip the keyboard uncovers
- **WHEN** the keyboard slides away in the dark theme, in the light theme, or during an attack, on API 36 or API 26
- **THEN** the uncovered strip, the keyboard room included, has the page's own colour at every frame, never a lighter, dark or
  white band

#### Scenario: Rotation with the keyboard open
- **WHEN** the phone rotates portrait to landscape to portrait while the keyboard is open on the note, at 200% font
- **THEN** the note keeps its text and cursor, and if the keyboard opens again its cursor line shows 8 dp above it

#### Scenario: Background and process death
- **WHEN** the app goes to the background with the keyboard open, and its activity is recreated or its process is restored
  later
- **THEN** on return no keyboard room is left at the bottom, and the note keeps what was typed

#### Scenario: A hardware keyboard
- **WHEN** a hardware keyboard is used and no on-screen keyboard shows
- **THEN** no space is kept for a keyboard, Tab gives one stop into the note with the cursor at the end, and the cursor line
  stays inside the window above the navigation bar as the user types or moves with arrow keys

#### Scenario: A floating keyboard or split screen
- **WHEN** the keyboard floats, or the app runs in split screen, and Android gives no bottom keyboard inset
- **THEN** Migrely adds no space of its own, no keyboard room ever appears, and the screen works as without a keyboard

#### Scenario: The keyboard's full-screen editor
- **WHEN** a keyboard opens its own full-screen editor in landscape
- **THEN** Migrely adds nothing, and on return the note shows the text typed there; if QA cannot reach this editor, the row is
  recorded as not reachable, not as a pass

#### Scenario: Remove animations
- **WHEN** "Remove animations" is on and the keyboard opens on a field, or the cursor moves under it
- **THEN** the content moves to show the line with no frames in between

#### Scenario: Right to left
- **WHEN** the layout is right to left and the keyboard is open on a field
- **THEN** the same rules hold, mirrored, and nothing draws under the status bar

#### Scenario: TalkBack while the keyboard opens and closes
- **WHEN** TalkBack is on and the keyboard opens or closes on the note, or the page scrolls for the cursor
- **THEN** nothing extra is announced, accessibility focus stays on the note, and the reading order is D178's: top bar,
  message, content

### Requirement: One spoken name per control
A control SHALL have one spoken name. When its spoken label differs from its visible text, the visible text SHALL be left
out of its semantics, and the label SHALL contain the visible words in the same order. Role, action, focus, size and look
SHALL not change. Each site SHALL be proven with a failing test before it is fixed, and a site that does not read twice
SHALL be left alone (BRD EC-UX-45, D181).

#### Scenario: A message's Undo
- **WHEN** TalkBack focuses the Undo of "Migraine started" on any screen
- **THEN** it says "Undo the start, Button", once, and the message's text stays readable as before

#### Scenario: Attack mode
- **WHEN** TalkBack focuses "Details" or the "15 min" chip under "Started earlier?" in attack mode
- **THEN** it says "Details of this migraine, Button" or "Move the start 15 min earlier, Button", once each

#### Scenario: Reminders
- **WHEN** TalkBack focuses the visit's "Remove" or the notice's "Open settings" on Reminders
- **THEN** it says "Remove the next visit, Button" or "Open settings for notifications, Button", once each

#### Scenario: The pain dialog's time
- **WHEN** TalkBack focuses the time button "At 14:30" in the pain dialog
- **THEN** it says "At 14:30, change the time, Button", once, and the button still opens the time picker

#### Scenario: Label in name
- **WHEN** any control's spoken label differs from its visible text
- **THEN** the spoken label contains the visible words in the same order, so a voice command naming the visible text can
  match it

#### Scenario: The fix changes nothing else
- **WHEN** a fixed control is tapped, focused with Tab, or reached by Switch Access
- **THEN** it acts, looks and measures as before, and its touch target is still at least 48 dp, or 64 dp in attack mode

### Requirement: Every keyboard stop is visible and in view
Every Tab stop SHALL show a visible focus indicator and SHALL be scrolled fully into view, at every font size and in
both orientations. A node that cannot show one, such as a group, a scroll container, the legend or a spacer, SHALL never
be a stop. Shift+Tab SHALL be the exact reverse of Tab (BRD EC-UX-46, EC-UX-19, D181).

#### Scenario: No invisible stop
- **WHEN** the user moves through any screen with Tab and Shift+Tab on a hardware keyboard, at 100% and 200% font
- **THEN** each stop shows its focus indicator fully on screen, and no key press lands on a stop that shows nothing

#### Scenario: Reverse order
- **WHEN** the user presses Tab from one control and then Shift+Tab
- **THEN** focus is back on the control it started from

### Requirement: No label breaks inside a word
No label SHALL break inside a word, with or without a hyphen, at any font size or width. Where a label's longest word
does not fit, the layout SHALL change, for example by stacking, and the type SHALL NOT shrink: 200% stays 200%. Wrapping
at a space is allowed (BRD EC-UX-48, D181).

#### Scenario: Largest font size on a narrow phone
- **WHEN** the font size is 200% on a 360 dp phone
- **THEN** every label shows whole words, and none is cut, ellipsized or hyphenated

### Requirement: Closing the keyboard never moves the page
On Attack details (note), Edit medication (name, dose), Log medication (name) and Delete all data (the word), when the keyboard
closes or shrinks, the scroll position SHALL stay the same to the pixel on any API level and animation setting. If less
than the visible part's growth lies below, room of the difference SHALL appear after the last item; otherwise none. An aside
top bar SHALL return and lower the page by its height, with no scroll (BRD EC-UX-61, EC-UX-65, EC-UX-75, D189 (1), (7), D192).

#### Scenario: Near the end on each screen
- **WHEN** a field is focused near the page's end (less than a keyboard's height of content below the visible part) on any of
  the four screens, at 100% font in portrait on API 36, and the user presses Back once
- **THEN** the field's bounds are the same before and after the keyboard is gone (within 1 px), and the keyboard room fills
  the area the keyboard left

#### Scenario: API 26
- **WHEN** the same steps run on Android 8, where the keyboard inset changes in one step
- **THEN** the field's bounds are the same before and after, and the page does not jump down

#### Scenario: Far from the end
- **WHEN** the note is focused mid-page with more than a keyboard's height of content below the visible part, and the user
  presses Back
- **THEN** no keyboard room appears, nothing on screen moves, and hidden content is uncovered in place

#### Scenario: 200% font in portrait and landscape
- **WHEN** the same steps run at 200% font in portrait and in landscape on windows 411 dp and 360 dp tall
- **THEN** the field's bounds are the same, or, where the top bar had stepped aside (API 36 with Gboard at 360 dp), lower by
  exactly the bar's height; the scroll position is the same, the room is never taller than the keyboard was, and the field,
  its input line and its supporting line stay fully visible

#### Scenario: Remove animations
- **WHEN** "Remove animations" is on and the keyboard closes near the page's end on Attack details or Delete all data
- **THEN** the keyboard goes in one frame and the field's bounds are the same; no frame shows the page lower

#### Scenario: Free, Premium and first use
- **WHEN** the screen is opened by a Free or a Premium user, or Log medication shows its first-use form, and the keyboard has
  not closed yet on this visit
- **THEN** there is no keyboard room, and after a close the same rules apply to both

#### Scenario: Without the room
- **WHEN** the near-the-end steps run on v1.3.0, or on the candidate with the room removed from the screen under test
- **THEN** the field moves down by more than 0 px, which shows the check can fail

### Requirement: The keyboard room looks like the page
The keyboard room SHALL be the screen's own background only: the theme background, or the dark room background when the
screen shows in attack mode. It SHALL have no divider, shadow, gradient, end marker or other colour, at any frame of the
keyboard's animation, and SHALL sit below the bottom padding the page already has (BRD EC-UX-61, EC-UX-65, D189 (2)).

#### Scenario: Dark and light theme
- **WHEN** the keyboard closes near the page's end on Attack details and on Log medication, in the dark theme and in the light
  theme
- **THEN** every pixel of the room equals the page background sampled at the top of the same screenshot, and none is brighter
  than it; in the light theme the room is the light page background, not white and not a surface tint

#### Scenario: Log medication from attack mode
- **WHEN** Log medication is opened from attack mode, the first-use Name field is focused near the page's end, and the keyboard
  closes
- **THEN** every pixel of the room equals `MigrelyColors.AttackModeBackground`, and the screen only gets darker as the light
  keyboard goes

#### Scenario: The colour check can fail
- **WHEN** the same pixel check runs on the keyboard area of the keyboard-open screenshot
- **THEN** it fails, which shows the check can tell the keyboard from the page

### Requirement: The keyboard room goes only with the person's scroll
The keyboard room SHALL shrink only as the person scrolls up (finger, fling, TalkBack's scroll action, Tab or Shift+Tab, Page
Up, mouse wheel), in step with that scroll, or as the keyboard reopens or grows, and SHALL never grow back from scrolling or be
animated away. Content that grows SHALL use the room up. The page SHALL NOT move because of the room (BRD EC-UX-62, D189 (3)).

#### Scenario: Scrolling back up
- **WHEN** the room shows and the user swipes up slowly by about a third of it, then by more than it, then scrolls to the end
- **THEN** the field moves with the finger and the room shrinks by the same amount; after the second swipe there is no room,
  and at the end the page ends with its normal bottom padding

#### Scenario: TalkBack, Tab and Page Up scroll the room away
- **WHEN** the room shows and the page is scrolled up by TalkBack's scroll action, by Tab or Shift+Tab bringing a control
  into view, by Page Up or by a mouse wheel
- **THEN** the room shrinks by the scrolled amount, as with a finger

#### Scenario: The keyboard reopens
- **WHEN** the room shows and the user taps the field again
- **THEN** the keyboard opens, the room shrinks as the visible part shrinks, the page does not jump, and the cursor line is at
  least 8 dp above the keyboard (EC-UX-39), with single-line fields as U1 says

#### Scenario: The keyboard changes height while open
- **WHEN** the keyboard gets shorter (a suggestion strip goes, another layout or keyboard) or taller while open on a field
- **THEN** a shorter keyboard adds room as a close does, and a taller one uses room first and then follows D180 (2)

#### Scenario: Content changes while the room shows
- **WHEN** the room shows and a chip is added, an error line appears, or Log medication's form is replaced after "Add and log"
- **THEN** the grown content uses the room up and the page does not move because of the room

#### Scenario: A message while the room shows
- **WHEN** the room shows on Attack details and a message appears, or the keyboard closes while a message shows
- **THEN** the message's position equals its position with no room, and it does not cover Back (D180 (4))

#### Scenario: A room that never shrinks
- **WHEN** the scrolling rows run on a mutation build whose room never shrinks
- **THEN** the room shows again at the page's end, which shows the check can fail

### Requirement: The keyboard room is not kept
The keyboard room SHALL NOT be saved. After rotation, process death, or leaving the screen and coming back, the screen SHALL
show no room, at its saved scroll position or the nearest one the page allows. Background and return without recreation SHALL
keep the room as it was. With no bottom keyboard inset, no room SHALL ever appear (BRD EC-UX-63, D189 (4), (5)).

#### Scenario: Rotation and process death with the room
- **WHEN** the room shows on Attack details or Edit medication and the phone rotates and back, or the app goes to the
  background, its process is killed and it returns from recents
- **THEN** there is no room after each, the note or form keeps its text, and nothing is cut off

#### Scenario: Leaving and coming back
- **WHEN** the room shows and the user leaves the screen with Back, or opens a forward screen and returns
- **THEN** the screen shows no room, and TalkBack starts where Android and D178 put it on any screen entry

#### Scenario: Background without recreation
- **WHEN** the room shows and the app goes to the background and returns with its activity kept
- **THEN** the room is as it was; if the keyboard reopens because the field still has focus, the reopen rule applies

#### Scenario: No keyboard inset
- **WHEN** a hardware keyboard is used on Attack details, or a floating keyboard or split screen gives no bottom keyboard inset
- **THEN** no room appears and nothing changes on close; a case QA cannot reach is recorded as not reachable, not a pass

### Requirement: The keyboard room is invisible to assistive technology
The keyboard room SHALL have no semantics, SHALL NOT be focusable or a Tab stop, and a tap in it SHALL do nothing. Closing the
keyboard SHALL send no scroll event from the page, SHALL keep input and TalkBack focus on the field, and SHALL announce nothing
new: no string, live region or state change (BRD EC-UX-61, D189 (6)).

#### Scenario: TalkBack stays on the field
- **WHEN** on each of the four screens, on API 36 with TalkBack on the field near the page's end at 100% font in portrait, the
  user presses Back once
- **THEN** within 3 s TalkBack says only the field itself or "keyboard hidden", and one more double tap reopens the keyboard
  in that field with no chip, radio or kind changed in the record

#### Scenario: Other ways to close
- **WHEN** on Attack details and on one single-line screen, with TalkBack on, the keyboard is closed with its hide key, then
  with Done where the field has it
- **THEN** TalkBack stays on the field as with Back

#### Scenario: No scroll event
- **WHEN** an accessibility event log runs across Back from the near-the-end set-up on each screen
- **THEN** it shows no TYPE_VIEW_SCROLLED from the page's scroll container within 3 s

#### Scenario: Nothing to tap in the room
- **WHEN** after the close a layout dump is read at the place where the keyboard's hide key was on Attack details, and that
  place is tapped
- **THEN** there is no clickable or focusable node there, and the tap changes nothing: no focus cleared, no keyboard, no ripple

#### Scenario: TalkBack walk past the room
- **WHEN** at 200% font in landscape on Attack details, after the close, the user swipes forward from the note and then back
  several items
- **THEN** forward reaches "Delete migraine" and then the end of the page, never the room, and back walks in D178 order with
  no jump

#### Scenario: The focus check can fail
- **WHEN** the TalkBack and scroll-event steps run on v1.3.0, or on the mutation build with the room removed
- **THEN** on Attack details TalkBack's focus moves to "Not selected. Stress" and the log shows a scroll event; on a screen
  where v1.3.0 shows no jump, the row proves only that no jump was observed

### Requirement: Delete all data keeps the field still
On Delete all data, closing the keyboard SHALL NOT move the confirmation field. Only the Delete row SHALL move with the keyboard
as D180 (8) defines, and after Done or Back the field and "Delete everything" SHALL show together. With the page's end in view
the divider above the row SHALL stay hidden (BRD EC-UX-64, D180 (8), D189 (8)).

#### Scenario: The row rides above the keyboard
- **WHEN** at 100% font in portrait the word is typed, with the Delete row above the keyboard, and the user taps Done
- **THEN** the field's bounds are the same, the field and "Delete everything" show together, and no divider shows above the row

#### Scenario: The row stays behind the keyboard
- **WHEN** at 200% font in landscape on a window 360 dp tall the word is typed, with the Delete row behind the keyboard, and the
  user taps Done
- **THEN** the field's bounds are the same and the field and "Delete everything" show together

#### Scenario: Delete all data without the room
- **WHEN** the same steps run on the candidate with the room removed from Delete all data, and on v1.3.0
- **THEN** the field moves down on the mutation build, which shows the check can fail; v1.3.0's result is recorded as it is,
  and a v1.3.0 field that does not move proves nothing either way

### Requirement: The line being typed always shows
On Attack details (note), Edit medication (name, dose), Log medication (name) and Delete all data (the word), while the
keyboard is up, the line being typed SHALL be fully visible. If the visible part H under the top bar is at least the line
height L plus 8 dp above and below, nothing changes. If H < L, the top bar SHALL step aside while the keyboard is up, and the
line SHALL show with 8 dp on both sides where that fits (BRD EC-UX-72, EC-UX-77, D192).

#### Scenario: Attack details in the 360 dp window (the issue)
- **WHEN** on API 36 with Gboard, at 200% font in landscape in a window 360 dp tall, the user types 9 lines in the note, with
  the cursor at the end, then at the start, then in the middle
- **THEN** the cursor line shows whole, 112 of 112 px, with at least 8 dp on both sides, Back and the title are absent from
  the layout dump, and the status bar is unchanged

#### Scenario: Single-line fields in the 360 dp window
- **WHEN** the same window is used for Edit medication's name and dose, Log medication's name and Delete all data's word
- **THEN** the input line shows whole, the supporting line too where it fits (U1 order), and the top bar steps aside only if
  the input line did not fit under it

#### Scenario: Delete all data decides the row first
- **WHEN** the keyboard opens on Delete all data in the 360 dp window
- **THEN** the Delete row is decided first as D180 (8) says, and the top bar steps aside only if the input line still does not
  fit

#### Scenario: Log medication from attack mode
- **WHEN** Log medication was opened from attack mode in the 360 dp window and the user types a first medication's name
- **THEN** the same happens, and the status bar and the area the bar leaves are the dark room background with no lighter
  frame

#### Scenario: The 411 dp window keeps the bar
- **WHEN** on API 36 at 200% landscape in a window 411 dp tall, the user types in the note (H 137 px, L 112 px)
- **THEN** the top bar stays at its usual bounds and the line shows whole, with the margins as today (4 and 21 px by cursor
  direction), not evened out

#### Scenario: API 26 keeps the bar
- **WHEN** on API 26 with the AOSP keyboard at 200% landscape, in windows 411 dp and 360 dp tall, the user types in the note
  and the single-line fields
- **THEN** the top bar stays, and the line shows whole with at least 8 dp on both sides, as on v1.3.1

#### Scenario: Portrait keeps the bar
- **WHEN** the user types on any of the four screens at 100% or 200% font in portrait
- **THEN** the top bar never steps aside, and EC-UX-39 and EC-UX-40 hold as before

#### Scenario: A window too small even without the bar
- **WHEN** the line would not fit even with the top bar aside
- **THEN** the app does nothing beyond stepping the bar aside, and the result is recorded as "window too small", never as a
  pass

#### Scenario: Free and Premium, light and dark
- **WHEN** the same steps run on Free and on Premium, in the light and the dark theme
- **THEN** the result is the same

### Requirement: Margins give way before the line is cut
When the line height L fits the visible part H but L plus 8 dp above and below does not, the line SHALL stay whole and the
margins SHALL take what is left, down to 0 dp. The page SHALL move only as far as the line needs to stay whole, never to even
out the margins, and the top bar SHALL stay. This requirement ships only if a test shows the line cut on v1.3.1 in that band
(BRD EC-UX-73, D192).

#### Scenario: A window in the band
- **WHEN** on API 36 with Gboard at 200% landscape, in a window where 112 ≤ H < 133 px (about 401 dp for Attack details,
  about 384 dp for Edit medication), the user types with the cursor at the end, then at the start
- **THEN** the line shows whole, 112 of 112 px, the top bar stays, and the margins are what is left

#### Scenario: v1.3.1 in the band
- **WHEN** the same steps run on v1.3.1 in the same window
- **THEN** the line is cut by up to 21 px at the keyboard edge (end) or the bar edge (start); if it is whole there, this
  requirement is dropped from the release

#### Scenario: No evening out
- **WHEN** a build evens out the two margins in the 411 dp window
- **THEN** the line moves by about 8 px while typing, and the check for "margins as today" fails

### Requirement: The top bar's decision holds while the keyboard is up
The top bar SHALL step aside when H < L, with H from the keyboard's final height and L the real height of the line being typed
from layout, never a dp or sp constant. It SHALL be decided once per keyboard opening and SHALL NOT change while the keyboard
stays up. A new opening, a rotation or a font change SHALL decide again. With no bottom keyboard inset it SHALL never step
aside (BRD EC-UX-74, EC-UX-77, D192).

#### Scenario: Moving and typing while the bar is aside
- **WHEN** the bar is aside and the user moves the cursor up with arrow keys or the handle, types 10 more lines, or scrolls the
  page by hand
- **THEN** the bar does not come back or flicker, and the cursor line keeps at least 8 dp from the status bar when it moves up

#### Scenario: The keyboard changes height
- **WHEN** the bar is aside and the user switches Gboard to symbols or to emoji
- **THEN** the bar stays aside

#### Scenario: A message while the bar is aside
- **WHEN** the bar is aside and a message appears
- **THEN** the bar stays aside

#### Scenario: Rotation with the keyboard up
- **WHEN** the bar is aside and the phone rotates to portrait and back to landscape with the keyboard up
- **THEN** in portrait the bar shows, and back in landscape the decision is made again

#### Scenario: A trigger that is a constant
- **WHEN** a build sets the trigger to a constant 64 dp and runs on API 26 at 360 dp (H 156 px)
- **THEN** the bar leaves there, which v1.3.1 and the candidate never do, and the guard row fails

#### Scenario: Hardware or floating keyboard
- **WHEN** a hardware keyboard, a floating keyboard or a split screen gives no bottom keyboard inset
- **THEN** the bar never steps aside; a case QA cannot reach is recorded as not reachable, not a pass

### Requirement: The top bar moves with the keyboard
The top bar SHALL leave and come back following the keyboard's own inset animation, in the same frames, with no curve of the
app's own, in one step on API 26 to 29 and with "Remove animations". It SHALL NOT fade through another colour, SHALL NOT be
drawn half over the page's text, and the area it leaves SHALL be the page's own background (BRD EC-UX-76, D192).

#### Scenario: Animations on
- **WHEN** a 60 fps recording on API 36 runs across the keyboard opening and closing in the 360 dp window
- **THEN** the bar moves in the same frames as the keyboard and no frame shows a lighter colour

#### Scenario: Remove animations and API 26
- **WHEN** "Remove animations" is on, or the device is on API 26 to 29
- **THEN** the bar goes and comes back in one step, with no frame showing it half drawn over text

#### Scenario: Right to left
- **WHEN** the steps run in a right-to-left language
- **THEN** Back is mirrored when the bar returns

### Requirement: Closing the keyboard brings the top bar back without a scroll
When the keyboard closes while the bar is aside (Back, the hide key, Done, focus leaving, a dialog), the bar SHALL come back
in the same frames. The scroll position SHALL stay the same: the page moves down by the bar's height and the keyboard room
fills the rest. No scroll event SHALL be sent, TalkBack's focus SHALL stay on the field, and nothing SHALL be announced (BRD
EC-UX-75, D189, D192).

#### Scenario: Back closes the keyboard
- **WHEN** the bar is aside in the 360 dp window and the user presses Back once
- **THEN** the keyboard closes, the bar is back at the bounds it had before typing, the line being typed is still on screen,
  lower by the bar's height, and a second Back leaves the screen

#### Scenario: Hide key and Done
- **WHEN** the keyboard is closed with its hide key, or with Done on a single-line field
- **THEN** the same happens as with Back

#### Scenario: TalkBack stays on the field
- **WHEN** TalkBack is on (VERBOSE log) and the keyboard closes while the bar is aside
- **THEN** within 3 s TalkBack says only the field or "keyboard hidden", stays on the field, and zero TYPE_VIEW_SCROLLED come
  from the page

#### Scenario: After a drag to the page's end
- **WHEN** the user dragged the page to its end while typing with the bar aside, and then closes the keyboard
- **THEN** the scroll position is the same, the room fills the rest, and no scroll event is sent

#### Scenario: A close that scrolls
- **WHEN** a build keeps the line's screen position on close by scrolling
- **THEN** a scroll event is sent, and the TalkBack row fails

#### Scenario: The keyboard-room guards
- **WHEN** the keyboard-room tests of #238 run on the candidate
- **THEN** they all pass unchanged, except that a case with the bar aside expects the field lower by exactly the bar's height

### Requirement: An aside top bar is gone for assistive technology and messages
While the bar is aside, its nodes SHALL be removed from the accessibility tree, not merely hidden, so TalkBack, Switch Access
and Tab never land on it, and nothing SHALL be announced when it leaves or returns. A message SHALL keep its D180 (4)
coordinates to the pixel: the bar never moves it, and a message never brings the bar back (BRD EC-UX-76, D192).

#### Scenario: TalkBack swipe while the bar is aside
- **WHEN** the bar is aside and the user swipes back from the field with TalkBack
- **THEN** TalkBack reaches the content above the field and then the top of the page, never Back or the title

#### Scenario: After closing
- **WHEN** the keyboard is closed and the user swipes back again
- **THEN** Back is reachable in D178 order

#### Scenario: Tab and Switch Access
- **WHEN** the bar is aside and the user presses Shift+Tab from the field, or scans with Switch Access
- **THEN** no stop lands on Back, the title or a bar action

#### Scenario: A message with the bar aside
- **WHEN** a message shows and the keyboard opens and closes in the 360 dp window
- **THEN** the message's bounds are identical with and without the keyboard and the bar

#### Scenario: A message moved by the bar
- **WHEN** a build offsets the message by the bar's height
- **THEN** the message row fails

### Requirement: Opening a field with TalkBack keeps focus on it
On Attack details (note), Log medication (name), Edit medication (name, dose) and Delete all data (the word), when TalkBack
or another accessibility service opens a text field, accessibility focus SHALL stay on that field while the keyboard opens.
No other control SHALL gain accessibility focus or be activated without the person, and no stored value SHALL change
without the person's own action (BRD EC-UX-83, D194).

#### Scenario: The note in the 360 dp window (the issue)
- **WHEN** TalkBack is on, on API 36 with Gboard at 200% font in landscape in a window 360 dp tall, and the user opens
  Attack details' note by double tap
- **THEN** within 3 s TalkBack's focus is still on the note and on no radio, chip or heading, and the line being typed shows
  112 of 112 px once the keyboard has settled
- **AND** the top bar has stepped aside as D192 says

#### Scenario: A recorded impact stays recorded
- **WHEN** a migraine has "Impact on your day" recorded as 2, TalkBack opens its note in the 360 dp window, and the user
  double taps once more
- **THEN** the double tap lands in the note, and the database still holds impact 2 for that migraine

#### Scenario: An empty impact stays empty
- **WHEN** the same steps run on a migraine with no impact recorded
- **THEN** the database still holds no impact for that migraine

#### Scenario: The other three screens
- **WHEN** TalkBack opens Log medication's name (in the usual and the dark room form), Edit medication's name and dose, and
  Delete all data's word, in the 360 dp and the 411 dp window
- **THEN** focus stays on the field, and no intake, medication or deletion is written by the next double tap

#### Scenario: The 411 dp window
- **WHEN** TalkBack opens the note in a window 411 dp tall, where the top bar stays
- **THEN** focus stays on the note and the line shows whole

#### Scenario: Remove animations and API 26
- **WHEN** TalkBack opens the note with "Remove animations" on, or on API 26 with the AOSP keyboard, at 360 dp and 411 dp
- **THEN** focus stays on the note and the database is unchanged

#### Scenario: The checks can fail
- **WHEN** the note row runs in the 360 dp window on a build without this change (v1.3.2)
- **THEN** TalkBack's focus moves to an impact choice, which shows the row can fail
- **AND** on v1.3.1 the same row keeps focus on the note, as before D192

### Requirement: The line being typed comes into view with the keyboard
Inside a screen's typing space, the line being typed SHALL come into view while the keyboard opens, in the same frames as
the keyboard: never further in a frame than the keyboard's top moved plus 1 px, only upward, and still once the keyboard has
settled, from any start position. Its rest position SHALL equal the one before this change within 1 px, TalkBack on or off
(BRD EC-UX-84, EC-UX-87, D194).

#### Scenario: Frames while the keyboard opens
- **WHEN** a 60 fps recording on API 36 with Gboard at 200% landscape in the 360 dp window covers the note's keyboard
  opening, with TalkBack off and then on
- **THEN** in each frame the cursor line moved up no further than the keyboard's top did plus 1 px, counting the bar's step
  in the frame where it steps aside, no frame moves it down, and no frame moves it twice

#### Scenario: No tail
- **WHEN** the same recording is read at the keyboard's last frame and 500 ms later
- **THEN** the two frames are identical

#### Scenario: No settle from the page's end
- **WHEN** the note starts wholly in view at the page's end, taller than the space the keyboard leaves, and the keyboard opens
  with TalkBack on, then with "Remove animations"
- **THEN** the page lands where the system's own scroll to a focused field would put it, so the keyboard's last frame and
  the frame 500 ms later are identical, and the cursor line shows 112 of 112 px

#### Scenario: No catch-up leap
- **WHEN** frames are skipped while the keyboard opens
- **THEN** the line never moves more than the keyboard's top moved across the same frames, plus 1 px

#### Scenario: The rest position is unchanged
- **WHEN** the final frame is compared with develop's in each #245 cell, at 411 dp (margins 4 and 21 px by cursor
  direction), at 360 dp, and in the margins band where QA reaches it
- **THEN** the line's position is the same within 1 px, and at 360 dp it shows 112 of 112 px with TalkBack off

#### Scenario: Remove animations and API 26 to 29
- **WHEN** "Remove animations" is on, or the device is on API 26 to 29
- **THEN** the page moves in one step with the keyboard, and at most one frame shows the line hidden

#### Scenario: The page's end
- **WHEN** the keyboard opens on the note at the bottom of Attack details or on Delete all data's word, in the dark theme
- **THEN** no overscroll stretch or glow shows

#### Scenario: Dark backgrounds
- **WHEN** the keyboard opens on Log medication opened from attack mode (dark room) in the 360 dp window
- **THEN** no frame shows a lighter colour

#### Scenario: Free, Premium, light theme and right to left
- **WHEN** the same steps run on Free and Premium, in the light theme and in a right-to-left language
- **THEN** the result is the same

#### Scenario: The motion checks can fail
- **WHEN** the recording runs on develop, whose follow is an animated bring
- **THEN** the line moves after the keyboard's last frame, which shows the no-tail check can fail

### Requirement: Only the person ends the keyboard follow
A focused field SHALL follow the keyboard from the start of its own bring into view. A layout made by the app or by Compose
SHALL NOT end the following; a scroll by the person while the keyboard holds still, or a drag that stops the field's bring,
SHALL end it. A keyboard that shrinks or closes SHALL never scroll the page. A field outside a typing space SHALL keep its
earlier bring (BRD EC-UX-85, EC-UX-86, D194).

#### Scenario: Compose's own layout before the follow
- **WHEN** a layout at the keyboard's new height runs before the follow has acted on that height, as Compose's semantics check
  does under an accessibility service
- **THEN** the following goes on and the line is brought into view, and on the next frames too

#### Scenario: A keyboard that comes during the field's bring
- **WHEN** the note is focused and the keyboard starts opening while the page is still bringing the note into view
- **THEN** the note keeps following, and its line shows in every frame once it has shown

#### Scenario: A drag during the field's bring
- **WHEN** the user drags the page while the field's bring runs, moving the line out of view, and the keyboard then grows
- **THEN** the drag stops the bring and ends the following, and the page is not pulled back to the line

#### Scenario: A scroll away still ends it
- **WHEN** the user scrolls the page away from the line while the keyboard holds still, and the keyboard then grows
- **THEN** the page is not pulled back to the line
- **AND** if the field was scrolled wholly out of view it stays where it is; if some of it still shows and the keyboard would
  leave less than its line, the page holds what showed (requirement "The keyboard never hides a focused field that shows"),
  and still does not pull it to its line

#### Scenario: The keyboard changes height while typing
- **WHEN** the line is followed and Gboard's suggestion strip appears, or the user switches to symbols or emoji
- **THEN** a taller keyboard moves the line up with it, a shorter one scrolls nothing, and focus stays on the field

#### Scenario: Rotation with the keyboard up (known limit #264)
- **WHEN** the phone rotates with the keyboard up and TalkBack on
- **THEN** the behaviour is the same as in v1.3.1: the keyboard closes and TalkBack's focus moves to Back
- **AND** this is a known limit (#264, S3, planned for 1.4.0), not a behaviour this change promises

#### Scenario: Closing the keyboard
- **WHEN** the keyboard closes by Back, the hide key or Done, with TalkBack on and off
- **THEN** the scroll position moves 0 px (or by the bar's height where it was aside) and the field keeps focus
- **AND** with TalkBack on, zero TYPE_VIEW_SCROLLED come from the page after the close and TalkBack's focus stays on the
  field

#### Scenario: No bottom keyboard inset
- **WHEN** a hardware keyboard, a floating keyboard or a split screen gives no bottom keyboard inset
- **THEN** the page does not move for the keyboard and focus stays on the field; a case QA cannot reach is recorded as not
  reachable, not as a pass

#### Scenario: A field outside a typing space
- **WHEN** a following field is placed outside any typing space and the keyboard grows
- **THEN** its line is brought into view as before this change

#### Scenario: Moving between fields with the keyboard up
- **WHEN** the keyboard is up and focus moves from Edit medication's name to its dose
- **THEN** the dose is brought into view as before this change

### Requirement: The keyboard never hides a focused field that shows
When a keyboard frame would leave a focused field that is not following the keyboard, and showed at least 1 px, with less
than its line, the page SHALL hold the lesser of its line with its clearance and what showed (within 1 px), in that same frame,
for every user. In each such frame the line SHALL move no further than the keyboard's and the bar's step plus 1 px, and not at all
after the keyboard stops. A field that covers the whole visible part SHALL NOT be held (BRD EC-UX-88, D194).

#### Scenario: Reopening the keyboard with TalkBack (the reopen path)
- **WHEN** TalkBack is on in the 360 dp window, the note is focused, Back closed the keyboard, the page was scrolled so the
  note's top is near the bottom edge, and a double tap reopens the keyboard without moving the cursor
- **THEN** in every frame at least the lesser of the line with its clearance (up to 8 dp each side) and what showed before
  stays visible, TalkBack's focus stays on
  the note, and the next double tap writes nothing

#### Scenario: Each held frame keeps to the keyboard
- **WHEN** the same reopening is recorded frame by frame
- **THEN** each held frame moves the line by at most the keyboard's step plus the bar's step plus 1 px, and 0 px once the
  keyboard has stopped

#### Scenario: Remove animations and API 26
- **WHEN** the same reopening runs with "Remove animations" on, or on API 26
- **THEN** the hold is one step with the keyboard, and the note stays in the accessibility tree
- **AND** what showed is kept within 1 px: a one-step inset may keep 1 px less (#262, S4, a known limit)

#### Scenario: Later growth
- **WHEN** the user scrolled the note partly out of view with the keyboard up, and then switches Gboard to symbols, which is
  taller
- **THEN** the page holds what showed, up to the line, and does not pull the note to its line

#### Scenario: A field scrolled wholly away
- **WHEN** the note was scrolled wholly out of view before the keyboard grows
- **THEN** the page does not move for it

#### Scenario: A field that covers the whole visible part
- **WHEN** the note already covers the whole visible part before the keyboard grows, as in a split screen where that part is
  under about 154 px
- **THEN** the page does not hold it, and the keyboard's frame moves nothing for it

#### Scenario: The first typed character after a hold
- **WHEN** the note was held with its caret line cut, and the user types one character
- **THEN** the typing follow (D180 (2), unchanged) brings the whole caret line in one direction, settles within 15 frames,
  leaves it cut by 0 px, and focus and data are unchanged; its peak speed per frame is logged, not capped
- **AND** with "Remove animations" that bring is one step, of up to about 430 px

#### Scenario: Typing, then a double tap (device row K1)
- **WHEN** TalkBack is on, the caret is at the end of 4 lines, 60 px of the note shows, the keyboard reopens, the user types
  one character and then double taps
- **THEN** focus stays on the note, nothing is written, and the caret line shows whole

#### Scenario: Until the first keystroke (a known limit)
- **WHEN** the keyboard reopens on a focused note whose cursor did not move, and the note is held
- **THEN** the caret line may stay cut until the first keystroke, while the note stays visible and focused

#### Scenario: Ordinary openings are unchanged
- **WHEN** a field is opened by a tap or a double tap, so it follows the keyboard
- **THEN** no hold applies, and the requirement "The line being typed comes into view with the keyboard" governs every frame

#### Scenario: The checks can fail
- **WHEN** the reopen path runs on develop
- **THEN** the note is wholly hidden, TalkBack's focus moves, and the next double tap writes, which shows the row can fail
- **AND** a build that brings the line instead of holding (rejected: its rescue frame moved 158 to 494 px against a 62 px
  keyboard step) fails the per-frame row
