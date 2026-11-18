## MODIFIED Requirements

### Requirement: An error on the other screens goes when a later save succeeds
On Home, History, My medications, Edit medication, Log medication, Reminders, Doctor report, Your data and Add past migraine,
while an error shows, the next write there that succeeds SHALL replace it with that write's own message, or else with "Saved."
on Home, History and Reminders. A created report on Doctor report and a logged intake on Log medication SHALL clear it with no
message. It SHALL clear on success, never on the attempt (BRD EC-UX-35, D178, D190).

#### Scenario: Home Today, a day mark after freeing space
- **WHEN** marking today's period failed because storage was full, the user frees space without closing the message, and marks
  it again
- **THEN** within 1 second "Saved." replaces "Couldn't save", the mark is stored, TalkBack speaks "Saved.", and it leaves after
  8 seconds or the accessibility timeout

#### Scenario: Home in attack mode
- **WHEN** recording a pain level during an attack failed, and a later pain level, a start moved earlier or a long-attack answer
  saves
- **THEN** "Saved." replaces the error, dim like attack mode, with no haptic, and the attack-mode controls stay uncovered as before

#### Scenario: History, a day mark
- **WHEN** an error shows on History and a period or pain-free mark then saves
- **THEN** "Saved." replaces the error

#### Scenario: Reminders
- **WHEN** an error shows on Reminders and the daily reminder, its time, the effect reminder or the visit date then saves
- **THEN** "Saved." replaces the error

#### Scenario: Reminders, a visit date that asks for permission
- **WHEN** an error shows on Reminders and a visit date saves and the app then asks for the notification permission
- **THEN** the error is gone, "Saved." replaces it even if it sits behind the system dialog or times out there, and the row shows
  the date

#### Scenario: Doctor report
- **WHEN** creating a report failed because storage was full, and a later attempt creates it
- **THEN** the error is gone, the share sheet opens, and no "Saved." shows, because the app saved nothing

#### Scenario: A write with its own message
- **WHEN** an error shows and the next successful write brings its own message, such as "Backup saved" on Your data, the archive
  message with "Undo" on My medications, "Removed. Nothing was logged." after an Undo on Log medication, "Migraine started" or
  "Undone."
- **THEN** that message replaces the error, and no "Saved." shows

#### Scenario: Log medication, a dose logged after freeing space
- **WHEN** logging Ibuprofen failed with "Couldn't save. Your phone's storage is full.", the user frees space without closing
  the message, and taps Ibuprofen again
- **THEN** within 1 second the error is gone with no "Saved." and no haptic, "Ibuprofen 400 mg logged at 14:20" shows with
  "Done" and "Undo" and nothing over it, and TalkBack speaks the logged line

#### Scenario: A write that closes the screen
- **WHEN** an error shows on Edit medication or Add past migraine and a later save succeeds
- **THEN** the screen closes as before, and the error does not show on the screen it returns to

#### Scenario: Nothing was written
- **WHEN** an error shows and an action succeeds without writing anything, such as a pain level after the attack was already
  ended or a visit date that did not change
- **THEN** no "Saved." shows and the error stays

#### Scenario: No error on screen
- **WHEN** a write saves on any of these screens and no error shows
- **THEN** no "Saved." shows

#### Scenario: Undo or information on screen
- **WHEN** a message with "Undo" or an information message shows on any of these screens but Log medication, and a later write
  saves
- **THEN** that message stays with the time it has left
- **AND** on Log medication a logged intake clears it, because the logged result replaces the whole page (medication-intakes
  "A logged intake clears the message on Log medication")

#### Scenario: It fails again
- **WHEN** an error shows, storage is still full, and the user tries again
- **THEN** the error shows in the same place and TalkBack speaks it again, with no flash and no change of surface colour

#### Scenario: Two quick saves
- **WHEN** an error shows and two writes succeed one right after the other
- **THEN** one "Saved." shows, and the second write finds no error and adds nothing

#### Scenario: A late success after leaving
- **WHEN** a write succeeds after the user has already left the screen
- **THEN** nothing shows there, and on return no error and no "Saved." show

#### Scenario: Free and Premium
- **WHEN** the same steps run on Free and on Premium
- **THEN** the messages are the same

#### Scenario: Largest font and landscape
- **WHEN** "Saved." shows at 200% font, in portrait or landscape
- **THEN** its whole text is visible and its place follows the screen's usual rules
