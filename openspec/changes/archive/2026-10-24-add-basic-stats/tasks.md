# Tasks

## 1. Shared rule (design D1)

- [x] 1.1 `:features:attackdays:domain` with `AttackDays` and its tests; History and home use it, and their copies go.
  Verify: `./gradlew :features:attackdays:domain:test :features:history:domain:test :features:home:domain:test`

## 2. Domain (design D2)

- [x] 2.1 `MonthStatsDomainModel`, `ProgressDomainModel`, `MonthStatsCalculator` with tests.
- [x] 2.2 `GetAttackMedicationIntakesRepository`; `GetHistoryMonthUseCase` reads the extra data and fills `stats`;
  tests for the failure paths.
  Verify: `./gradlew :features:history:domain:test`

## 3. Data (design D3)

- [x] 3.1 `HistoryRepository` reads attack-medication intakes; DI.
  Verify: `./gradlew :features:history:data:test :app:assembleDebug`

## 4. Presentation and UI (design D4)

- [x] 4.1 Presentation models and mappers; ViewModels unchanged apart from mapping.
- [x] 4.2 History's `MonthSummary` and home's progress line; strings with plurals.
  Verify: `./gradlew :features:history:presentation:test :features:history:ui:test`

## 5. Verify

- [x] 5.1 Gate.
- [x] 5.2 Emulator walk on emulator-5570 with seeded data: a past month, the current month, the chronic card, the
  progress message (better, worse, too few entries), screen readers, 200% font, landscape.
- [x] 5.3 Independent review; fix and record.

## Review (5.3)

An independent `superpowers:code-reviewer` pass found nothing critical. It confirmed:
- the start-zone counting, months crossed, attacks in progress, pain-free days under attacks, intakes by their zone and
  the union of entry days;
- January against December, the window margins, the failure routing, the shared rule matching the old copies, DI, and
  accessibility;
- the contrast of the progress colour (about 7.7:1).

| Finding | Outcome |
|---|---|
| **I1** on a month's last day the current month read as a whole month ("than September") | fixed: the calculator takes today and decides by the month; a test for 31 October |
| **I2** the 10-day threshold and month lengths were not pinned | added tests: 9 against 10 days, 30 March against February, a past January against December, overlapping entries counted once, failed reads of the month before's attacks and marks |
| Home showed progress before any attack (EC-HIS-01) | fixed in the domain: no previous period before the first attack |
| `PeriodEntries` was a domain model outside `model/` | now `PeriodEntriesDomainModel` in `model/` |
| DRY: the medication prompt kept its own zone margin; start dates and migraine dates were computed twice | the prompt uses `AttackDays`; the calculator reuses both |
| The average peak had two ways to say "none" | its own `PainLevelUiModel`, null for none |
| The orphaned KDoc in `GetTodayUseCase`; "—" was translatable; `progress`'s KDoc | fixed |
| A zone-ahead attack shows on a future cell but is not counted | accepted and documented (design D2): future dates never count |
| The chronic copy hard-codes 15 | accepted: it is the clinical definition (ICHD-3) |

## Verification record

**Emulator walk** (a walk agent on emulator-5570, seeded through SQL and removed afterwards; the database dump was
byte-identical to the baseline at the end). All 7 scenarios passed.

| Scenario | Result |
|---|---|
| October as is | 12 started, 12 migraine days, Moderate, as counted by hand from the database; an attack ending at midnight leaves the next day out; the 30 September–1 October attack is only a day |
| Pain-free and medication | a mark under an attack is not counted; a preventive intake is not counted; an intake at 23:30 Tashkent recorded in Tokyo counts on Tokyo's date |
| A past month | titled "September", without "so far" |
| Progress | "11 more migraine days than this time last month" in neutral grey, on History and home; it shows with exactly 10 entry days and goes with 9; "1 more migraine day than August" (singular); "As many migraine days as August"; "14 fewer migraine days than July" in the progress colour |
| Chronic | the calm card at 15 days in July; none at 14 |
| Average peak | "—" and "no pain level logged"; 1.5 → Moderate; 2.5 → Severe; 1 and 3 → Mild |
| Accessibility | one node per row; 200% font in portrait and landscape with nothing clipped |
| Attack mode | no month card and no progress line |

**Changed after the walk** (BRD D58):
- The summary showed "Migraines: 12" above a list headed "13 migraines in October". The difference was an attack from
  30 September. The first number now reads "Migraines started", and a scenario covers it.
- On home the progress line sat under the legend, below the fold. It now sits under the month card's title.

**Gate after the fixes:** see the PR.
