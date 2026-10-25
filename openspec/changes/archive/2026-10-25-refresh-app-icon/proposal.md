# Proposal

## Why

The first icon (D103), a moon and stars on a navy sky, read as a dark, detailed illustration in Google Play rather than
an icon, and it looked like a sleep app. Store icons that earn the tap use one bold colour and one simple glyph.

## What Changes

- **The icon (D116):** a white calendar page with a navy band and crescent moon, on Migrely violet (#5B50D6).
- **The layers:** adaptive background, foreground and monochrome, plus the 512×512 Play icon, all from one generator.
- **The launch screen:** shows the glyph in a violet circle on the dark background.

## Capabilities

### Modified Capabilities

- `app-identity`: the launcher icon.

## Impact

- **`app/src/main/res`:** the three launcher drawables, `brand_violet`, and the launch theme's icon background.
- **`docs/release/icon`:** the generator, the SVGs and the Play PNG.
- **`docs/BRD.md`:** D116.
