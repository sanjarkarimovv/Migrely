# Maestro end-to-end flows

These flows test Migrely the way a person uses it: on a real Android build, through the accessibility layer, with no
test code inside the app.

## Run

```bash
qa/maestro.sh                         # every flow (regression)
qa/maestro.sh --tags smoke            # the release-gate subset
qa/maestro.sh .maestro/past-attacks   # one folder or one file
QA_SERIAL=emulator-5582 qa/maestro.sh --tags smoke   # on Android 8.0
qa/maestro.sh --install --tags smoke  # reinstall the debug APK first
QA_APK=<apk> qa/maestro.sh --install  # the build under test, for example a release candidate's (D201)
```

The script runs only on the QA emulators `emulator-5580` (AVD `Migrely_QA_API_36`) and `emulator-5582` (AVD
`Migrely_QA_API_26`), one QA script per emulator at a time (`qa/lib.sh` lock). Flows clear the app's data, so they must
never run on the dev emulator `emulator-5570` or on a personal phone. Reports (JUnit XML, screenshots, logs) go to
`build/qa/maestro/<run>/`.

It runs three passes:
1. the default timing;
2. `a11y-timeout` flows with a 20 s accessibility timeout;
3. `known-bug` flows, whose failure is the expected reproduction of an open issue: it is reported and does not fail
   the run, while a pass is reported as "NOW PASSES".

The exit status comes from passes 1 and 2 only.

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
  - `known-bug`: reproduces an open issue (the flow links it) and runs in pass 3. Remove the tag together with the fix.
  - `geometry`: taps a screen point measured on the QA AVD (API 36). It is skipped below API 29, where the navigation
    bar moves the controls.
  - `a11y-timeout`: checks what Undo does inside its window. `qa/maestro.sh` runs these in a second pass with
    Android's "Time to take action" at 20 s, which the app honours (EC-UX-16). The default 5 s window has its own
    flow, `attack-logging/undo-window-passes.yaml`.
  - `simulated-store`: buys Premium through the debug build's simulated Google Play (D201). It is skipped on a build
    that is not debuggable, such as the R8 release APK, which has no simulated store (D113).
- `a11y-tour/` and `device/` are not test suites. `qa/a11y_audit.sh` and `qa/device_scenarios.sh` run them step by
  step.
- Flows that depend on screen geometry say so in a comment and assume the QA AVD (pixel_6, 1080×2400, 420 dpi).

## Timing rules (software-GPU emulator, 1–7 s per step)

| Situation | Rule |
|---|---|
| After "Migraine started" or "Migraine ended" | Use `subflows/start-attack.yaml` or `end-attack.yaml`: they wait out the 1–1.2 s tap guard (D32) |
| Acting inside an undo window | Tag the flow `a11y-timeout`, tap Undo as the very next step with `waitToSettleTimeoutMs: 0` and `retryTapIfNoChange: false`, then check "Undone." first |
| Unchecked chip or disabled button | `checked: false` / `enabled: false` do not match when the label sits on a child. Use `assertNotVisible` with `checked: true`, or check the behaviour |
| Process death | Not with `killApp` (a force stop clears saved state). Use `qa/device_scenarios.sh` |
| Double tap | `doubleTapOn` (both taps in one quick gesture on the device). Exact gaps between taps need adb: `qa/device_scenarios.sh double_tap` |
| A fixed wait | `extendedWaitUntil` on a text that never appears, with `optional: true`; `waitForAnimationToEnd` returns early once the screen settles |
| Copying a label | `copyTextFrom` the element, then `evalScript` to cut it, for example the day of a calendar cell |
| Turning the screen | Put `setOrientation: PORTRAIT` in `onFlowComplete`, so a failed flow never leaves the device in landscape |
| Keyboard focus | `pressKey: Tab` works, but `focused: true` misses labels on a child of the focused node. Check keys in `qa/device_scenarios.sh` (`expect_focus`, `tab_until`) |
| Below the month | History's chosen day, its marks and "Show all of …" sit below the month summary: `scrollUntilVisible` before tapping or asserting them |
| Home's today card | It is one label, "Today: <sky>. <title>. <subtitle>."; assert the whole label, not "Pain-free today" alone |
| Keeping the app alive | `stopApp` and a plain `launchApp` force-stop the app, which cancels its WorkManager jobs and removes its notifications. Reminder steps use `launchApp: stopApp: false` and end with `pressKey: Home` |
| Permissions | `launchApp` grants every permission by default; use `permissions: {all: unset}` or open the app from the script when a permission dialog must appear |
| Typographic text | Some system and app texts use a curly apostrophe ("Don’t allow", "That’s") or a narrow no-break space before "PM". Match them with `.` in the regex |
| Onboarding | A fresh install shows four onboarding steps; `subflows/launch-clean.yaml` skips them |
| Premium | `subflows/buy.yaml` buys a plan (PLAN, SHEET). After onboarding the paywall shows itself while the store is available; `subflows/launch-clean.yaml` closes it, and `onboarding/paywall-after-onboarding.yaml` checks it. Store commands need adb, so they live in `qa/device_scenarios.sh` (`qa_store`) |
| The system file picker | It opens on "Recent". Use its search, then pick the file under the "Files…" heading (`subflows/import-file.yaml`); saving lands in Download |
| App language | `cmd locale set-app-locales` recreates the screen in place, so the next step may start where the app was |
