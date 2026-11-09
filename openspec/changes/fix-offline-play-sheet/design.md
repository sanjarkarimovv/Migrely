# Design

The facts come from `main` 50a6775e (tag v1.1.0) and the owner's phone evidence. The Android API notes come from the platform docs and are **not yet run on a
device**. Task 1 is the spike that proves them.

## D1. What "offline" means (BRD D166 (2))

Offline means **no network on the phone has `NET_CAPABILITY_INTERNET`**.

1. `getActiveNetwork()` and its `NetworkCapabilities`: if it has INTERNET, the phone is online.
2. Otherwise, look at the other networks. Android documents that `getActiveNetwork()` returns null when the default network is "blocked for the caller",
   and a block on Migrely is not a block on Play. If any network has INTERNET, the phone is online.
3. If none has, the phone is offline.

VALIDATED is not asked: a captive portal or a validation in progress would otherwise cost real purchases. This is exactly Play's
`ERR_INTERNET_DISCONNECTED` case.

| Question | Decision | Why |
|---|---|---|
| When it is checked | At the tap, synchronously, before any Play call | No listener, no cache, no state to go stale. Flapping is judged per tap |
| On an exception | `SecurityException` or anything else gives "unknown", and "unknown" opens Play as today | Money: a wrong "offline" loses a sale |
| Where it lives | A `NetworkStateDataSource` in `datasource`, a repository, and the domain use cases for buy, restore and manage return an `Offline` outcome before touching billing | One place. The 3 ViewModels (paywall, Premium section, trial banner) get it for free |
| Debug "store for testing" | The same check runs before the simulated sheet | So QA can test the debug build |
| Buttons | Stay enabled | A disabled control looks broken, and a tap that answers at once is the fix |

## D2. Messages

| Path | Where it shows | Text |
|---|---|---|
| Buy, 3 plans | The paywall's message host (D158 placement, 0.4 s guard) | "You're offline. Connect to the internet to buy Premium. Your diary works offline." |
| Restore, paywall | The same | "You're offline. Connect to the internet to restore purchases. Your diary works offline." |
| Restore, Settings | The Premium section's note, where "Google Play can't be reached" shows today | The same restore text |
| Manage, paywall and Settings | As Restore in each place | "You're offline. Connect to the internet to manage your subscription. Your diary works offline." |
| Manage, trial banner on Home Today | Home's message host. `ux-designer` confirms the place in a light check | The manage text |

Information kind ("i"), polite, 8 s stretched by the accessibility timeout, and a body tap closes it. No haptic. The newest message wins. No health claim.

## D3. Edge cases

| Case | Result |
|---|---|
| Airplane mode, Wi-Fi off | Offline message |
| Wi-Fi and mobile off | Offline message |
| Airplane mode with Wi-Fi on | Online, so the sheet opens |
| Captive portal, not validated | Sheet opens (residual, Play's UI) |
| VPN up | Follows Android. With INTERNET, the sheet opens. A lockdown VPN with no network under it can still report INTERNET, so the sheet opens (residual) |
| Work profile | The same check |
| Default network blocked for Migrely, another has INTERNET | Online, so the sheet opens (D1 step 2) |
| Data Saver, app in the foreground | Online |
| Network drops while the sheet is open | Play's UI. Back leaves, and D153 answers on the paywall |
| Flapping | Per tap |
| Check throws, permission stripped | Today's behaviour |
| TalkBack | Polite message, spoken in full |
| 200% font, landscape | The existing message host rules |
| Paywall guard | Close answers on the first tap, because the message sits below the Close row (D158) |
| Restore offline | Never "No purchases were found" |
| Premium and Free | Same check. The diary keeps working offline, and Premium status is untouched |

## D4. datatransport

Play Billing's datatransport declares the same permission, and the merge now keeps it. With it, datatransport can see the network and may schedule its
log uploads. Without `INTERNET` they fail. The engineer checks that this crashes nothing: logcat after an online paywall visit, and after forcing its jobs
with `adb shell cmd jobscheduler run -f com.migrely.app <id>`. A `FATAL` stops the hotfix and goes to `team-lead`.

## Release notes (draft, 1.1.1, under 500 characters)

```
<en-US>
Fixes:
• With no internet connection, Premium's buttons now tell you you're offline instead of opening Google Play.
• Restore purchases no longer says no purchases were found when you're offline.
• The safety card after a first pain of 10 now scrolls into view.
Migrely now reads whether the phone is online ("view network connections"), only for this. It still has no internet permission.
</en-US>
```

## Data safety and public text

- **Data safety: no change.** It covers data that is collected or shared. This reads a status on the phone, and nothing is stored or sent, and there is no
  `INTERNET`.
- **`aso-seo-specialist` checks:**
  - the site's permission list (the "privacy check" page that lists each permission: add "view network connections", `ACCESS_NETWORK_STATE`, and why);
  - the sentence there that Google's usage logging declares this permission and that it is removed;
  - "Android's permission list with no network access" in the video description, and the promo video's shot of the permission list, which is now out of
    date;
  - any listing or FAQ sentence that lists Migrely's permissions.
  "No internet permission" stays true everywhere.
- **`legal-finance-analyst` checks:** the privacy policy's permissions part and any "no network permissions" sentence, and confirms that Data safety is
  unchanged.

## Acceptance (QA, then the owner's phone)

| # | Build | Steps | Pass |
|---|---|---|---|
| H1 | Release | Airplane mode with Wi-Fi off: tap each of the 3 buy buttons, Restore (paywall and Settings), Manage (paywall, Settings, banner) | Our message at once in each, no Play UI, TalkBack speaks it |
| H2 | Release | Wi-Fi off and mobile off, not in airplane mode | As H1 |
| H3 | Release | Online: each buy button | Play's sheet opens (Back to leave) |
| H4 | Release | Restore online, then offline | Online: today's answer. Offline: the restore message, never "No purchases were found" |
| H5 | Release | Airplane mode with Wi-Fi on | Sheet opens |
| H6 | Release | Offline message, turn Wi-Fi on, tap again | Sheet opens |
| H7 | Same commit with only the permission removed | H1 | Play's sheet opens as on 1.1.0: the check can fail, and it fails open |
| H8 | Release | `:app:checkReleasePermissions` report | 1.1.0's list plus `ACCESS_NETWORK_STATE`, nothing else |
| H9 | Release | Logcat during H1 to H6 and a forced datatransport job | No `FATAL` |
| H10 | Owner's phone, Play internal track, owner present | H1, H3, H4 | As above |

## What this does not prove

- Google's reviewer's device and network, or that review passes.
- Captive portals, VPNs and work profiles beyond the rows run.

## Risks

- **A false "offline" loses money.** That is why the check has no VALIDATED and has D1 step 2, fails open, and has spike task 1.1.
- **datatransport.** D4.
- **The public text says the app has no network permissions.** `aso-seo-specialist` and `legal-finance-analyst` must check it before Production.
