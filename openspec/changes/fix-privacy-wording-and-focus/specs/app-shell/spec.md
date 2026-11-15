## ADDED Requirements

### Requirement: One spoken name per control
A control SHALL have one spoken name. When its spoken label differs from its visible text, the visible text SHALL be left
out of its semantics, and the label SHALL contain the visible words in the same order. Role, action, focus, size and look
SHALL not change. Each site SHALL be proven with a failing test before it is fixed, and a site that does not read twice
SHALL be left alone (BRD EC-UX-45, D181).

#### Scenario: A message's Undo
- **WHEN** TalkBack focuses the Undo of "Migraine started" on any screen
- **THEN** it says "Undo the start, Button", once, and the message's text stays readable as before

#### Scenario: Attack mode
- **WHEN** TalkBack focuses "Details" or the "15 min" chip under "Started earlier?" in attack mode
- **THEN** it says "Details of this migraine, Button" or "Move the start 15 min earlier, Button", once each

#### Scenario: Reminders
- **WHEN** TalkBack focuses the visit's "Remove" or the notice's "Open settings" on Reminders
- **THEN** it says "Remove the next visit, Button" or "Open settings for notifications, Button", once each

#### Scenario: Label in name
- **WHEN** any control's spoken label differs from its visible text
- **THEN** the spoken label contains the visible words in the same order, so a voice command naming the visible text can
  match it

#### Scenario: The fix changes nothing else
- **WHEN** a fixed control is tapped, focused with Tab, or reached by Switch Access
- **THEN** it acts, looks and measures as before, and its touch target is still at least 48 dp, or 64 dp in attack mode

### Requirement: Every keyboard stop is visible and in view
Every Tab stop SHALL show a visible focus indicator and SHALL be scrolled fully into view, at every font size and in
both orientations. A node that cannot show one, such as a group, a scroll container, the legend or a spacer, SHALL never
be a stop. Shift+Tab SHALL be the exact reverse of Tab (BRD EC-UX-46, EC-UX-19, D181).

#### Scenario: No invisible stop
- **WHEN** the user moves through any screen with Tab and Shift+Tab on a hardware keyboard, at 100% and 200% font
- **THEN** each stop shows its focus indicator fully on screen, and no key press lands on a stop that shows nothing

#### Scenario: Reverse order
- **WHEN** the user presses Tab from one control and then Shift+Tab
- **THEN** focus is back on the control it started from

### Requirement: No label breaks inside a word
No label SHALL break inside a word, with or without a hyphen, at any font size or width. Where a label's longest word
does not fit, the layout SHALL change, for example by stacking, and the type SHALL NOT shrink: 200% stays 200%. Wrapping
at a space is allowed (BRD EC-UX-48, D181).

#### Scenario: Largest font size on a narrow phone
- **WHEN** the font size is 200% on a 360 dp phone
- **THEN** every label shows whole words, and none is cut, ellipsized or hyphenated
