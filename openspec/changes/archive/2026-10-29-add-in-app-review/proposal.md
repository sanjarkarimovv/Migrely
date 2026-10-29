# Proposal

## Why

Ratings decide where a new app stands in Google Play search and how many store visitors install it. Migrely 1.0.1 is
the first production release and has no ratings yet. BRD §14.4 planned an in-app rating request after the 3rd
migraine in a session without a crash, but it was never built. The launch plan of 2026-10-28 makes it the first
product task after 1.0.2. Its rules are BRD D129–D132 and EC-REV-01…12.

## What Changes

- **Google Play's in-app review flow** (D129):
  - It uses `com.google.android.play:review` 2.0.2.
  - Migrely asks no question of its own before or during the flow and shows no stars or button of its own: Google
    forbids that, and a question first would gate ratings.
  - The flow talks to the Play Store app over IPC and adds no permission. The build's permission check still forbids
    INTERNET.
- **When it asks** (D130): on a home load in today's mode, only when all of these hold:
  - at least 3 migraines are logged and not deleted;
  - no attack is in progress;
  - no paywall showed itself in the last 3 days;
  - the previous exit was not a crash or ANR (Android 11+);
  - it never asked before, or it asked fewer than 3 times and the last time at least 120 days ago.

  Home must stay in today's mode for 2 seconds while the app is on screen. The app tries at most once per process.
- **What counts as a request** (D131):
  - A request counts, with its time, when Google's flow completes. Play never says whether the card showed.
  - A flow Google cannot run counts as nothing, shows nothing and is not retried in that process.
  - The marks live next to the paywall marks in the preferences file: Auto Backup restores them, and "Delete all
    data" erases them.
- **Architecture** (D132):
  - A new `:features:review:*` feature.
  - A new home slot, `reviewPrompt`, that `:app` fills.
  - A new `ReviewDataSource`, whose Play implementation takes the activity from `CurrentActivityHolder`.
  - `AppInfoDataSource` learns the previous exit's reason.

**Out of scope:**
- any custom rating UI or feedback form;
- asking from Settings;
- reading whether a rating was given, which Play does not offer;
- store listing changes;
- the review flow in builds not installed from Play, beyond completing quietly.

## Capabilities

### New Capabilities

- `in-app-review`: covers the review flow, when the app asks, letting home settle, and how a request counts.

### Modified Capabilities

None. Home's visible behaviour does not change; the new slot draws nothing.

## Impact

- **New datasource** `review`: the `ReviewDataSource` interface and the `PlayReviewDataSourceImpl` implementation.
- **`PreferencesDataSource`:** the review marks (count and last time).
- **`AppInfoDataSource`:** `previousExitCrashed()`, backed by `ActivityManager.getHistoricalProcessExitReasons`.
- **New `:features:review:{domain,data,presentation,ui}`.**
- **Home UI:** the `reviewPrompt` slot.
- **`:app`:** the DI module, the slot filled in `AppNavHost`, and the modules in `settings.gradle.kts`.
- **The version catalog:** `play-review`.
- **Docs:**
  - BRD §9.11, §13 (change 15), §14.4 and §17;
  - `docs/ARCHITECTURE.md`, for the module and the datasource package;
  - the privacy policy, with one line saying that a rating goes to Google Play, not to Migrely.
