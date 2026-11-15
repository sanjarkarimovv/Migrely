## ADDED Requirements

### Requirement: A message ignores the keyboard
Opening or closing the keyboard SHALL NOT move a message, change how it fits (D178 (6)), or let it cover the top bar or Back.
The keyboard MAY cover a message's lower part; the message SHALL stay dismissible. No keyboard padding SHALL apply to
`MessageLayout`, the message host or their ancestors, and a test SHALL fail when the host is padded (BRD EC-UX-41, D180).

#### Scenario: A message, then the keyboard
- **WHEN** a message shows on Attack details and the user taps the note, at 100% or 200% font, in portrait or landscape
- **THEN** the message stays where it was, to the pixel, below the top bar, and a first tap on Back leaves the screen

#### Scenario: The keyboard, then a message
- **WHEN** the keyboard is open on a field and a message appears, such as "Couldn't save. Your phone's storage is full."
- **THEN** it appears at the same place as with the keyboard closed, below the top bar, and does not cover Back

#### Scenario: The keyboard closes under a message
- **WHEN** a message shows and the keyboard closes
- **THEN** the message does not move

#### Scenario: The keyboard covers the message's button
- **WHEN** at 200% font in a short landscape window the keyboard covers a message's lower part, its Dismiss or Undo included
- **THEN** the message never lies over the keyboard, and a sideways swipe on its visible part, TalkBack's Dismiss or Undo action,
  or closing the keyboard each still clears or reaches it

#### Scenario: A test with a keyboard inset
- **WHEN** a test shows a message on Attack details and then gives the window a keyboard inset of 250 dp
- **THEN** the message's top and Back's bounds are the same as before the inset, and `MessageFitInsetsTest` still passes

#### Scenario: The host padded for the keyboard
- **WHEN** the same test runs with keyboard padding put on the message host as a mutation
- **THEN** the test fails, which shows it can fail; the mutation is reverted

#### Scenario: The safety card
- **WHEN** the safety card shows with a message and the keyboard closed
- **THEN** D165 holds as before: the content starts below the message, and the message covers no part of the card
