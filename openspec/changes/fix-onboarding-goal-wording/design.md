# Design

## Context

Step 1 of onboarding (`OnboardingScreen.kt` `GoalStep`) shows three `Choice` rows, each a full-width text button with
`bodyLarge` text that wraps. Choosing one saves an `OnboardingGoalPresentationModel`, which is mapped down to `GoalDataModel`
and stored in `migrely_preferences` under `goal`. `FewerPainkillers` is stored as `"fewer_painkillers"`. No code reads `goal()`,
and the goal is not part of the JSON backup (D78). Auto Backup copies the preferences file.

## Decisions

### D1. The words are medical-reviewer's

"Keep track of my medication days". Two other versions were considered and rejected:
- "Take fewer painkillers" (FAIL: it backs a cut without a doctor and reads as an outcome);
- "Keep track of my painkiller days" (FIX: "painkiller" leaves out triptans).

The PM's role is the scope and the edge cases, not the words. The final diff goes back to `medical-reviewer` (tasks 6.1).

### D2. Only the label changes

The stored value, the enum names and the resource id stay. This means:
- no migration;
- an upgraded user's stored answer keeps its meaning ("the medicine goal");
- a backup or an Auto Backup restore behaves exactly as before.

A rename of the stored value would gain nothing, because nothing reads it, and it would need a migration with a test for every
shipped version.

### D3. No caveat, no new element

The new label suggests no action, so a warning has nothing to qualify. A second sentence on the first screen costs a person in
pain more than it helps. The focus order and the TalkBack output keep their structure. Only the spoken text of one choice
changes.

### D4. Fit is checked, not designed

The row already wraps (`fillMaxWidth`, no `maxLines`). The new label is 32 characters against 22 for the old one, so at 200%
it may take 2 lines on a 360 dp screen. `ux-designer` confirms that this is acceptable. Abbreviating is not allowed: the medical
wording is exact.

## Risks

- **Two lines at 200%** push the disclaimer and "Restore from a backup file" lower. They stay reachable by scrolling (EC-ONB-02).
  The walk checks this in landscape, the tightest layout.
- **A stale copy elsewhere.** Search covered the repo, the site, the store art, the Play assets and the content sources; only
  `ig_lint.py` and research notes hold the phrase, on purpose. Video and Reels frames were not searched (proposal, Out of scope).

## Migration

None. The resource text changes on upgrade. Stored data and backups are untouched.
