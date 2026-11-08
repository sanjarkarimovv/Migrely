## MODIFIED Requirements

### Requirement: Kind and Category while the form opens
On Add and Edit, Kind and Category SHALL ignore touches for 700 ms after the form appears. Name, Dose, Save and Back SHALL
take a first touch at once, unless it is ignored as the second tap of a double tap. A deliberate first touch on Kind or Category in that time SHALL be ignored with no feedback, and
a rotation SHALL arm it again. Screen readers, switch access and keyboards SHALL NOT be affected (BRD EC-MED-15, D157, D159).

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
- **THEN** that control answers, unless the press is ignored as the second tap of a double tap (D159)

#### Scenario: A deliberate tap in the guard
- **WHEN** the user deliberately taps Kind within 700 ms of the form appearing
- **THEN** nothing changes and nothing says so, and a second tap after 700 ms works

#### Scenario: A rotation
- **WHEN** the editor is rotated
- **THEN** Kind and Category ignore touches for 700 ms again, and the form is kept

#### Scenario: A screen reader in the guard
- **WHEN** TalkBack activates Kind within 700 ms of the form appearing
- **THEN** the choice changes
