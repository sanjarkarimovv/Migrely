# Design

Read from `bugfix/195-preview-activity-release` 24bdf68b. Components were listed from the worktree's merged manifests built at that commit
(`app/build/intermediates/merged_manifests/{debug,release}/…/AndroidManifest.xml`, 2026-11-11): debug has `PreviewActivity`,
`androidx.activity.ComponentActivity` and `SimulatedBillingReceiver`, all exported; release has none of them. The app's own activity is
`com.migrely.app.MainActivity`.

## Decisions

### D1. Check the outcome, not the inputs

| Option | Verdict |
|---|---|
| Read the release merged manifest and fail on a listed component | **Chosen** (lead). The manifest is what ships in the APK and the AAB base module, whatever the artifact is called |
| Exact artifact names on the runtime classpath (24bdf68b) | Removed: `ui-tooling-android`, the 1.2.0 culprit, passes; so would a transitive path |
| Both checks | Rejected: two lists that age apart (lead: one guard) |
| Artifact name prefixes | Rejected: still blind to another group's transitive dependency |

### D2. Which manifest and when

`variant.artifacts.get(SingleArtifact.MERGED_MANIFEST)`, as `CheckPermissionsTask` reads it. Registered for every variant whose build type is not
`debug` (today only `release`), as `check<Variant>DebugComponents`, and made a dependency of `assemble<Variant>` and `bundle<Variant>` (the bundle
does not depend on assemble, BRD D44). Not registered for debug, so debug builds never fail on it. A missing or unreadable manifest fails the task
(`@InputFile`), never a silent pass (EC-REL-05).

### D3. Matching

A component is an `activity`, `activity-alias`, `service`, `receiver` or `provider` element; it matches when its fully qualified `android:name` equals
a list entry exactly. Exact names, never prefixes: `androidx.activity.ComponentActivity` as a prefix would hit unrelated classes. The merged manifest
has fully qualified names and no `tools:` attributes, so a component removed with `tools:node="remove"` is absent and passes (EC-REL-06): it does not
ship. Parsing is the engineer's choice (XML parser or the same regex style as `CheckPermissionsTask`), as long as attribute order and line breaks do
not matter. The task writes the release component names to a report under `build/reports/`, like the permission report.

### D4. The list

In `app/build.gradle.kts`, one entry per line, each with a comment naming its source:

- `androidx.compose.ui.tooling.PreviewActivity`: `androidx.compose.ui:ui-tooling` (any artifact name).
- `androidx.activity.ComponentActivity`: `androidx.compose.ui:ui-test-manifest`, for the Compose test rule.
- `com.migrely.app.datasource.billing.datasource.SimulatedBillingReceiver`: `app/src/debug/AndroidManifest.xml` (BRD D113). Added by PM: a simulated
  store in release could unlock Premium for free (money). Today only a source-set mistake could bring it in.

The error names every match and says: declare the library with `debugImplementation`, or keep the component in `src/debug`.

## Risks

- A future release-only library might legitimately declare `androidx.activity.ComponentActivity`. None does today; the build would fail loudly and
  the list entry would be revisited, which is the safe direction.
- The guard proves the manifest, not the dex. A debug class kept without a manifest entry is not an entry point; R8 normally drops it.
- Negative controls are manual edits that must be reverted; tasks require `git diff` to be empty afterwards.
