# Tasks

## 1. Fix

- [x] 1.1 Test first: `openInBrowserIfAny` returns quietly when the handler throws `IllegalArgumentException`, and
  opens the address when a browser is there. It failed to compile before the fix
- [x] 1.2 UI: `UriHandler.openInBrowserIfAny`, used by `PolicyLink`

## 2. Verify

- [x] 2.1 Gate: `assembleDebug test ktlintCheck detekt`
- [x] 2.2 `openspec validate fix-privacy-link-no-browser --strict`
- [x] 2.3 On an emulator with Chrome disabled, tap "Read the full privacy policy": the app keeps running and the
  address stays on the page; with Chrome enabled, the page opens

## Verification record

- **Cause:** confirmed with `javap -c` on `ui-android` 1.9.3 `AndroidUriHandler.openUri`. Its exception table catches
  `ActivityNotFoundException`, and it then throws a new `IllegalArgumentException(String, Throwable)`. The old catches
  (`IllegalStateException`, `ActivityNotFoundException`) never ran.
- **Tests:** `PolicyLinkTest` was written first and failed to compile ("Unresolved reference 'openInBrowserIfAny'").
  After the fix: 2/2.
- **Gate:** `assembleDebug test ktlintCheck detekt` passes. The first run failed only on ktlint, for the one-line
  `clickable` lambda, which was then wrapped.
- **Emulator (emulator-5570, API 36, debug build, existing data kept):**
  - Only `com.android.chrome` handled the link. With it disabled, `query-activities` found no activity.
  - Tapping the link: pid 3582 before and after, and no FATAL EXCEPTION in logcat. Logcat shows the VIEW intent
    unhandled (`result code=-91`). The Privacy page and "migrely.health/privacy" stay on screen.
  - With Chrome enabled again, the tap starts Chrome (`result code=0`), and Back returns to the Privacy page with the
    same pid.
- **Not checked:**
  - The page's load in Chrome: Chrome stopped on its first-run screen, and logcat shortens the address.
  - The R8 release build: the change adds no reflection or keep rule.
  - QA's device scenario `privacy_link`, which runs on QA's side.
