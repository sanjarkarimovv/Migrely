## ADDED Requirements

### Requirement: A field problem replaces the message on the medication forms
On Edit medication and on Log medication's first-medication form, when Save or "Add and log" finds a field problem, any
message on the screen SHALL go in the same frame, silently, so the field error is the only error and no message covers
Name. A later attempt that fails for another reason SHALL show its message again (BRD EC-UX-70, D191).

#### Scenario: Storage message, then an empty name
- **WHEN** "Couldn't save. Your phone's storage is full." shows on Edit medication, the user frees space, clears Name and
  saves
- **THEN** the storage message is gone in the same frame that "Enter a name." appears, Name and its error are in view and
  not covered, and only "Enter a name." is announced

#### Scenario: Storage still full after the fix
- **WHEN** the storage message went with a name problem, storage is still full, and the user fixes the name and saves
- **THEN** "Couldn't save. Your phone's storage is full." shows again and is announced

#### Scenario: Any other message
- **WHEN** "Something went wrong. Please try again.", "You can keep up to 50 medications. Remove one you no longer take." or
  "Pick a time up to now." shows and Save or "Add and log" then finds a field problem
- **THEN** the message goes silently in the same frame and the field problem follows the field rules

#### Scenario: The first-medication form
- **WHEN** a message shows on Log medication's first-medication form and "Add and log" finds the name empty
- **THEN** the message goes in the same frame and nothing covers "Taken at" or Name

#### Scenario: Rotation after the clear
- **WHEN** the message went with a field problem and the phone rotates or the process is restored
- **THEN** the field error shows and the message does not come back
