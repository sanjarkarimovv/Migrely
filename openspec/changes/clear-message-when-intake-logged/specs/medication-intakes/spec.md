## ADDED Requirements

### Requirement: A logged intake clears the message on Log medication
When an intake is stored on Log medication, by a tap on a medication or by "Add and log", every message on that screen SHALL
go at once, with no new message and no haptic, and the logged line SHALL be announced with nothing over it. Only a stored
intake SHALL clear it: a time in the future, full storage, another failure or a refused medication SHALL keep the list and
show their message as before (BRD EC-MED-16, D190).

#### Scenario: Storage full, then logged after freeing space
- **WHEN** "Couldn't save. Your phone's storage is full." shows after a tap on Ibuprofen failed, the user frees space and taps
  Ibuprofen again
- **THEN** one new intake is stored, the error is gone within 1 second, "Ibuprofen 400 mg logged at 14:20" shows with "Done"
  and "Undo", no "Saved." shows, there is no haptic, and TalkBack speaks the logged line

#### Scenario: Another error, then logged
- **WHEN** "Something went wrong. Please try again." or "Pick a time up to now." shows and the next tap on a medication stores
  an intake
- **THEN** the message is gone and the logged result shows as above

#### Scenario: First medication added and logged
- **WHEN** no medication exists, "Add and log" failed because storage was full, and a later "Add and log" stores the
  medication and its intake
- **THEN** the error is gone, the logged result shows, and Ibuprofen is stored once as a medication with one intake

#### Scenario: An information message on screen
- **WHEN** "Removed. Nothing was logged." shows after an Undo and the user logs another medication before it times out
- **THEN** the message goes at once and the logged line shows with nothing over it

#### Scenario: It fails again
- **WHEN** an error shows, storage is still full, and the user taps Ibuprofen again
- **THEN** no intake is stored, the list stays, and the error shows in the same place and TalkBack speaks it again

#### Scenario: Not on the tap
- **WHEN** an error shows and the user taps Ibuprofen while the write is still running
- **THEN** the error stays until the write's outcome, and goes only if the intake was stored

#### Scenario: Two quick taps
- **WHEN** an error shows and the user taps Ibuprofen twice quickly after freeing space
- **THEN** exactly one intake is stored and the logged result shows once

#### Scenario: One outcome for the page and the message
- **WHEN** the logged outcome reaches the screen
- **THEN** the logged result and the cleared message show together, in either order of handling, and no frame after that
  shows the logged result under the old message

#### Scenario: Rotation and a restored process after the log
- **WHEN** the logged result shows after an error and the phone rotates, or the process is killed in the background and
  restored
- **THEN** the logged result shows again with no message over it

#### Scenario: The dark room
- **WHEN** Log medication was opened from attack mode, an error shows, and a later tap stores the intake
- **THEN** the error goes the same way, the logged result keeps the dark room's colours, and nothing else changes

#### Scenario: Keyboard and TalkBack
- **WHEN** the user logs by pressing Enter on a medication, or by a TalkBack double tap, while an error shows
- **THEN** the error goes, focus does not land on where it was, and TalkBack reads the logged line once

#### Scenario: Largest font size and landscape
- **WHEN** the same steps run at 200% font, in portrait and in landscape
- **THEN** the whole logged line, "Done" and "Undo" are visible with no message over them

#### Scenario: Free and Premium
- **WHEN** the same steps run on Free and on Premium
- **THEN** the result is the same

### Requirement: Undo on Log medication keeps the logged result until the intake is removed
After "Undo" on the logged result, the result with "Done" and "Undo" SHALL stay until the removal is stored; then the list
SHALL show with "Removed. Nothing was logged.". If the removal fails, the result SHALL stay with the error over it and "Undo"
SHALL work again. A second "Undo" before the outcome SHALL do nothing (BRD EC-MED-17, D190).

#### Scenario: Undo succeeds
- **WHEN** the user taps "Undo" on "Ibuprofen 400 mg logged at 14:20" and the removal is stored
- **THEN** the intake is gone, the list of medications shows, and "Removed. Nothing was logged." shows

#### Scenario: Undo with full storage
- **WHEN** the user taps "Undo" and the removal fails because storage is full
- **THEN** the intake is still stored, the logged result with "Done" and "Undo" stays, "Couldn't save. Your phone's storage
  is full." shows over it, and TalkBack speaks it

#### Scenario: Undo again after freeing space
- **WHEN** the removal failed, the user frees space and taps "Undo" again
- **THEN** the intake is removed, the list shows, and "Removed. Nothing was logged." replaces the error

#### Scenario: Undo fails for another reason
- **WHEN** the removal fails with any other error
- **THEN** the logged result stays and "Something went wrong. Please try again." shows over it

#### Scenario: Two quick taps on Undo
- **WHEN** the user taps "Undo" twice before the outcome
- **THEN** one removal runs and one outcome shows

#### Scenario: Done before the outcome
- **WHEN** the user taps "Undo" and then "Done" before the outcome
- **THEN** the screen closes as before, and no message from the late outcome shows on the screen it returns to

#### Scenario: Rotation or a restored process during Undo
- **WHEN** the phone rotates, or the process is restored, while a removal runs
- **THEN** the screen shows the logged result if the intake may still be stored, a later "Undo" removes it or confirms it is
  gone with "Removed. Nothing was logged.", and no second intake is ever removed
