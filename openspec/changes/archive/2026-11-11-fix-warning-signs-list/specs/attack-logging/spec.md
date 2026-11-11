## MODIFIED Requirements

### Requirement: Check attacks open for 72 hours
An attack SHALL never be closed automatically. When the app is opened while an attack has been in progress for 72 hours
or more, the app SHALL ask whether it is still going and say that, if it is, the person should get urgent medical
advice, in the words of the Warning signs page. The user SHALL be able to answer "Still going", "It has ended" or "Don't
ask again for this attack" (BRD EC-ATK-02, SAF-F3, D171, EC-SAF-10).

#### Scenario: Prompt after 72 hours
- **WHEN** the app is opened and the attack in progress started 73 hours ago
- **THEN** the app asks "Still going?" and shows "If it's still going after 72 hours, get urgent medical advice."

#### Scenario: Still going
- **WHEN** the user answers "Still going"
- **THEN** the attack stays in progress and the question is not asked again until another 72 hours have passed

#### Scenario: Don't ask again
- **WHEN** the user answers "Don't ask again for this attack"
- **THEN** the question is never asked again for this attack

#### Scenario: It has ended
- **WHEN** the user answers "It has ended" and picks a date and time
- **THEN** the attack ends at the picked time
- **AND** only times between the attack's start and now can be picked
