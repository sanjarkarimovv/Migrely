# Maestro end-to-end flows

These flows test Migrely the way a person uses it: on a real Android build, through the accessibility layer, with no
test code inside the app.

## Run

```bash
qa/maestro.sh                         # every flow (regression)
qa/maestro.sh --tags smoke            # the release-gate subset
qa/maestro.sh .maestro/past-attacks   # one folder or one file
qa/maestro.sh --install --tags smoke  # reinstall the debug APK first
```

The script runs only on the QA emulator `emulator-5580` (AVD `Migrely_QA_API_36`). Flows clear the app's data, so they
must never run on the dev emulator `emulator-5570` or on a personal phone. Reports (JUnit XML, screenshots, logs) go to
`build/qa/maestro/<run>/`.

## Conventions

- **One folder per OpenSpec capability.** Each flow lists the scenarios it covers in `# Covers:` lines, written as
  `<capability> › <requirement> › <scenario>`, so the coverage of `openspec/specs` can be traced.
- **Selectors are what a person perceives:** visible text first, then the spoken label (content description). If a
  flow cannot find a control by its label, TalkBack users cannot either, which is a bug, not a reason to add a
  `testTag`.
- **Every flow starts from a clean install** (`subflows/launch-clean.yaml`) and builds its own data through the UI.
- **Tags:**
  - `smoke`: fast and must pass for every release candidate;
  - `regression`: everything;
  - `known-bug`: expected to fail until the linked issue is fixed. Remove the tag together with the fix.
- Flows that depend on screen geometry say so in a comment and assume the QA AVD (pixel_6, 1080×2400, 420 dpi).
