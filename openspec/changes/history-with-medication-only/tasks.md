# Tasks

## 1. Data

- [x] 1.1 `MedicationDataSource.earliestAcuteIntake()`: its implementation, DAO query and instrumented test.

## 2. History

- [x] 2.1 Domain: the earliest-intake repository and its use in `GetHistoryMonthUseCase`, `anythingLogged`; tests.
- [x] 2.2 Data: the repository implementation and mapping; tests.
- [x] 2.3 Presentation: Ready or NothingLogged by `anythingLogged`; tests.
- [ ] 2.4 Calendar: a past day with attack medication only (D100): domain flag, sky, drawing, spoken text, legend;
  tests.

## 3. Verify

- [ ] 3.1 Gate, and instrumented tests.
- [ ] 3.2 Emulator check:
  - medication only: summary and indicator, previous month reachable;
  - marks only;
  - nothing logged;
  - preventive only;
  - a day with medication only, on History and home, with TalkBack.
