# Proposal

## Why

QA saw it in `paywall_self_show`, once in 3 runs, and rated it S2 because starting a migraine is the core flow (#116).
- **What happened:** the launcher shortcut "Start migraine" fired with home already open. The attack was stored, but home stayed on the calm today card instead of attack mode.
- **Cause:** the shortcut makes a new home entry, which starts two reads of today at once.
  - The start's read: `onEnter(startAttack = true)` starts the attack and then reads today.
  - The resume's read: `onResume()` reads today too.
  - The reads run side by side and `loadToday` showed whatever answered. When the resume's read was made before the attack was stored but answered last, its calm today replaced attack mode.
- **The month calendar** reloads on every home load and read the database later, so it showed the attack.
- **The date tick** adds a third read. Under load the order is easy to lose.

## What Changes

- **`HomeViewModel.loadToday`:**
  - numbers its reads, and shows only the newest one's answer;
  - says "Something went wrong" only for the newest read's failure;
  - announces a recent deletion from any read, as before (home's messages offer each deletion once).
- **Threads:** reads are made and answered on the main thread, so the counter needs no lock.

**Unchanged:** what is read, and when.

## Capabilities

None changed. The fix makes home meet `attack-logging` (attack mode as soon as an attack starts, from the shortcut too, BRD D76).

## Impact

- `:features:home:presentation`: `HomeViewModel.kt` and `HomeViewModelTest`.
