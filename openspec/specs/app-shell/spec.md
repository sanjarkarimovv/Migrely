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
what it sends on entering SHALL count. The check reads the observer's screen, not the sender's (BRD EC-UX-21, D154).

#### Scenario: Two rows on Home's today list
- **WHEN** the user touches two rows of "Logged today" within a fade, by two taps or a resting finger
- **THEN** one details screen opens, and Delete returns to home with "Undo the deletion"

#### Scenario: The same row twice
- **WHEN** the user double taps one row of "Logged today"
- **THEN** one details screen opens

#### Scenario: A first tap on a screen fading in
- **WHEN** the user taps Back on the editor while it is still fading in
- **THEN** the list shows

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
working. Dialogs and popups are separate windows and SHALL NOT be covered. Layout SHALL NOT change (BRD EC-UX-22, D155).

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
- **THEN** the control answers

#### Scenario: A pop where the leaving screen takes no touch
- **WHEN** the user taps a control on the screen fading in after a pop, where the leaving screen has no control or scroll
  column
- **THEN** the control answers

#### Scenario: A pop where the leaving screen has a control
- **WHEN** the user taps during a pop where the leaving screen has a control or a scroll column
- **THEN** the tap does nothing, for at most the length of the fade

#### Scenario: Nothing navigated
- **WHEN** a finger rests on a control and lifts with no navigation in between
- **THEN** the control clicks as usual

#### Scenario: Returning to the screen
- **WHEN** a control's screen was left and is current again after the fade
- **THEN** the control answers

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
