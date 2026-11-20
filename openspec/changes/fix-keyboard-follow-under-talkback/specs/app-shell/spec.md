## ADDED Requirements

### Requirement: Opening a field with TalkBack keeps focus on it
On Attack details (note), Log medication (name), Edit medication (name, dose) and Delete all data (the word), when TalkBack
or another accessibility service opens a text field, accessibility focus SHALL stay on that field while the keyboard opens.
No other control SHALL gain accessibility focus or be activated without the person, and no stored value SHALL change
without the person's own action (BRD EC-UX-83, D194).

#### Scenario: The note in the 360 dp window (the issue)
- **WHEN** TalkBack is on, on API 36 with Gboard at 200% font in landscape in a window 360 dp tall, and the user opens
  Attack details' note by double tap
- **THEN** within 3 s TalkBack's focus is still on the note and on no radio, chip or heading, and the line being typed shows
  112 of 112 px once the keyboard has settled
- **AND** the top bar has stepped aside as D192 says

#### Scenario: A recorded impact stays recorded
- **WHEN** a migraine has "Impact on your day" recorded as 2, TalkBack opens its note in the 360 dp window, and the user
  double taps once more
- **THEN** the double tap lands in the note, and the database still holds impact 2 for that migraine

#### Scenario: An empty impact stays empty
- **WHEN** the same steps run on a migraine with no impact recorded
- **THEN** the database still holds no impact for that migraine

#### Scenario: The other three screens
- **WHEN** TalkBack opens Log medication's name (in the usual and the dark room form), Edit medication's name and dose, and
  Delete all data's word, in the 360 dp and the 411 dp window
- **THEN** focus stays on the field, and no intake, medication or deletion is written by the next double tap

#### Scenario: The 411 dp window
- **WHEN** TalkBack opens the note in a window 411 dp tall, where the top bar stays
- **THEN** focus stays on the note and the line shows whole

#### Scenario: Remove animations and API 26
- **WHEN** TalkBack opens the note with "Remove animations" on, or on API 26 with the AOSP keyboard, at 360 dp and 411 dp
- **THEN** focus stays on the note and the database is unchanged

#### Scenario: The checks can fail
- **WHEN** the note row runs in the 360 dp window on a build without this change (v1.3.2)
- **THEN** TalkBack's focus moves to an impact choice, which shows the row can fail
- **AND** on v1.3.1 the same row keeps focus on the note, as before D192

### Requirement: The line being typed comes into view with the keyboard
Inside a screen's typing space, the line being typed SHALL come into view while the keyboard opens, in the same frames as
the keyboard: never further in a frame than the keyboard's top moved plus 1 px, only upward, and still once the keyboard has
settled, from any start position. Its rest position SHALL equal the one before this change within 1 px, TalkBack on or off
(BRD EC-UX-84, EC-UX-87, D194).

#### Scenario: Frames while the keyboard opens
- **WHEN** a 60 fps recording on API 36 with Gboard at 200% landscape in the 360 dp window covers the note's keyboard
  opening, with TalkBack off and then on
- **THEN** in each frame the cursor line moved up no further than the keyboard's top did plus 1 px, counting the bar's step
  in the frame where it steps aside, no frame moves it down, and no frame moves it twice

#### Scenario: No tail
- **WHEN** the same recording is read at the keyboard's last frame and 500 ms later
- **THEN** the two frames are identical

#### Scenario: No settle from the page's end
- **WHEN** the note starts wholly in view at the page's end, taller than the space the keyboard leaves, and the keyboard opens
  with TalkBack on, then with "Remove animations"
- **THEN** the page lands where the system's own scroll to a focused field would put it, so the keyboard's last frame and
  the frame 500 ms later are identical, and the cursor line shows 112 of 112 px

#### Scenario: No catch-up leap
- **WHEN** frames are skipped while the keyboard opens
- **THEN** the line never moves more than the keyboard's top moved across the same frames, plus 1 px

#### Scenario: The rest position is unchanged
- **WHEN** the final frame is compared with develop's in each #245 cell, at 411 dp (margins 4 and 21 px by cursor
  direction), at 360 dp, and in the margins band where QA reaches it
- **THEN** the line's position is the same within 1 px, and at 360 dp it shows 112 of 112 px with TalkBack off

#### Scenario: Remove animations and API 26 to 29
- **WHEN** "Remove animations" is on, or the device is on API 26 to 29
- **THEN** the page moves in one step with the keyboard, and at most one frame shows the line hidden

#### Scenario: The page's end
- **WHEN** the keyboard opens on the note at the bottom of Attack details or on Delete all data's word, in the dark theme
- **THEN** no overscroll stretch or glow shows

#### Scenario: Dark backgrounds
- **WHEN** the keyboard opens on Log medication opened from attack mode (dark room) in the 360 dp window
- **THEN** no frame shows a lighter colour

#### Scenario: Free, Premium, light theme and right to left
- **WHEN** the same steps run on Free and Premium, in the light theme and in a right-to-left language
- **THEN** the result is the same

#### Scenario: The motion checks can fail
- **WHEN** the recording runs on develop, whose follow is an animated bring
- **THEN** the line moves after the keyboard's last frame, which shows the no-tail check can fail

### Requirement: Only the person ends the keyboard follow
A focused field SHALL follow the keyboard from the start of its own bring into view. A layout made by the app or by Compose
SHALL NOT end the following; a scroll by the person while the keyboard holds still, or a drag that stops the field's bring,
SHALL end it. A keyboard that shrinks or closes SHALL never scroll the page. A field outside a typing space SHALL keep its
earlier bring (BRD EC-UX-85, EC-UX-86, D194).

#### Scenario: Compose's own layout before the follow
- **WHEN** a layout at the keyboard's new height runs before the follow has acted on that height, as Compose's semantics check
  does under an accessibility service
- **THEN** the following goes on and the line is brought into view, and on the next frames too

#### Scenario: A keyboard that comes during the field's bring
- **WHEN** the note is focused and the keyboard starts opening while the page is still bringing the note into view
- **THEN** the note keeps following, and its line shows in every frame once it has shown

#### Scenario: A drag during the field's bring
- **WHEN** the user drags the page while the field's bring runs, moving the line out of view, and the keyboard then grows
- **THEN** the drag stops the bring and ends the following, and the page is not pulled back to the line

#### Scenario: A scroll away still ends it
- **WHEN** the user scrolls the page away from the line while the keyboard holds still, and the keyboard then grows
- **THEN** the page is not pulled back to the line
- **AND** if the field was scrolled wholly out of view it stays where it is; if some of it still shows and the keyboard would
  leave less than its line, the page holds what showed (requirement "The keyboard never hides a focused field that shows"),
  and still does not pull it to its line

#### Scenario: The keyboard changes height while typing
- **WHEN** the line is followed and Gboard's suggestion strip appears, or the user switches to symbols or emoji
- **THEN** a taller keyboard moves the line up with it, a shorter one scrolls nothing, and focus stays on the field

#### Scenario: Rotation with the keyboard up
- **WHEN** the phone rotates with the keyboard up and TalkBack on
- **THEN** the bar's decision is made again as D192 says, the line shows, and focus stays on the field

#### Scenario: Closing the keyboard
- **WHEN** the keyboard closes by Back, the hide key or Done, with TalkBack on and off
- **THEN** zero TYPE_VIEW_SCROLLED come from the page, the scroll position moves 0 px (or by the bar's height where it was
  aside), and TalkBack's focus stays on the field

#### Scenario: No bottom keyboard inset
- **WHEN** a hardware keyboard, a floating keyboard or a split screen gives no bottom keyboard inset
- **THEN** the page does not move for the keyboard and focus stays on the field; a case QA cannot reach is recorded as not
  reachable, not as a pass

#### Scenario: A field outside a typing space
- **WHEN** a following field is placed outside any typing space and the keyboard grows
- **THEN** its line is brought into view as before this change

#### Scenario: Moving between fields with the keyboard up
- **WHEN** the keyboard is up and focus moves from Edit medication's name to its dose
- **THEN** the dose is brought into view as before this change

### Requirement: The keyboard never hides a focused field that shows
When a keyboard frame would leave a focused field that is not following the keyboard, and showed at least 1 px, with less
than its line, the page SHALL hold the lesser of its line with its clearance and what showed, in that same frame, for every
user. In each such frame the line SHALL move no further than the keyboard's and the bar's step plus 1 px, and not at all
after the keyboard stops (BRD EC-UX-88, D194).

#### Scenario: Reopening the keyboard with TalkBack (the reopen path)
- **WHEN** TalkBack is on in the 360 dp window, the note is focused, Back closed the keyboard, the page was scrolled so the
  note's top is near the bottom edge, and a double tap reopens the keyboard without moving the cursor
- **THEN** in every frame at least the lesser of the line with its clearance (up to 8 dp each side) and what showed before
  stays visible, TalkBack's focus stays on
  the note, and the next double tap writes nothing

#### Scenario: Each held frame keeps to the keyboard
- **WHEN** the same reopening is recorded frame by frame
- **THEN** each held frame moves the line by at most the keyboard's step plus the bar's step plus 1 px, and 0 px once the
  keyboard has stopped

#### Scenario: Remove animations and API 26
- **WHEN** the same reopening runs with "Remove animations" on, or on API 26
- **THEN** the hold is one step with the keyboard, and the note stays in the accessibility tree

#### Scenario: Later growth
- **WHEN** the user scrolled the note partly out of view with the keyboard up, and then switches Gboard to symbols, which is
  taller
- **THEN** the page holds what showed, up to the line, and does not pull the note to its line

#### Scenario: A field scrolled wholly away
- **WHEN** the note was scrolled wholly out of view before the keyboard grows
- **THEN** the page does not move for it

#### Scenario: The first typed character after a hold
- **WHEN** the note was held with its caret line cut, and the user types one character
- **THEN** the typing follow (D180 (2), unchanged) brings the whole caret line in one direction, settles within 15 frames,
  leaves it cut by 0 px, and focus and data are unchanged; its peak speed per frame is logged, not capped
- **AND** with "Remove animations" that bring is one step, of up to about 430 px

#### Scenario: Typing, then a double tap (device row K1)
- **WHEN** TalkBack is on, the caret is at the end of 4 lines, 60 px of the note shows, the keyboard reopens, the user types
  one character and then double taps
- **THEN** focus stays on the note, nothing is written, and the caret line shows whole

#### Scenario: Until the first keystroke (a known limit)
- **WHEN** the keyboard reopens on a focused note whose cursor did not move, and the note is held
- **THEN** the caret line may stay cut until the first keystroke, while the note stays visible and focused

#### Scenario: Ordinary openings are unchanged
- **WHEN** a field is opened by a tap or a double tap, so it follows the keyboard
- **THEN** no hold applies, and the requirement "The line being typed comes into view with the keyboard" governs every frame

#### Scenario: The checks can fail
- **WHEN** the reopen path runs on develop
- **THEN** the note is wholly hidden, TalkBack's focus moves, and the next double tap writes, which shows the row can fail
- **AND** a build that brings the line instead of holding (rejected: its rescue frame moved 158 to 494 px against a 62 px
  keyboard step) fails the per-frame row
