# Proposal

## Why

**#245 (S3, accessibility, pre-existing: develop 76c38c38 and v1.3.0 match to the pixel).** On a small phone in landscape,
with the font at 200% because small text hurts, a person adds a few words to a migraine's note. With the keyboard up, the
line they are typing is not visible: they type blind. A person who needs 200% text is the last person who can type without
seeing it. Found during QA of #238.

**QA re-measure (#245 comment 6970307531, qa-245, 2026-11-18; no drag after the keyboard opened).** L, the line being typed,
is 112 px = 42.7 dp at 200% on API 26 and API 36; 8 dp margins are 21 px, so a line plus two margins needs 154 px.

| API, keyboard | Window | Screen | H (under the top bar to the keyboard) | Line today |
|---|---|---|---|---|
| 36, Gboard | 360 dp | Attack details | 61 px | 40 of 112 px: unreadable |
| 36, Gboard | 360 dp | Edit name, Log name | 87 px | 68 of 112 px: unreadable |
| 36, Gboard | 411 dp | Attack details | 137 px | whole (margins 4 and 21 px) |
| 26, AOSP | 360 dp | Attack details | 156 px | whole, 8 dp both sides |

So #245 is real only on API 36 with Gboard at 360 dp, 200% landscape. API 26 is fine (QA's earlier "1 px" was a measuring
error). Not measured: Edit medication's dose, Delete all data's word, other fonts and keyboards, windows between 360 and 411
dp, real phones.

**Cause.** The keyboard (two thirds of the window), the status bar and the 200% text are not the app's to shrink. What is
left is eaten by the top bar (74 dp on Attack details, 64 dp on the one-line bars), which U2 and D180 (7) say never collapses.
Separately (ux 2.2), today's follow aligns the line-plus-margins area with the visible part's edge, which cuts the line by up
to 21 px once H < L + M = 133 px: an estimated band of windows about 395 to 409 dp tall (Attack details) and 377 to 391 dp
(one-line bars). That band is an estimate from two points, not a measured cell.

The design is `ux-designer`'s `docs/ux/245-short-window-cursor.md` (R1 to R12, acceptance SW1 to SW13; 11f26d76), merged into
this branch. Scope by `team-lead` (2026-11-18). The BRD decision is D192.

## What Changes

- **The top bar steps aside (R3, the fix).** On the four screens with a text field, when H < L, the top bar leaves while the
  keyboard is up; the status bar stays. In the 360 dp window that gives 255 px, so the line shows whole with 8 dp on both
  sides. It comes back with the keyboard.
- **The trigger is the real line (R5).** H from the keyboard's final height, L from layout (the caret's line box, or the laid-out
  input line), never a dp constant. Decided once per keyboard opening; it never flips while the keyboard stays up.
- **Margins give way first (reduced R2, conditional).** When L fits H but L plus two margins does not, the line stays whole and
  the margins take what is left; never evened out. **Condition (team-lead):** the engineer's first task is a red test on
  v1.3.1's code showing the line cut in that band. If it cannot be shown red honestly, R2 leaves 1.3.2.
- **#238 holds.** Closing the keyboard never scrolls: the bar returns, the page moves down by the bar's height, the keyboard
  room fills the rest, TalkBack's focus stays on the field. The keyboard-room tests stay as guards.
- **Motion, access, messages (R6, R10, R11).** The bar follows the keyboard's own inset animation (one step on API 26 to 29
  and with Remove animations). While aside its nodes leave the accessibility tree. Messages keep their D180 (4) coordinates.
- **Records.** BRD D192, EC-UX-72 to EC-UX-77 (new), EC-UX-39, EC-UX-40, EC-UX-43 (pointers), §13 row 56. `app-shell`
  "The window does not pan for the keyboard" and "Closing the keyboard never moves the page" get the bar exception.

## Out of scope

- R4 (the line does not fit even without the bar): defined, not built; recorded as "window too small", never a pass.
- The equal-margins polish of R2 (cut by ux and team-lead).
- Pulling the line back into view on close (rejected: a scroll on close moved TalkBack's focus in 6 of 6 rows in #238).
- A smaller title, Android's full-screen editor, an own echo line, a see-through bar (ux section 3).
- Screens without a text field (D180 (3)), the message host's place, any string, #237 and #239.
- Domain, data, DI, navigation, permission, Room schema, backup, migration. No health sentence, so no `medical-reviewer`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `app-shell`: ADDED "The line being typed always shows", "Margins give way before the line is cut" (conditional), "The top
  bar's decision holds while the keyboard is up", "The top bar moves with the keyboard", "Closing the keyboard brings the top
  bar back without a scroll", "An aside top bar is gone for assistive technology and messages"; MODIFIED "The window does not
  pan for the keyboard" (body; scenario "The tightest window") and "Closing the keyboard never moves the page" (body; scenario
  "200% font in portrait and landscape"). All other scenarios unchanged.

## Impact

- **Code:** `designsystem` (one shared mechanism: the decision H < L at keyboard opening, the bar's height following the inset,
  its semantics removed while aside; the follow keeping the line whole before its margins), then the four screens'
  `ui` modules: `features/attack/ui` (`AttackDetailsScreen`), `features/medication/ui` (`EditMedicationScreen`,
  `LogMedicationScreen`), `features/settings/ui` (`DeleteAllDataScreen`, after its row decision). Three screens use
  `MessageLayout`'s `topBar` slot; Delete all data has its own `TopAppBar`.
- **Tests:** Robolectric tests in `designsystem` and the screens' `ui` modules, red on `develop` first; `KeyboardRoomTest` and
  the screens' keyboard-room tests stay green as guards.
- **No change to:** manifest, permissions, strings, Room schema, backup, Free or Premium.
- **BRD:** D192, EC-UX-39, EC-UX-40, EC-UX-43, EC-UX-72 to EC-UX-77, §13 row 56.
- **Issue:** `Refs #245` in every commit and the PR, never a closing keyword: QA filed #245 and closes it after the retest.
- **Release:** 1.3.2 (tracker T75), with #237 and #239. Files shared with #239: `EditMedicationScreen` and
  `LogMedicationScreen` (different parts: the top bar and scroll here, the form's problem feedback there); the later branch
  merges `develop` before review.
