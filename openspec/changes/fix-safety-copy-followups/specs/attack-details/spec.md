## MODIFIED Requirements

### Requirement: First maximum pain safety card
When the user enters a pain level of 10 and no saved attack has a 10, the details screen SHALL show a card that does not block the
screen. Its sentence SHALL lead with the call to action and name the sign in the Warning signs page's own words. It SHALL show
whether the entry is saved or not, and when the saved 10s cannot be read. The card SHALL be dismissible and SHALL NOT appear once a
10 is saved (BRD SAF-F2, EC-ATK-22, D163, D165, D173, EC-SAF-11, EC-SAF-12).

#### Scenario: Card text
- **WHEN** the card shows
- **THEN** its title reads "Sudden, extremely bad pain?"
- **AND** its sentence reads "Call your local emergency number now if this is extremely bad pain that came on suddenly."
- **AND** its button reads "Got it"

#### Scenario: The card and the page name the sign the same way
- **WHEN** the card's sentence is compared with the first emergency sign on the Warning signs page
- **THEN** the sentence contains that sign's words, "extremely bad pain that came on suddenly"
- **AND** neither the title nor the sentence contains "worst" or "emergency services"

#### Scenario: First 10
- **WHEN** no attack has ever had a pain entry of 10 and the user adds one
- **THEN** the safety card appears above the details and the rest of the screen stays usable

#### Scenario: Later 10
- **WHEN** an earlier attack already has a pain entry of 10 and the user adds another 10
- **THEN** no safety card appears

#### Scenario: A first 10 that fails to save
- **WHEN** no saved attack has a 10, the user adds a 10, and the phone's storage is full
- **THEN** the safety card appears, and "Couldn't save. Your phone's storage is full." shows under the top bar
- **AND** at any font and orientation, no part of the card is under the message and no line is clipped; when the card is taller
  than the area below the message, the rest is reached by scrolling (D167 (1))

#### Scenario: A first 10 that fails for another reason
- **WHEN** no saved attack has a 10, the user adds a 10, and the save fails for another reason or the entry is refused for its time
- **THEN** the safety card appears, and that outcome's own message shows as it does today

#### Scenario: The saved 10s cannot be read
- **WHEN** the user adds a 10 and reading whether any attack has a 10 fails
- **THEN** the safety card appears

#### Scenario: The same 10 saved later
- **WHEN** a first 10 failed to save, the person frees space, and adds the 10 again, and it saves
- **THEN** the safety card appears again, because the stored history now has its first 10
- **AND** "Saved." replaces "Couldn't save"

#### Scenario: A failed 10 does not count as saved
- **WHEN** a first 10 failed to save and the user adds another 10 that also fails
- **THEN** the safety card appears again

#### Scenario: The attack is gone
- **WHEN** the user adds a 10 and the attack no longer exists
- **THEN** the screen says so, and no card appears

#### Scenario: TalkBack hears both
- **WHEN** TalkBack is on and a first 10 fails to save
- **THEN** it speaks the card's title and warning sentence first, and then the message, each in full
- **AND** the card stays until "Got it", and the message stays until it is closed, a later save succeeds, or the user leaves the
  screen

#### Scenario: TalkBack reads the new words
- **WHEN** TalkBack is on, the user has scrolled down to Pain, and adds a first 10 that saves
- **THEN** once the card is in view, TalkBack speaks "Sudden, extremely bad pain?" as a heading and then "Call your local emergency
  number now if this is extremely bad pain that came on suddenly." in full
- **AND** accessibility focus stays where it was

#### Scenario: The card stays whole at 200% font
- **WHEN** the font is 200%, the screen is 320 dp wide or in landscape, the user has scrolled down to Pain, and a first 10 fails to
  save
- **THEN** the card scrolls into view as before: its top, with the title, is at the top of the content area, just below the
  message, and no part of the card is under the message
- **AND** the title, the whole sentence and "Got it" are all shown with no line clipped or shortened; when the card is taller
  than the area, the rest is reached by scrolling, as D167 (1) allows

#### Scenario: Rotation
- **WHEN** the phone rotates with the card and the message showing
- **THEN** both are still there, and the message does not cover the card

#### Scenario: During an attack, offline, on Free
- **WHEN** a first 10 is added during an attack in the dark room, with no network, by a Free user
- **THEN** the card shows the same title and sentence on the dark theme, with nothing locked behind Premium

#### Scenario: Upgraded with a 10 already saved
- **WHEN** the app is updated from an earlier version and a saved attack already has a 10
- **THEN** no card appears when the user adds another 10, and the Warning signs page names the same sign in the same words
