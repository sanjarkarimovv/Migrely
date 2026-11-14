## MODIFIED Requirements

### Requirement: Letting home settle
The app SHALL ask only after home has shown today's mode for 2 seconds while the app is on screen and no error, nor the "Saved."
that replaced one, shows; such a message SHALL drop a wait under way. It SHALL first have Google Play prepare the flow, and SHALL
launch it only if home is still on screen, no attack is in progress and no paywall has shown itself. It SHALL launch the flow at
most once per process, whatever the rotations or home loads (D130, D165, D178).

#### Scenario: Leaving within 2 seconds
- **WHEN** within 2 seconds of home showing today's mode the user starts an attack, opens another screen or sends the
  app to the background
- **THEN** nothing is asked, and the rules are checked again on the next home load in today's mode (EC-REV-07)

#### Scenario: Leaving while Google Play prepares
- **WHEN** after home has settled the user starts an attack, opens another screen or a paywall opens before Google
  Play has prepared the flow
- **THEN** the flow is not launched, nothing counts, and the next settled home load in today's mode tries again
  (EC-REV-01, EC-REV-03, EC-REV-07)

#### Scenario: Rotation and reloads
- **WHEN** the screen rotates or home reloads several times after the flow was launched in this process
- **THEN** the flow is not launched again in this process (EC-REV-08)

#### Scenario: An error on Home Today
- **WHEN** a request is due and "Couldn't save" or "Something went wrong" shows on Home Today, or appears during the 2-second
  wait or while Google Play prepares the flow
- **THEN** nothing is asked and nothing counts while it shows (EC-REV-13)

#### Scenario: "Saved." after an error
- **WHEN** "Saved." has replaced an error on Home Today
- **THEN** nothing is asked while it shows (EC-REV-13)

#### Scenario: The message goes
- **WHEN** the error or "Saved." leaves by its time, Dismiss, a swipe, a newer message or leaving the screen, and home then shows
  today's mode for 2 seconds with the other rules met
- **THEN** the request may run, counted from when the message left (EC-REV-13)

#### Scenario: Undo or information on Home Today
- **WHEN** a message with "Undo" or an information message shows while a request is due
- **THEN** the request behaves as before this change (EC-REV-13)
