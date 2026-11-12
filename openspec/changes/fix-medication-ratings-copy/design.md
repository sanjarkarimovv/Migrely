# Design

BRD D174, issue #205. Copy verdicts: ux-designer, then medical-reviewer, 2026-11-12. These are copy and source checks, not clinical sign-off: T31
is open, and nothing says "medically reviewed".

## Context

The person who opens Insights is usually calm, looking back over weeks. The person who meets the Insights paywall is deciding
whether to pay. Both read the heading first and may read nothing else. A heading that says "What helped" answers a question the
app cannot answer: which medicine works. The numbers under it are the person's own ratings, and the heading must say that.

## Decisions

### D1. Copy

| String | Old | New | Verdict |
|---|---|---|---|
| `insights_medications_title` | "What helped" | "How you rated your medications" | PASS |
| `subscription_lead_insights` | "Premium shows your triggers, when migraines come, and what helped." | "Premium shows triggers often logged with your migraines, when they come, and how you rated your medications." | PASS; old FAIL (causation, INS-F6); new matches the benefit line and the site |
| `subscription_benefit_timing` | "When migraines come, and what helped" | "When migraines come, and how you rated your medications" | PASS ("your" matches the heading) |
| fallback for the row above | | "When migraines come, and how you rated medications" | PASS |

"Medications", not the site's "medicines": the app says "medications" everywhere (Medications screen, doctor report).

### D2. The fallback rule (EC-INS-04)

The benefit row is an icon and a `Text` with no `maxLines`, so it wraps rather than clips. The fallback applies only if, at 200%
font and 360 dp, the row is clipped, ends in "…", or pushes the close button or the plan list out of reach. Wrapping to more lines
alone is not a reason. `android-engineer` records which string shipped, with the screenshot path, in tasks 1.3 and BRD D174 via
`product-manager`.

Outcome (2026-11-12): the full row shipped. At 200% font and 360 dp it wraps to 4 lines in portrait and 2 in landscape, with no
clipping and no "…", so D2 did not trigger and the fallback is unused.

### D3. What stays

The "Did it help?" question, the four answers, the 3-answer threshold, and the sub-lines "Helped a lot or completely: N of M
answers (P%)" and "Needs 3 answers (has N)". Each describes the person's answers, and the heading now frames them as ratings.

### D4. Spec mechanics

- `trigger-insights`: RENAMED "What helped" → "How you rated your medications", then MODIFIED under the new name (OpenSpec applies
  renames first). The old text "SHALL show how many helped a lot or completely" restated the claim, so it is rewritten to count
  answers. The existing "Ibuprofen" scenario keeps its exact sub-line. `qa/device_scenarios.sh` carries a `# Covers:` line with
  the old name, so QA updates it.
- `subscription`: an ADDED requirement. The pending `fix-offline-play-sheet` change modifies "What a purchase depends on" and
  "Restore and Manage without Google Play", so modifying "The paywall" here would not collide, but a separate requirement keeps the
  copy rule easy to find and leaves "The paywall" untouched.
- `medication-effect`: the claim is in the Purpose, which deltas cannot change. The main spec is edited in place in this change.
  Its requirements do not repeat the claim.

## Risks

- A store or site surface still says "what helped" until the follow-ups land. Tracked in D174; not a regression.
- The Maestro paywall flow fails until QA updates line 62. That is expected and is the negative control for the new lead.
