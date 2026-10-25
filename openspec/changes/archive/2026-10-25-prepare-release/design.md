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

- **The art:** one generator, `docs/release/icon/make_icon.py`, draws on a 108-unit grid:
  - the background is a vertical gradient from #121A2E at the top to #26305A, the home sky's calm night;
  - the crescent is #C8CCD2, made of two circle arcs;
  - two small sparkle stars.
- **What it writes:** the SVG sources, the three launcher drawables in `app/src/main/res/drawable`, and the 512×512 Play
  icon as a 32-bit PNG.
- **The adaptive icon:**
  - background, foreground and a monochrome layer of the same shapes in one colour;
  - `ic_launcher_round` uses the same layers.
- **The template PNGs:** the template's density PNGs are removed. minSdk is 26, so `mipmap-anydpi-v26` always wins and
  they were never used.
- **The splash** already shows the foreground, so it shows the moon too.

### D4. Store documents

`docs/release/` holds:
- `listing.md`: title, short and full description, category, contact, keywords;
- `privacy-policy.md` and a self-contained `privacy-policy.html`;
- `data-safety.md`;
- `content-rating.md`;
- `health-declaration.md`.

`TODO(user)` marks the developer name, the contact email and the policy URL.
