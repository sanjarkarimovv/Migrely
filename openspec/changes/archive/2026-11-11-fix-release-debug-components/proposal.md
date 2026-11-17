# Proposal

## Why

GitHub #195 (S3, pre-existing, found by `code-reviewer` during the #193 review, confirmed by `team-lead`). Release builds ship
`androidx.compose.ui.tooling.PreviewActivity` with `android:exported="true"`:

- The 1.2.0 AAB manifest (bundletool dump of `v1.2.0`) has `<activity android:exported="true" android:name="androidx.compose.ui.tooling.PreviewActivity"/>`;
  the release manifest-merger report says `ADDED from [androidx.compose.ui:ui-tooling-android:1.9.3]`. `v1.1.1` has the same build lines.
- Cause: 11 feature UI modules declared `implementation(libs.compose.ui.tooling)`; `app` and `designsystem` already used `debugImplementation`.

It is an Android Studio preview tool. Any app on the phone can start it, and it adds an entry point and dead code that a release does not need. It
cannot send data off the phone (no `INTERNET`); no crash or data access was shown (not proven on a device).

Commit 24bdf68b moved `ui-tooling` to `debugImplementation` and added `:app:checkReleaseDebugOnlyLibraries`, which looks for exact artifact names on
the release runtime classpath. `code-reviewer`: merge, with two minors. (1) `ui-tooling-android`, the artifact that actually brought the activity in
1.2.0, or any transitive path, passes that check. (2) No BRD or spec covered the new release-build behaviour. The lead decided to check the outcome,
the release merged manifest, instead of library names (BRD D172).

While here: the live `release-build` spec still says "no network state", which D166 replaced in 1.1.1 by allowing `ACCESS_NETWORK_STATE` for the
offline check only.

## What Changes

- One release guard in `:app` reads the release variant's merged manifest, the same artifact `CheckPermissionsTask` reads, and fails
  `assembleRelease` and `bundleRelease` when any listed debug-only component is declared, naming it and the fix.
- The list starts with `androidx.compose.ui.tooling.PreviewActivity` (ui-tooling), `androidx.activity.ComponentActivity` (the exported entry that
  ui-test-manifest adds) and `com.migrely.app.datasource.billing.datasource.SimulatedBillingReceiver` (the debug simulated store, BRD D113; added by PM
  for money risk).
- `checkReleaseDebugOnlyLibraries` and its artifact-name list are removed: one guard.
- The 11 `debugImplementation` lines from 24bdf68b stay. Debug builds keep previews, Compose tests and the simulated store.
- `docs/ARCHITECTURE.md` §4 names the new guard and says it reads the merged manifest.
- The `release-build` spec's permission requirement matches D166.

## Out of scope

- Shipping this in 1.2.1 (kept to the #193 fix, issue #195). It goes out with the next normal release.
- Any other exported component in the release manifest (for example WorkManager's `DiagnosticsReceiver`, `ProfileInstallReceiver`,
  `SystemJobService`): they are release components of shipped libraries, guarded by their own permissions, and not debug tools.
- Checking classes in the dex (for example a preview class kept without a manifest entry): R8 already drops code nothing references; the guard is about
  entry points.
- Version bump, release notes, the release branch.
- Any UI, copy, data, schema or permission change. No `ux-designer` or `medical-reviewer` work.
- Closing #195: the reporter (lead) retests and closes (`Refs #195`).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `release-build`: a release build carries no debug-only component, checked on the merged manifest; the permission requirement follows D166.

## Impact

- `app/build.gradle.kts`: the guard task and its list replace `CheckDebugOnlyLibrariesTask` and `debugOnlyLibraries`; wiring to `assemble` and
  `bundle` of non-debug variants. No Kotlin product code, no manifest, no dependency change beyond 24bdf68b.
- `docs/ARCHITECTURE.md` §4 step 4 wording.
- BRD: D172, §9.14 EC-REL-01…06, §13 row 41. Issue #195.
