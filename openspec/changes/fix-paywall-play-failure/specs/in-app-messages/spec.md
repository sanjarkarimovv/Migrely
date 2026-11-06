## MODIFIED Requirements

### Requirement: Messages appear at the top and stay dim
Short messages SHALL appear at the top of the screen, inside the system bars and display cutouts, and never cover the
large controls. On a screen with a top bar, the top edge SHALL sit below the bar; on the paywall, below the Close row; on
Home Today, below the Settings row. A message SHALL never be the brightest element on screen: text no brighter than
`#C8CCD2`, on a dark surface with a thin outline (BRD EC-UX-01, EC-UX-16, EC-UX-20, D158).

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
- **THEN** it is centred at the top of the screen, as in portrait, below the top bar or the Settings row

#### Scenario: Close on the paywall
- **WHEN** a message shows on the paywall, at 100% or 200% font, in portrait or landscape
- **THEN** Close is not covered, moved or guarded, and a first tap on it closes the paywall

#### Scenario: A screen with a top bar
- **WHEN** a message shows on a screen with a top bar, such as History, My medications or Your data
- **THEN** its top edge is below the bar, and Back and the bar's actions answer a first tap

#### Scenario: Home Today
- **WHEN** a message with "Undo" shows on Home Today with the date on one line and the screen not scrolled
- **THEN** its top edge is below the Settings row, and a tap on Settings opens Settings and does not undo

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
