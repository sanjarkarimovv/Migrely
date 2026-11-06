## ADDED Requirements

### Requirement: Only the current screen navigates
A navigation event SHALL navigate only while the screen observing it is the current back-stack entry, compared by entry
id. A late result of a screen no longer current SHALL NOT navigate. A screen fading in is current, so its first tap and
what it sends on entering SHALL count. The check reads the observer's screen, not the sender's (BRD EC-UX-21, D154).

#### Scenario: Two rows on Home's today list
- **WHEN** the user touches two rows of "Logged today" within a fade, by two taps or a resting finger
- **THEN** one details screen opens, and Delete returns to home with "Undo the deletion"

#### Scenario: The same row twice
- **WHEN** the user double taps one row of "Logged today"
- **THEN** one details screen opens

#### Scenario: A first tap on a screen fading in
- **WHEN** the user taps Back on the editor while it is still fading in
- **THEN** the list shows

#### Scenario: A screen that navigates as it enters
- **WHEN** a screen sends a navigation event while it fades in
- **THEN** the event is obeyed

#### Scenario: A late result after leaving
- **WHEN** a screen's async work ends after the user has left that screen for another destination
- **THEN** no navigation happens

#### Scenario: An entry replaced by launchSingleTop
- **WHEN** `launchSingleTop` replaces the current entry's object
- **THEN** it keeps its id and still counts as current

#### Scenario: Two entries of one destination
- **WHEN** a late result from a leaving entry arrives while another entry of the same destination is current
- **THEN** it navigates, because the check knows the observer's screen and not the sender's

#### Scenario: A paywall due on a screen just left
- **WHEN** a paywall is due on home's load and the user has just left home
- **THEN** it does not open, nothing is marked as shown, and home's next load asks again

### Requirement: Touches only on the current screen
The pointer input of this window on a back-stack entry that is not current SHALL be consumed, read at each event, so a
finger already down cancels instead of clicking. Semantics SHALL NOT be touched, so screen readers and switch access keep
working. Dialogs and popups are separate windows and SHALL NOT be covered. Layout SHALL NOT change (BRD EC-UX-22, D155).

#### Scenario: A resting finger on Remove
- **WHEN** a finger rests on a row's Remove in My medications, another finger taps another row, and the first lifts during
  the fade
- **THEN** the editor opens and nothing is archived

#### Scenario: A resting finger on Delete migraine
- **WHEN** a finger rests on "Delete migraine", another finger taps Back, and the first lifts during the fade
- **THEN** nothing is deleted

#### Scenario: A resting finger on Delete everything
- **WHEN** DELETE is typed, a finger rests on "Delete everything", another finger taps Back, and the first lifts during the
  fade
- **THEN** nothing is deleted and Settings shows

#### Scenario: A new touch falling through
- **WHEN** a new touch lands during a push where the leaving screen below has a control
- **THEN** it does not reach the leaving screen

#### Scenario: A push
- **WHEN** the user taps a control on a screen fading in after a push
- **THEN** the control answers

#### Scenario: A pop where the leaving screen takes no touch
- **WHEN** the user taps a control on the screen fading in after a pop, where the leaving screen has no control or scroll
  column
- **THEN** the control answers

#### Scenario: A pop where the leaving screen has a control
- **WHEN** the user taps during a pop where the leaving screen has a control or a scroll column
- **THEN** the tap does nothing, for at most the length of the fade

#### Scenario: Nothing navigated
- **WHEN** a finger rests on a control and lifts with no navigation in between
- **THEN** the control clicks as usual

#### Scenario: Returning to the screen
- **WHEN** a control's screen was left and is current again after the fade
- **THEN** the control answers

#### Scenario: A screen reader on a leaving screen
- **WHEN** an accessibility service performs the click action on a control of a leaving screen
- **THEN** the action runs, and any navigation it sends is dropped by the rule above

#### Scenario: Size and place
- **WHEN** a screen is shown through this rule
- **THEN** it has the size and place it would have without it

### Requirement: One fade between screens
Every navigation SHALL use one 200 ms linear fade, set in one place, for entering and leaving, on a push and on a pop. The
library's default SHALL NOT be used. With the system "Remove animations" setting on, no fade SHALL play (BRD EC-UX-23, D156).

#### Scenario: Any navigation
- **WHEN** the user moves between any two screens
- **THEN** the screens fade linearly over 200 ms, and the leaving screen is gone after that

#### Scenario: Tapping after Back
- **WHEN** the user presses Back and taps the screen below 300 ms later
- **THEN** the tap answers

#### Scenario: Animations removed
- **WHEN** the system "Remove animations" setting is on
- **THEN** the next screen shows at once, with no fade
