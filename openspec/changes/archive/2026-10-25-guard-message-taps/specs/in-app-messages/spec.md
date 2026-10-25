## ADDED Requirements

### Requirement: A message never passes taps to what lies under it
While a message arrives or goes, and for 0.8 seconds after, a touch on its place SHALL not reach the controls under it,
such as a top bar's actions. The message itself SHALL stay tappable at once. Screen readers, keyboards and switch access
SHALL be unaffected (BRD EC-UX-16, D114).

#### Scenario: Undo just as the message goes
- **WHEN** a migraine is deleted in History and the user taps "Undo the deletion" just as the message fades out
- **THEN** neither "Doctor report" nor "Insights" opens

#### Scenario: Afterwards
- **WHEN** the message has been gone for more than 0.8 seconds
- **THEN** the top bar's actions work as usual
