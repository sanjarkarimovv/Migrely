# attack-details Specification

## Purpose
Defines the attack details screen: exact start and end times that never overlap another attack, pain entries over
time, where it hurts, symptoms, triggers, functional impact, a note, saving every change on its own, and deleting with
undo, so that a person can complete the record of an attack later, calmly and without losing data (BRD ATK-F4 to ATK-F7).

## Requirements

### Requirement: Open an attack's details
The user SHALL be able to open a details screen for any attack listed on today's card and for the attack in progress.
The screen SHALL show the attack's date, start, end (or "In progress"), duration and every detail stored for it. When
the attack no longer exists, the screen SHALL say so and offer to go back.

#### Scenario: Open an ended attack
- **WHEN** the user taps today's attack "14:05 – 16:30" on the home screen
- **THEN** the details screen opens for that attack and shows its start, end, duration and details

#### Scenario: Open the attack in progress
- **WHEN** an attack is in progress and the user taps "Details" in attack mode
- **THEN** the details screen opens for that attack and shows "In progress" instead of an end

#### Scenario: Attack no longer exists
- **WHEN** the details screen is opened for an attack that has been deleted
- **THEN** it shows that the migraine no longer exists and a way back

### Requirement: Exact start and end times
The user SHALL be able to set the start, and the end of an ended attack, to any date and time with a date picker
followed by a time picker. Times SHALL be shown and picked in the attack's time zone, in the device's 12- or 24-hour
format. A time more than 5 minutes after now SHALL be rejected. An end before the start SHALL be rejected. Each
rejection SHALL be shown as an inline error under the time, and nothing SHALL be stored (BRD EC-ATK-03, EC-ATK-04,
EC-TIM-02, EC-TIM-04, EC-TIM-05).

#### Scenario: Correct the start
- **WHEN** the user changes the start of an attack from 14:05 to 13:40
- **THEN** the start is stored as 13:40 and the duration updates

#### Scenario: End in the future
- **WHEN** the user picks an end 20 minutes after now
- **THEN** an inline error says the time cannot be in the future and the end is unchanged

#### Scenario: Clock tolerance
- **WHEN** the user picks an end 3 minutes after the phone's current time
- **THEN** the end is stored

#### Scenario: End before start
- **WHEN** the user picks an end earlier than the start
- **THEN** an inline error says the end must be after the start and nothing is stored

#### Scenario: Attack in progress
- **WHEN** the details of the attack in progress are shown
- **THEN** its start can be changed and its end shows "In progress" without a picker

### Requirement: Times never overlap another attack
A start or end that would make the attack overlap other attacks SHALL be rejected with an inline error that names every
overlapping attack's start and end, oldest first, and offers "Merge them". Merging SHALL follow `past-attacks`, keep this
attack, and show "Migraines merged.". Attacks that only touch at one instant SHALL NOT count as overlapping, and an
overlap shorter than a minute SHALL snap to the other attack's boundary (BRD EC-ATK-05, EC-ATK-15, EC-ATK-18, D27).

#### Scenario: Overlapping start
- **WHEN** another attack ran 12:00–13:30 and the user moves this attack's start from 14:05 to 13:00
- **THEN** an inline error says it would overlap the migraine from 12:00 to 13:30, offers "Merge them", and the start
  stays 14:05

#### Scenario: Two overlapping attacks
- **WHEN** attacks ran 09:00–10:00 and 12:00–13:30 and the user moves this attack's start from 14:05 to 08:30
- **THEN** the inline error names both attacks, 09:00–10:00 first

#### Scenario: Merge from the details screen
- **WHEN** the inline error names the migraine from 12:00 to 13:30 and the user taps "Merge them"
- **THEN** this attack runs from 12:00 to its end with the other attack's pain entries and details, the other attack
  is gone, and "Migraines merged." is shown

#### Scenario: Note typed before merging
- **WHEN** the user types "Took ibuprofen" in this attack's note, the other attack's note is "Bright office", and the
  user taps "Merge them" and then leaves the screen
- **THEN** the stored note is "Bright office", a blank line and "Took ibuprofen"

#### Scenario: Touching attacks
- **WHEN** another attack ended at 13:30 and the user sets this attack's start to 13:30
- **THEN** the start is stored

#### Scenario: Seconds do not overlap
- **WHEN** another attack ended at 13:30:40 and the user sets this attack's start to 13:30
- **THEN** the start is stored as 13:30:40

### Requirement: Pain entries over time
The user SHALL be able to add a pain level from 1 to 10 at a time inside the attack (from its start to its end, or to
now while it is in progress) and to remove any pain entry. A removed entry SHALL be restorable with "Undo". The
attack's peak SHALL be the highest of its entries, and "Not recorded" when there are none (BRD ATK-F4, EC-ATK-08).

#### Scenario: Add a later, stronger entry
- **WHEN** an attack has an entry of 6 at 14:10 and the user adds 8 at 15:00
- **THEN** both entries are listed in time order and the peak is 8

#### Scenario: Entry outside the attack
- **WHEN** the user tries to add an entry at a time after the attack's end
- **THEN** the entry is rejected with an inline error and nothing is stored

#### Scenario: Remove and undo
- **WHEN** the user removes the entry of 8 and taps "Undo"
- **THEN** the entry of 8 at 15:00 is back

### Requirement: Pain entries outside new times
When a new start or end leaves pain entries outside the attack, the new times SHALL be stored, those entries SHALL be
removed in the same step, and a message SHALL say how many were removed with an "Undo" that restores the previous times
and entries (BRD EC-ATK-09).

#### Scenario: Shorten the attack
- **WHEN** an attack has entries at 14:10 and 17:00 and the user changes its end from 18:00 to 16:00
- **THEN** the end is 16:00, the 17:00 entry is removed and the message says one pain entry was removed

#### Scenario: Undo the shortening
- **WHEN** the user taps "Undo" on that message
- **THEN** the end is 18:00 again and the 17:00 entry is back

### Requirement: First maximum pain safety card
The first time the user ever records a pain level of 10, the details screen SHALL show a card that does not block the
screen: "If this pain came on suddenly and is the worst of your life, call emergency services now." The card SHALL be
dismissible and SHALL NOT appear for later 10s (BRD SAF-F2).

#### Scenario: First 10
- **WHEN** no attack has ever had a pain entry of 10 and the user adds one
- **THEN** the safety card appears above the details and the rest of the screen stays usable

#### Scenario: Later 10
- **WHEN** an earlier attack already has a pain entry of 10 and the user adds another 10
- **THEN** no safety card appears

### Requirement: Where it hurts, symptoms and triggers
The user SHALL be able to select any number of locations, symptoms and triggers for an attack. The built-in choices
are listed in the scenarios. Symptoms and triggers also offer the user's own tags (BRD ATK-F5).

#### Scenario: Built-in choices
- **WHEN** the details screen is shown
- **THEN** locations offer left side, right side, both sides, forehead, back of the head, around the eyes and whole head
- **AND** symptoms offer aura, nausea, vomiting, sensitivity to light, sensitivity to sound, sensitivity to smell,
  dizziness and neck pain
- **AND** triggers offer stress, too little sleep, too much sleep, skipped meal, dehydration, alcohol, caffeine,
  period, weather, bright light or screens, smell, physical effort and travel

#### Scenario: Select symptoms
- **WHEN** the user selects "Aura" and "Nausea"
- **THEN** both are stored for the attack and shown as selected the next time the screen opens

#### Scenario: Quick double tap
- **WHEN** the user taps "Forehead" twice quickly
- **THEN** "Forehead" ends up not selected and nothing else changes

### Requirement: Functional impact
The user SHALL be able to choose at most one functional impact: 0 no impact, 1 less active, 2 had to stop, 3 bed rest.
Tapping the selected impact again SHALL clear it (BRD ATK-F5, P3).

#### Scenario: Change the impact
- **WHEN** the user selects "Had to stop" and then "Bed rest"
- **THEN** only "Bed rest" is stored

#### Scenario: Clear the impact
- **WHEN** the user taps the selected impact again
- **THEN** the attack has no impact stored

### Requirement: Note with a limit
The user SHALL be able to write a note of up to 1,000 characters. Input SHALL stop at 1,000 characters and a counter
SHALL show the characters used (BRD ATK-F5, EC-ATK-13).

#### Scenario: Reaching the limit
- **WHEN** the note has 1,000 characters and the user types another
- **THEN** the note stays at 1,000 characters and the counter shows "1000/1000"

### Requirement: Every change saves itself
Every change on the details screen SHALL be stored without a "Save" button: selections, times and pain entries at once,
and the note while typing and when the screen is left or the app goes to the background. When storage is full, the
screen SHALL show "storage full" and keep what the user entered so it can be tried again (BRD ATK-F6, EC-ATK-10,
EC-STO-02).

#### Scenario: Leave right after typing
- **WHEN** the user types a note and immediately presses back
- **THEN** the note is stored and shown the next time the attack is opened

#### Scenario: App killed while editing
- **WHEN** the user selects "Nausea" and the app is killed a second later
- **THEN** "Nausea" is still selected when the attack is opened again

### Requirement: Delete with undo
The user SHALL be able to delete an attack from its details screen without a confirmation dialog. The app SHALL return
to the home screen and show "Migraine deleted" with "Undo" for the undo window of `in-app-messages`. Undo SHALL restore
the attack with all its details. After the window the attack SHALL be unrecoverable and SHALL never be shown, counted or
exported (BRD ATK-F7, EC-ATK-14, D23).

#### Scenario: Delete and undo
- **WHEN** the user deletes an attack and taps "Undo" on the home screen within the window
- **THEN** the attack is back with its times, pain entries and details

#### Scenario: Window passes
- **WHEN** the user deletes an attack and does not tap "Undo"
- **THEN** the message disappears and the attack is never shown again

#### Scenario: Delete the attack in progress
- **WHEN** the user deletes the attack in progress
- **THEN** the home screen leaves attack mode and offers "Undo"

### Requirement: Accessible details screen
Every control SHALL have a spoken label. Selected choices SHALL be announced as selected. The pain level control SHALL
be adjustable with accessibility actions. Every part of the screen SHALL stay visible and usable at a 200% font size and
in landscape (BRD EC-UX-03, EC-UX-04).

#### Scenario: Screen reader on a symptom
- **WHEN** TalkBack focuses the selected "Aura" choice
- **THEN** it reads "Aura, selected"

#### Scenario: Largest font size
- **WHEN** the system font size is 200%
- **THEN** every section of the details screen can be read and used by scrolling
