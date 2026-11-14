## ADDED Requirements

### Requirement: An error on the other screens goes when a later save succeeds
On Home, History, My medications, Edit medication, Log medication, Reminders, Doctor report, Your data and Add past migraine,
while an error shows, the next write there that succeeds SHALL replace it with that write's own message, or else with "Saved."
on Home, History and Reminders. On Doctor report a created report SHALL clear it with no message. It SHALL clear on success,
never on the attempt (BRD EC-UX-35, D178).

#### Scenario: Home Today, a day mark after freeing space
- **WHEN** marking today's period failed because storage was full, the user frees space without closing the message, and marks
  it again
- **THEN** within 1 second "Saved." replaces "Couldn't save", the mark is stored, TalkBack speaks "Saved.", and it leaves after
  8 seconds or the accessibility timeout

#### Scenario: Home in attack mode
- **WHEN** recording a pain level during an attack failed, and a later pain level, a start moved earlier or a long-attack answer
  saves
- **THEN** "Saved." replaces the error, dim like attack mode, with no haptic, and the attack-mode controls stay uncovered as before

#### Scenario: History, a day mark
- **WHEN** an error shows on History and a period or pain-free mark then saves
- **THEN** "Saved." replaces the error

#### Scenario: Reminders
- **WHEN** an error shows on Reminders and the daily reminder, its time, the effect reminder or the visit date then saves
- **THEN** "Saved." replaces the error

#### Scenario: Doctor report
- **WHEN** creating a report failed because storage was full, and a later attempt creates it
- **THEN** the error is gone, the share sheet opens, and no "Saved." shows, because the app saved nothing

#### Scenario: A write with its own message
- **WHEN** an error shows and the next successful write brings its own message, such as "Backup saved" on Your data, the archive
  message with "Undo" on My medications, the logged message with "Undo" on Log medication, "Migraine started" or "Undone."
- **THEN** that message replaces the error, and no "Saved." shows

#### Scenario: A write that closes the screen
- **WHEN** an error shows on Edit medication or Add past migraine and a later save succeeds
- **THEN** the screen closes as before, and the error does not show on the screen it returns to

#### Scenario: Nothing was written
- **WHEN** an error shows and an action succeeds without writing anything, such as a pain level after the attack was already
  ended or a visit date that did not change
- **THEN** no "Saved." shows and the error stays

#### Scenario: No error on screen
- **WHEN** a write saves on any of these screens and no error shows
- **THEN** no "Saved." shows

#### Scenario: Undo or information on screen
- **WHEN** a message with "Undo" or an information message shows and a later write saves
- **THEN** that message stays with the time it has left

#### Scenario: It fails again
- **WHEN** an error shows, storage is still full, and the user tries again
- **THEN** the error shows in the same place and TalkBack speaks it again, with no flash and no change of surface colour

#### Scenario: Two quick saves
- **WHEN** an error shows and two writes succeed one right after the other
- **THEN** one "Saved." shows, and the second write finds no error and adds nothing

#### Scenario: A late success after leaving
- **WHEN** a write succeeds after the user has already left the screen
- **THEN** nothing shows there, and on return no error and no "Saved." show

#### Scenario: Free and Premium
- **WHEN** the same steps run on Free and on Premium
- **THEN** the messages are the same

#### Scenario: Largest font and landscape
- **WHEN** "Saved." shows at 200% font, in portrait or landscape
- **THEN** its whole text is visible and its place follows the screen's usual rules

### Requirement: An error on the other screens belongs to the visit
When one of the screens named above stops being the current screen, by Back or by going forward, its error SHALL be cleared and
SHALL NOT show on return. Undo and information messages SHALL behave as before. A dialog, a picker, the system file picker, the
share sheet, the background, Android Settings, a rotation and a restored process SHALL NOT clear it (BRD EC-UX-35, D178).

#### Scenario: Forward and back
- **WHEN** an error shows on Home and the user opens Settings or a migraine's details and comes back, or an error shows on History
  and the user opens a migraine and comes back
- **THEN** no error shows

#### Scenario: Back and reopened
- **WHEN** an error shows on Reminders, the user goes back and opens Reminders again
- **THEN** no error shows

#### Scenario: Android Settings to free space
- **WHEN** an error shows, the user presses Home, frees space in Android Settings, and returns
- **THEN** the error still shows until a later save succeeds

#### Scenario: The system file picker
- **WHEN** an error shows on Your data and the user opens the system file picker and returns
- **THEN** the error still shows

#### Scenario: The share sheet
- **WHEN** an error shows on Doctor report and a share sheet opened by an earlier report is closed
- **THEN** the error still shows

#### Scenario: A dialog or a picker
- **WHEN** an error shows and the user opens a dialog, a date or time picker or a menu on the same screen
- **THEN** the error still shows

#### Scenario: Rotation and a restored process
- **WHEN** an error shows and the phone rotates, or the process is killed in the background and restored
- **THEN** the error still shows with no duration

#### Scenario: Undo across a trip
- **WHEN** a message with "Undo" shows and the user goes forward and comes back within its time
- **THEN** it behaves as before this change

#### Scenario: A message after the return
- **WHEN** a message is posted after the user has come back
- **THEN** it shows as usual

### Requirement: Keyboard, Switch Access and TalkBack reach a message right after the top bar
On every screen that shows messages, the keyboard focus order and TalkBack's order SHALL be: the top bar's controls, then the
message's text and its Dismiss or Undo, then the content. On a screen with no top bar the message SHALL come first. A message
SHALL NOT take focus when it appears. Its place, size, drawing over the content and tap guard SHALL NOT change (BRD EC-UX-36,
D178).

#### Scenario: Attack details with the safety card
- **WHEN** "Couldn't save" shows above the safety card on Attack details and the user presses Tab from the top with a hardware
  keyboard
- **THEN** the stops are Back, Dismiss, "Got it", then the content, and Dismiss closes the message

#### Scenario: A screen with top bar actions
- **WHEN** a message with "Undo" shows on History and the user presses Tab from the top
- **THEN** the stops are Back, the bar's actions, "Undo", then the month and its days

#### Scenario: Shift+Tab
- **WHEN** focus is on the content's first control and the user presses Shift+Tab
- **THEN** focus moves to the message's control, then to the top bar

#### Scenario: TalkBack
- **WHEN** TalkBack is on, a message shows, and the user swipes forward from the top bar's last control
- **THEN** TalkBack reads the message's text, then its Dismiss or Undo, then the first content item

#### Scenario: A screen with no top bar
- **WHEN** a message with "Undo" shows on Home or on onboarding and the user presses Tab from the top
- **THEN** the message's control is the first stop

#### Scenario: Switch Access
- **WHEN** Switch Access scans a screen with a message
- **THEN** it highlights the message's control in the same order, separately from the controls under it, and selecting it works

#### Scenario: A message appears while focus is elsewhere
- **WHEN** keyboard or TalkBack focus is on a content control and a message appears
- **THEN** focus stays on that control, and TalkBack speaks the message through its live region

#### Scenario: The focused message leaves
- **WHEN** focus is on a message's control and the message times out, is undone or is replaced
- **THEN** focus is not lost, the app does not crash, and the next Tab reaches a control on screen

#### Scenario: Large font, landscape and right to left
- **WHEN** the font is 200%, the phone is in landscape or the language is right to left, and a message shows with its action
  below its text
- **THEN** the order is the same: the top bar, the message's text, its control, then the content

#### Scenario: The message does not move
- **WHEN** a message shows on any of the 13 screens at 100% and 200% font
- **THEN** its bounds match the build before this change within 1 pixel, and a tap where it was within 0.8 s still reaches nothing
  under it

### Requirement: A message is never announced empty
On every screen, a message that appears or replaces another SHALL never be fully transparent in any frame while it is in the
accessibility tree, so TalkBack always speaks its text. A replacement SHALL switch in place with no cross-fade. A message that
arrives when none shows SHALL still slide in, looking as before, with no new flashing or faster motion (BRD EC-UX-37, D178).

#### Scenario: A failed retry is spoken again
- **WHEN** "Couldn't save" shows on Reminders, Home or History, storage is still full, and the user tries again after more than
  100 ms of quiet
- **THEN** TalkBack speaks the full text again, and no announcement is empty

#### Scenario: A message after a quiet moment
- **WHEN** no message shows, the screen has been still for more than 100 ms, and a message appears on any screen
- **THEN** TalkBack speaks its full text

#### Scenario: One message at a time
- **WHEN** a message replaces another on any screen
- **THEN** in no frame do two messages show, and the new one is never transparent

#### Scenario: Arrival looks as before
- **WHEN** a message arrives when none shows
- **THEN** it slides in within 250 ms as before, and no frame is visibly brighter than its neighbours

#### Scenario: Reduce motion
- **WHEN** "Remove animations" is on and a message appears or replaces another
- **THEN** it shows in its final place at once

## MODIFIED Requirements

### Requirement: Messages appear at the top and stay dim
Short messages SHALL appear at the top of the screen, inside the system bars and display cutouts, and never cover the large
controls. On a screen with a top bar the top edge SHALL sit below the bar; on the paywall, below the Close row; on Home Today,
below the Settings row, and in a short window over the start column. A message SHALL never be the brightest element: text no
brighter than `#C8CCD2`, on a dark surface with a thin outline (BRD EC-UX-01, EC-UX-16, EC-UX-20, EC-UX-38, D158, D178).

#### Scenario: Message during attack mode
- **WHEN** an attack is started and the "Migraine started" message appears
- **THEN** it is shown at the top of the screen, below the status bar
- **AND** the intensity choices and "Migraine ended" stay fully visible and usable

#### Scenario: Portrait attack mode keeps its place
- **WHEN** a message with "Undo" shows in portrait attack mode
- **THEN** it lies where it did before, over the rain card, which is accepted because that card is the screen's content and
  not a top-bar control

#### Scenario: Landscape with a display cutout
- **WHEN** a message appears while the phone is in landscape
- **THEN** the message stays clear of the status bar and the display cutout and is at most 560 dp wide

#### Scenario: Landscape normal mode
- **WHEN** a message appears in landscape with no attack in progress, on a screen other than Home Today
- **THEN** it is centred at the top of the screen, as in portrait, below the top bar

#### Scenario: Close on the paywall
- **WHEN** a message shows on the paywall, at 100% or 200% font, in portrait or landscape
- **THEN** Close is not covered, moved or guarded, and a first tap on it closes the paywall

#### Scenario: A screen with a top bar
- **WHEN** a message shows on a screen with a top bar, such as History, My medications or Your data
- **THEN** its top edge is below the bar, and Back and the bar's actions answer a first tap

#### Scenario: Home Today
- **WHEN** a message with "Undo" shows on Home Today in a window that is not short, such as portrait, with the date on one
  line and the screen not scrolled
- **THEN** its top edge is below the Settings row, and a tap on Settings opens Settings and does not undo

#### Scenario: Home Today in a short window
- **WHEN** a message with "Undo" shows on Home Today in a window 411 or 274 dp high, short enough to pick the two-column layout,
  at 100%, 130% or 200% font, with the screen not scrolled
- **THEN** it lies over the start column, below the Settings row, and ends before the column with "Migraine started"
- **AND** a tap anywhere on "Migraine started", its top edge included, starts a migraine

#### Scenario: Settings under a message in a short window
- **WHEN** a message with "Undo" shows on Home Today in a window 411 or 274 dp high, at 100%, 130% or 200% font, and the user
  taps Settings
- **THEN** Settings opens at the first tap, the tap does not fire "Undo", and Settings stays in TalkBack's tree while the message
  shows

#### Scenario: Short window, right to left
- **WHEN** the same message shows in a short window in a right-to-left language
- **THEN** it lies over the start column, which is on the right, and "Migraine started" and Settings stay uncovered

#### Scenario: A screen with no top bar
- **WHEN** a message shows on a screen with no top bar, such as onboarding
- **THEN** it lies where it did before

#### Scenario: Reduce motion
- **WHEN** reduce motion is on and a message shows on a screen with a top bar
- **THEN** it appears at once at the same place below the bar

### Requirement: Message durations by kind
Information messages, "something went wrong" and "Saved." SHALL stay for 8 seconds, extended by the accessibility timeout in
the same way. "Storage full" SHALL stay until the user dismisses it or a newer message replaces it, because it means the last
action was not saved. On Attack details and on the screens of D178 a later successful save or leaving the screen SHALL also
take it away (BRD EC-UX-16, EC-UX-34, EC-UX-35, EC-STO-01, D165, D178).

#### Scenario: Information message
- **WHEN** a start move is rejected
- **THEN** the message naming the previous attack's end stays for 8 seconds

#### Scenario: Storage full
- **WHEN** an action fails because storage is full on Home, History, a medication screen, Reminders, Doctor report, Your data or
  Add past migraine
- **THEN** the "storage full" message stays until the user closes it, swipes it away, a newer message replaces it, a later save
  succeeds, or the user leaves the screen

#### Scenario: Storage full on Attack details
- **WHEN** an action fails because storage is full on Attack details
- **THEN** the "storage full" message stays until the user closes it, a later save succeeds, or the user leaves the screen

#### Scenario: Saved
- **WHEN** "Saved." shows on any screen
- **THEN** it stays for 8 seconds, or longer if the accessibility timeout asks for it

### Requirement: Gentle motion and haptics for messages
A message that arrives when none shows SHALL slide and fade in within 250 ms. A message that leaves SHALL fade out within 200 ms,
and one that replaces another SHALL switch in place, with no flashing. With reduce motion on, a message SHALL appear and
disappear without animation. Error messages SHALL give one short "reject" haptic. Start and end keep their confirm haptic (BRD
EC-UX-07, EC-UX-08, EC-UX-37, D178).

#### Scenario: Reduce motion
- **WHEN** reduce motion is on and a message appears
- **THEN** it is shown in its final position at once

#### Scenario: A replacement
- **WHEN** "Migraine ended" replaces "Migraine started" with motion on
- **THEN** the text and icon change in place, the surface stays where it is, and nothing cross-fades

#### Scenario: Error haptic
- **WHEN** "something went wrong" is shown
- **THEN** the phone gives one short reject vibration
