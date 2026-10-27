# Tasks

## 1. Requirements and the Go/No-Go record

- [x] 1.1 BRD §13 row 13c, §17 D203 and D204. Verify: `grep -n "| 13c |\|| D203 \|| D204 " docs/BRD.md`
- [x] 1.2 `docs/qa/go-no-go-1.0.0.md` for build 4's Play state:
  - the closed testing release and managed publishing, with Publish held for QA's verdict;
  - line 11 is the closed track's pre-launch report, read before Publish;
  - line 12 is waived by the user on 2026-10-27.
  Verify: `grep -n "waived\|managed publishing\|pre-launch" docs/qa/go-no-go-1.0.0.md`

## 2. Release-fix scenarios

- [ ] 2.1 `qa/device_scenarios.sh privacy_link` (design D2): the link to Chrome, and "No browser" with Chrome disabled.
  `restore()` and `qa/reset_device.sh` enable Chrome again. Verify: passes twice; after a run stopped half-way
  (Ctrl-C while Chrome is disabled), `pm list packages -d` does not list Chrome
- [ ] 2.2 `.maestro/day-markers/track-periods-off-marks.yaml` (design D3). Verify: passes 3 times; it fails when step 3's
  check is pointed at a day that keeps its mark (mutation of the flow, reverted)
- [ ] 2.3 Seed `report-heavy-old-notes` in `qa/seed_backup.py`, with its self-test, and its check in `report_heavy`
  (design D4). Verify: the self-test recounts it; `report_heavy` passes twice
- [ ] 2.4 "Google Play offers no products" in `premium_store_states` once D122 is on develop (design D5). Otherwise it
  goes to the D61 table with its follow-up. Verify: passes twice, or the D61 row

## 3. Accessibility audit on every screen (D203)

- [ ] 3.1 Tour states `onboarding-first`, `settings-main`, `settings-warning-signs`, `settings-privacy`,
  `settings-theme`, `medications-list`, `reminders` and `your-data` (design D1). Verify: `qa/a11y_audit.sh <state>`
  captures each at 100%, 200% and landscape
- [ ] 3.2 Tour states `paywall`, `report-period` and `insights` through the simulated store. Verify: as 3.1
- [ ] 3.3 The full audit on build 4's debug APK. Every failure is filed as an `accessibility` issue with its capture,
  and every "not judged" line is looked at. Verify: the audit's summary in the verification record

## 4. Release-candidate checks and residual risk (D204)

- [ ] 4.1 Lint and the instrumented tests on build 4 (design D7), with the results and the #74 exception in the
  Go/No-Go record. Verify: the record's lines name the counts and #74
- [ ] 4.2 `qa/split_apks.sh` and the device specs in `qa/fixtures/device-specs/` (design D8). Verify:
  - it refuses emulator-5570;
  - `pm path` lists 4 APKs for each profile;
  - the smoke suite and the D117 checks pass on ru-26 (emulator-5582) and on he-36, uz-36 and en-36 (emulator-5580);
  - the system language is back afterwards
- [x] 4.3 The residual-risk section in `docs/qa/go-no-go-1.0.0.md` (design D9), and line 9's new pass condition in
  `docs/QA.md` §5. Verify: the section lists every capability under 50% from `qa/e2e_coverage.py`, D42, C3–C5 and the
  checks that could not run
- [x] 4.4 `docs/qa/test-strategy.md` (design D10). Verify: every capability of `openspec/specs/` has a row (a script
  compares the two lists); every level has its tool, owner and when

## 5. Exploratory testing on build 4

- [ ] 5.1 The core-flow sweep (design D6): every one of the 92 scenarios is PASS, FAIL or NOT RUN with a reason, and every
  edge case has notes. Verify: the results in the verification record; an issue for every defect, with its blocker
  verdict sent to the dev session
- [ ] 5.2 Charters 1–4 of design D6, each with notes: what was tried, and what was found. Verify: the notes in the
  verification record; an issue for every defect

## 6. Documentation and verification

- [ ] 6.1 `docs/QA.md`:
  - the audit's screen list (D203);
  - `privacy_link` and the new seed;
  - §5's new lines (lint, instrumented, split APKs) and the residual-risk rule (D204).
  Verify: `grep -n "D203\|D204\|privacy_link\|report-heavy-old-notes\|split_apks" docs/QA.md`
- [ ] 6.2 Coverage, `qa/resource_names.py`, `openspec validate --strict`, smoke, and every new flow and scenario as its
  task says. Verify: the verification record below
- [ ] 6.3 Review (`superpowers:code-reviewer`), with every finding fixed or answered. Verify: a re-run of what each fix
  touched
- [ ] 6.4 Gate `./gradlew assembleDebug assembleRelease test ktlintCheck detekt --no-daemon` after merging develop.
  Verify: exit 0
