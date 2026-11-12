## RENAMED Requirements

- FROM: `### Requirement: What helped`
- TO: `### Requirement: How you rated your medications`

## MODIFIED Requirements

### Requirement: How you rated your medications
Insights SHALL head its medication section "How you rated your medications". For each attack medication with at least 3
answers to "Did it help?" in the period, it SHALL show how many of those answers were "A lot" or "Completely"; with fewer
answers it SHALL say how many it has. It SHALL never say which medication works (BRD INS-F4, INS-F6, D92, D174).

#### Scenario: Ibuprofen
- **WHEN** 7 of 9 answers for Ibuprofen were "A lot" or "Completely"
- **THEN** Ibuprofen shows "Helped a lot or completely: 7 of 9 answers (78%)"

#### Scenario: The heading
- **WHEN** Insights shows the medication section
- **THEN** its heading reads "How you rated your medications"
- **AND** no text on the Insights screen says "What helped"

#### Scenario: Fewer than three answers
- **WHEN** Sumatriptan has 2 answers to "Did it help?" in the period
- **THEN** under the same heading Sumatriptan shows "Needs 3 answers (has 2)"

#### Scenario: No attack medication
- **WHEN** no attack medication was logged in the period
- **THEN** the section keeps its heading and says "No attack medication was logged in this period."

#### Scenario: Large font, narrow screen, landscape
- **WHEN** the font is at 200% on a 360 dp wide screen, in portrait or landscape
- **THEN** the heading wraps onto more lines and is never clipped or cut with "…" (BRD EC-INS-04)

#### Scenario: TalkBack
- **WHEN** TalkBack moves through Insights
- **THEN** "How you rated your medications" is read as a heading
- **AND** each medication's name is read together with its answers line, as before (BRD EC-INS-05)

#### Scenario: Upgrade or restore
- **WHEN** a person updates from a version that showed "What helped", or restores a backup made by one
- **THEN** every answer, count and percentage is the same as before, and only the heading's words differ (BRD EC-INS-06)
