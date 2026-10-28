# Proposal

## Why

QA #106 (S4): Settings' "Restore purchases" result line disappears when the phone is turned. Examples of the line:
"No purchases were found for this Google account.", "Premium was found and is on.".
- **Cause:** the Premium section keeps what it shows across a rotation, the note included. But the status is read
  again on every resume, and that read brings no note. Its answer replaced what was shown, and the note went with it.
- **Expected:** the result stays, as messages do (`in-app-messages` › Messages survive rotation). The inline line has
  no requirement of its own.

## What Changes

- **Subscription UI:** a `PremiumSectionShownResolver` decides what the section shows when the status is read again.
  - An answer without a note keeps the shown note while the status is the same.
  - A new status drops the note, since it may no longer hold.
  - A new restore shows its own note.
- **Wiring:** `PremiumSectionDependencies` and `:app`'s `SubscriptionModule` provide it.

**Unchanged:**
- when the status is read;
- the paywall;
- the notes' texts.

## Capabilities

None changed.

## Impact

- `:features:subscription:ui`: new `mapper/PremiumSectionShownResolver.kt` and its test, `PremiumSection.kt` and
  `SubscriptionDependencies.kt`.
- `:app`: `SubscriptionModule.kt`.
