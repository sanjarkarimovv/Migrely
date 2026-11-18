## ADDED Requirements

### Requirement: The line being typed always shows
On Attack details (note), Edit medication (name, dose), Log medication (name) and Delete all data (the word), while the
keyboard is up, the line being typed SHALL be fully visible. If the visible part H under the top bar is at least the line
height L plus 8 dp above and below, nothing changes. If H < L, the top bar SHALL step aside while the keyboard is up, and the
line SHALL show with 8 dp on both sides where that fits (BRD EC-UX-72, EC-UX-77, D192).

#### Scenario: Attack details in the 360 dp window (the issue)
- **WHEN** on API 36 with Gboard, at 200% font in landscape in a window 360 dp tall, the user types 9 lines in the note, with
  the cursor at the end, then at the start, then in the middle
- **THEN** the cursor line shows whole, 112 of 112 px, with at least 8 dp on both sides, Back and the title are absent from
  the layout dump, and the status bar is unchanged

#### Scenario: Single-line fields in the 360 dp window
- **WHEN** the same window is used for Edit medication's name and dose, Log medication's name and Delete all data's word
- **THEN** the input line shows whole, the supporting line too where it fits (U1 order), and the top bar steps aside only if
  the input line did not fit under it

#### Scenario: Delete all data decides the row first
- **WHEN** the keyboard opens on Delete all data in the 360 dp window
- **THEN** the Delete row is decided first as D180 (8) says, and the top bar steps aside only if the input line still does not
  fit

#### Scenario: Log medication from attack mode
- **WHEN** Log medication was opened from attack mode in the 360 dp window and the user types a first medication's name
- **THEN** the same happens, and the status bar and the area the bar leaves are the dark room background with no lighter
  frame

#### Scenario: The 411 dp window keeps the bar
- **WHEN** on API 36 at 200% landscape in a window 411 dp tall, the user types in the note (H 137 px, L 112 px)
- **THEN** the top bar stays at its usual bounds and the line shows whole, with the margins as today (4 and 21 px by cursor
  direction), not evened out

#### Scenario: API 26 keeps the bar
- **WHEN** on API 26 with the AOSP keyboard at 200% landscape, in windows 411 dp and 360 dp tall, the user types in the note
  and the single-line fields
- **THEN** the top bar stays, and the line shows whole with at least 8 dp on both sides, as on v1.3.1

#### Scenario: Portrait keeps the bar
- **WHEN** the user types on any of the four screens at 100% or 200% font in portrait
- **THEN** the top bar never steps aside, and EC-UX-39 and EC-UX-40 hold as before

#### Scenario: A window too small even without the bar
- **WHEN** the line would not fit even with the top bar aside
- **THEN** the app does nothing beyond stepping the bar aside, and the result is recorded as "window too small", never as a
  pass

#### Scenario: Free and Premium, light and dark
- **WHEN** the same steps run on Free and on Premium, in the light and the dark theme
- **THEN** the result is the same

### Requirement: Margins give way before the line is cut
When the line height L fits the visible part H but L plus 8 dp above and below does not, the line SHALL stay whole and the
margins SHALL take what is left, down to 0 dp. The page SHALL move only as far as the line needs to stay whole, never to even
out the margins, and the top bar SHALL stay. This requirement ships only if a test shows the line cut on v1.3.1 in that band
(BRD EC-UX-73, D192).

#### Scenario: A window in the band
- **WHEN** on API 36 with Gboard at 200% landscape, in a window where 112 ≤ H < 133 px (about 401 dp for Attack details,
  about 384 dp for Edit medication), the user types with the cursor at the end, then at the start
- **THEN** the line shows whole, 112 of 112 px, the top bar stays, and the margins are what is left

#### Scenario: v1.3.1 in the band
- **WHEN** the same steps run on v1.3.1 in the same window
- **THEN** the line is cut by up to 21 px at the keyboard edge (end) or the bar edge (start); if it is whole there, this
  requirement is dropped from the release

#### Scenario: No evening out
- **WHEN** a build evens out the two margins in the 411 dp window
- **THEN** the line moves by about 8 px while typing, and the check for "margins as today" fails

### Requirement: The top bar's decision holds while the keyboard is up
The top bar SHALL step aside when H < L, with H from the keyboard's final height and L the real height of the line being typed
from layout, never a dp or sp constant. It SHALL be decided once per keyboard opening and SHALL NOT change while the keyboard
stays up. A new opening, a rotation or a font change SHALL decide again. With no bottom keyboard inset it SHALL never step
aside (BRD EC-UX-74, EC-UX-77, D192).

#### Scenario: Moving and typing while the bar is aside
- **WHEN** the bar is aside and the user moves the cursor up with arrow keys or the handle, types 10 more lines, or scrolls the
  page by hand
- **THEN** the bar does not come back or flicker, and the cursor line keeps at least 8 dp from the status bar when it moves up

#### Scenario: The keyboard changes height
- **WHEN** the bar is aside and the user switches Gboard to symbols or to emoji
- **THEN** the bar stays aside

#### Scenario: A message while the bar is aside
- **WHEN** the bar is aside and a message appears
- **THEN** the bar stays aside

#### Scenario: Rotation with the keyboard up
- **WHEN** the bar is aside and the phone rotates to portrait and back to landscape with the keyboard up
- **THEN** in portrait the bar shows, and back in landscape the decision is made again

#### Scenario: A trigger that is a constant
- **WHEN** a build sets the trigger to a constant 64 dp and runs on API 26 at 360 dp (H 156 px)
- **THEN** the bar leaves there, which v1.3.1 and the candidate never do, and the guard row fails

#### Scenario: Hardware or floating keyboard
- **WHEN** a hardware keyboard, a floating keyboard or a split screen gives no bottom keyboard inset
- **THEN** the bar never steps aside; a case QA cannot reach is recorded as not reachable, not a pass

### Requirement: The top bar moves with the keyboard
The top bar SHALL leave and come back following the keyboard's own inset animation, in the same frames, with no curve of the
app's own, in one step on API 26 to 29 and with "Remove animations". It SHALL NOT fade through another colour, SHALL NOT be
drawn half over the page's text, and the area it leaves SHALL be the page's own background (BRD EC-UX-76, D192).

#### Scenario: Animations on
- **WHEN** a 60 fps recording on API 36 runs across the keyboard opening and closing in the 360 dp window
- **THEN** the bar moves in the same frames as the keyboard and no frame shows a lighter colour

#### Scenario: Remove animations and API 26
- **WHEN** "Remove animations" is on, or the device is on API 26 to 29
- **THEN** the bar goes and comes back in one step, with no frame showing it half drawn over text

#### Scenario: Right to left
- **WHEN** the steps run in a right-to-left language
- **THEN** Back is mirrored when the bar returns

### Requirement: Closing the keyboard brings the top bar back without a scroll
When the keyboard closes while the bar is aside (Back, the hide key, Done, focus leaving, a dialog), the bar SHALL come back
in the same frames. The scroll position SHALL stay the same: the page moves down by the bar's height and the keyboard room
fills the rest. No scroll event SHALL be sent, TalkBack's focus SHALL stay on the field, and nothing SHALL be announced (BRD
EC-UX-75, D189, D192).

#### Scenario: Back closes the keyboard
- **WHEN** the bar is aside in the 360 dp window and the user presses Back once
- **THEN** the keyboard closes, the bar is back at the bounds it had before typing, the line being typed is still on screen,
  lower by the bar's height, and a second Back leaves the screen

#### Scenario: Hide key and Done
- **WHEN** the keyboard is closed with its hide key, or with Done on a single-line field
- **THEN** the same happens as with Back

#### Scenario: TalkBack stays on the field
- **WHEN** TalkBack is on (VERBOSE log) and the keyboard closes while the bar is aside
- **THEN** within 3 s TalkBack says only the field or "keyboard hidden", stays on the field, and zero TYPE_VIEW_SCROLLED come
  from the page

#### Scenario: After a drag to the page's end
- **WHEN** the user dragged the page to its end while typing with the bar aside, and then closes the keyboard
- **THEN** the scroll position is the same, the room fills the rest, and no scroll event is sent

#### Scenario: A close that scrolls
- **WHEN** a build keeps the line's screen position on close by scrolling
- **THEN** a scroll event is sent, and the TalkBack row fails

#### Scenario: The keyboard-room guards
- **WHEN** the keyboard-room tests of #238 run on the candidate
- **THEN** they all pass unchanged, except that a case with the bar aside expects the field lower by exactly the bar's height

### Requirement: An aside top bar is gone for assistive technology and messages
While the bar is aside, its nodes SHALL be removed from the accessibility tree, not merely hidden, so TalkBack, Switch Access
and Tab never land on it, and nothing SHALL be announced when it leaves or returns. A message SHALL keep its D180 (4)
coordinates to the pixel: the bar never moves it, and a message never brings the bar back (BRD EC-UX-76, D192).

#### Scenario: TalkBack swipe while the bar is aside
- **WHEN** the bar is aside and the user swipes back from the field with TalkBack
- **THEN** TalkBack reaches the content above the field and then the top of the page, never Back or the title

#### Scenario: After closing
- **WHEN** the keyboard is closed and the user swipes back again
- **THEN** Back is reachable in D178 order

#### Scenario: Tab and Switch Access
- **WHEN** the bar is aside and the user presses Shift+Tab from the field, or scans with Switch Access
- **THEN** no stop lands on Back, the title or a bar action

#### Scenario: A message with the bar aside
- **WHEN** a message shows and the keyboard opens and closes in the 360 dp window
- **THEN** the message's bounds are identical with and without the keyboard and the bar

#### Scenario: A message moved by the bar
- **WHEN** a build offsets the message by the bar's height
- **THEN** the message row fails

## MODIFIED Requirements

### Requirement: The window does not pan for the keyboard
`MainActivity` SHALL declare `adjustResize`. With the on-screen keyboard open on any screen, the window SHALL NOT pan: the top
bar and Back SHALL stay where they are, never scroll away, and answer a first tap, at every font size and orientation. The
one exception is a top bar that steps aside while the line being typed cannot fit (requirement "The line being typed always
shows"). Nothing SHALL draw under the status bar (BRD EC-UX-40, D180, D192).

#### Scenario: The note on Attack details
- **WHEN** the user opens a migraine's details, scrolls to Note and taps it, at 100% or 200% font, in portrait or landscape
- **THEN** the keyboard opens, the top bar with Back keeps its place and size, and a first tap on Back leaves the screen
- **AND** no trigger chip or other content draws under the status bar

#### Scenario: The tightest window
- **WHEN** the keyboard is open on the note at 200% font in landscape on a window 360 dp tall
- **THEN** where the line being typed fits under the top bar (API 26, 156 px), the top bar with Back stays whole and fixed;
  where it does not (API 36 with Gboard, 61 px for a 112 px line), the top bar steps aside while the keyboard is up and the
  window still does not pan; nothing draws under the status bar in either case

#### Scenario: TalkBack with the keyboard open
- **WHEN** TalkBack is on and the keyboard is open on the note
- **THEN** swiping back from the note reaches the content above it, then the message if one shows, then Back, and activating
  Back leaves the screen

#### Scenario: The merged manifest
- **WHEN** the debug and the release build are assembled
- **THEN** `MainActivity`'s merged manifest entry declares `adjustResize`, and the permission report equals 1.2.1's

#### Scenario: The checks can fail
- **WHEN** the note row runs on develop ea41869a on API 36, which has no `adjustResize` and pans
- **THEN** Back's bounds are `[0,0][0,0]` with the keyboard open, as in #83, which shows the row can fail
- **AND** a test of `MainActivity`'s window fails without `adjustResize`, and on Android 8 the copy rows that pass on this
  change stay stale on develop ea41869a

### Requirement: Closing the keyboard never moves the page
On Attack details (note), Edit medication (name, dose), Log medication (name) and Delete all data (the word), when the keyboard
closes or gets shorter, the scroll position SHALL stay the same to the pixel, on every API level and animation setting.
If less than the visible part's growth lies below it, room of the difference SHALL appear after the last item. A top bar that
was aside SHALL return and move the page down by its height, with no scroll (BRD EC-UX-61, EC-UX-65, EC-UX-75, D189, D192).

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
- **THEN** the field's bounds are the same, or, where the top bar had stepped aside (API 36 with Gboard at 360 dp), lower by
  exactly the bar's height; the scroll position is the same, the room is never taller than the keyboard was, and the field,
  its input line and its supporting line stay fully visible

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
