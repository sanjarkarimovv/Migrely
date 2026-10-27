# Design

## Decisions

### D1. A guard under the message, not over it

- **The layers:** `MigrelySnackbarHost` draws a `TapGuard` first, then the message.
  - A tap on the message hits the message, so Undo works at once.
  - A tap on the message's place while no message covers it hits the guard, so it is swallowed.
- **Arming:** the guard's key is the message, or `NoMessage` when none shows, so a message leaving re-arms it as one
  arriving does.

### D2. Its size

- **The height:** the message's own height, measured with `onSizeChanged` on the content at full size from the first
  frame. It is kept after the message goes, so the guard covers the place during the fade.
- **The width:** the message's, capped at 560 dp.

### D3. Its time

800 ms covers the 250 ms slide in or 200 ms fade out, plus a finger already moving. It is the same time onboarding's
guard uses.

## Testing

Instrumented tests in `:designsystem` (`MigrelySnackbarHostTest`) put a tappable strip under the host and check three
moments:
- a tap a few frames into the arrival;
- a tap 300 ms after leaving, which fails without the guard;
- a tap 1.5 s after leaving, which reaches the strip.
