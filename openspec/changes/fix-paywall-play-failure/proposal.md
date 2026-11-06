# Proposal

## Why

Google rejected 1.0.1 under "Broken Functionality: unresponsive UI elements" (#146). The reviewer tapped Yearly, Monthly
and Lifetime on the paywall. Each time a white sheet from Google Play said "Error. No internet connection." with OK, and
then the same paywall was back with nothing to explain it.

The sheet belongs to Google Play: its text is not in this repository, and the app cannot remove or restyle it. The app
has no `INTERNET` and no `ACCESS_NETWORK_STATE`, and that stays, so it cannot detect a connection. Which response code
Play returns after the sheet is not measured and may be `USER_CANCELED`, so the fix cannot depend on the code.

What the app can do is say, in plain words on the screen, what buying depends on and what keeps working. It can also
stop being silent in the one case where its own code knows that Play refused and still shows nothing.

## What Changes

- **One line that is always there.** Wherever a buy button shows, under the terms: "Purchases need Google Play and an
  internet connection. Your diary works offline." It has no state and no detection, and it opens no dialog.
- **A person's own cancel stays silent.** The app shows nothing, because backing out is not an error.
- **`Unavailable` after a sheet no longer ends in silence.** If Play still offers the products, the paywall says "Google
  Play can't be reached. Try again later." If it offers none, only the existing region notice shows.
- **"Restore purchases" moves above "What Premium adds".** Adding the line then leaves both the buy button and Restore no
  lower than today, at every font size.

- **The unreachable notice also speaks about the diary.** When the reload after a sheet fails, the cards and the line
  leave, so its text becomes "Check your connection, then try again. Your diary works offline."

The existing `Failed` message and Restore's messages do not change.

## Out of scope

- `INTERNET`, `ACCESS_NETWORK_STATE`, or any way of detecting a connection.
- Google Play's own sheet, which cannot be changed.
- Play Console text, including the review instructions. These are handled outside this repository.
- Dialogs, new screens, new view states.
- Prices, plans and the purchase flow. `sheetResultOf` and `BuyPremiumUseCase` stay as they are.
- Telling Play's error apart from a person's cancel.
- Restore saying "No purchases were found" if Play answers from its cache while offline. See the design's risks.
- Translation: the app is English only (D148).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `subscription`: what the paywall shows, and what it says after a sheet that did not complete.

## Impact

- `docs/BRD.md`: D153, EC-BIL-14…18, §7.3, §13 row 28.
- `features/subscription/ui`: one new string, one changed string, one `Text`, two blocks swapped in `PaywallOffer`.
- `features/subscription/presentation`: `PaywallViewModel`, a few lines and tests.
- `.maestro/`: `qa-engineer` updates `paywall-and-purchase.yaml`, which asserts the paywall without scrolling.
- No permission, schema, price, datasource or domain change.
- No health claim, so `medical-reviewer` is not needed. `ux-designer` reads the copy before the code.
- Target release: 1.1.0.
