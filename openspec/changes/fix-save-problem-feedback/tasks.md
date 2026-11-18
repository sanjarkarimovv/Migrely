# Tasks

Base: `bugfix/239-save-error-feedback` (worktree `migraine-app-239`) from `origin/develop` 76c38c38, with
`origin/ux/239-237-feedback` 4001103f merged (fast-forward). Release: 1.3.2, tracker T75. Write `Refs #239` in every commit
and in the PR into `develop`, never a closing keyword: QA filed #239 and closes it after the retest. BRD D191, EC-UX-49,
EC-UX-50, EC-UX-66 to EC-UX-71. Design: `docs/ux/239-237-validation-and-save-feedback.md` section 3 (F1 to F12) and section
5 (V1 to V16); this change's `design.md` (D1 to D5).

Ownership:
- `product-manager`: 0 (done) and 8;
- `ux-designer`: 1 (done);
- `android-engineer`: 2 to 5;
- `code-reviewer`: 6;
- `qa-engineer`: 7 (emulator-5580 API 36, then emulator-5582 API 26, one at a time).

No `medical-reviewer`: no string and no health sentence changes. Layers: UI of `features/medication` only. Every test below
is written first and shown red on `develop` 76c38c38, then green; each mutation is a deliberately broken build that must turn
the test red again. Logs go to `migrely-work/239/eng/`. Robolectric tests live in `features/medication/ui/src/testDebug`.

## 0. Plan (`product-manager`)

- [x] 0.1 BRD D191, EC-UX-66 to EC-UX-71, EC-UX-49 and EC-UX-50 pointers, §13 row 55; proposal, design, `medication-list`,
  `medication-intakes` and `in-app-messages` deltas, tasks. Verify: `openspec validate fix-save-problem-feedback --strict` and
  `openspec validate --all --strict` exit 0.

## 1. Design (`ux-designer`)

- [x] 1.1 Rules F1 to F12, per-screen notes, acceptance V1 to V16: `docs/ux/239-237-validation-and-save-feedback.md`
  (4001103f). Scope by `team-lead` 2026-11-18: Edit medication and Log medication first use in; Add past migraine F2/F3 to
  the backlog; F9 in. Verify: `git log --oneline -1 -- docs/ux/239-237-validation-and-save-feedback.md` on this branch.

## 2. Edit medication (`android-engineer`, `features/medication/ui`)

- [ ] 2.1 Announce once per arrival (D1, D2; F5, F7). Verify: a test (for example `EditMedicationProblemFeedbackTest`) that
  clears Name, presses Save, and asserts one polite live-region node carrying "Enter a name." appears once per `NotSaved`
  arrival; a second Save with no edit announces again (the node's content changes or is re-stamped). Red on 76c38c38 (no
  such node). Mutation: key the effect on the problem value instead of the arrival count → the second-Save case red.
- [ ] 2.2 Real error in the field's semantics, one stop (D3; F6). Verify: the merged node for Name has error "Enter a name."
  (not "Invalid input"); no other focusable node carries the error text; the announcement node is not focusable or
  traversable. Red on 76c38c38 ("Invalid input"). Mutation: drop the override → red.
- [ ] 2.3 Typing is quiet (F8). Verify: after the error, typing "Ib" produces no live-region change and no node in the field
  with `liveRegion`. Mutation: put `liveRegion = Polite` on the supporting text → red.
- [ ] 2.4 Scroll into view (D5; F2). Verify: in a 411x891 dp window with a 250 dp IME inset, Name focused, page scrolled to
  Save: after Save, the field region's bounds are fully inside the visible part (below the top bar, above the IME inset). Same
  with no IME inset and Name unfocused. Already in view → scroll value unchanged. Red on 76c38c38 (Name off screen).
  Mutation: remove the bring-into-view request → red.
- [ ] 2.5 Focus and keyboard (D1; F3, F4). Verify: (a) keyboard mode → Name focused (existing, keep green); (b) touch, IME
  inset present, Usual dose focused → Name focused, cursor at end, selection collapsed, no IME hide call; (c) touch, no IME
  inset → no field focused; (d) touch, IME inset, Name focused → focus unchanged. (b) red on 76c38c38. Mutation: request
  focus in every mode → (c) red.
- [ ] 2.6 Pending and one field (F1, F12). Verify: two quick presses → one `onSaveAction`, one announcement; empty name and
  invalid dose → only Name has an error. Mutation: allow the press while pending → red.

## 3. Log medication first use and messages (`android-engineer`, `features/medication/ui`)

- [ ] 3.1 Apply 2.1 to 2.5 to `FirstMedicationForm` through the same mechanism (no second copy), in the normal page and the
  dark room. Hardware keyboard: the park then the move to Name in the same result frame. Verify: a test class for the
  first-use form with the announcement, field semantics, scroll and keyboard-mode focus cases, each red on 76c38c38; the
  dark-room variant of the announcement and scroll cases.
- [ ] 3.2 A field problem clears an older message (D4; F9). Verify: in `MedicationMessages` tests and one screen test per
  form: "Couldn't save. Your phone's storage is full." shows, then `NotSaved(InvalidName)` → the message node is gone after
  one frame, no haptic call, the field error shows; then `Message(StorageFull)` → shown again. Red on 76c38c38. Mutation:
  clear only error-tone messages → the "You can keep up to 50 medications…" case red.
- [ ] 3.3 Rotation and process death (EC-UX-70 "Rotation after the clear"). Verify: `StateRestorationTester` after a clear
  by `NotSaved`: the field error shows, no message, and no new announcement on restore. Mutation: re-announce on restore →
  red.

## 4. Merge and gate (`android-engineer`)

- [ ] 4.0 After #237 (`clear-message-when-intake-logged`) is merged into `develop`, merge `origin/develop` into this branch
  and resolve `MedicationMessages` so both clears (`Logged`, `NotSaved`) exist; rerun 3.2 and #237's 3.1. Verify: both test
  classes green on the merged tree; `git log --oneline -1 origin/develop` recorded here. If #237 is not merged yet when
  review starts, record that and do this before QA.
- [ ] 4.1 Full gate. Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`
  exit 0, with the counts of tests run (never `testDebugUnitTest` alone).
- [ ] 4.2 No permission, string or manifest change. Verify: `./gradlew :app:checkReleasePermissions` exit 0 and
  `git diff origin/develop -- '*.xml' '*strings*' '*AndroidManifest*'` empty except tests.

## 5. Device walk (`android-engineer`, emulator-5570, headless first)

- [ ] 5.1 V1, V2, V4, V5 and V6 on the candidate (debug build of the branch head, hash read back from the device), API 36,
  TalkBack 16 at VERBOSE, Gboard, 100% portrait. Verify: per row the TalkBack utterances in the 5 s after the tap that contain
  the error text, split into app-caused and TalkBack-caused (ux section 5), and a `uiautomator dump` with Name's bounds and
  the IME's top.
- [ ] 5.2 V11 (dark room, first use) and V12 at 200% portrait and landscape. Verify: dumps with the field region's bounds and
  one screenshot per cell in `migrely-work/239/shots/`.
- [ ] 5.3 Restore font scale, rotation, animator scales, TalkBack, keyboard and locale. Verify: the settings read back.

## 6. Review (`code-reviewer`)

- [ ] 6.1 Review sections 2 to 4 against design D1 to D5: one mechanism for both forms, effect keyed on arrivals, no live
  region on the field, no "Invalid input", focus rules by mode and IME, no keyboard show or hide call, clear on `NotSaved`
  without fade or haptic, merge with #237 correct, no string change. Verify: a review packet with findings, each fixed or
  answered by the engineer before QA.

## 7. Acceptance (`qa-engineer`, this diff only)

- [ ] 7.1 V1 to V16 (ux section 5) on the PR head with v1.3.1 as the negative control (V1, V2, V3, V4, V9, V10 and V11 must
  show the defect on v1.3.1, or that row proves nothing and says so); V7 with its mutation build; V15 as a guard. APK hashes
  read back on the device. Verify: a QA record `docs/qa/fix-save-problem-feedback.md` with each row, build SHA, result,
  control result and the app-caused and TalkBack-caused utterance counts.
- [ ] 7.2 Restore every device setting. Verify: the settings read back in the QA record.

## 8. Close (`product-manager`, then `android-engineer`)

- [ ] 8.1 Before archive: diff the MODIFIED "Edit medication with a keyboard" against the live
  `openspec/specs/medication-list/spec.md`, and check that `in-app-messages` and `medication-intakes` gained nothing from #237
  that conflicts with the ADDED requirements here. Verify: the diff shows only the body's BRD ids and the two rewritten
  scenarios.
- [ ] 8.2 BRD D191 updated with QA's results (what was proved, what stays open, including the speech counts), then archive.
  Verify: `openspec archive fix-save-problem-feedback --yes` and `openspec validate --all --strict` exit 0.
- [ ] 8.3 PR into `develop` with `Refs #239`, merge commit, worktree removed after merge, tracker T75 updated by `team-lead`.
  Verify: `gh pr view <n> --json state,mergeCommit` shows MERGED.
