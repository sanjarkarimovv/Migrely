# Proposal

## Why

QA found that build 4 (1.0.0) crashes when "Full privacy policy" in Settings is tapped on a phone where no app opens web
pages. The app-settings spec says that, in this case, nothing opens and the app keeps running ("No browser").

`PolicyLink` caught `IllegalStateException` and `ActivityNotFoundException` around `UriHandler.openUri`. Compose UI
1.9.3's `AndroidUriHandler` catches `ActivityNotFoundException` itself and throws
`IllegalArgumentException("Can't open …", e)` instead, as its documentation says (`@throws IllegalArgumentException`).
So neither catch ever ran. QA's `javap` of `ui-release.aar` showed this, and so did dev's.

## What Changes

- **UI:** the link opens the page through `UriHandler.openInBrowserIfAny`. That function returns quietly when Compose
  reports a missing browser as `IllegalArgumentException`, or Android reports it as `ActivityNotFoundException`. The
  `IllegalStateException` catch goes, since nothing throws it here.
- **Tests:** a unit test of the function with a `UriHandler` that throws as Compose does.

**Unchanged:**
- the link and its words;
- the address written under it;
- what happens when a browser is there.

**Out of scope:**
- the other links that open outside the app. "Manage subscription" goes through Play's own intent and is handled in
  the billing layer.
- No BRD change and no spec change: the spec already says what should happen, and the code now does it.

## Capabilities

None changed. The fix makes the code meet the existing `app-settings` scenario "No browser".

## Impact

`:features:settings:ui`: `SettingsPages.kt` (`PolicyLink`), new `PolicyLinkTest`.
