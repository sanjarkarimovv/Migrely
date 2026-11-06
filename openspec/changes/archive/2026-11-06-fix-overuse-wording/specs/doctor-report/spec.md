## MODIFIED Requirements

### Requirement: The summary page
The first page SHALL show the period, the date made, and, for the period:
- migraine days a month on average;
- the number of attacks;
- attack-medication days a month on average, and the 30-day spans back from its last day that reach an ICHD-3 8.2
  medication-day threshold, as home's card counts them;
- the number of "Bed rest" attacks;
- the average peak level;
- whether a calendar month it touches had 15 or more migraine days, as History counts them.

(BRD REP-F2, D82, D151.)

#### Scenario: Three months
- **WHEN** the report covers 92 days with 18 migraine days and 4 attacks at "Bed rest"
- **THEN** the summary says 5.9 migraine days a month and 4 "Bed rest" attacks

#### Scenario: A month only partly in the period
- **WHEN** a one-month report starts on 26 September and triptans were taken on 11 of its days across the month change
- **THEN** the span 26 September to 25 October reaches the threshold, as home's card says

#### Scenario: The label does not say limit
- **WHEN** the summary page shows the overuse spans
- **THEN** the label reads "30-day spans reaching an ICHD-3 8.2 medication-day threshold", and the value is the dates or
  "None"
