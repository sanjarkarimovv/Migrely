## ADDED Requirements

### Requirement: Today's attacks open their details
Below today's sky, the home screen SHALL list every ended attack that touched today, oldest first, each with its start
and end time and its peak (or "Not recorded"). An attack that started on an earlier day SHALL show that day. Tapping an
attack SHALL open its details (BRD 18.3 A).

#### Scenario: Two attacks today
- **WHEN** attacks ran today 09:10–10:00 peaking at 4 and 14:05–16:30 peaking at 9
- **THEN** the home screen lists "09:10 – 10:00 · 4/10" and "14:05 – 16:30 · 9/10"

#### Scenario: Attack from last night
- **WHEN** an attack ran from yesterday 23:30 to today 02:00
- **THEN** it is listed with yesterday's day before its start time

#### Scenario: Open from the list
- **WHEN** the user taps a listed attack
- **THEN** that attack's details screen opens

### Requirement: Deleted attack can be undone from home
After an attack is deleted from its details screen, the home screen SHALL show "Migraine deleted" with "Undo" for the
undo window of `in-app-messages`. Undo SHALL restore the attack. When another attack has started since, so that
restoring would create two attacks in progress or an overlap, the home screen SHALL say "This can no longer be undone."
(BRD ATK-F7, EC-ATK-14, EC-UX-16).

#### Scenario: Undo from home
- **WHEN** the user deletes today's only attack and taps "Undo"
- **THEN** today's card and list show that attack again

#### Scenario: Rotation keeps the undo
- **WHEN** the user rotates the phone 2 seconds after the deletion
- **THEN** "Migraine deleted" with "Undo" is still shown for the rest of the window
