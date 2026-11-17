# onboarding Specification

## Purpose
Defines Migrely's first start: four short, skippable steps that learn the user's goal and how often migraines come,
set period tracking, offer the daily check-in, show the medical disclaimer, and lead a returning user back to their
backup (BRD ONB-F1–F3, EC-BAK-05, D76–D80).

## Requirements

### Requirement: Shown once, to a new user
Onboarding SHALL show on the first start, before home, when the phone holds no health record. It SHALL NOT show when
the app opens to start an attack, nor once it was finished or skipped. A phone that already holds records SHALL mark it
finished without showing it. Home SHALL NOT appear before the decision (BRD ONB-F2, D76).

#### Scenario: A new user
- **WHEN** Migrely starts for the first time on a phone with no entries
- **THEN** onboarding shows its first step

#### Scenario: An update
- **WHEN** a user with logged attacks updates to this version
- **THEN** home opens and onboarding never shows

#### Scenario: Starting an attack from the shortcut
- **WHEN** a new user opens Migrely through the "Start attack" shortcut
- **THEN** the attack starts and no onboarding is shown

### Requirement: Four skippable steps
Onboarding SHALL have four steps: the goal, how often migraines come, period tracking, and the daily reminder. Each
SHALL show "Step N of 4" and "Skip". Choosing an answer SHALL save it and move on, and "Skip" SHALL move on without
one. Back SHALL return to the previous step. After the last step home SHALL open, and onboarding is finished (BRD
ONB-F1, ONB-F2, D77).

#### Scenario: Answering
- **WHEN** the user chooses "Find what triggers my migraines" on step 1
- **THEN** it is saved and step 2 shows

#### Scenario: Skipping everything
- **WHEN** the user taps "Skip" on all four steps
- **THEN** home opens, nothing was saved but onboarding being finished, and period tracking is on

### Requirement: The disclaimer
The first step SHALL show the one-line disclaimer "Migrely is a headache diary. It doesn't diagnose or treat.", with
nothing to accept and nothing to scroll through (BRD ONB-F3).

#### Scenario: Seeing it
- **WHEN** step 1 shows
- **THEN** the disclaimer is on it and the user can go on at once

### Requirement: Restore from a backup
The first step SHALL offer "Restore from a backup file", which opens Your data. Back from there SHALL return to
onboarding (BRD EC-BAK-05).

#### Scenario: A new phone without Android's backup
- **WHEN** the user taps "Restore from a backup file" and imports their file
- **THEN** their entries are on the phone, and Back returns to onboarding

### Requirement: The daily reminder offer
The fourth step SHALL offer the daily reminder at 8:00 PM, with "Turn on" and "Not now". "Turn on" SHALL ask for
notification permission by the Reminders rules. A refusal SHALL still finish the step, with the reminder off (BRD
REM-F1, EC-REM-01, D80).

#### Scenario: Turning it on
- **WHEN** the user taps "Turn on" and allows notifications
- **THEN** the daily reminder is on at 8:00 PM and home opens

### Requirement: The paywall after onboarding
After the last onboarding step, a free user SHALL see the paywall once, and closing it SHALL lead to home. It SHALL count
toward the 7-day limit on paywalls that show themselves (BRD §7.3, D110).

#### Scenario: Finishing onboarding
- **WHEN** a new user finishes or skips the last onboarding step
- **THEN** the paywall opens, and closing it shows home

### Requirement: The goal choices name a record, not an outcome
Step 1 SHALL offer "Keep a record for my doctor", "Find what triggers my migraines" and "Keep track of my medication days".
No goal SHALL suggest taking less medicine or promise a result. The medicine goal SHALL keep its stored value
`fewer_painkillers`, so an upgrade, a backup and a restore change nothing (BRD ONB-F1, D77, D177).

#### Scenario: The label
- **WHEN** step 1 shows
- **THEN** its third choice reads "Keep track of my medication days"
- **AND** no text on step 1 says "fewer painkillers", in any letter case

#### Scenario: Choosing it
- **WHEN** a new user taps "Keep track of my medication days"
- **THEN** the goal is saved as `fewer_painkillers` and step 2 shows (BRD EC-ONB-04)

#### Scenario: An upgrade with the old goal stored
- **WHEN** a user who chose "Take fewer painkillers" in an earlier version updates to this one
- **THEN** the stored `fewer_painkillers` still reads as the medicine goal, with no migration and no error
- **AND** onboarding does not show again (BRD EC-ONB-01, D76)

#### Scenario: Large font, narrow screen, landscape
- **WHEN** step 1 shows at 200% font on a 360 dp wide screen, in portrait or in landscape
- **THEN** "Keep track of my medication days" wraps onto more lines without being clipped, cut with "…" or broken inside a word
- **AND** each choice keeps a touch target of at least 48 dp, and the disclaimer, "Restore from a backup file" and "Skip" are
  reachable by scrolling (BRD EC-ONB-02)

#### Scenario: TalkBack and keyboard
- **WHEN** a TalkBack, keyboard or Switch Access user moves through step 1
- **THEN** the third choice is announced as "Keep track of my medication days" and is actionable
- **AND** the order stays: step counter, "Skip", title, the three choices, disclaimer, "Restore from a backup file" (BRD EC-ONB-03)

#### Scenario: Skip and Back on step 1
- **WHEN** the user taps "Skip" on step 1
- **THEN** step 2 shows and no goal is saved
- **AND** the system Back on step 1 still leaves the app, and onboarding shows again next time (BRD EC-ONB-04, D77)

#### Scenario: Backup and restore
- **WHEN** the user exports a JSON backup, or imports one with Add or Replace
- **THEN** the file holds no goal, exactly as before this change, and it stays readable by earlier versions
- **AND** an Auto Backup restore of `fewer_painkillers` on a new phone reads as the medicine goal (BRD EC-ONB-05, D78)

#### Scenario: Offline, Free or Premium, dark theme
- **WHEN** step 1 shows with no network, for a free or a Premium user, in the dark theme
- **THEN** the same three choices show, with the same colours and contrast as before (BRD EC-ONB-06)
