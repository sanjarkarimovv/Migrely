## ADDED Requirements

### Requirement: The window does not pan for the keyboard
`MainActivity` SHALL declare `adjustResize`. With the on-screen keyboard open on any screen, the window SHALL NOT pan: the top
bar and Back SHALL stay where they are, never collapse or scroll away, and answer a first tap, at every font size and
orientation. Nothing SHALL draw under the status bar (BRD EC-UX-40, D180).

#### Scenario: The note on Attack details
- **WHEN** the user opens a migraine's details, scrolls to Note and taps it, at 100% or 200% font, in portrait or landscape
- **THEN** the keyboard opens, the top bar with Back keeps its place and size, and a first tap on Back leaves the screen
- **AND** no trigger chip or other content draws under the status bar

#### Scenario: The tightest window
- **WHEN** the keyboard is open on the note at 200% font in landscape on a window 360 dp tall
- **THEN** the top bar with Back stays whole and fixed, and the content gets the lines that remain between it and the keyboard

#### Scenario: TalkBack with the keyboard open
- **WHEN** TalkBack is on and the keyboard is open on the note
- **THEN** swiping back from the note reaches the content above it, then the message if one shows, then Back, and activating
  Back leaves the screen

#### Scenario: The merged manifest
- **WHEN** the debug and the release build are assembled
- **THEN** `MainActivity`'s merged manifest entry declares `adjustResize`, and the permission report equals 1.2.1's

#### Scenario: Without the change
- **WHEN** the same note row runs on a build of the same commit without `adjustResize`
- **THEN** Back's bounds are empty or off screen as in #83, which shows the check can fail

### Requirement: A focused field stays above the keyboard
On Attack details, Edit medication and Log medication the scroll's visible part SHALL end at the keyboard's top. A focused
single-line field SHALL show its box and its supporting line when they fit; if not, its input line first, then the supporting
line, then the label. Controls below SHALL be reachable by scrolling (BRD EC-UX-40, D180).

#### Scenario: Edit medication at 200% in landscape
- **WHEN** the user taps the name or the dose field on Edit medication, at 200% font in landscape
- **THEN** the field's input line shows at least 8 dp above the keyboard, its supporting line too when it fits, and scrolling
  down reaches Save while the keyboard stays open

#### Scenario: A medication error while typing
- **WHEN** a name or dose error appears in the field's supporting line while the keyboard is open
- **THEN** the input line keeps priority, the error line shows if it fits, and it shows again when the keyboard closes

#### Scenario: Log medication's name field
- **WHEN** the user types a name on Log medication, at 100% or 200% font, in portrait or landscape, on API 36 or API 26
- **THEN** the input line shows above the keyboard, and the buttons below it can be scrolled to

#### Scenario: Without the change on Edit medication
- **WHEN** the Edit medication row runs on a build with `adjustResize` and with the keyboard padding still inside the scroll
- **THEN** the field or Save can end under the keyboard, which shows the check can fail

### Requirement: Delete all data with the keyboard open
On Delete all data the Delete row SHALL ride above the keyboard only while the input line plus 8 dp still shows above it.
Otherwise the row SHALL stay at the window's bottom, behind the keyboard, and Done or Back shows the field and the row together.
This SHALL be decided once per keyboard opening, from the keyboard's final height (BRD EC-UX-40, D180).

#### Scenario: A window with room
- **WHEN** the user types in the confirmation field at 100% or 200% font in portrait, or at 200% in landscape on a 411 dp tall
  window where the input line plus 8 dp fits above the Delete row
- **THEN** the input line and the Delete row both show above the keyboard

#### Scenario: A short window
- **WHEN** the user types in the confirmation field at 200% font in landscape on a window 360 dp tall, where the input line
  would not show above the Delete row
- **THEN** the input line shows above the keyboard, the Delete row stays behind it, and after Done the field and "Delete
  everything" show together

#### Scenario: No jump while the keyboard moves
- **WHEN** the keyboard animates open or closed on Delete all data
- **THEN** the Delete row takes its place once, from the keyboard's final height, and does not jump midway

#### Scenario: API 26
- **WHEN** the user types in the confirmation field on Android 8, with no text toolbar shown
- **THEN** the input line shows above the keyboard, and the Delete row follows the same rule, never left under the keyboard
  with the field hidden

#### Scenario: TalkBack order
- **WHEN** TalkBack is on at 100% font in portrait on Delete all data
- **THEN** it reads the warning, then the field, then "Delete everything"

### Requirement: A screen without a text field ignores the keyboard
Home, History, Insights, Settings and its pages, the paywall, Doctor report, My medications, Your data, Reminders and
onboarding SHALL pad only for the system bars and the display cutout, never for the keyboard. One shared inset SHALL serve them
and the message's fit rule (BRD EC-UX-42, D180).

#### Scenario: A keyboard inset reaches Home
- **WHEN** a test gives Home Today and History a keyboard inset of 250 dp
- **THEN** their layout is the same as with no keyboard: Home shows every day cell, and History's list ends at the same place

#### Scenario: Back from the note with the keyboard open
- **WHEN** the keyboard is open on the note and the user presses Back until Home or History shows, at 200% font, in portrait
  or landscape
- **THEN** Home shows its whole month and History its whole list, with no blank lower part

#### Scenario: Android 8 after a copy
- **WHEN** on Android 8 the user long-presses a word in the note, in an Edit medication field or in the Delete all data field,
  taps COPY in the toolbar and presses Back until Home shows, then opens History
- **THEN** Home shows its day cells and History its whole list, with no blank lower half, and no restart is needed (#145)

#### Scenario: Android 8 after a copy, back on a field screen
- **WHEN** on Android 8 the user copies from a field's toolbar, closes the keyboard and stays on that screen or opens another
  screen with a field
- **THEN** the screen keeps no space for a keyboard that is closed

#### Scenario: Without the change on Android 8
- **WHEN** the copy rows run on the build of develop ea41869a
- **THEN** Home loses the keyboard's height and History ends early, as QA measured for #145, which shows the check can fail

#### Scenario: Short window on Home
- **WHEN** the keyboard state changes while Home Today is in a short window
- **THEN** Home keeps the same layout choice, because the choice reads the window's height and not the keyboard

### Requirement: Closing the keyboard and other keyboards
When the keyboard closes, no blank space SHALL stay, the page SHALL stay where it is, and focus SHALL stay in the field. With a
hardware or floating keyboard, split screen or a full-screen editor, Migrely SHALL follow Android's inset and add nothing.
"Remove animations" SHALL make every scroll a jump. Nothing SHALL be announced (BRD EC-UX-43, D180).

#### Scenario: The keyboard closes
- **WHEN** the keyboard is open on a field and the user closes it with Back, the keyboard's hide key or Done
- **THEN** the page does not scroll back, it settles down by at most the keyboard's height if it was scrolled to its end, and
  no blank space is left above the navigation bar
- **AND** the first Back closes only the keyboard, and the second leaves the screen

#### Scenario: The strip the keyboard uncovers
- **WHEN** the keyboard slides away in the dark theme, in the light theme, or during an attack, on API 36 or API 26
- **THEN** the uncovered strip has the page's own colour, never a dark or white band

#### Scenario: Rotation with the keyboard open
- **WHEN** the phone rotates portrait to landscape to portrait while the keyboard is open on the note, at 200% font
- **THEN** the note keeps its text and cursor, and if the keyboard opens again its cursor line shows 8 dp above it

#### Scenario: Background and process death
- **WHEN** the app goes to the background with the keyboard open, or its process is restored later
- **THEN** on return no blank space is left at the bottom, and the note keeps what was typed

#### Scenario: A hardware keyboard
- **WHEN** a hardware keyboard is used and no on-screen keyboard shows
- **THEN** no space is kept for a keyboard, Tab gives one stop into the note with the cursor at the end, and the cursor line
  stays inside the window above the navigation bar as the user types or moves with arrow keys

#### Scenario: A floating keyboard or split screen
- **WHEN** the keyboard floats, or the app runs in split screen, and Android gives no bottom keyboard inset
- **THEN** Migrely adds no space of its own, and the screen works as without a keyboard

#### Scenario: The keyboard's full-screen editor
- **WHEN** a keyboard opens its own full-screen editor in landscape
- **THEN** Migrely adds nothing, and on return the note shows the text typed there; if QA cannot reach this editor, the row is
  recorded as not reachable, not as a pass

#### Scenario: Remove animations
- **WHEN** "Remove animations" is on and the keyboard opens on a field, or the cursor moves under it
- **THEN** the content moves to show the line with no frames in between

#### Scenario: Right to left
- **WHEN** the layout is right to left and the keyboard is open on a field
- **THEN** the same rules hold, mirrored, and nothing draws under the status bar

#### Scenario: TalkBack while the keyboard opens and closes
- **WHEN** TalkBack is on and the keyboard opens or closes on the note, or the page scrolls for the cursor
- **THEN** nothing extra is announced, accessibility focus stays on the note, and the reading order is D178's: top bar,
  message, content
