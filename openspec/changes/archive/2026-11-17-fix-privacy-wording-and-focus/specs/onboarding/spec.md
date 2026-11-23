## ADDED Requirements

### Requirement: Skip has one spoken name
On every step, "Skip" SHALL show the word "Skip" and SHALL be read by TalkBack once, as "Skip this step" with its button
role, never followed by "Skip". The button SHALL keep its role, click action, focus order and 48 dp height, so that
"Skip this step" still finds it for automation and "Tap Skip" can match it in Voice Access (BRD EC-ONB-07, EC-ONB-03,
D181, #217).

#### Scenario: TalkBack on a step
- **WHEN** TalkBack is on, the user touches "Step 1 of 4" and swipes forward once
- **THEN** TalkBack says "Skip this step, Button" and does not say "Skip" after it

#### Scenario: Every step
- **WHEN** the user reaches Skip on steps 1, 2, 3 and 4, at 100% and 200% font, in portrait and landscape
- **THEN** each time the node has the content description "Skip this step", no separate text node, and the screen shows
  "Skip"

#### Scenario: Skip still works
- **WHEN** the user taps Skip, or presses Enter on it with a keyboard, on step 1
- **THEN** step 2 shows without saving a goal, as before
