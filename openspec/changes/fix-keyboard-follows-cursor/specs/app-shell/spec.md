## ADDED Requirements

### Requirement: The window does not pan for the keyboard
`MainActivity` SHALL declare `adjustResize`. With the on-screen keyboard open on any screen, the window SHALL NOT pan: the top
bar and Back SHALL stay where they are and answer a first tap, and nothing SHALL draw under the status bar (BRD EC-UX-40, D180).

#### Scenario: The note on Attack details
- **WHEN** the user opens a migraine's details, scrolls to Note and taps it, at 100% or 200% font, in portrait or landscape
- **THEN** the keyboard opens, the top bar with Back keeps its place and size, and a first tap on Back leaves the screen
- **AND** no trigger chip or other content draws under the status bar

#### Scenario: TalkBack with the keyboard open
- **WHEN** TalkBack is on and the keyboard is open on the note
- **THEN** swiping back from the note reaches Back in the top bar, and activating it leaves the screen

#### Scenario: The merged manifest
- **WHEN** the debug and the release build are assembled
- **THEN** `MainActivity`'s merged manifest entry declares `adjustResize`, and the permission report equals 1.2.1's

#### Scenario: Without the change
- **WHEN** the same note row runs on a build of the same commit without `adjustResize`
- **THEN** Back's bounds are empty or off screen as in #83, which shows the check can fail

### Requirement: A focused field stays above the keyboard
On Attack details, Edit medication and Log medication, the scrolling area's visible part SHALL end at the keyboard's top, so the
focused field shows above the keyboard and each control below it can be scrolled to. On Delete all data the Delete row SHALL
stay above the keyboard (BRD EC-UX-40, D180).

#### Scenario: Edit medication at 200% in landscape
- **WHEN** the user taps the name or the dose field on Edit medication, at 200% font in landscape
- **THEN** the field and its cursor show above the keyboard, and scrolling down reaches Save while the keyboard stays open

#### Scenario: Log medication's name field
- **WHEN** the user types a name on Log medication, at 100% or 200% font, in portrait or landscape
- **THEN** the field and its cursor show above the keyboard, and the buttons below it can be scrolled to

#### Scenario: Delete all data
- **WHEN** the user types in the confirmation field on Delete all data, at 100% or 200% font, in portrait or landscape
- **THEN** the field and the Delete row both show above the keyboard

#### Scenario: Without the change on Edit medication
- **WHEN** the Edit medication row runs on a build with `adjustResize` and with the keyboard padding still inside the scroll
- **THEN** the field or Save can end under the keyboard, which shows the check can fail

### Requirement: A screen without a text field ignores the keyboard
Home, History, Insights, Settings and its pages, the paywall, Doctor report, My medications, Your data and Reminders SHALL pad
only for the system bars and the display cutout, never for the keyboard. One shared inset SHALL serve them and the message's fit
rule (BRD EC-UX-42, D180).

#### Scenario: A keyboard inset reaches Home
- **WHEN** a test gives Home Today and History a keyboard inset of 250 dp
- **THEN** their layout is the same as with no keyboard: Home shows every day cell, and History's list ends at the same place

#### Scenario: Back from the note with the keyboard open
- **WHEN** the keyboard is open on the note and the user presses Back until Home shows
- **THEN** Home shows its whole month, with no blank lower part

#### Scenario: Android 8 after a copy
- **WHEN** on Android 8 the user long-presses a word in the note, taps COPY in the toolbar and presses Back until Home shows
- **THEN** Home shows its day cells and History its whole list, with no blank lower half, and no restart is needed (#145)

#### Scenario: Short window on Home
- **WHEN** the keyboard state changes while Home Today is in a short window
- **THEN** Home keeps the same layout choice, because the choice reads the window's height and not the keyboard

### Requirement: Closing the keyboard and other keyboards
When the keyboard closes, for any reason, no blank space SHALL stay and the scroll position SHALL NOT jump. With a hardware
keyboard, a floating keyboard, split screen or a keyboard's own full-screen editor, Migrely SHALL follow the inset Android
gives and add nothing. With "Remove animations" any scroll SHALL jump (BRD EC-UX-43, D180).

#### Scenario: The keyboard closes
- **WHEN** the keyboard is open on a field and the user closes it with Back or the keyboard's own hide key
- **THEN** the content fills the screen down to the navigation bar again, and what was on screen stays on screen

#### Scenario: Rotation with the keyboard open
- **WHEN** the phone rotates while the keyboard is open on the note
- **THEN** the note keeps its text and cursor, and if the keyboard opens again its cursor line shows above it

#### Scenario: Background and process death
- **WHEN** the app goes to the background with the keyboard open, or its process is restored later
- **THEN** on return no blank space is left at the bottom, and the note keeps what was typed

#### Scenario: A hardware keyboard
- **WHEN** a hardware keyboard is used and no on-screen keyboard shows
- **THEN** no space is kept for a keyboard, and the cursor line stays inside the visible area as the user types

#### Scenario: A floating keyboard or split screen
- **WHEN** the keyboard floats, or the app runs in split screen, and Android gives no bottom keyboard inset
- **THEN** Migrely adds no space of its own, and the screen works as without a keyboard

#### Scenario: The keyboard's full-screen editor
- **WHEN** a keyboard opens its own full-screen editor in landscape
- **THEN** Migrely adds nothing, and on return the note shows the text typed there

#### Scenario: Remove animations
- **WHEN** "Remove animations" is on and the keyboard opens on a field
- **THEN** the content moves to show the field with no frames in between

#### Scenario: Right to left
- **WHEN** the layout is right to left and the keyboard is open on a field
- **THEN** the same rules hold, mirrored, and nothing draws under the status bar

#### Scenario: TalkBack while the keyboard opens and closes
- **WHEN** TalkBack is on and the keyboard opens or closes on the note
- **THEN** accessibility focus stays on the note, and the reading order is the one D178 set: top bar, message, content
