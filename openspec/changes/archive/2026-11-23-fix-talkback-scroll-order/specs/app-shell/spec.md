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
- **WHEN** the walks run with Remove animations, right to left, on API 26 and API 36, in light, dark and dark room, in Free
  and Premium, and in attack mode
- **THEN** each walk reaches both ends with every item once

#### Scenario: A message over the top of the page
- **WHEN** a message shows over the top of a page (D180 (4)) and the user swipes through the page with TalkBack
- **THEN** the walk reaches the message, with its Dismiss, and every item it does not cover, each once; items under the
  message are out of the tree while it shows, as on `develop`; after the message goes, the walk reaches both ends with every
  item once

#### Scenario: Rotation or process death during a walk
- **WHEN** the phone is rotated, or the process is killed and restored, while TalkBack is partway down a page
- **THEN** TalkBack starts where Android and D178 put it, and the walk from there reaches both ends with every item once

### Requirement: An item outside the visible part is not a stop
An item wholly outside a scroll area's visible part SHALL NOT be in the accessibility tree, and no node in the tree SHALL
have empty bounds in the window. This SHALL hold when the visible part ends above the window's bottom: with the keyboard up,
above the navigation bar, above a dialog's buttons, and under a message or the top bar. A picker left out of exact bounds
is a recorded exception (BRD EC-UX-78, D193 (3a), (4)).

#### Scenario: Just below the visible edge
- **WHEN** a chip lies 1 to 24 dp below the visible part's bottom edge, with the keyboard up or above the navigation bar
- **THEN** the chip is not in the accessibility tree, and it is not placed after the top bar in TalkBack's order

#### Scenario: Just above the visible edge
- **WHEN** an item lies 1 to 24 dp above the visible part's top edge after a scroll
- **THEN** it is not in the accessibility tree

#### Scenario: A dialog at 200%
- **WHEN** a scrolling dialog, such as an import dialog on Your data, is open at 200% font and its content scrolls
- **THEN** no item hidden behind or above its buttons is a stop, and TalkBack walks the dialog with every item once

### Requirement: TalkBack's scroll keeps its place
When TalkBack, or Switch Access through the same action, scrolls a page, one step SHALL move it by floor(V/2) px, where V is
the part of the scroll area actually visible: its height minus any top bar, message, keyboard, navigation bar or dialog
buttons over it. Less than a step left scrolls to the end. While the gap to the next stop is under V/2 the focused item SHALL
stay on screen; in every case the next stop SHALL be the next item in order (BRD EC-UX-79, D193 (5)).

#### Scenario: Swiping back past the top edge (v1.3.1 skipped a block)
- **WHEN** TalkBack is on the "Stress" chip at the top of the visible part on Attack details and the user swipes previous
- **THEN** the next stops are "Possible triggers", "Add your own symptom" and each symptom chip in order, and "Symptoms" comes
  only after them; the focused item stays on screen

#### Scenario: Swiping on past the bottom edge
- **WHEN** TalkBack is on the last item above the visible part's bottom edge, the gap to the next stop is under V/2, and the
  user swipes next
- **THEN** the page scrolls by one step, the next stop is the item just below, and the focused item stays on screen

#### Scenario: A gap of half the visible part or more
- **WHEN** at 200% landscape in a 411 dp tall window with the keyboard up on Attack details (V 137 px, step 68 px), or on a
  page with a large block between stops, TalkBack crosses a gap of V/2 or more
- **THEN** the next stop is still the next item in order, with no skip and no loop; the focused item may leave the screen,
  and the test asserts the order, not that the item stayed

#### Scenario: One scroll action
- **WHEN** TalkBack's or Switch Access's scroll forward or backward action runs once on a scrolling page
- **THEN** the page moves by floor(V/2) px within 1 px, with V measured from layout, and not by its whole visible height

#### Scenario: An inset inside the scroll
- **WHEN** a page puts an inset or a keyboard room inside its scroll content, so the scroll's viewport is taller than what
  shows
- **THEN** the step is half of what shows, not half of the viewport

### Requirement: Every stop says its name
Every item TalkBack stops on SHALL say its visible words with its state, also when cut by the visible edge, never a state
or role alone. Chips SHALL carry `spokenLabel ?: label` on their own node with the inner Text's semantics cleared, only with
exact bounds. Other stops SHALL get their words on their own node only where the cut sweep shows them losing their name,
and each such stop SHALL pass the D181 one-name check (BRD EC-UX-80, D181, D193 (5)).

#### Scenario: A chip at the edge
- **WHEN** a symptom chip is cut by the visible edge and TalkBack stops on it
- **THEN** it says "Not selected. Nausea", or "Selected. Nausea" when chosen, as a whole chip does, and never only "Not
  selected"

#### Scenario: The cut sweep
- **WHEN** for each page and dialog, each stop is scrolled so that 1 px to 12 dp of it shows at the top edge and then at the
  bottom edge, and its name is read from the unmerged tree as TalkBack builds it
- **THEN** every stop's name is its full visible words; a stop whose name comes back empty or partial fails the sweep

#### Scenario: Only where the sweep finds it
- **WHEN** a stop keeps its name in the cut sweep
- **THEN** its semantics are not changed

#### Scenario: One name after the change
- **WHEN** a chip or a stop found by the sweep is given its words on its own node
- **THEN** TalkBack says its words once, not twice, and the spoken name contains the visible words in the same order

#### Scenario: The sweep can fail
- **WHEN** a chip without its own name runs the cut sweep with exact bounds, or the sweep reads the merged tree
- **THEN** the unmerged sweep is red for that chip, which is why the merged read is not used

#### Scenario: Voice Access by name
- **WHEN** a voice command names a chip's visible text
- **THEN** it matches that chip, as before this change

### Requirement: The traversal fix draws nothing new
The fix SHALL change only semantics, touch nodes and TalkBack's scroll step. Every screen SHALL look the same to the
pixel at rest and pressed. Inside exact bounds every clickable, toggleable or selectable node SHALL itself be at least 48×48
dp (64 dp for attack mode's intensity choices, "Migraine ended", Undo and Dismiss), or its whole cell pitch where cells sit
closer, from its own size, never from touch-target widening (BRD EC-UX-82, EC-UX-89, D193 (2), (4)).

#### Scenario: Screens look the same
- **WHEN** screenshots of each changed page are taken at 100% portrait and 200% landscape, before and after the change, at
  rest and with a control pressed
- **THEN** they are identical to the pixel; the pressed state is compared on a device, because Robolectric draws no ripple

#### Scenario: TalkBack's outline shows the touch area
- **WHEN** TalkBack focuses an in-page icon button or a History day
- **THEN** its focus outline is the 48 dp node or the day's whole pitch, where it was the 40 dp circle or the drawn cell;
  this is the one intended visible change, drawn by TalkBack and only while focused

#### Scenario: In-page icon buttons
- **WHEN** a tap lands 2 dp outside an in-page icon button's 40 dp circle but inside 48 dp, inside a scrolling page
- **THEN** the button acts, as it did before the change; its circle, icon and ripple look as Material's do

#### Scenario: Controls outside exact bounds
- **WHEN** the audit looks at top-bar Back and actions, the Paywall's close row above its scroll, the message host, and the
  checkbox, switch and radio rows
- **THEN** the first three stay Material and outside exact bounds, and each toggle row is itself the node of 48 dp or more,
  so none of them changes

#### Scenario: A control smaller than 48 dp
- **WHEN** the touch-target audit, at 360 and 411 dp wide and at 100% and 200% font, finds a clickable control under its bar
  on a page
- **THEN** either its own node grows to its bar with no visual change and is measured again, or the page is left out of
  the bounds part, listed in `design.md` and D193, and a follow-up issue is filed for it at close-out

#### Scenario: History calendar days at 360 dp
- **WHEN** History's month calendar is shown 360 dp wide at 200% font, where a day cell is about 41.7 dp with 6 dp gaps
- **THEN** each day's click, focus and semantics sit on a slot of its whole cell pitch (47 to 48 dp), a tap in a gap opens
  the nearer day, and the drawn cell, its clip and its keyboard focus ring are identical to the pixel

#### Scenario: The Material pickers stay out
- **WHEN** the date picker, the date-time picker or Reminders' time dialog is open, whose mode toggle, month arrows,
  month/year button, AM/PM and time-step Cancel and OK are 40 dp tall
- **THEN** it is outside exact bounds and keeps the half step, D193 records it as a residual, and a follow-up issue is
  filed for these pickers at close-out

#### Scenario: Attack mode's secondary buttons
- **WHEN** the audit measures Details, the move-start options and the long-check buttons in attack mode
- **THEN** they pass at 48 dp and are not enlarged, and attack mode is not left out because of them

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

### Requirement: Exact bounds cover only the scroll content
Exact bounds SHALL wrap only the content inside a scroll area. The top bar, the message host with its Undo and Dismiss,
bottom bars and dialog buttons SHALL stay outside it, with the system's touch target and its widening (BRD EC-UX-82, D193
(3a)).

#### Scenario: Outside the scroll
- **WHEN** a test reads the minimum touch target in the top bar, the message host and a dialog's buttons on a changed page
- **THEN** it is the system's 48 dp, and only nodes inside the scroll content read zero

#### Scenario: Wrapped too wide
- **WHEN** a build wraps a whole screen, as the prototype wrapped Attack details' content box, instead of the scroll content
- **THEN** that test fails

### Requirement: One entry point for every vertical scroll
Every vertical scroll in the app's main code SHALL go through one `designsystem` entry point that applies both parts, or
the step alone for a picker left out of exact bounds, so no screen keeps its own copy. The one other raw `verticalScroll(`
SHALL be the typing space's `scrollingPage` (D194), called only by the entry point's typing-space overload. Any other raw
`verticalScroll(` or `scrollingPage(` call in main code SHALL fail a check in the gate (BRD EC-UX-90, D193 (1), (3), (4)).

#### Scenario: A new scrolling screen
- **WHEN** a build adds `Modifier.verticalScroll(...)` directly in a feature's main code
- **THEN** the source check fails and names the file and line

#### Scenario: All sites covered
- **WHEN** the source check runs on the candidate
- **THEN** it passes, and the entry point is used at all 29 sites of `develop` f70b6450 (23 page, 6 dialog)

#### Scenario: A typing screen keeps the keyboard follow
- **WHEN** Attack details, Log medication, Edit medication or Delete all data scrolls
- **THEN** it goes through the entry point's typing-space overload, which sets TalkBack's step and then applies the typing
  space's `scrollingPage` on the same scroll state, so #259's keyboard follow (D194) is unchanged

#### Scenario: A way around the entry point
- **WHEN** a build calls `scrollingPage(` from any file but the entry point, or adds a second raw `verticalScroll(` in the
  typing space
- **THEN** the source check fails and names the file

### Requirement: The traversal check can fail
Each traversal and scroll-step test SHALL be shown red on `develop` f70b6450 before the fix, with a case that passes there,
and removing either part SHALL turn its tests red again. A Compose change to the test-only traversal data SHALL fail loudly,
never pass silently (BRD EC-UX-90, D193 (7)).

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
