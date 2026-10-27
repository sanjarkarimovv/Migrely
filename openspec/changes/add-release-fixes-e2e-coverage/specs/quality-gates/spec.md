## ADDED Requirements

### Requirement: Every screen is in the accessibility audit
The accessibility audit's tour SHALL hold at least one state for every screen the app shows, and a change that adds a
screen SHALL add its state. A screen behind Premium SHALL be reached through the simulated store (D201). A failure
SHALL be filed as an `accessibility` issue, never worked around in the audit (BRD §10, EC-UX-03, EC-UX-04, D42, D203).

#### Scenario: The screens of 1.0.0
- **WHEN** the audit runs on 1.0.0
- **THEN** it captures home, attack mode, History, attack details, the past-migraine screen, onboarding, Settings and
  its pages, My medications, Reminders, Your data, the paywall, the doctor report and Insights

#### Scenario: The premium screens
- **WHEN** the audit runs
- **THEN** the paywall, the doctor report's period screen and Insights are each captured at 100% font, at 200% font and
  in landscape, and checked by the same rules as the other states

#### Scenario: A screen without a state
- **WHEN** a change adds a screen and the tour has no state for it
- **THEN** QA's review of the change finds the missing state, and the change does not pass verification until the state
  is added

### Requirement: A test strategy names the risk left
QA SHALL keep a test strategy that names each test level with its tool, its owner and when it runs. Its risk register
SHALL hold every capability of the specs, with:
- its checks at each level;
- the scenarios that have no check at any level;
- the follow-up that closes them.
A QA change that is deferred SHALL stay in the register as open until it is done (D204).

#### Scenario: A capability with few checks
- **WHEN** under half of a capability's scenarios have an end-to-end flow or a device scenario
- **THEN** the register names the capability as a risk, with the levels that do check it and the follow-up

#### Scenario: A deferred QA change
- **WHEN** a QA change named by a BRD decision is not done, such as D42's visual regression
- **THEN** the register lists it as open, and every Go/No-Go record lists it among the residual risks

## MODIFIED Requirements

### Requirement: Release Go/No-Go gate
A release candidate SHALL go to a Play track only when all of these hold:
- the build gate passes;
- Android Lint reports no error on the candidate;
- the instrumented tests pass on the QA emulator;
- the smoke suite passes on Android 8.0 and the target version;
- the smoke suite passes on the split APKs that Play builds from the candidate's bundle, for the device profiles of
  `docs/QA.md`;
- the full regression passes on the target version;
- the accessibility audit, the device scenarios, the random-input run and the permission audit pass;
- no S1 or S2 qa issue is open.

The Go/No-Go record SHALL state what each check does not prove. It SHALL also list the residual risks:
- the capabilities with under half of their scenarios checked;
- the deferred QA changes;
- the checks that could not run, with the reason.

(BRD §14.3, D44, D204)

#### Scenario: Open S2
- **WHEN** an S2 qa issue is open for a release candidate
- **THEN** the candidate is No-Go

#### Scenario: All checks pass
- **WHEN** every check in the gate passes for a release candidate
- **THEN** the candidate is Go for the internal testing track

#### Scenario: A lint error
- **WHEN** Android Lint reports an error on a release candidate
- **THEN** the candidate is No-Go until the error is fixed. A lint baseline never holds an error. A candidate checked
  before this rule existed records each error as a stated exception: why it cannot reach users, and the issue that
  fixes it

#### Scenario: The APKs users install
- **WHEN** the candidate's bundle is turned into split APKs for a device profile and installed
- **THEN** the smoke suite passes on them. In the profile's language, read back from the device, the dates and
  pickers stay in English with the region's date order and first weekday (D117)

#### Scenario: Residual risk in the record
- **WHEN** QA writes the Go/No-Go record of a release candidate
- **THEN** the record names, next to its verdict, each capability with under half of its scenarios checked, each
  deferred QA change and each check that could not run
