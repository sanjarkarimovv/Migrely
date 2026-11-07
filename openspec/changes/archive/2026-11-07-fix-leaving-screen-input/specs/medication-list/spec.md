## ADDED Requirements

### Requirement: The editor belongs to one medication
The medication editor SHALL hold one medication's id: another id SHALL start it afresh, and the form and the row Save writes
SHALL always be the same medication. A read of another medication SHALL NOT fill the form. A loaded form SHALL win over
NotFound, which carries no id (BRD EC-MED-14, D157).

#### Scenario: Two touches on My medications
- **WHEN** the user touches two rows within a fade, by two taps or a resting finger, then renames and saves
- **THEN** only the medication on the form changes, and the other row, its intakes and its overuse warnings stay

#### Scenario: A late read of another medication
- **WHEN** a read of medication A arrives while the editor is open for medication B
- **THEN** the form stays on B, and Save writes B

#### Scenario: Two reads in one frame
- **WHEN** the right read and a late read of another medication arrive in one frame
- **THEN** the form shows the right medication and is not blank

#### Scenario: A late read on Add
- **WHEN** a late read of a medication arrives on the Add form
- **THEN** the form does not take it

#### Scenario: A late empty read on Edit
- **WHEN** a late empty read from Add arrives on the Edit form
- **THEN** the form does not take it

#### Scenario: A second read of the same medication
- **WHEN** a second read of the same medication arrives after the user has typed
- **THEN** what was typed stays

#### Scenario: A late NotFound before the right read
- **WHEN** a NotFound of another medication arrives before the right read
- **THEN** the form shows once the right read arrives

#### Scenario: A late NotFound after the right read
- **WHEN** a NotFound of another medication arrives after the right read
- **THEN** the form stays on screen

#### Scenario: A medication that no longer exists
- **WHEN** the edited medication no longer exists
- **THEN** the editor says so and offers no Save

#### Scenario: Recreation
- **WHEN** the editor is rotated or its process killed after the form has loaded
- **THEN** what was typed stays, and Save writes the same medication

### Requirement: Kind and Category while the form opens
On Add and Edit, Kind and Category SHALL ignore touches for 700 ms after the form appears. Name, Dose, Save and Back SHALL
take a first touch at once. A deliberate first touch on Kind or Category in that time SHALL be ignored with no feedback, and
a rotation SHALL arm it again. Screen readers, switch access and keyboards SHALL NOT be affected (BRD EC-MED-15, D157).

#### Scenario: A stray tap on Edit
- **WHEN** the user taps a row of My medications, and a second tap lands on "Preventive" within 700 ms of the form appearing
- **THEN** the kind does not change

#### Scenario: A stray tap on Add
- **WHEN** a tap lands on "Preventive" within 700 ms of the Add form appearing
- **THEN** it is not chosen

#### Scenario: A stray tap on a category
- **WHEN** a tap lands on a category within 700 ms of the form appearing
- **THEN** the category does not change

#### Scenario: After the guard
- **WHEN** the user taps Kind or Category after 700 ms
- **THEN** the choice changes

#### Scenario: Name, Dose, Save and Back are free
- **WHEN** the user's first tap just after the form appears is on Name, Dose, Save or Back
- **THEN** that control answers

#### Scenario: A deliberate tap in the guard
- **WHEN** the user deliberately taps Kind within 700 ms of the form appearing
- **THEN** nothing changes and nothing says so, and a second tap after 700 ms works

#### Scenario: A rotation
- **WHEN** the editor is rotated
- **THEN** Kind and Category ignore touches for 700 ms again, and the form is kept

#### Scenario: A screen reader in the guard
- **WHEN** TalkBack activates Kind within 700 ms of the form appearing
- **THEN** the choice changes
