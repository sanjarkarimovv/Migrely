## ADDED Requirements

### Requirement: A standing indicator in History
For the current month, History's summary SHALL show a "Last 30 days" section after "Days with attack medication". It
SHALL have one row per category with an attack-medication intake in the last 30 days, giving its days against its
limit, and a row for all attack medication when two or more categories were used. It SHALL count as the warning does,
it SHALL not be quieted by "Got it", and it SHALL not show without such an intake (BRD EC-MED-06, D45, D59).

#### Scenario: Below the limit
- **WHEN** simple painkillers were taken on 6 of the last 30 days
- **THEN** the section reads "Simple painkillers: 6 of 15 days" in the neutral colour

#### Scenario: At the limit
- **WHEN** a triptan was taken on 10 of the last 30 days
- **THEN** "Triptans: 10 of 10 days" reads in amber, followed by "At this many days, medication can make headaches
  more frequent. Talk to your doctor about it."

#### Scenario: Two categories
- **WHEN** simple painkillers were taken on 7 days and triptans on 5 other days
- **THEN** the section also reads "All attack medication: 12 of 10 days", in amber

#### Scenario: A past month
- **WHEN** History shows September
- **THEN** no "Last 30 days" section shows

#### Scenario: No attack medication
- **WHEN** no attack medication was taken in the last 30 days
- **THEN** no "Last 30 days" section shows
