## ADDED Requirements

### Requirement: TalkBack reaches every item on a scrolling page once
On every vertically scrolling page and dialog, swiping next or previous with TalkBack SHALL reach each focusable item exactly
once in D178 order, SHALL end at the top bar's Back (the page's top while the bar is aside) and at the last item, and SHALL
never loop or skip a block, at any font size, in both orientations, with the keyboard up or down. A page left out under the
touch-target rule is recorded (BRD EC-UX-78, EC-UX-79, EC-UX-81, D193).

#### Scenario: Swiping back from the note (the issue)
- **WHEN** TalkBack is on at 200% font in landscape in a window 360 dp tall, on Attack details of a past migraine with
  Severe pain and Nausea, focus is on the note, the keyboard is closed with Back, and the user swipes previous 40 times
- **THEN** focus moves up the page through every item once, in order, and reaches Back; no item is heard twice

#### Scenario: Portrait at 100% with no keyboard
- **WHEN** TalkBack is on at 100% font in portrait on a 411 dp wide phone with a navigation bar, no keyboard, and the user
  swipes previous from "Delete migraine" on Attack details
- **THEN** focus reaches Back with every item once, and swiping next from Back reaches "Delete migraine" with every item once

#### Scenario: Keyboard up
- **WHEN** the keyboard is up on the note at 100% portrait and at 200% landscape, and the user swipes previous from the note
- **THEN** focus reaches the top of the page with every item once and named, and no item below the keyboard is a stop

#### Scenario: Every scrolling page and dialog
- **WHEN** the same walks run on Add past attack, Today, attack mode, History, Insights, Report, Medications, Edit
  medication, Log medication, Your data and its two import dialogs, Reminders and its dialog, Settings, each Settings page,
  Delete all data, Paywall, Onboarding, and the date and date-time pickers, at 100% portrait and 200% landscape
- **THEN** each walk reaches both ends with every item once, or the page is listed as left out under the touch-target rule

#### Scenario: A page that does not scroll
- **WHEN** a page's content fits the window, so nothing scrolls
- **THEN** TalkBack's order and stops are the same as on the build before this change

#### Scenario: The top bar aside
- **WHEN** the top bar is aside on Attack details in the 360 dp window (D192) and the user swipes previous from the note
- **THEN** focus reaches the top of the page with every item once and never Back; after the keyboard closes, swiping previous
  reaches Back

#### Scenario: Conditions that must not change the result
- **WHEN** the walks run with a message showing, with Remove animations, right to left, on API 26 and API 36, in light,
  dark and dark room, in Free and Premium, and in attack mode
- **THEN** each walk reaches both ends with every item once

#### Scenario: Rotation or process death during a walk
- **WHEN** the phone is rotated, or the process is killed and restored, while TalkBack is partway down a page
- **THEN** TalkBack starts where Android and D178 put it, and the walk from there reaches both ends with every item once

### Requirement: An item outside the visible part is not a stop
An item wholly outside a scroll area's visible part SHALL NOT be in the accessibility tree, and no node in the tree SHALL
have empty bounds in the window. This SHALL hold when the visible part ends above the window's bottom: with the keyboard up,
above the navigation bar, above a dialog's buttons, and under a message or the top bar (BRD EC-UX-78, D193 (3a)).

#### Scenario: Just below the visible edge
- **WHEN** a chip lies 1 to 24 dp below the visible part's bottom edge, with the keyboard up or above the navigation bar
- **THEN** the chip is not in the accessibility tree, and it is not placed after the top bar in TalkBack's order

#### Scenario: Just above the visible edge
- **WHEN** an item lies 1 to 24 dp above the visible part's top edge after a scroll
- **THEN** it is not in the accessibility tree

#### Scenario: A dialog at 200%
- **WHEN** a scrolling dialog, such as an import dialog on Your data, is open at 200% font and its content scrolls
- **THEN** no item hidden behind or above its buttons is a stop, and TalkBack walks the dialog with every item once

### Requirement: TalkBack's scroll keeps the focused item on screen
When TalkBack, or Switch Access through the same scroll action, scrolls a page, one step SHALL move it by the step
`ux-designer` sets (proposed: half the visible part), never a whole visible height. The focused item SHALL stay on screen,
and the next stop SHALL be the next item in order (BRD EC-UX-79, D193 (3b), (5)).

#### Scenario: Swiping back past the top edge (v1.3.1 skipped a block)
- **WHEN** TalkBack is on the "Stress" chip at the top of the visible part on Attack details and the user swipes previous
- **THEN** the next stops are "Possible triggers", "Add your own symptom" and each symptom chip in order, and "Symptoms" comes
  only after them; the focused item never leaves the screen

#### Scenario: Swiping on past the bottom edge
- **WHEN** TalkBack is on the last item above the visible part's bottom edge and the user swipes next
- **THEN** the page scrolls by one step, the next stop is the item just below, and the focused item never leaves the screen

#### Scenario: One scroll action
- **WHEN** TalkBack's or Switch Access's scroll forward or backward action runs once on a scrolling page
- **THEN** the page moves by the set step, within 1 px, and not by its whole visible height unless less than one step is left

### Requirement: Every stop says its name
Every item TalkBack stops on SHALL say its visible name with its state, including a chip cut by the visible edge. A stop
SHALL never say only a state such as "Not selected". The spoken name SHALL contain the visible words in the same order
(BRD EC-UX-80, D181, D193 (5)).

#### Scenario: A chip at the edge
- **WHEN** a symptom chip is cut by the visible edge and TalkBack stops on it
- **THEN** it says the chip's name with its state, for example "Not selected, Nausea", and never only "Not selected"

#### Scenario: Voice Access by name
- **WHEN** a voice command names a chip's visible text
- **THEN** it matches that chip, as before this change

### Requirement: The traversal fix draws nothing new
The fix SHALL change only the accessibility tree and TalkBack's scroll step. Every screen SHALL look the same to the pixel;
touch, fling, Tab, Page Up and Page Down SHALL act as before; every clickable control inside a page SHALL keep a touch target
of at least 48 dp, or 64 dp in attack mode, measured before the change lands on that page (BRD EC-UX-82, EC-UX-83, D193 (2),
(4)).

#### Scenario: Screens look the same
- **WHEN** screenshots of each changed page are taken at 100% portrait and 200% landscape, before and after the change
- **THEN** they are identical to the pixel

#### Scenario: A control smaller than 48 dp
- **WHEN** the touch-target audit finds a clickable control under 48 dp (64 dp in attack mode) on a page
- **THEN** either its touch area is made 48 dp (64 dp) with no visual change and measured again, or the page is left out of
  the bounds part, listed in `design.md` and D193, and #255 stays open for that page

#### Scenario: Keyboard and Switch Access
- **WHEN** the user moves through a changed page with Tab and Shift+Tab, or scans it with Switch Access
- **THEN** the order and the stops are the same as before, and each stop shows its focus indicator in view

#### Scenario: The keyboard guards
- **WHEN** the keyboard closes on the four screens with a text field, with TalkBack on the field (#238, #245)
- **THEN** no scroll event is sent, focus stays on the field, the keyboard room and the top bar behave as D189 and D192 say,
  and the keyboard-room and top-bar tests pass unchanged

#### Scenario: One announcement for a refused Save
- **WHEN** Save is refused for a field problem on Edit medication or Log medication with TalkBack on (#239)
- **THEN** the problem is announced once, as D191 says

### Requirement: One entry point for every vertical scroll
Every vertical scroll in the app's main code SHALL go through one `designsystem` entry point that applies both parts, so no
screen keeps its own copy. A raw `verticalScroll(` in main code outside that entry point SHALL fail a check in the gate
(BRD EC-UX-84, D193 (1), (3)).

#### Scenario: A new scrolling screen
- **WHEN** a build adds `Modifier.verticalScroll(...)` directly in a feature's main code
- **THEN** the source check fails and names the file and line

#### Scenario: All sites covered
- **WHEN** the source check runs on the candidate
- **THEN** it passes, and the entry point is used at all 29 sites of `develop` f70b6450 (23 page, 6 dialog)

### Requirement: The traversal check can fail
Each traversal and scroll-step test SHALL be shown red on `develop` f70b6450 before the fix, with a case that passes there,
and removing either part SHALL turn its tests red again. A Compose change to the test-only traversal data SHALL fail loudly,
never pass silently (BRD EC-UX-84, D193 (7)).

#### Scenario: Red before the fix
- **WHEN** the order and scroll tests run on `develop` f70b6450
- **THEN** the keyboard and landscape order tests, the navigation-bar case and the scroll tests fail with the wrong step named,
  and the portrait no-keyboard order test without an inset passes

#### Scenario: Each part is needed
- **WHEN** the bounds part is removed from the candidate, or the scroll step part is removed
- **THEN** the order tests, or the scroll tests, fail again

#### Scenario: Compose changes its test data
- **WHEN** a Compose upgrade stops exposing the traversal data the tests read
- **THEN** the tests fail with "only N items", not pass

#### Scenario: The device control
- **WHEN** QA runs the device walks on v1.3.1 and on `develop` f70b6450
- **THEN** the walk on Attack details loops or skips a block, so the same walk on the candidate can show the fix
