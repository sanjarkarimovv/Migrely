# Tasks

Base: `hotfix/1.1.1` from `origin/main` 50a6775e (tag v1.1.0). `Refs #36` in the commits and the PR. Ownership: `android-engineer` for code, version and
build, `qa-engineer` for H1 to H9, `team-lead` with the owner for H10.

## 1. Spike first (device facts)

- [x] 1.1 On `emulator-5570` and, with the owner present, on the owner's phone, log what Migrely sees with the permission and no `INTERNET`:
  - `getActiveNetwork()`, its capabilities, and the other networks;
  - online Wi-Fi, online mobile, airplane mode with Wi-Fi on, airplane mode with everything off, Wi-Fi and mobile off.
  Verify: online, at least one network has INTERNET in every online row. If not, stop and report to `team-lead`, because the check would block purchases. Done: `emulator-5570` 6 of 6 rows as expected (`migrely-work/walks/hotfix-111/spike-5570.txt`), the owner's phone 5 of 5 (3 online, 2 offline, `migrely-work/hotfix-111/phone-probe.txt`).

## 2. Datasource and data

- [x] 2.1 `NetworkStateDataSource` (interface in `datasource/source`, Android implementation): online, offline or unknown, per design D1, with exceptions
  giving unknown. Verify: unit tests for each branch, including a thrown `SecurityException` giving unknown. Done: 7d185255, in the gate.
- [x] 2.2 Repository and DI binding. Verify: `./gradlew :features:subscription:data:test` passes. Done: 7d185255, in the gate.

## 3. Domain

- [x] 3.1 The buy, restore and manage use cases return an `Offline` outcome before any billing call when the source says offline, and go on as today for
  online or unknown. Verify: domain tests show billing is never called on offline, and is called on unknown. Done: 7d185255, in the gate.

## 4. Presentation and UI

- [x] 4.1 `PaywallViewModel`, `PremiumSectionViewModel` and `TrialBannerViewModel` map `Offline` to their offline notification. Verify: ViewModel tests for
  each of the 7 paths. Done: 7d185255 and 4233719a, in the gate.
- [x] 4.2 The three strings, information kind, polite, 8 s, placed per design D2. The trial banner's place gets `ux-designer`'s light copy and placement
  check. Verify: UI tests show the text, and a semantics test shows the polite live region. Done: the trial banner uses an inline polite note under Manage (`team-lead` decision, design D2). Walks: `debug-a3f25cdb-banner-manage-*`, `settings-*` in `migrely-work/walks/hotfix-111/`.

## 5. Build and release

- [x] 5.1 Remove `ACCESS_NETWORK_STATE` from `forbiddenPermissions`, delete its `tools:node="remove"` line, and rewrite both comments (D166). Verify:
  `./gradlew :app:checkReleasePermissions` passes, and `diff` of its report with 1.1.0's
  (`android.permission.POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, WAKE_LOCK, com.android.vending.BILLING, com.migrely.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`)
  shows exactly one added line. Done: bdf97706. `permissions-before.txt` to `permissions-after.txt` in `migrely-work/walks/hotfix-111/` adds exactly `android.permission.ACCESS_NETWORK_STATE`.
- [x] 5.2 versionCode 9, versionName 1.1.1, and the 1.1.1 block in `docs/release/whats-new.md` from design "Release notes", 500 characters at most.
  Verify: `grep -n "versionCode = 9" app/build.gradle.kts`, and a character count of the block. Done: bdf97706 and 4233719a.
- [x] 5.3 `docs/ARCHITECTURE.md` line on datatransport's permissions. Verify: `grep -n ACCESS_NETWORK_STATE docs/ARCHITECTURE.md` shows the new wording. Done: bdf97706.

## 6. Gate, walk, review

- [x] 6.1 The gate. Verify: `set -o pipefail; nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exits 0. Done: `gate.exit` 0 at a3f25cdb and `gate2.exit` 0 at 4233719a in `migrely-work/walks/hotfix-111/`.
- [x] 6.2 A walk on `emulator-5570`: H1, H3, H5 and H9 on debug. Restore airplane mode and Wi-Fi afterwards. Verify: the results are in the packet. Done: H1 to H6 shots at bae78209 and H9 `H9-datatransport-logcat.txt` (0 FATAL) in `migrely-work/walks/hotfix-111/`. Negative controls fail as they should: `nc1-no-check-offline-buy.png`, `nc2-no-permission-offline-buy.png` (Play's sheet opens).
- [x] 6.3 `code-reviewer`. Verify: no open blocker. Done: the review of PR #173 has no blocking finding (eng-hotfix's packet).

## 7. QA and release

- [x] 7.1 `qa-engineer` runs H1 to H9 on the release candidate's exact SHA, H7 on a local negative-control build. Verify: a Go/No-Go in `docs/qa/`. Done
  (record 2026-11-14, T63): `docs/qa/go-no-go-1.1.1.md` (PR #184, develop cbd364c2), GO for `hotfix/1.1.1` d7376af0. The H rows ran
  on a3f25cdb, whose product code equals d7376af0 (`git diff eeb69f8a d7376af0 -- ':!docs' ':!openspec'` empty, and a3f25cdb to
  eeb69f8a touches only `SafetyCardReveal.kt`). H1 to H6 pass on debug, H7 opens the sheet offline 6/6, H8 is 1.1.0's 5 plus
  `ACCESS_NETWORK_STATE` with the INTERNET control failing, API 26 6/6. Not proved, accepted in that GO: H9's forced datatransport
  job, buy and Manage on R8 (no Play billing on the image), H5 on API 26.
- [x] 7.2 `aso-seo-specialist` and `legal-finance-analyst` check the items in design "Data safety and public text" before Production. Verify: their packets. Done
  (record 2026-11-14, T63): `legal-finance-analyst`'s draft `reports/hotfix-111-legal-permission-check.md` and `aso-hotfix`'s
  `migrely-work/hotfix-111/aso-permission-claims.md`, both 2026-11-09, outside the repo. Data safety answer: no change. Their
  fixes merged in PR #176 (develop 3170865b: the sixth permission on /privacy-check/, the policy paragraph "Checking whether you
  are online", "no network access" changed to "no internet permission"), deployed to migrely.health 2026-11-11 (deployment
  877da238). Not done: no one has read the live Console Data safety form since (legal item 5.3), and `docs/release/data-safety.md`
  still has no 1.1.1 line (legal item 1.6). Both are sent to `legal-finance-analyst`; neither blocks this archive.
- [x] 7.3 Owner's phone H10, then Git-flow release: PR into `main`, tag `v1.1.1`, merge into `develop`. Uploads only with the owner's permission. Verify:
  the tag, and `git diff <built-sha> v1.1.1` is empty. Done (record 2026-11-14, T63): PR #173 merged into `main` as
  64e08db8, annotated tag `v1.1.1` on it, merge-back PR #175 (develop 01205cc4, second parent d7376af0). The AAB
  `Migrely-1.1.1-9.aab` (sha256 cc1c0088…) was built from d7376af0 (`migrely-work/release/111/aab/Migrely-1.1.1-9.commit.txt`), and
  `git diff --quiet d7376af0 v1.1.1` exits 0. H10 on the owner's phone from Play internal testing, 2026-11-09 14:34–14:45, owner
  present: H1 3/3, H3, H4, H5, Settings Restore, Close, offline cold start, 0 crash lines (`migrely-work/phone-111/EVIDENCE.md`);
  not run there: Manage, the trial banner, a completed purchase. Live on Play since 2026-11-13: the store page returns 200 and
  shows version 1.1.1 (checked again 2026-11-14).

## 8. Archive

- [x] 8.1 Sync the release-build delta before archiving (T63, 2026-11-14). `fix-release-debug-components` (archived 2026-11-11) already
  rewrote "Release permissions" with D166 in it, so this delta now keeps that newer text and scenario and adds only "INTERNET comes
  back". The 1.1.1-only scenario "One permission more than 1.1.0" is dropped: the 1.1.1 Go/No-Go (H8) holds its result. Verify:
  `openspec validate --specs --strict` exits 0 after the archive.
