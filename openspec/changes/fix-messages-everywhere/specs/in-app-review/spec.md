## MODIFIED Requirements

### Requirement: Letting home settle
The app SHALL ask only after home has shown today's mode for 2 seconds while on screen with no message showing; a message SHALL
drop a wait under way. After an error during a visit to home, it SHALL NOT ask until the next visit. It SHALL have Google Play
prepare the flow and launch it only if home is still on screen, no attack runs and no paywall showed; at most once per process
(D130, D165, D178).

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
- **THEN** nothing is asked and nothing counts (EC-REV-13)

#### Scenario: The rest of the visit after an error
- **WHEN** an error has shown on Home Today and has gone, by "Saved.", Dismiss or a swipe, and the user stays on home
- **THEN** nothing is asked for the rest of that visit, including after rotation or a restored process (EC-REV-13)

#### Scenario: The next visit
- **WHEN** after such an error the user opens Settings and comes back, or starts the app again, and home then shows today's mode
  for 2 seconds with no message and the other rules met
- **THEN** the request may run (EC-REV-13)

#### Scenario: Undo or information on Home Today
- **WHEN** a request is due and "Migraine ended" with "Undo", or an information message, shows on Home Today
- **THEN** nothing is asked while it shows, and the 2 seconds start when it leaves (EC-REV-13)
