# Tasks

## 1. Insights feature

- [x] 1.1 Domain: `InsightsCalculator`, `GetInsightsUseCase`, tests.
- [x] 1.2 Data: `InsightsRepository`, mappers, tests.
- [x] 1.3 Presentation: `InsightsViewModel`, tests.
- [x] 1.4 UI: the Insights screen, the bars, accessible text.

## 2. History and app

- [x] 2.1 History's "Insights" action, the route, and DI.

## 3. Verify

- [x] 3.1 Gate (1163 unit tests, 0 failures), and instrumented tests (datasource 90/90, designsystem 28/28).
- [x] 3.2 Emulator walk with a seed:
  - not enough data;
  - each section;
  - a tie;
  - Track periods off;
  - fewer than two cycles;
  - a medication under 3 answers;
  - trends with TalkBack text;
  - 200% font.
- [x] 3.3 Independent review; fix and record.

## Verification record

**Emulator walk** on emulator-5570 (ef5e6fc), with seeds in transactions and the database restored to the baseline
afterwards (dump md5 unchanged). All 10 scenarios passed, with no bugs:
- entry from History, with 6 months chosen;
- not enough data ("2 more migraines", "20 more days"), with the trends still showing;
- every section against the seed: triggers with shares, the weekday and the time of day, the trends; no causal word
  anywhere;
- a weekday tie ("No single day stands out");
- the cycle: windows of 15 days for 3 starts, the base rate by hand, a start before the period, and the section gone
  with one start;
- Track periods off, which hides the section and the built-in Period trigger;
- medications, with "3 of 4 answers (75%)" and "Needs 3 answers (has 2)", and preventives left out;
- chips tapped quickly, which always end on the last chosen period;
- an unknown zone, with no crash, read as UTC;
- accessibility: 48 dp chips, one node per bar row with its numbers, and 200% font in portrait and landscape.

## Review (3.3)

An independent `superpowers:code-reviewer` pass:

| Finding | Outcome |
|---|---|
| **C1** an unknown zone crashed the records mapper | fixed: UTC, as History's mapper; tested on attacks and intakes |
| **I1** a stale result could stay on progress, or show the wrong months | fixed: `InsightsShownStateResolver` accepts only the chosen months' result |
| **I2** English trigger words in the data layer | fixed: `BuiltIn(kind)` or `Custom(name)` in every layer; the UI names built-ins from its strings |
| M1 trend rows at 200% font | fixed: the value goes under the label from 1.5× |
| M2 Period trigger with tracking off | fixed: only the built-in Period is hidden |
| M3 the base rate and edge shares | fixed: "(a of b days)", "<1%" and ">99%" |
| M5 no Loading preview | fixed |
| Test gaps | fixed: a tie at the fifth place, a cycle starting before the period |

