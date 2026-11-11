## ADDED Requirements

### Requirement: The safety card comes into view when it appears
When the first-10 safety card appears and is not fully inside the visible content area, the content SHALL scroll back to
its top, where the card is the first item, so the card sits at its unscrolled place. The scroll SHALL ease over 300 ms, or jump
with "Remove animations", and a touch SHALL NOT stop it. Accessibility focus SHALL NOT move. A card restored by a rotation or a
process SHALL NOT scroll (BRD EC-ATK-23, D167, #174).

#### Scenario: Scrolled down to Pain, a first 10 that saves
- **WHEN** the user has scrolled until Pain's Add is on screen and the card's place is above the view, at 100% or 200% font,
  and adds a first 10 that saves
- **THEN** within 1 second the content is back at its top, and the card's title, sentence and "Got it" show

#### Scenario: Scrolled down, a first 10 that fails to save
- **WHEN** the same, and the save fails because storage is full
- **THEN** within 1 second the content is back at its top, and the card's sentence and "Got it" show
- **AND** the message covers no more of the card than in 1.1.0: the title at 100% (#166), and never the first line of the
  sentence (#174)

#### Scenario: Landscape and a card taller than the area
- **WHEN** the phone is in landscape, at 100% or 200% font, and the card appears out of view, taller than the area or not
- **THEN** the content is back at its top, the card's top shows, and the rest can be scrolled

#### Scenario: The card is already in view
- **WHEN** the card appears fully inside the visible content area
- **THEN** nothing scrolls

#### Scenario: Remove animations
- **WHEN** "Remove animations" is on and the card appears out of view
- **THEN** the content jumps to it, with no frames in between

#### Scenario: A touch during the scroll
- **WHEN** the user touches or drags the content during the 300 ms scroll
- **THEN** the scroll still ends with the card in view, and the next drag scrolls as usual

#### Scenario: A restored card
- **WHEN** the phone rotates or the process is restored while the card shows
- **THEN** the content keeps the place the user scrolled to

#### Scenario: Card text
- **WHEN** the card shows
- **THEN** this change leaves its title, sentence and "Got it" as they are (their words are set by "First maximum pain safety
  card", BRD D173)

### Requirement: TalkBack speaks the card once it is in view
When the card scrolls into view, TalkBack SHALL speak its title and its sentence, each in full, once the card is in view,
without moving accessibility focus. When a message lies over the title, as on a failed save at 100% font, the sentence SHALL
still be spoken in full (BRD EC-ATK-23, D167, D163).

#### Scenario: A first 10 that saves, scrolled down
- **WHEN** TalkBack is on, the user has scrolled down to Pain, and adds a first 10 that saves
- **THEN** TalkBack speaks the card's title and sentence in full

#### Scenario: A first 10 that fails, scrolled down, at 200%
- **WHEN** TalkBack is on at 200% font, scrolled down, and a first 10 fails to save
- **THEN** TalkBack speaks the card's title and sentence in full, and the message

#### Scenario: Focus
- **WHEN** TalkBack's focus is on Pain's Add and the card scrolls into view
- **THEN** focus stays where it was

#### Scenario: Without this change
- **WHEN** the same rows run on a build without the scroll, as 1.1.0
- **THEN** the card stays out of view and TalkBack does not speak its title, which shows the check can fail
