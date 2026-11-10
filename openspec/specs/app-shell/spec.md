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
