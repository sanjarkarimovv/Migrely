## MODIFIED Requirements

### Requirement: Messages appear at the top and stay dim
Short messages SHALL appear at the top of the screen, inside the system bars and display cutouts, and never cover the
large controls. On a screen with a top bar, the top edge SHALL sit below the bar; on the paywall, below the Close row; on
Home Today, below the Settings row unless the window is short. A message SHALL never be the brightest element on screen:
text no brighter than `#C8CCD2`, on a dark surface with a thin outline (BRD EC-UX-01, EC-UX-16, EC-UX-20, D158).

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
- **WHEN** a message appears in landscape with no attack in progress
- **THEN** it is centred at the top of the screen, as in portrait, below the top bar
- **AND** on Home Today it is below the Settings row only if the window is not short

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
- **WHEN** a message with "Undo" shows on Home Today in a window 411 dp high, short enough to pick the two-column layout, at
  100%, 130% or 200% font
- **THEN** it lies at the top where it did before, not below the Settings row, and clear of "Migraine started"
- **AND** a tap on the top edge of "Migraine started" starts a migraine
- **AND** a smaller window is the known exception, #153: at 274 dp high and 200% font the message lies over the top of the
  button, and only a tap below the message starts a migraine

#### Scenario: Settings under a message in a short window
- **WHEN** a message with "Undo" shows on Home Today in a window 411 or 274 dp high, at 100% or 200% font, and the user taps
  Settings
- **THEN** the tap does not fire "Undo"
- **AND** if the message's body lies over Settings, nothing happens, which is the accepted cost of leaving the message at the
  top, and Settings answers once the message has gone and its guard has ended

#### Scenario: A screen with no top bar
- **WHEN** a message shows on a screen with no top bar, such as onboarding
- **THEN** it lies where it did before

#### Scenario: Reduce motion
- **WHEN** reduce motion is on and a message shows on a screen with a top bar
- **THEN** it appears at once at the same place below the bar

### Requirement: Messages can be dismissed
The user SHALL be able to dismiss any message by swiping it sideways. Messages without "Undo" SHALL also have a close button
with the spoken label "Dismiss". A tap anywhere on a message with a timeout and no action SHALL dismiss it, by touch only
with no new semantics. A tap on the body of a message with an action or with no timeout SHALL do nothing, so a stray tap
cannot remove the offer to undo or the word that nothing was saved (BRD EC-UX-16, D158).

#### Scenario: Swipe away
- **WHEN** the user swipes a message sideways
- **THEN** it disappears and its action is not performed

#### Scenario: Close an error
- **WHEN** the user taps the close button on "storage full"
- **THEN** the message disappears

#### Scenario: A tap on a message with a timeout and no action
- **WHEN** the user taps the text of a message with a timeout and no action
- **THEN** the message disappears

#### Scenario: A stray tap on "storage full"
- **WHEN** a tap lands on the body of the "storage full" message
- **THEN** the message stays, and its close button and a swipe still close it

#### Scenario: A tap on the body of a message with Undo
- **WHEN** the user taps the text of a message with "Undo"
- **THEN** the message stays with the time it has left, and "Undo" still works

#### Scenario: A control under a message
- **WHEN** a message with a timeout and no action lies over a control, the user taps the message, and then taps the control
  after the guard has ended
- **THEN** the first tap dismisses the message, and the second reaches the control

#### Scenario: A screen reader
- **WHEN** TalkBack moves through a message with no action
- **THEN** it reads the text and the "Dismiss" button as before, and no new clickable item is added

### Requirement: A message never passes taps to what lies under it
While a message arrives or goes, and for 0.8 seconds after, a touch on its place SHALL not reach the controls under it, such
as a top bar's actions. On the paywall this SHALL be 0.4 seconds. The message itself SHALL stay tappable at once. Screen
readers, keyboards and switch access SHALL be unaffected (BRD EC-UX-16, D114, D158).

#### Scenario: Undo just as the message goes
- **WHEN** a migraine is deleted in History and the user taps "Undo the deletion" just as the message fades out
- **THEN** neither "Doctor report" nor "Insights" opens

#### Scenario: Afterwards
- **WHEN** the message has been gone for more than 0.8 seconds
- **THEN** the top bar's actions work as usual

#### Scenario: On the paywall
- **WHEN** a message on the paywall lies over the buy button, the user taps the message, and taps the button 200 ms later
- **THEN** the second tap does nothing

#### Scenario: On the paywall, afterwards
- **WHEN** the same second tap comes more than 0.4 seconds after the message was dismissed
- **THEN** the buy button answers

#### Scenario: Another screen keeps 0.8 seconds
- **WHEN** a message goes on any screen other than the paywall
- **THEN** its place keeps ignoring touches for 0.8 seconds

## ADDED Requirements

### Requirement: The first row under the bar may lie under a message
The first row under a top bar or the Settings row SHALL be allowed to lie under a message for the message's duration, only
where it is visible and either reversible or temporary. Messages SHALL NOT move back over the top bar to avoid it. My
medications' Remove is measured in `tasks.md`; the other cases come from reading the code until measured there (BRD D158).

#### Scenario: Remove on row 1 of My medications
- **WHEN** a message with "Undo" shows on My medications, at any font and orientation, and the user taps Remove on row 1
- **THEN** the tap can land on "Undo" instead, which restores the last removal and shows "Undone"

#### Scenario: History's month chevrons
- **WHEN** a message with "Undo" shows on History and the user taps a month chevron under it
- **THEN** "Next month" can lie under "Undo", so the tap restores the deletion and shows "Undone"
- **AND** "Previous month" under the message does nothing
- **AND** both answer once the message has gone and its guard has ended

#### Scenario: The trial banner's close button
- **WHEN** a message lies over the close button of the trial banner on Home Today and the user taps it
- **THEN** the tap reaches the message and not the button, and the banner stays
- **AND** the button answers once the message has gone and its guard has ended

#### Scenario: The safety card on Attack details
- **WHEN** a message shows on Attack details with the safety card on screen and scrolled to the top, and its title lies under
  the message
- **THEN** the card stays until it is dismissed, it can be scrolled, and its live region still reads it

#### Scenario: "Storage full" over the safety card
- **WHEN** "storage full" shows over the title of the safety card
- **THEN** it can cover the title until it is closed, and its close button and a swipe still close it
