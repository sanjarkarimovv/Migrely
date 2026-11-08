## MODIFIED Requirements

### Requirement: First maximum pain safety card
When the user enters a pain level of 10 and no saved attack has a 10, the details screen SHALL show a card that does not block the
screen: "If this pain came on suddenly and is the worst of your life, call emergency services now." It SHALL show whether the entry is
saved or not, and when the saved 10s cannot be read. The card SHALL be dismissible and SHALL NOT appear once a 10 is saved
(BRD SAF-F2, EC-ATK-22, D163).

#### Scenario: First 10
- **WHEN** no attack has ever had a pain entry of 10 and the user adds one
- **THEN** the safety card appears above the details and the rest of the screen stays usable

#### Scenario: Later 10
- **WHEN** an earlier attack already has a pain entry of 10 and the user adds another 10
- **THEN** no safety card appears

#### Scenario: A first 10 that fails to save
- **WHEN** no saved attack has a 10, the user adds a 10, and the phone's storage is full
- **THEN** the safety card appears, and "Couldn't save. Your phone's storage is full." shows as it does today

#### Scenario: A first 10 that fails for another reason
- **WHEN** no saved attack has a 10, the user adds a 10, and the save fails for another reason or the entry is refused for its time
- **THEN** the safety card appears, and that outcome's own message shows as it does today

#### Scenario: The saved 10s cannot be read
- **WHEN** the user adds a 10 and reading whether any attack has a 10 fails
- **THEN** the safety card appears

#### Scenario: The same 10 saved later
- **WHEN** a first 10 failed to save, the person frees space, and adds the 10 again, and it saves
- **THEN** the safety card appears again, because the stored history now has its first 10

#### Scenario: A failed 10 does not count as saved
- **WHEN** a first 10 failed to save and the user adds another 10 that also fails
- **THEN** the safety card appears again

#### Scenario: The attack is gone
- **WHEN** the user adds a 10 and the attack no longer exists
- **THEN** the screen says so, and no card appears

#### Scenario: TalkBack hears both
- **WHEN** TalkBack is on and a first 10 fails to save
- **THEN** it speaks "Couldn't save. Your phone's storage is full." and then the card's text, each in full, and both stay on screen
  and reachable until dismissed

#### Scenario: Rotation
- **WHEN** the phone rotates with the card and the message showing
- **THEN** both are still there
