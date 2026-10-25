# Proposal

## Why

The MVP's features are done apart from the subscription, but the app cannot go to Play yet:
- the release build has no R8 and no signing setup;
- the launcher icon is still Android's template;
- the store documents are missing (listing, privacy policy, Data safety, content rating, health apps declaration);
- the BRD still plans analytics (ADR-09), which the no-INTERNET rule (D44) rules out.

This change prepares everything that does not need the user's accounts or names (BRD D101–D104). The rest moves to 13b
`publish-to-play`.

## What Changes

- **Release build** (D102): R8 with resource shrinking; signing from a git-ignored `keystore.properties`, or the debug
  key for a local smoke only. A release smoke on the emulator.
- **No analytics** (D101): ADR-09 is superseded. Data safety answers "no data collected or shared".
- **App icon** (D103): a night sky with a crescent moon, as an adaptive icon with a monochrome layer, plus a 512×512 PNG
  for Play.
- **Store documents** (D104) in `docs/release/`, with `TODO(user)` where the user's name, email or URL goes.

## Capabilities

### New Capabilities

- `release-build`: the shrunk, signed release build and its smoke.
- `app-identity`: the launcher icon.

## Impact

- **`app/build.gradle.kts`:** release minify, shrinking, signing, and `proguard-rules.pro`.
- **`app/src/main/res`:** the icon.
- **New docs:** `docs/release/*.md` and `docs/release/privacy-policy.html`.
- **BRD:** §11.3 is updated; ADR-09 and D9's analytics are marked superseded.
- **Settings:** one string, the Privacy page's backup line, now says the encryption holds from Android 9.
- **Build:** the permission check now also runs before the bundle.
