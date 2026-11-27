# Release Go/No-Go: Migrely 1.4.0 (versionCode 16)

## Decision

**QA (qa-140), 2026-11-25: GO** for `release/1.4.0` @ **87d9e187465667f2b53303ea39c1e00462dc7664**.
The record lists what these checks do not prove and the open risks.

- **New content, each accepted by QA on debug builds:**
  - #255 (QA GO, record on develop);
  - #278 (GO at ede18a75, closed after the develop retest on 64c3a675);
  - T72 word-of-mouth loops (GO at 69dff460);
  - T23 German (GO at 7b200368, the same code as 2b51ae7e).
- **The release diff over develop 860d03e4 is the version bump and the release notes.** No product file changed.
- **What this Go/No-Go adds is R8.** None of the four GOs ran on the release variant. Every row below ran on QA's own
  R8 build of 87d9e187 (sha256 `61ebd9b7…`), on API 36 and API 26, plus the update from the shipped 1.3.3. All results
  pass, or are void rows that passed when rerun (each named below).
- **No S1 or S2 is open** (`gh issue list --state open`, 2026-11-25). No new issue was filed.

## Candidate, scope and builds

**What the release diff reaches (checked by QA with `git diff --name-only`, 2026-11-25):**

| Range | Paths |
|---|---|
| develop 860d03e4 (= the T23 merge) → 27e0533f | `app/build.gradle.kts` (`versionCode = 15 → 16`, `versionName = "1.3.3" → "1.4.0"`), `docs/release/whats-new.md` |
| 27e0533f → 87d9e187 (775780e6, 87d9e187) | `docs/release/whats-new.md` only: two bullets narrowed by medical-reviewer, and its PASS recorded |

**Release notes:** the two narrowed claims are backed by accepted rows. "TalkBack moves in order on most scrolling pages
and dialogs" is #255. "At very large text, you can reach the "Still going?" card and all of the pain and effect
dialogs" is #274 and #276 (T23 A-01/A-02, closed by QA). Both are re-checked on R8 here (R8-6).

**Scope (owner rule 2026-11-09):** only what the diff reaches, with negative controls. The bump changes no behaviour.
R8 can change behaviour that the debug builds never showed: renamed or removed classes, shrunk resources (German
strings, the localeConfig file), and reflection paths. So one row per new mechanism ran again on the R8 build. The rows
moved from T23 to the release (M-1 to M-4) ran here too.

**Builds (QA's own):**
- Worktree `migraine-app-qa-rel140`, detached at 87d9e187. Module build folders were cleared on the commit switch.
  The tree was clean before and after (`git status --porcelain`: 0).
- `nice -n 19 ./gradlew --max-workers=2 :app:assembleRelease` → BUILD SUCCESSFUL in 1 m 35 s, exit 0.
  `:app:checkReleasePermissions` ran in the same build. `:app:assembleDebug` → exit 0.
- There is no `keystore.properties` in the QA worktree, so the APK is signed with QA's debug key, as in 1.3.0 to 1.3.3.

| APK | sha256 | Use |
|---|---|---|
| `r8-87d9e187` (candidate) | `61ebd9b7cff8c9f788112e944bfba69509fa45c7cdf8095b881e5ac8a7b71065` | every candidate row |
| `debug-87d9e187` | `7d286f8c24720e15…` | S-4 control; instrumented tests |
| `r8-d023ba76` (1.3.3 R8, tree = `v1.3.3`) | `0c892c21163f1696…` | update base; controls (S-2, S-3, S-5, M-4) |

## Static rows

| Row | Result |
|---|---|
| S-1 Identity (`aapt2 dump badging`) | `com.migrely.app`, **versionCode 16, versionName 1.4.0**, not debuggable. The debug APK reads debuggable=1, which shows the reader sees the flag |
| Identity on the device | every row logs the installed base.apk sha256 = `61ebd9b7…` (host and device equal at each install) |
| S-2 Permissions | `aapt2 dump permissions` equals the 1.3.3 R8 (sorted diff, exit 0): POST_NOTIFICATIONS, ACCESS_NETWORK_STATE, BILLING, WAKE_LOCK, RECEIVE_BOOT_COMPLETED, DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION. **No INTERNET** (0 lines) |
| S-3 Merged manifest | against the 1.3.3 R8, with line numbers and resource ids ignored: versionCode 15 → 16, versionName 1.3.3 → 1.4.0, and T23's `android:localeConfig`. Nothing else |
| S-4 `qa/release_apk.py` | "Release APK ok: no simulated store, not debuggable", exit 0. Control: the debug APK fails it (exit 1; code 57, resources 2, manifest 4) |
| Signer | candidate and 1.3.3 R8 share certificate SHA-256 `7eae4e48…` (QA's debug key), so the update is a real `install -r` |
| S-5 R8 keeps the new code | mapping.txt: `OpeningGuard` → `dh.p`, `ExternalAppLauncher` → `dh.o`, `UnsavedMarksDataSourceImpl` → `wb.c`, `ScrollingDialogContentKt` → `oc.j`, `SystemLocale` → `ke.g`, `FormattingLocaleResolverKt` → `ke.a`, `ReportPageLayout` → `dc.g`, `SetReportCreditUseCase` → `cg.c`. R8 removed only inlined classes (two Hilt `@Provides` factories, `ReportPageLayoutKt`, the `RangeHeadline` composable's file). Their behaviour is covered by the device rows. A dex search by name finds nothing in R8, so the mapping is the evidence |
| S-5b Resources after shrinking | 851 strings in the R8 APK. **0** of them lack a German value that the debug APK has. 0 app-own strings are missing (the 116 German names dropped are unused library strings, `abc_*` and the like). The localeConfig file is kept (`res/Br.xml`: en, de) |
| S-6 OpenSpec | `openspec validate --all --strict` at 87d9e187: 31 passed, 0 failed |
| S-7a Lint | carried from the engineer's gate at 27e0533f (`assembleDebug test ktlintCheck detekt lint :app:checkReleasePermissions`, clean tree, BUILD SUCCESSFUL, GATE_EXIT=0, 4008 tests; `:app:lintDebug` ran). 87d9e187 differs from it only in `docs/` |
| S-7b Instrumented | see "Instrumented tests" below: 251 tests, 1 failure = #277 (S4, pre-existing) |
| S-8 After the tag | `git diff 87d9e187 v1.4.0` must be empty; QA checks it when the tag exists |

## R8 rows, API 36 (emulator-5580, Migrely_QA_API_36)

Drivers: the T72, T23 and #278 acceptance drivers, unchanged, run through `migrely-work/qa-140/bin/rel_rows.py` with
`QA_R8=1`. That shim makes every `run-as` read a root read (adbd root), and puts the evidence in `qa-140/rel140/`.
The device has no Play Store (google_apis image), so billing reads "unavailable". The app then keeps a stored
entitlement, so Premium was given by writing a Lifetime entitlement file with the app stopped (R-PREM-0 proves it
holds). The loads given are the 1-minute host load when each row ran.

| Row | Result | Load |
|---|---|---|
| R8-1 Smoke | `QA_APK=<R8> qa/maestro.sh --install --tags smoke`: **17 of 17 flows passed** (14 + 3), exit 0 | 5.7 at start |
| MK-1 Monkey | `qa/monkey.sh 20261024 5000` on the R8: **0 crashes, 0 ANRs**, finished. Monkey switched Do Not Disturb on; the script restored it | 7.7 |
| R-PREM-0 Premium on R8 | report screen with "Create report" and no paywall after two cold starts; the entitlement file still Lifetime | 7.7 to 16 |
| R-01/R-02 Report credit (medical-reviewer condition) | same seed, switch off then on: `pdfcredit.py` passes both PDFs, 2 pages each, disclaimer on every page, credit on the last page only | 5.2 |
| S-01 Share | one system chooser, **text only** (no URI, no grant), exact text and link | 7.3 |
| S-01c Negative control | the report's own PDF share reads as NOT text only (the check can fail) | 6.9 |
| S-02 Double tap guard | 3 runs of two touches 108 to 120 ms apart: one chooser each | 5.0 |
| S-03 Share then Rate | only the share sheet starts | 5.6 |
| S-04 Rate without the Play app | the https page opens in Chrome; no review flow | 6.4 |
| S-05 Nothing can open Play | Play and Chrome disabled: the message shows, the app stays alive, a second tap shows it again | 6.7 |
| R-05 EC-REP-12 (#278) | full disk (root fill to 16 MB free): tapping the credit switch shows the message, the switch returns to the stored value, unchanged after a restart | 7.7 |
| U-3b Per-app German across the update | v1.3.3 R8 used once, its per-app language set to de-DE, the candidate installed over it: German at once, migraines kept | 10.1 |
| R8-2 Per-app German | Home calm, attack mode, Log medication (logged), Settings/Delete, History month, "Did it help?" at 360 dp 200%: German captured, then English. **T01: 0 English in every German capture.** No "…". a11y audit 0 failures, except History's day cells 45 dp wide (#283, S4, pre-existing) | 5.3 to 7.2 |
| R8-3 System German | the system language set to de-DE by root and read back from the framework (X-00): onboarding German. The share sheet in German (X-5): text only, German title, the English share text as designed. The static shortcut resolves to **"Migräne beginnt"** (N04). The system is back to en-US, read back (X-99) | 4.8 to 7.1 |
| R8-5 Full disk in German (X-7) | the credit switch and Track periods each show the German "not saved" message and keep the stored value; with Play and browsers disabled, the message shows. **The first run was void** (report-open did not find "Opens History"; load 5.2). It passed when rerun at load 3.0 | 3.0 |
| R8-6 #274 reach (A-01) | 360 × 640 dp, 200%, German: the "Still going?" card's title and body, the rest line, the start time and Details are all reachable | 14.1 |
| R8-6 TalkBack pain dialog (L-01) | TalkBack (log level VERBOSE, then ERROR, read back), German, p411 at 100% and l360 at 200%: the title is spoken once, with no bare "Dialog" | 4.7, 3.9 |

### Rows moved from T23

| Row | Result |
|---|---|
| M-1 Light theme, the dialog's edge | Light theme (written with the app stopped), a **past** migraine's details, the pain dialog. At 640 × 360 dp and 200% (content overflows), in English and German: the dialog is light, and a 1 dp divider (3 px at 480 dpi, colour 200,197,190) runs the dialog's width above the buttons, where more content lies. In the fitting control (100%, portrait) there are **0** divider-colour pixels. The row's automatic light check printed "fail" only because its sample points fell on the dimmed screen around the small dialog in the control; the pixel check above is the judgement. The fade is the surface colour over an empty surface, so it cannot be seen, and is not proven |
| M-2 Notifications in German | "Did it help?" ("Hat es geholfen?" / "Tippe, um in Migrely zu antworten.") and the visit reminder ("Dein Termin ist am Sa., 28. Nov." / "Erstelle einen Bericht zum Mitnehmen.", posted 10:00 two days before, per D168) at 360 dp and 200%: shown expanded in the shade with the full text, no ellipsis. Each is `vis=PRIVATE` with a public version |
| M-3 Insights with Premium data | Premium on R8 (R-PREM-0) plus the `insights-patterns` seed, German at 360 dp, 200% and 130%: words whole ("Migräneattacken" unbroken), T01 0 English, audit 0 failures |
| M-4 12.3 control | v1.3.3 R8 on the same German system: English stays (Settings, Today, Migraine started; no German word). The candidate on the same system is German (R8-3) |

## Update from 1.3.3 (vc15) to 1.4.0 (vc16), R8 to R8

Driver: `migrely-work/qa-140/bin/uprel140.py`, the 1.3.3 GO's `uprel133.py` with its paths moved (checks unchanged).
Seeding on a fake date 2026-11-10: 1.3.3 R8 installed fresh, then seeded through the UI:
- two migraines, Severe, Nausea, a custom symptom;
- Ibuprofen with intakes at 09:00 and 09:05;
- the daily reminder on;
- theme Light.

Then a 1.3.3 backup is exported, and the candidate is installed with `install -r` **without opening it**.

| Row | API 36 | API 26 (emulator-5582) |
|---|---|---|
| Builds read back | before `0c892c21…`, after `61ebd9b7…` "Success", not debuggable | same |
| code_cache mark before the update (#189/#193 path) | pass | pass |
| Database: schema 7 → 7, **every table's content equal** (sha256 per table) | pass | pass |
| `migrely_preferences.xml` byte-equal | pass | pass |
| Pending "Did it help?" across the update (clock 11:06, app not opened) | posted 11:06:01 | posted 11:06:04 |
| Kept-records flow on 1.4.0 | pass | pass |
| Next day's daily reminder (clock 20:00, app not opened) | posted 20:01:12 | posted 20:00:02 |
| Crash buffer after the update | 0 lines | 0 lines |
| The 1.3.3 backup imported into a clean 1.4.0 | every count equal, file schema 7 | not run (driver skips it on API 26) |
| U-3 / A26-3 first open | Settings shows "Share Migrely" and "Rate Migrely" | system de-DE: German at once, migraines 2 → 2 |

The first API 36 update attempt was void: the seed flow named a removed worktree (harness). It passed when rerun.
Loads were 2.2 to 7.6. The clock went back to the host's time with auto_time 1.

## API 26 (emulator-5582, Migrely_QA_API_26), R8

Android 8.0 has no per-app language, so German here means the system language, set by the emulator's Custom Locale
app and read back.

| Row | Result | Load |
|---|---|---|
| A26-3 Update | in the update table above: every check passes | 3.4 to 5.6 |
| A26-3 First open in German | after the update, the system set to de-DE: German at once ("Heute: ruhig…", "Einstellungen"), migraines 2 → 2 | 3.3 |
| A26-1 German screens | Home calm, attack mode, Log medication (logged), Warning signs, History month; at 360 dp 200% and at the AVD's own size 100%, German then English. **T01: 0 English in every German capture**, no "…". Audit: History's day cells 45 dp (#283). The audit also printed "contrast 1.00:1, 0 glyphs" on three screen titles in English and German alike: on this image the screenshot under a `wm size` override has black bars, so the audit sampled the bar. The captures show the titles clearly (harness, not product) | 3.1 to 5.5 |
| A26-2 Share | one chooser, **text only**, no grant | 5.5 |
| A26-2 Rate without the Play app | the market and web intents start and nothing is written; the screen ends on the launcher. The image's only browser has crashed on start before, so whether the page shows is not proven on API 26 (it is on API 36, S-04) | 5.9 |

## Instrumented tests (`connectedDebugAndroidTest`, every module, 87d9e187)

- **On the QA AVD with its usual software renderer (swiftshader):** 251 tests, 6 failures:
  - #277 (S4, pre-existing);
  - `DialogLiftOnScreenTest` ×1: passed 2/2 when rerun alone;
  - `MigrelyIconButtonPressedTest` ×4: failed again in 2 reruns, 4 of 5 each time.
- **Disproved as a product defect:** the same AVD cold-booted with the host GPU (`-gpu host`, Apple M4 Pro, GLES 3.0):
  - `MigrelyIconButtonPressedTest` passes 5/5 twice;
  - the full suite then reads **251 tests, 1 failure = #277**.
- The pressed-state test compares two platform ripples pixel by pixel. That depends on the renderer: it is how the
  engineer accepted it on host graphics (#255 task 6.4). The test is unchanged since 2026-11-20, so the release diff
  does not reach it.

## Go/No-Go lines (`docs/QA.md`)

| Line | Here | Why |
|---|---|---|
| 1 Build gate | carried from 27e0533f (engineer), docs-only since | QA built debug and R8 itself (exit 0) |
| 1b Instrumented | 251, 1 failure = #277 (host GPU) | above |
| 2 Smoke | R8 API 36: 17/17 flows; API 26: the A26 rows above (no Maestro smoke there) | |
| 3 Full regression | not per release | owner rule 2026-11-09 |
| 4 Accessibility | R8-2, R8-6, M-1 to M-3, A26-1; the audit was rerun per capture at its own density (`bin/caps_audit.py`) | the first combined check read a 420 dpi capture as 480 dpi and flagged M-1's buttons at 42 dp; at their own density they are 48 dp |
| 5, 6a Update | 1.3.3 → 1.4.0 on API 36 and API 26 | |
| 6 Monkey | R8, API 36: 0 crashes, 0 ANRs | |
| 7 Permissions | equal to 1.3.3, no INTERNET | |
| 8 Open defects | `gh issue list --state open`, 2026-11-25: **no S1 or S2**. S3: #148, #179, #230, #233, #252, #264, #269, #272, #273, #275, #279. S4: #82, #109, #118, #144, #149, #150, #153, #224, #229, #236, #250, #254, #260, #262, #277, #280, #283 | |
| 11, 12 Pre-launch report, internal testing | not run | Play steps are the owner's |

## Carried over, and why the release diff cannot reach them

| Record | Carried | Why |
|---|---|---|
| #255 QA record (develop) | every row not re-run here | same product code (develop 860d03e4 → 87d9e187: the bump and docs) |
| #278 (ede18a75, retest 64c3a675) | every row except R-05 and X-7 | same product code |
| T72 (69dff460) | every row except S-01 to S-05, R-01/R-02, R-05 | same product code |
| T23 (7b200368 = 2b51ae7e) | every row except R8-2, R8-3, X-5, X-7, N04, A-01, L-01 (pain) and M-1 to M-4 | same product code |

## Known limits shipped with 1.4.0

- From T23: #279 (S3; with TalkBack at 200% in landscape, the pain dialog's time row is skipped); a per-app language
  change made while the app stays in front applies at its next start; TalkBack puts its own English frame around German
  labels when only the app is German.
- #280 (S4): Rate and Share start other apps inside Migrely's task.
- #283 (S4) and #275 (S3), pre-existing: History's calendar at 360 dp and 200%.

## What these checks do not prove

- **Play signing, Play Billing and the Play build.** QA installed its own R8 APK, signed with QA's debug key. The
  production build is made from the AAB and signed by Play. The source is tied to production only by the tree rule:
  `git diff 87d9e187 v1.4.0` must be empty after the tag.
- **Real Premium.** Premium on R8 came from a stored entitlement on an image without Play. A real purchase, Play's
  answer "not owned" (which would remove it), In-App Review and the Play page in German were not run (promo codes are
  7.3, the owner's).
- **Real phones and real TalkBack users.** Every row ran on emulator images. TalkBack ran on API 36 only, for the pain
  dialog. A human ear did not judge German TalkBack speech.
- **Rates.** The double-tap guard was tested 3 times, and each TalkBack row once. Timing faults rarer than that would
  not show.
- **M-1's fade, and the collapsed notifications.** The fade is invisible over an empty surface. The collapsed one-line
  notification was not captured. M-3 captured the top of Insights only, not its lower parts.
- **API 26 coverage is short by design:** no Maestro smoke, no monkey, no TalkBack and no Premium rows ran on API 26;
  the API 26 R8 rows are the update, German screens, Share and Rate.
- **The update** ran from QA's 1.3.3 R8 (the same tree as `v1.3.3`), not from a Play install. Reminders were shown by
  moving the clock, on emulators that do not deep-sleep.
- **Loads.** Some rows ran at a host load of 8 to 16, from other projects (R-PREM-0, U-3b, A-01). They are layout or
  data rows, not timing rows. The M-3 captures were taken at a load of about 40: they are still images.

## Residual risks

- **Production must get the tagged tree:** `git diff 87d9e187 v1.4.0` must be empty after the tag.
- **#279** reaches German and English TalkBack users at 200% in landscape (S3, known).

## Device state at the end

- **emulator-5580:** restored to the run's baseline; the readback diff is empty (font 1.0, user_rotation 0, no `wm`
  override, night mode as found, auto_time 1, airplane off, no package left disabled, per-app language cleared).
  - The system language is en-US, read back from `persist.sys.locale` and the framework's configuration.
  - TalkBack is off, and its log level is ERROR (read back on a later boot: "before: ERROR").
  - adbd is shell.
  - For the instrumented control, the emulator was cold-booted once with `-gpu host`, and then shut down with
    `emu kill`.
- **emulator-5582:** the readback diff is empty.
  - The system language is en-US, font 1.0, rotation 0, no `wm` override, auto_time 1, and the device and host epoch
    are equal.
  - adbd was unrooted and read back as shell, then the emulator was shut down with `emu kill`.
- Both were run one at a time and are off. No other emulator was touched.

Evidence (outside the repo), in `migrely-work/qa-140/`:
- `rel140/` and `rel140-26/`: `logs/rows.jsonl`, per-row logs, `caps/`, `pdf/`, `shots/`;
- `apk/`: APKs, sha256 files and the R8 mapping;
- `rel140/logs/static.log`, `S-7-*.log`;
- the plan `rel140-plan.md`.
