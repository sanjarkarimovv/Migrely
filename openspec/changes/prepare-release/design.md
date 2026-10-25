# Design

## Decisions

### D1. R8

- **The release build type:** `isMinifyEnabled = true` and `isShrinkResources = true`, with
  `proguard-android-optimize.txt`.
- **`proguard-rules.pro`:** starts empty but for comments. Rules come only for breakages found by the release smoke.
- **Libraries:** Room, Hilt, Compose, WorkManager and kotlinx.serialization ship their own consumer rules. Any JSON
  models the backup reads by reflection need checking.

### D2. Signing

- **The config:** read `keystore.properties` at the root (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`). If it
  exists, a `release` signing config uses it.
- **Otherwise:** release uses `signingConfigs.debug`, and the build logs a warning: "Release signed with the debug key:
  for local smoke only".
- **Git:** `keystore.properties`, `*.jks` and `*.keystore` are already ignored.

### D3. The icon

- **The art:** one SVG source in `docs/release/icon/`, drawn on a 108-unit grid:
  - the background is a vertical gradient from #202945 to #121A2E, the home sky's calm gradient;
  - the crescent is #C8CCD2, made of two circle arcs;
  - two small stars.
- **The adaptive icon:**
  - vector drawables for background and foreground;
  - the monochrome layer is the foreground's shapes in one colour;
  - legacy `mipmap-*` PNGs are rendered from the SVG with `rsvg-convert` for API 26 and older launchers that ignore
    the adaptive one (minSdk is 26, so they are only a fallback);
  - `ic_launcher_round` uses the same layers.
- **The Play icon:** 512×512 PNG, full-bleed, rendered from the SVG.

### D4. Store documents

`docs/release/` holds:
- `listing.md`: title, short and full description, category, contact, keywords;
- `privacy-policy.md` and a self-contained `privacy-policy.html`;
- `data-safety.md`;
- `content-rating.md`;
- `health-declaration.md`.

`TODO(user)` marks the developer name, the contact email and the policy URL.
