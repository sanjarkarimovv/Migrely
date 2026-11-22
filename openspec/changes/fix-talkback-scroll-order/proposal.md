# Proposal

## Why

**#255 (S3, accessibility, pre-existing: on v1.3.1 and on `develop`).** A person with a migraine, in a dark room, uses
TalkBack because the screen hurts to look at. On Attack details they add a word to the note and then swipe back to leave.
Focus cycles among the pain-location and symptom chips, or jumps over whole blocks, and never reaches Back. Many chips say
only "Not selected", not their name. Touch exploration and the system Back still work, but the person who relies on swipes
is stuck. QA found it during #245's acceptance (row SW9) and saw the same on v1.3.1.

**Root cause** (eng-255, `migrely-work/255/eng-255-report.md`; proven on emulator-5570 with TalkBack 16 and TalkBack's own
order dump; Compose 1.9.3, BOM 2025.10.00, same code in 1.10.4). It is Compose framework behaviour, not FlowRow,
`traversalIndex`, `MessageLayout` or chip semantics:

- **B, the loop (main cause).** A clickable item up to 24 dp outside a scroll area's visible part stays in the accessibility
  tree, because Compose clips touch bounds against the scroll clip widened by half a 48 dp touch target. Its bounds in the
  window are empty (`Rect.Zero`), so the sort puts it at the top of the page, right after the top bar. This happens whenever
  that 24 dp band lies inside the window: with the keyboard up, and also at 100% portrait with no keyboard, because the
  navigation bar inset ends the visible part above the window's bottom. TalkBack "previous" from the first visible item goes
  to that hidden item at the bottom, scrolls to it and cycles. Its label is clipped, so TalkBack says only "Not selected".
- **A, skipped blocks.** When TalkBack scrolls at the edge, Compose moves the page by a whole visible height. The focused item
  leaves the screen and the tree, and TalkBack falls back to the first or last item in the scroll area (v1.3.1: "Stress →
  Symptoms", skipping "Possible triggers" and every symptom chip).

The same mechanism applies to every vertical scroll in the app: `develop` f70b6450 has 29 `verticalScroll` call sites (23 on
pages of 17 screens, 6 in dialogs), no `LazyColumn`, no horizontal scroll. BRD decision: D193.

## What Changes

- **Items leave the tree at the visible edge (fixes B).** Inside every scrolling page and dialog, Compose's minimum touch
  target expansion is set to zero for accessibility bounds, so an item outside the visible part is not a stop. Every other
  view configuration value (double tap timeout, touch slop, long press) stays the system's.
- **TalkBack's scroll step keeps its place (fixes A).** A scroll by TalkBack (and by Switch Access, which uses the same
  action) moves the page by floor(V/2) px, V being the part actually visible, never a whole height. The focused item stays on
  screen while the gap to the next stop is under V/2; the next stop is always the next item in order (ux 91ceda44).
- **One shared entry point.** Both parts live in `designsystem` and every one of the 29 sites uses them; a JVM source check
  fails on a raw `verticalScroll(` in main code outside `designsystem`.
- **Every stop says its name.** A chip cut by the edge says "Not selected. Nausea", never only "Not selected": the chip
  carries `spokenLabel ?: label` on its own node with the inner Text cleared ("option 3"), only together with exact bounds.
  Other stops change only where a cut-sweep test shows them losing their name, each passing D181's one-name check.
- **Exact bounds cover only the scroll content**, never the top bar, the message host or dialog buttons.
- **Nothing drawn changes.** Screenshots stay identical to the pixel. Every clickable control inside an affected page is
  measured before the bounds part lands there. History's calendar days at 360 dp get a touch area of their whole cell pitch;
  a Material date or time picker with a control under 48 dp stays out of the bounds part and is recorded; attack mode is
  unchanged.
- **Guards.** #238 (closing the keyboard never scrolls, D189), #245 (`TopBarAside`, `TypingSpace`, D192), #239 (one
  announcement per refused Save, D191), D181 (one spoken name, Tab order), D178 (order: top bar, message, content).
- **Records.** BRD D193, EC-UX-78 to EC-UX-82, EC-UX-89 and EC-UX-90, §13 row 57. `app-shell` gets eight ADDED requirements.

## Out of scope

- Reporting the Compose behaviour to Google: an outside send is the owner's (handbook section 5). Recorded as a note only;
  if Compose fixes it, the bounds part can go and the tests stay.
- Any change to text, colour, layout, motion or strings; any new accessibility feature (custom actions, headings).
- Horizontal scrolls and lazy lists (none in the app today; the source check makes a new one visible).
- TalkBack itself, other screen readers, real-gesture calibration.
- #252 and #254 (other open keyboard issues), the 1.3.2 release (QA-accepted, not widened).
- Domain, data, DI, navigation, permissions, Room schema, backup, migration, Free or Premium. No health sentence, so no
  `medical-reviewer`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `app-shell`: ADDED "TalkBack reaches every item on a scrolling page once", "An item outside the visible part is not a
  stop", "TalkBack's scroll keeps its place", "Every stop says its name", "The traversal fix draws nothing new", "Exact
  bounds cover only the scroll content", "One entry point for every vertical scroll", "The traversal check can fail". No existing requirement changes: the
  D178, D181, D189, D191 and D192 requirements stay as written and are guards.

## Impact

- **Code:** `designsystem` (the two parts and the entry point; the date and date-time pickers), then the `ui` modules of
  `features/attack` (`ChoiceChip`), `home`, `medication`, `settings`, `history`, `insights`, `report`, `backup`, `reminders`,
  `subscription`, `onboarding`. A JVM source check (module chosen by the engineer).
- **Tests:** eng-255's red tests (0ecb3bc6: `AttackDetailsTalkBackOrderTest`, `AttackDetailsTalkBackScrollTest`,
  `TalkBackTree`) stay; `TalkBackTree` likely moves to a shared test fixture for other modules. A navigation-bar-inset
  variant is added. Each screen group gets red-first traversal tests.
- **Maestro:** chips expose their name as a content description on a fake child instead of a TextView's text;
  `qa-engineer` checks `.maestro/` selectors (likely `attack-details/`, `custom-tags/`, `attack-logging/`, `past-attacks/`,
  `a11y-tour/custom-tag-dialog.yaml`, `device/details-*.yaml`, more if the sweep reaches buttons and rows).
- **No change to:** manifest, permissions, strings, Room schema, backup, Free or Premium.
- **BRD:** D193, EC-UX-78 to EC-UX-82, EC-UX-89 and EC-UX-90, §13 row 57.
- **Issue:** `Refs #255` in every commit and the PR, never a closing keyword: QA filed #255 and closes it after the retest.
- **Release:** 1.3.3 (team-lead decision 2026-11-19), new tracker row. Branch `bugfix/255-details-talkback-order`.
