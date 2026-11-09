# Release Go/No-Go: Migrely 1.1.1 (versionCode 9), hotfix

## Decision

**QA's verdict, 2026-11-09 at 14:00: GO for `hotfix/1.1.1` @ d7376af0, with the risks below open.**

- **No S1 or S2 is open.**
  - #171 (S1, the card off-screen) was retested on eeb69f8a and closed.
  - #174 (S2, found in this run: on a3f25cdb the scroll put the card's top at the view's top, and on a failed save at 100% "Couldn't save" then hid the sentence's first line) was fixed in eeb69f8a, retested and closed.
- **The offline check works on every path where Play can be reached on an emulator, and it fails open.**
  - H1–H6 pass on debug.
  - The permission-removed build (H7) opens the sheet offline, 6 of 6.
- **One permission added, no INTERNET** (H8), on the build report and the APKs.
- **The card comes into view and is spoken.** S1 passes 16 of 16 on debug and 4 of 4 on R8. S2, S3, S5, S6 (100%) and S7 pass. On 1.1.0's code the card stays off-screen and silent (S4).
- **Not proved here:**
  - H9's forced datatransport job;
  - buy and Manage on the R8 build (no Play billing on the emulator image);
  - the API 26 row of H1;
  - 200% landscape (S6);
  - H10, the real Play build on the owner's phone, which is `team-lead`'s after upload.
- **Release steps (D125):** PR #173 into `main`, an annotated tag `v1.1.1`, then QA checks that `git diff d7376af0 v1.1.1` is empty. Uploads only with the owner's permission (standing order: none while the app is in review).

## Candidate, scope and builds

- **Candidate:** `hotfix/1.1.1` **d7376af0** (d7376af0510b1a40be69e53a90382a3d21d2a070), from `main` 50a6775e (tag v1.1.0), PR #173. versionName 1.1.1, versionCode 9.
- **What changed (product code):**
  - D166, `fix-offline-play-sheet`: the offline check before buy, Restore and Manage, and `ACCESS_NETWORK_STATE`;
  - D167, `fix-safety-card-into-view`: the first-10 safety card scrolls into view (#171). #174's fix, eeb69f8a, makes it scroll to the content's top.
- **Which SHA each row ran on.** Product code differs only in the safety card's scroll target:
  - `git diff a3f25cdb eeb69f8a -- ':!docs' ':!openspec' ':!**/src/test*/**' --stat` is one file,
    `features/attack/ui/.../SafetyCardReveal.kt` (11+/10−);
  - `git diff eeb69f8a d7376af0 -- ':!docs' ':!openspec'` is **empty** (d7376af0 is BRD and openspec only); and
    `git diff a3f25cdb 2c2bd1b3 -- ':!docs' ':!openspec' ':!**/src/test*/**'` was empty (4233719a and 2c2bd1b3 are tests and docs).
  - So:
    - the offline rows (H) ran on a3f25cdb and hold for the final SHA;
    - the card rows (S) ran on eeb69f8a;
    - the gate ran on a3f25cdb and on 2c2bd1b3 (QA), and on eeb69f8a (the engineer's gate3, exit 0, checked by `team-lead`).
- **Scope (owner rule, relayed by `team-lead` 2026-11-09):** a hotfix Go/No-Go covers only the rows the diff can reach. The whole-app
  regression runs nightly, separately, so the regression smoke was dropped. Everything else is carried over from
  1.1.0's GO (`docs/qa/go-no-go-1.1.0.md`) where the diff cannot reach it; see "Carried over".
- **Builds** (QA's, from detached worktrees; trees clean before and after):

| APK | SHA | sha256 (first 10) | Note |
|---|---|---|---|
| `debug-a3f2` | a3f25cdb | 18d9f1a530 | simulated store ("store for testing") |
| `r8-a3f2` | a3f25cdb | 9f371fa305 | minified, **signed with the debug key** (QA has no upload key, D102), no simulated store |
| `debug-a3f2-noperm`, `r8-a3f2-noperm` | a3f25cdb + H7 mutation | | only `ACCESS_NETWORK_STATE` removed again (`tools:node="remove"`), `negative-control-H7.diff`; local only, reverted |
| `debug-eeb6` | eeb69f8a | 86225d0760 | |
| `r8-eeb6` | eeb69f8a | b36a3fcbd4 | debug key, as above |
| `debug-c2761cba` | develop c2761cba | | **S4 negative control:** `git diff --stat v1.1.0 c2761cba` on product code is empty, so this is 1.1.0's code |

- **Emulators:** `Migrely_QA_API_36` (emulator-5580, Google APIs image, **no Play Store**) and `Migrely_QA_API_26`
  (emulator-5582). No real Google Play sheet can open on either: the "sheet" in the H rows is the debug build's simulated
  store, which runs after the same check (design D1). The real Play build on the owner's phone is H10, `team-lead`'s
  after upload.
- **Evidence root:** `migrely-work/qa-111/` beside the repo. Raw rows: `logs/t111.jsonl` (card), `logs/h111.jsonl`
  (offline), `logs/f1.jsonl` (#174). Counted by `count.py` and `judge.py`. Drivers: `t111.py`, `h111.py`, `f1.py`, `h9.sh`, `chain.sh`.

## The rows

### Build, permissions and gate (all SHAs as stated)

| # | Check | Result | Evidence (`migrely-work/qa-111/`) |
|---|---|---|---|
| 1 | Gate `assembleDebug test ktlintCheck detekt lint` | ✅ a3f25cdb: exit 0, 1992 tasks, tree clean before and after. ✅ 2c2bd1b3 (adds 2 test files): exit 0, 1992 tasks. eeb69f8a: the engineer's gate3, exit 0 (`team-lead`) | `logs/build.out`, `gate.log`, `gate-2c2b.log` |
| 1a | Lint warnings | ✅ 0 errors. 58 warnings against 1.1.0's 57: the one new warning is `ModifierParameter` in `AttackPainSection.kt`, a file this diff changes (a style warning, nothing a user sees) | `logs/lint-a3f2.txt`, `qa-110/logs/lint8f27.txt` |
| 1b | Instrumented tests the diff reaches | ✅ a3f25cdb: `app` 3/3 and `features/attack/ui` 5/5. eeb69f8a: `features/attack/ui` 5/5. 0 failed, errors or skipped (from the result XMLs). No instrumented test changed. datasource's 112 are DB tests the diff does not reach (carried over) | `logs/step-inst.out`, `step-e-inst.out` |
| H8 | `:app:checkReleasePermissions` | ✅ 1.1.0's 5 entries plus `android.permission.ACCESS_NETWORK_STATE`, nothing else. The same against 1.1.0's own R8 APK (`aapt2 dump permissions`, r8-8f27 against r8-a3f2): one added line. eeb69f8a's report is identical. **Negative control:** with INTERNET put back in the manifest, the check fails (exit 1, it names INTERNET); reverted | `logs/perm-release-a3f2.txt`, `aapt-perm-*.txt`, `perm-internet-control.log` |

### D166, offline (debug `debug-a3f2` with the simulated store; R8 as stated)

The network state of every row is Android's own (`dumpsys connectivity`: airplane mode, active network, agents), logged with the row. "At once" is the first screen read, about 2.0 s. The sheet measures the same way, so this is the measuring, not the app.

| Plan row | Result | Rows |
|---|---|---|
| H1 airplane, Wi-Fi off: Yearly, Monthly, Lifetime | ✅ the buy offline message, no sheet | 3/3 |
| H1 Restore offline, paywall and Settings | ✅ the restore offline message, never "No purchases were found" | 2/2 (plus 2/2 with Wi-Fi and mobile off) |
| H1 Manage offline: paywall, Settings, trial banner (clock +6 days, "Your free trial ends tomorrow") | ✅ the manage offline message, no page. The banner's note is inline | 3/3 (plus the banner with Wi-Fi and mobile off, 1/1) |
| H1 TalkBack (VERBOSE speech log) | ✅ buy, paywall Restore, Settings Restore, Settings Manage, paywall Manage, banner Manage: each note spoken in full (utterance completed) | 6/6 |
| H2 Wi-Fi and mobile off, not airplane | ✅ the offline message | 2/2 buy, plus the Restore and Manage rows above |
| H3 online: each buy button | ✅ the sheet opens | 3/3 |
| H4 Restore online, then offline | ✅ online "No purchases were found…" (today's answer); offline the restore message | paywall 3/3, Settings 3/3 |
| H5 airplane mode with Wi-Fi on | ✅ the sheet opens | 2/2 |
| H6 offline message, then online, tap again | ✅ message, then the sheet | 1/1 |
| Manage online (Settings, paywall, banner) | ✅ the page opens | 3/3 |
| Cached prices offline | ✅ plans and prices shown, the button enabled, the purchase line shown | 1/1 |
| Many taps offline (3, 300 ms apart) | ✅ one message, no sheet | 1/1 |
| Close guard with the offline message showing | ✅ Close answers on the first tap | 1/1 |
| **H7, negative control** (same SHA, only the permission removed) | ✅ the check can fail and fails open: offline, every buy tap opened the sheet (airplane ×3, Wi-Fi and mobile off ×2, H6a), 6/6; online and airplane with Wi-Fi as normal. Its later rows are void: with the sheet open offline, the many-taps row completed a simulated trial purchase | 6/6 |
| R8 (`r8-a3f2`) | ⚠️ partly. With no Play billing on the image, the paywall shows "Premium isn't available in your region yet", so **buy and Manage cannot be reached on R8 here**. Settings Restore offline shows the offline message 1/1, so the check works in the minified build. The R8 paywall Restore rows are void (the static region line matched the watcher) | 1/1 |
| H9 logcat | ✅ no `FATAL` and no Migrely crash in any H row, debug or R8. **Not proved:** the forced datatransport job. `h9.sh` found no Migrely job to force on R8, so no upload was attempted; 0 datatransport lines and 0 "Status Code: 200" | `logs/step-r8h9.out`, `h9-r8-a3f2-logcat.txt` |
| API 26 row | **Not run.** `team-lead` asked to finish fast; it is about 10 minutes on emulator-5582 | |

All rows: `logs/h111.jsonl`, listed by `count.py` (`logs/count-h-debug.txt`). Frames: `shots/<build>-<row>-<net>.png`.

**TalkBack and two notes in a row (residual, accepted by `team-lead`).** The raw log of the banner row shows the pattern:
- the driver's "focus" tap already activated Manage, and that note was spoken in full (completed 12:06:48.5);
- the first scripted activation (12:06:47.26) produced a SPEAK action for the same text while it was still being spoken, and TalkBack's duplicate filter dropped it;
- the second, at 12:06:57, after the first note ended, was spoken in full.

So a note that arrives after the previous one ends is spoken again, and an identical note that arrives mid-speech is dropped. The driver's taps do not map one-to-one to activations under TalkBack. That is why each row reads "spoken 1" for 2 scripted activations.

### D167, the card (S rows on eeb69f8a)

The pass rule was fixed before the eeb69f8a rows ran (`judge.py`):
- Got it's top within 6 px of the card's natural, unscrolled place (665 px at 100%, 1070 at 200%, from 1.1.0's no-scroll rows);
- the card region in the frame grabbed 1.0 s after the tap equals the settled frame (≥ 99% of pixels);
- on a failed save, the sentence region equals the no-scroll control's frame.

The same rule fails every a3f25cdb row (Got it at 644 / 1049; the sentence region 88.5%), so it can fail. TalkBack speech is read from VERBOSE logs. TalkBack's log level was ERROR on this boot, so the first 3 TalkBack rows on a3f25cdb heard nothing, including the control. They are void. The level was set to VERBOSE, and restored to ERROR at the end.

| Plan row | Result | Rows |
|---|---|---|
| S1 debug: saved and failed × 100%, 200% × animations on, off × 2 | ✅ | **16/16** |
| S1 R8 (`r8-eeb6`): saved and failed × 100%, 200%, animations on | ✅ (R8 has no `run-as`: the 10s are counted on the UI, and the saved one is removed between rows) | **4/4** |
| #174 rows: failed, 100%, sentence region against the no-scroll control | ✅ 100% equal at 1 s and 10 s (2/2 rerun rows, 4/4 S1 rows; R8 1/1). **Negative control** a3f25cdb: 88.5%, 2/2 | 7/7 |
| S2 TalkBack, saved, scrolled down | ✅ title and full sentence spoken, completed (100% and 200%) | 4/4 |
| S3 TalkBack, failed, 200% | ✅ by speech: "Couldn't save", then the title and full sentence, all completed. The 10 was not saved | 2/2 |
| Failed, 100%, TalkBack | ✅ by the spec's own rule (D167: the message lies over the title, and the sentence must be spoken in full). Message and sentence spoken and completed, title not (#166) | 2/2 |
| S4, negative control: 1.1.0's code (c2761cba) | ✅ fails as it should: the card is not on screen (saved and failed), and its title is not spoken (TalkBack spoke other lines) | 3/3 |
| S5 card already in view | ✅ nothing moves (Got it at 665), spoken | 1/1 plain, 4/4 TalkBack |
| S6 landscape | ✅ 100%: title 74 px under the area's top. **200%: not proved** (a QA driver error left the rotation in landscape for the next rows) | 1/1 |
| S7 rotate with the card scrolled away, and back | ✅ no jump back | 1/1 |
| R1 "Couldn't save" not cut off by the card | ✅ the message's utterance completed in every failed TalkBack row | 4/4 |
| R3 a drag during the 300 ms scroll | ✅ drags starting 116 and 266 ms after the tap: the card still ends at its place, and the next drag scrolls | 2/2 |
| R4 TalkBack does not jump back to Add | ✅ no "Add" or "Add pain level" spoken after the card | 12/12 |

**Rows that show the card as not visible ("vis none"), and why:**
1. **`deep-S-D400` and `deep-S-D2000`.** "D" is a drag that starts that many ms after the Add tap (R3). The scroll had ended by about 0.39 s: the 100 ms row's frame at 0.39 s already shows the card in place. So both drags come after the reveal, and moving the card away is correct. The 2 s row is the control that shows a drag works. Not covered: a drag starting between about 0.27 and 0.39 s. My pre-set R3 rule assumed the scroll window ran up to 1 s; the measurement corrects that.
2. **`deep-S-T` and `top-S-T`, the 2 rows each with vis none.** These are the 200% TalkBack rows. About 3 s after the card, a window titled "Dialog" (67–1012 × 487–1810) opens over the screen in every 200% TalkBack row, and in 1.1.0's own 200% TalkBack controls (t53). So the tree read at 14 s cannot see the card. It is a TalkBack-at-200% artefact the diff does not reach. All 4 rows spoke the title and the full sentence. **What that window is was not identified.** It is listed below.
3. **`deep-F-T`.** Of 4 rows, the 2 at 100% are valid (message and sentence spoken, as above). The 2 at 200% are marked invalid because the row's validity check needs the message in the tree at 14 s, and that same window hides it. The speech proves the failed save ("Couldn't save" spoken, 0 tens), and both rows spoke the title and the full sentence.

Evidence: `logs/t111.jsonl`, `logs/judge-eeb6.txt`, `logs/judge-a3f2.txt`, `logs/f1.jsonl`, TalkBack logs `logs/debug-eeb6-*-T-*-logcat.txt`, frames `shots/`.

## Carried over from 1.1.0's GO (`docs/qa/go-no-go-1.1.0.md`), and why the diff cannot reach them

The product diff from v1.1.0 is the offline check (subscription domain, data, presentation and UI, a datasource, DI, the manifest and the permission list), the strings, and the card's reveal in `features/attack/ui` (`AttackDetailsScreen`, `AttackPainSection`, `SafetyCardReveal`).

| 1.1.0 line | Carried over because |
|---|---|
| 2, 2a smoke on API 26 and split APKs | No resource other than 3 English strings, no locale or split config changed. **The API 26 offline row was not run** (above) |
| 3, 10 full regression, debug and R8 | The owner's rule: the whole-app regression runs nightly. The diff reaches the paywall, Settings' Premium section, Home's trial banner and the details screen, all covered by the H and S rows here |
| 4 accessibility audit | Touched UI: 3 notes (polite live regions; TalkBack speech measured above) and the card's scroll. Their contrast and size were not audited |
| 5, 6, 6a device scenarios, monkey, upgrade | No DB, migration, backup, reminder or navigation code changed. Upgrade from 1.1.0 with data kept was not run |
| 7 permissions on the release APK | Rerun here (H8) |
| 7.14–7.16, #146, #152, #119 rows | Their code (tap guards, the paywall's guard and message placement, the review request) is unchanged. The paywall's Close guard was rerun with the offline message |

## What these checks do not prove

| Row | Passing it does not show |
|---|---|
| H rows | the real Google Play sheet, account or network: the "sheet" is the debug build's simulator after the same check. On R8 only Settings Restore was reachable. A captive portal, VPN, work profile or Data Saver (D3) were not run. One emulator (API 36); not API 26 |
| H7 | that a real phone that strips the permission fails open: only that this build does |
| H9 | that datatransport's upload job fails quietly: no job was forced |
| S rows | a human hand, a real phone, other screen sizes and other TalkBack versions; 200% landscape |
| TalkBack rows | that a note activated while an identical note is still being spoken is heard (it is dropped) |
| Gate and instrumented | code coverage; the screens beyond the 8 instrumented tests run |

## Residual risks

- **H10 is not done:** the real Play build on the owner's phone, offline and online, with the owner present (`team-lead`, after upload). Until then the check is proved only against the simulator. Separately, the lead's probe on the owner's phone gave the right offline/online verdict 5/5 (`migrely-work/hotfix-111/phone-probe.txt`).
- **API 26:** the network check was not run on Android 8. A false "offline" there would block a purchase.
- **datatransport (H9):** with `ACCESS_NETWORK_STATE`, Play Billing's logger may schedule uploads that fail without INTERNET. No crash was seen, but no job was forced.
- **#166 (S3, open, 1.2):** on a failed first 10, "Couldn't save" covers the card's title at 100% and 200%. QA corrected the 200% row in the issue. The sentence is visible and spoken.
- **TalkBack duplicate filter (accepted by `team-lead`):** an identical offline note that arrives mid-speech is dropped.
- **A "Dialog" window over the screen ~3 s after the card in 200% TalkBack rows** (also on 1.1.0's code): not identified. If it is the Add dialog reopening, it would be a separate defect.
- **Public text:** "no network permissions" claims on the site, listing and video are `aso-seo-specialist`'s and `legal-finance-analyst`'s to check (design "Data safety and public text"), not QA's.

