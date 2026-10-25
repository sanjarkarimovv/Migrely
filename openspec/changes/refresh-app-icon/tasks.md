# Tasks

## 1. Icon

- [x] 1.1 The generator draws the new icon and writes the drawables, SVGs and the 32-bit Play PNG.
- [x] 1.2 `brand_violet` and the launch theme's icon background.

## 2. Verify

- [x] 2.1 Gate.
- [x] 2.2 On the emulator: the launcher icon, the themed icon, the launch screen, and the app's shortcut icon unchanged.

## Verification record

- **Gate:** green. Both `assembleDebug` and `assembleRelease` build.
- **On emulator-5570, API 36:**
  - in the app drawer, the violet calendar in the launcher's circle mask stands out among the system apps;
  - a recorded cold start shows the launch screen as designed: the white calendar and moon in a violet circle on the
    dark background, opening smoothly from the drawer icon;
  - the badge on an already pinned "Migraine started" shortcut kept the old icon from the launcher's cache, which
    refreshes by itself; nothing in the app.
- **The themed icon:** the monochrome layer is the page with the moon cut out, plus the rings, all inside the safe
  circle. QA checks it on a device with themed icons on.
- **Masks and sizes:** checked on the icon choice page for circle, squircle, rounded square and teardrop, from 96 to
  32 px.

