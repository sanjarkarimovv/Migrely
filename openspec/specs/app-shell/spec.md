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
