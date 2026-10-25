# Proposal

## Why

QA #52 (S3). History's "Migraine deleted" message, with "Undo the deletion", appears over the top bar, where changes 10
and 11 put "Doctor report" and "Insights". A tap meant for Undo, landing just as the message went, opened Doctor report,
and the migraine stayed deleted. The message surface itself already takes the taps on it. The gap is the moment the
message changes, before a finger already on its way lands.

## What Changes

- **The guard:** the shared message host keeps the place of a message guarded while it arrives or goes and for 0.8 s
  after (BRD D114). Touches there no longer reach the controls under it, while the message itself stays tappable at
  once.
- **Scope:** one change in `:designsystem`, so every screen with messages is covered.

## Capabilities

### Modified Capabilities

- `in-app-messages`: the guard under a message.

## Impact

- **`MigrelySnackbarHost`:** a `TapGuard` under the message, sized to the last message.
- **Tests:** instrumented tests in `:designsystem`. No screen code changes.
