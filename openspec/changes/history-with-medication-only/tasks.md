# Tasks

## 1. Data

- [x] 1.1 `MedicationDataSource.earliestAcuteIntake()`: its implementation, DAO query and instrumented test.

## 2. History

- [x] 2.1 Domain: the earliest-intake repository and its use in `GetHistoryMonthUseCase`, `anythingLogged`; tests.
- [x] 2.2 Data: the repository implementation and mapping; tests.
- [x] 2.3 Presentation: Ready or NothingLogged by `anythingLogged`; tests.
- [x] 2.4 Calendar: a past day with attack medication only (D100): domain flag, sky, drawing, spoken text, legend;
  tests.

## 3. Verify

- [x] 3.1 Gate, and instrumented tests.
- [x] 3.2 Emulator check:
  - medication only: summary and indicator, previous month reachable;
  - marks only;
  - nothing logged;
  - preventive only;
  - a day with medication only, on History and home, with TalkBack.

## Verification record

**Gate:** 1198 unit tests on the branch before the develop merge, all passing; green again after it.
**Instrumented:** datasource 95/95 (the earliest-intake DAO tests among them), designsystem 33/33.

**Emulator check** on emulator-5570 (7883086), with the app data restored to the baseline afterwards (dump md5
unchanged). All 7 scenarios passed:
- **medication only:** the summary with 0 migraine days and the medication days; "Simple painkillers: 16 days
  (limit 15), over the limit"; History reaching back to September and stopping there;
- **the day look:** solid outlines on medication days, dashed elsewhere, in Dark and Light; "<day>: attack
  medication taken, no migraine logged."; the legend entry;
- **pain-free with medication:** keeps the clear sky and adds "Attack medication taken.";
- **marks only:** shows the summary, and reaches the mark's month;
- **nothing logged:** shows the empty state and only the current month;
- **preventive only:** still shows the empty state, with dashed days;
- **with attacks:** unchanged.

**Accepted:** on the dark theme both outlines are 1.83:1, dim by design, as the dashed one was before; they differ by
shape and by TalkBack text.

## Review

An independent `superpowers:code-reviewer` pass found nothing Critical.

| Finding | Outcome |
|---|---|
| **I1** `month-stats` and D53 still said the first-use card shows with no attack | fixed: a month-stats delta, D53 in D99 |
| **I2** the "First use" scenario was stale | fixed |
| **I3** the DAO test did not cover an intake with no attack | fixed: that intake is now the earliest, and a test has only it |
| M4 the progress message still needs an attack | kept on purpose, and recorded in design D2 and the delta |
| M5 EC-HIS-03 and ARCHITECTURE named only attacks and marks | fixed |
| M6 no test for a future intake | fixed: it is clamped to the current month |
| M7 test names that set nothing preventive | fixed, plus an "Only preventive medication" scenario |
| M8 `NoAttacks` now meant "nothing logged" | fixed: renamed `NothingLogged` |
| M9 long lines in the deltas | fixed |
| M10 medication-only days drawn as "nothing logged" | fixed by D100: a solid outline and its own spoken text |

