# Migrely test strategy and risk register

- **What this is:** what is tested at which level, by whom and when, and the risk that is left. BRD D204 asks for it
  after the outside quality audit of 2026-10-27 (`reports/2026-10-27-migraine-app-quality-engineering-plan.md`).
- **How it is kept current:**
  - Every QA change that adds a level or changes a capability's checks updates it.
  - So does every Go/No-Go, whose residual risks come from here.
- **How it is checked:** `python3 -I qa/e2e_coverage.py --register docs/qa/test-strategy.md` fails when a capability
  of `openspec/specs/` has no row in the register.
- **Rules it builds on:** `docs/QA.md`, BRD D40–D44, D61, D63, D201–D204, `docs/ARCHITECTURE.md` §11–12.

## 1. Test levels

| Level | Tool | Owner | Runs | Today | Target |
|---|---|---|---|---|---|
| Unit | JUnit4, Mockito-Kotlin, coroutines-test | dev | gate, every change | 228 `*Test.kt` files over the features, `:app` and the datasources | as today |
| Architecture rules | Konsist | dev | gate | names and packages | plus layer dependencies (audit C6) |
| Code coverage | Kover with a per-layer ratchet | dev | gate | **not measured** | audit C2: every layer shown, a fall fails the gate |
| Static analysis | ktlint, detekt, Android Lint | dev; QA reads lint on each candidate | gate; Go/No-Go line 1a | ktlint and detekt in the gate. Lint only on the candidate (D204) and failing (#74) | lint in the gate with no errors (D123) |
| Screen state | Compose UI test under Robolectric on `*Contents` | QA | gate | **none** (0 of 32 screens) | audit C3, after C1 adds the test dependencies |
| Visual | Roborazzi goldens of the `@Preview`s, `en-XA`, `ar-XB` | QA | gate | **none**; D42 promised it as a QA change | audit C4, after C1 |
| Instrumented | `connectedDebugAndroidTest`: DAO SQL, Room migrations, design system | dev; QA on each candidate | when a datasource changes; Go/No-Go line 1b | 138 tests, run by QA on a candidate since 2026-10-27 | also in CI (audit C5) |
| End-to-end | Maestro flows with `# Covers:` lines | QA | per QA change (D63); full regression per candidate | 258 of 507 scenarios (50%) | every high-risk capability at 80% or more (QA 13d) |
| Device scenarios | `qa/device_scenarios.sh`: clock, zone, kill, keyboard, files, billing states | QA | per candidate | 53 scenarios | grows with 13c and 13d |
| Device audits | a11y audit, monkey, permission audit, R8 APK, backup rules | QA | per candidate | a11y covers 10 states | every screen (D203) |
| Split APKs | `qa/split_apks.sh` on the bundle (D204) | QA | per candidate | from build 4 | as today |
| Exploratory | charters and sweeps on the candidate | QA | per candidate | since build 4 | as today |
| Play pre-launch report | Google Play, closed track | Play; QA reads it | per candidate | build 4's closed track | as today |
| CI | GitHub Actions | dev | every PR | **stopped**: the account's billing lock starts no job | gate plus device job (audit C5) once billing is fixed |

## 2. Risk register

- **"E2E":** the share of a capability's scenarios with a flow or a device scenario, from `qa/e2e_coverage.py` on
  2026-10-27.
- **"Unit":** the feature module with unit tests on its domain, data and presentation layers.
- **User risk:** what a defect costs the user:
  - **high:** a lost or wrong record, a safety warning, money, or data leaving the phone;
  - **medium:** a wrong view of correct data, or a blocked task with a workaround;
  - **low:** looks.

| Capability | E2E | Unit | Not checked end to end | User risk | Follow-up |
|---|---|---|---|---|---|
| attack-logging | 21/30 (70%) | attack | launcher shortcut; first and later intensity; attack across midnight and a daylight-saving change; storage full | high | build 4 sweep; QA 13d |
| attack-details | 6/37 (16%) | attack | time correction and its limits; overlaps and merge; pain entries outside new times; the safety card; delete with undo; medications in the details | high | build 4 sweep; QA 13d |
| past-attacks | 7/29 (24%) | attack | defaults; past-only times; overlaps and merge of three; long merged notes; double tap; rotation; add for a chosen day | high | build 4 sweep; QA 13d |
| attack-mode | 5/14 (35%) | home | elapsed time; medication taken; transitions; haptics; screen reader and largest font | high | build 4 sweep; QA 13d |
| attack-history | 19/30 (63%) | history | newest first; attack across two months; time moved to another day; screen reader | high | build 4 sweep; QA 13d |
| medication-intakes | 4/14 (28%) | medication | logging during an attack; a future time; links to their attack across undo, merge and zones; rotation keeps the time | high | build 4 sweep; QA 13d |
| medication-overuse | 11/22 (50%) | medication | two categories in one day; "Not sure"; preventives; the doctor warning; several categories; "Got it" over 30 days | high (safety warning) | sweep 2 on build 4; QA 13d |
| medication-list | 8/11 (72%) | medication | rename; recently used first; limit reached | medium | QA 13d |
| medication-effect | 10/13 (76%) | medication | answer later; screen reader; largest font | medium | a11y audit (D203) |
| data-backup | 13/16 (81%) | backup | Replace after a deletion and during an undo; Android backup to a new phone | high (data loss) | sweep 2; the new-phone case needs a phone with a Google backup |
| in-app-messages | 7/24 (29%) | home, history | messages in attack mode and landscape; kinds by shape; undo window with reduce motion; durations; storage full; rotation | high (undo of a delete) | sweep 2; QA 13d |
| today-sky | 18/34 (52%) | home | attack in progress or from last night; two attacks today; rotation keeps the undo; landscape; keyboard | medium | sweep 2 |
| month-sky | 12/36 (33%) | history, attackdays | the day rules: worst attack, unrecorded pain, medication-only and preventive days, period days | medium | unit tests hold the rules (attackdays); sweep 2 samples them; screenshots in C4 |
| month-stats | 17/20 (85%) | history | the current month; progress message edge cases | medium | QA 13d |
| day-markers | 14/19 (73%) | home, history | Track periods off (#66) | medium | QA 13c task 2.2 |
| custom-tags | 2/7 (28%) | attack | name too long or empty; archived match; 100 limit; remove a used tag | medium | sweep 2; QA 13d |
| app-shell | 5/16 (31%) | app | oldest Android install and a device below it; rotation on home; light/dark system; network traffic on launch; English dates on ru/en phones | high (data leaving the phone) | release_apk and permission audit hold the network promise; split APKs (D204) hold the language rules; QA 13d |
| app-settings | 8/10 (80%) | settings | privacy link and "No browser" | medium | QA 13c task 2.1 |
| onboarding | 9/9 (100%) | onboarding | — | medium | — |
| reminders | 12/13 (92%) | reminders | permission refused during onboarding | medium | QA 13d |
| subscription | 18/20 (90%) | subscription | grace period; Play offers no products | high (money) | D122, then QA 13c task 2.4; the grace period needs Play itself |
| doctor-report | 11/12 (91%) | report | no note among the migraines shown | high (shared with a doctor) | QA 13c task 2.3 |
| trigger-insights | 8/8 (100%) | insights, patterns | — | medium | — |
| app-identity | 0/3 (0%) | app | name, icon and package checked by hand at each release | low | a release-checklist line in QA 13d |
| brand-typography | 0/5 (0%) | designsystem | fonts and type scale | low | audit C4 (visual) |
| release-build | 0/3 (0%) | app | checked by `release_apk`, the R8 regression and the permission audit, which carry no Covers lines | high | Covers lines for those checks in QA 13d |
| quality-gates | 13/52 (25%) | — | QA's own rules; most are checked by doing them | low for users | — |

## 3. Open risks that no level covers yet

| Risk | Why it is open | Until it closes |
|---|---|---|
| Logic without a test | no code coverage (audit C2) | review asks for a test per use case |
| Error, empty and loading states of a screen | no screen-state tests (audit C3) | exploratory charters |
| Visual regressions: spacing, colours, long text, pseudo-locales | no Roborazzi (D42, audit C4) | the a11y audit's captures, looked at by hand |
| A PR that breaks a migration or a smoke journey | CI runs nothing (billing lock, audit C5) | the local gate, and lines 1b and 2 per candidate |
| State lost when a screen is rebuilt (audit S2) | view state is a SharedFlow without replay | the sweep's "Don't keep activities" and rotation checks |
| Play's grace period and account hold | need a failed renewal in Play | the dev session's real-phone tests |
