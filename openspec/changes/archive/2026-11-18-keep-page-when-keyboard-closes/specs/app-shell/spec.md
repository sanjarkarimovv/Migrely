## MODIFIED Requirements

### Requirement: Closing the keyboard and other keyboards
When the keyboard closes, the page SHALL NOT move or scroll to the field, and focus SHALL stay in the field. Near the page's
end the area the keyboard leaves SHALL show the page's own background until the person scrolls back (the keyboard room).
With a hardware or floating keyboard, split screen or a full-screen editor, Migrely SHALL follow Android's inset and add
nothing. "Remove animations" SHALL make every scroll a jump. Nothing SHALL be announced (BRD EC-UX-43, D180, D189).

#### Scenario: The keyboard closes
- **WHEN** the keyboard is open on a field and the user closes it with Back, the keyboard's hide key or Done
- **THEN** the page does not scroll back and does not settle down: its scroll position is the same, and near the page's end
  the keyboard room shows below the content instead
- **AND** the first Back closes only the keyboard, and the second leaves the screen

#### Scenario: Closing after scrolling away
- **WHEN** the keyboard is open on the note, or on Edit medication's name, and the user scrolls the page away from the field,
  then closes the keyboard, with or without "Remove animations"
- **THEN** the page stays where the user left it and does not scroll back to the field

#### Scenario: The keyboard grows after scrolling away
- **WHEN** the user has scrolled the field's line out of view and the keyboard then grows, as when it reopens or changes height
- **THEN** the page does not scroll back to the field, and the next tap, edit or cursor move in the field brings its line into
  view again

#### Scenario: The strip the keyboard uncovers
- **WHEN** the keyboard slides away in the dark theme, in the light theme, or during an attack, on API 36 or API 26
- **THEN** the uncovered strip, the keyboard room included, has the page's own colour at every frame, never a lighter, dark or
  white band

#### Scenario: Rotation with the keyboard open
- **WHEN** the phone rotates portrait to landscape to portrait while the keyboard is open on the note, at 200% font
- **THEN** the note keeps its text and cursor, and if the keyboard opens again its cursor line shows 8 dp above it

#### Scenario: Background and process death
- **WHEN** the app goes to the background with the keyboard open, and its activity is recreated or its process is restored
  later
- **THEN** on return no keyboard room is left at the bottom, and the note keeps what was typed

#### Scenario: A hardware keyboard
- **WHEN** a hardware keyboard is used and no on-screen keyboard shows
- **THEN** no space is kept for a keyboard, Tab gives one stop into the note with the cursor at the end, and the cursor line
  stays inside the window above the navigation bar as the user types or moves with arrow keys

#### Scenario: A floating keyboard or split screen
- **WHEN** the keyboard floats, or the app runs in split screen, and Android gives no bottom keyboard inset
- **THEN** Migrely adds no space of its own, no keyboard room ever appears, and the screen works as without a keyboard

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

## ADDED Requirements

### Requirement: Closing the keyboard never moves the page
On Attack details (note), Edit medication (name, dose), Log medication (name) and Delete all data (the word), when the keyboard
closes or gets shorter, the page's scroll position SHALL stay the same to the pixel, on every API level, with or without
animations. If less than the visible part's growth lies below it, room of the difference SHALL appear after the last item;
otherwise none (BRD EC-UX-61, EC-UX-65, D189 (1), (7)).

#### Scenario: Near the end on each screen
- **WHEN** a field is focused near the page's end (less than a keyboard's height of content below the visible part) on any of
  the four screens, at 100% font in portrait on API 36, and the user presses Back once
- **THEN** the field's bounds are the same before and after the keyboard is gone (within 1 px), and the keyboard room fills
  the area the keyboard left

#### Scenario: API 26
- **WHEN** the same steps run on Android 8, where the keyboard inset changes in one step
- **THEN** the field's bounds are the same before and after, and the page does not jump down

#### Scenario: Far from the end
- **WHEN** the note is focused mid-page with more than a keyboard's height of content below the visible part, and the user
  presses Back
- **THEN** no keyboard room appears, nothing on screen moves, and hidden content is uncovered in place

#### Scenario: 200% font in portrait and landscape
- **WHEN** the same steps run at 200% font in portrait and in landscape on windows 411 dp and 360 dp tall
- **THEN** the field's bounds are the same, the room is never taller than the keyboard was, and the field, its input line and
  its supporting line stay fully visible

#### Scenario: Remove animations
- **WHEN** "Remove animations" is on and the keyboard closes near the page's end on Attack details or Delete all data
- **THEN** the keyboard goes in one frame and the field's bounds are the same; no frame shows the page lower

#### Scenario: Free, Premium and first use
- **WHEN** the screen is opened by a Free or a Premium user, or Log medication shows its first-use form, and the keyboard has
  not closed yet on this visit
- **THEN** there is no keyboard room, and after a close the same rules apply to both

#### Scenario: Without the room
- **WHEN** the near-the-end steps run on v1.3.0, or on the candidate with the room removed from the screen under test
- **THEN** the field moves down by more than 0 px, which shows the check can fail

### Requirement: The keyboard room looks like the page
The keyboard room SHALL be the screen's own background only: the theme background, or the dark room background when the
screen shows in attack mode. It SHALL have no divider, shadow, gradient, end marker or other colour, at any frame of the
keyboard's animation, and SHALL sit below the bottom padding the page already has (BRD EC-UX-61, EC-UX-65, D189 (2)).

#### Scenario: Dark and light theme
- **WHEN** the keyboard closes near the page's end on Attack details and on Log medication, in the dark theme and in the light
  theme
- **THEN** every pixel of the room equals the page background sampled at the top of the same screenshot, and none is brighter
  than it; in the light theme the room is the light page background, not white and not a surface tint

#### Scenario: Log medication from attack mode
- **WHEN** Log medication is opened from attack mode, the first-use Name field is focused near the page's end, and the keyboard
  closes
- **THEN** every pixel of the room equals `MigrelyColors.AttackModeBackground`, and the screen only gets darker as the light
  keyboard goes

#### Scenario: The colour check can fail
- **WHEN** the same pixel check runs on the keyboard area of the keyboard-open screenshot
- **THEN** it fails, which shows the check can tell the keyboard from the page

### Requirement: The keyboard room goes only with the person's scroll
The keyboard room SHALL shrink only as the person scrolls up (finger, fling, TalkBack's scroll action, Tab or Shift+Tab, Page
Up, mouse wheel), in step with that scroll, or as the keyboard reopens or grows, and SHALL never grow back from scrolling or be
animated away. Content that grows SHALL use the room up. The page SHALL NOT move because of the room (BRD EC-UX-62, D189 (3)).

#### Scenario: Scrolling back up
- **WHEN** the room shows and the user swipes up slowly by about a third of it, then by more than it, then scrolls to the end
- **THEN** the field moves with the finger and the room shrinks by the same amount; after the second swipe there is no room,
  and at the end the page ends with its normal bottom padding

#### Scenario: TalkBack, Tab and Page Up scroll the room away
- **WHEN** the room shows and the page is scrolled up by TalkBack's scroll action, by Tab or Shift+Tab bringing a control
  into view, by Page Up or by a mouse wheel
- **THEN** the room shrinks by the scrolled amount, as with a finger

#### Scenario: The keyboard reopens
- **WHEN** the room shows and the user taps the field again
- **THEN** the keyboard opens, the room shrinks as the visible part shrinks, the page does not jump, and the cursor line is at
  least 8 dp above the keyboard (EC-UX-39), with single-line fields as U1 says

#### Scenario: The keyboard changes height while open
- **WHEN** the keyboard gets shorter (a suggestion strip goes, another layout or keyboard) or taller while open on a field
- **THEN** a shorter keyboard adds room as a close does, and a taller one uses room first and then follows D180 (2)

#### Scenario: Content changes while the room shows
- **WHEN** the room shows and a chip is added, an error line appears, or Log medication's form is replaced after "Add and log"
- **THEN** the grown content uses the room up and the page does not move because of the room

#### Scenario: A message while the room shows
- **WHEN** the room shows on Attack details and a message appears, or the keyboard closes while a message shows
- **THEN** the message's position equals its position with no room, and it does not cover Back (D180 (4))

#### Scenario: A room that never shrinks
- **WHEN** the scrolling rows run on a mutation build whose room never shrinks
- **THEN** the room shows again at the page's end, which shows the check can fail

### Requirement: The keyboard room is not kept
The keyboard room SHALL NOT be saved. After rotation, process death, or leaving the screen and coming back, the screen SHALL
show no room, at its saved scroll position or the nearest one the page allows. Background and return without recreation SHALL
keep the room as it was. With no bottom keyboard inset, no room SHALL ever appear (BRD EC-UX-63, D189 (4), (5)).

#### Scenario: Rotation and process death with the room
- **WHEN** the room shows on Attack details or Edit medication and the phone rotates and back, or the app goes to the
  background, its process is killed and it returns from recents
- **THEN** there is no room after each, the note or form keeps its text, and nothing is cut off

#### Scenario: Leaving and coming back
- **WHEN** the room shows and the user leaves the screen with Back, or opens a forward screen and returns
- **THEN** the screen shows no room, and TalkBack starts where Android and D178 put it on any screen entry

#### Scenario: Background without recreation
- **WHEN** the room shows and the app goes to the background and returns with its activity kept
- **THEN** the room is as it was; if the keyboard reopens because the field still has focus, the reopen rule applies

#### Scenario: No keyboard inset
- **WHEN** a hardware keyboard is used on Attack details, or a floating keyboard or split screen gives no bottom keyboard inset
- **THEN** no room appears and nothing changes on close; a case QA cannot reach is recorded as not reachable, not a pass

### Requirement: The keyboard room is invisible to assistive technology
The keyboard room SHALL have no semantics, SHALL NOT be focusable or a Tab stop, and a tap in it SHALL do nothing. Closing the
keyboard SHALL send no scroll event from the page, SHALL keep input and TalkBack focus on the field, and SHALL announce nothing
new: no string, live region or state change (BRD EC-UX-61, D189 (6)).

#### Scenario: TalkBack stays on the field
- **WHEN** on each of the four screens, on API 36 with TalkBack on the field near the page's end at 100% font in portrait, the
  user presses Back once
- **THEN** within 3 s TalkBack says only the field itself or "keyboard hidden", and one more double tap reopens the keyboard
  in that field with no chip, radio or kind changed in the record

#### Scenario: Other ways to close
- **WHEN** on Attack details and on one single-line screen, with TalkBack on, the keyboard is closed with its hide key, then
  with Done where the field has it
- **THEN** TalkBack stays on the field as with Back

#### Scenario: No scroll event
- **WHEN** an accessibility event log runs across Back from the near-the-end set-up on each screen
- **THEN** it shows no TYPE_VIEW_SCROLLED from the page's scroll container within 3 s

#### Scenario: Nothing to tap in the room
- **WHEN** after the close a layout dump is read at the place where the keyboard's hide key was on Attack details, and that
  place is tapped
- **THEN** there is no clickable or focusable node there, and the tap changes nothing: no focus cleared, no keyboard, no ripple

#### Scenario: TalkBack walk past the room
- **WHEN** at 200% font in landscape on Attack details, after the close, the user swipes forward from the note and then back
  several items
- **THEN** forward reaches "Delete migraine" and then the end of the page, never the room, and back walks in D178 order with
  no jump

#### Scenario: The focus check can fail
- **WHEN** the TalkBack and scroll-event steps run on v1.3.0, or on the mutation build with the room removed
- **THEN** on Attack details TalkBack's focus moves to "Not selected. Stress" and the log shows a scroll event; on a screen
  where v1.3.0 shows no jump, the row proves only that no jump was observed

### Requirement: Delete all data keeps the field still
On Delete all data, closing the keyboard SHALL NOT move the confirmation field. Only the Delete row SHALL move with the keyboard
as D180 (8) defines, and after Done or Back the field and "Delete everything" SHALL show together. With the page's end in view
the divider above the row SHALL stay hidden (BRD EC-UX-64, D180 (8), D189 (8)).

#### Scenario: The row rides above the keyboard
- **WHEN** at 100% font in portrait the word is typed, with the Delete row above the keyboard, and the user taps Done
- **THEN** the field's bounds are the same, the field and "Delete everything" show together, and no divider shows above the row

#### Scenario: The row stays behind the keyboard
- **WHEN** at 200% font in landscape on a window 360 dp tall the word is typed, with the Delete row behind the keyboard, and the
  user taps Done
- **THEN** the field's bounds are the same and the field and "Delete everything" show together

#### Scenario: Delete all data without the room
- **WHEN** the same steps run on the candidate with the room removed from Delete all data, and on v1.3.0
- **THEN** the field moves down on the mutation build, which shows the check can fail; v1.3.0's result is recorded as it is,
  and a v1.3.0 field that does not move proves nothing either way
