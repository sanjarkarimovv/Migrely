# Design

## Context

This change is the 1.3.0 polish pass (T66, BRD D181). It covers six independent items, all in the UI layer:

- privacy wording;
- one spoken name per control (#217);
- History's reverse Tab order;
- Home Today's Tab order when nothing follows Settings;
- quiet links that break inside a word;
- Tab order for three screens.

product-manager checked the code at `develop` 40536f49 and ran nothing. ux-designer's spec
(`migrely-work/t66/ux-spec.md`) is also a code read, not a device run. Every "today" claim below is therefore something
to prove red first, not a fact.

`fix-keyboard-follows-cursor` (D180, branch `bugfix/141-keyboard-follows-cursor`) is about to merge. It edits
`EditMedicationScreen.kt`, `LogMedicationScreen.kt`, `TodayContents.kt`, `AttackModeContents.kt`, `OnboardingScreen.kt`,
`RemindersScreen.kt`, `SettingsPages.kt` and `SettingsScreen.kt`. No code starts until it is in `develop` and merged into
this branch (task 0.1).

## Goals and non-goals

**Goals.**

- The app says what it does with data in the same words as the site, the policy and the listing.
- TalkBack, keyboard and Voice Access users meet:
  - one name per control;
  - a visible stop at every key press;
  - an order that reverses exactly;
  - whole words.

**Non-goals.**

- Message timing, pausing and dark-room size (T64).
- The disabled Pain-free stop.
- Site, policy and store docs.
- Any domain, presentation, data, DI or navigation change.

## Decisions

### 1. Privacy strings (D181 (1))

- The five strings are legal-t66's final text, verbatim. Apostrophes are escaped as `\'` in `strings.xml`.
- `settings_privacy_purchase` is new in `features/settings/ui`.
- `PrivacyContents` (`SettingsPages.kt:153-164`) adds one `Paragraph` right after `_offline`, which gives 7 stops.
- No heading is added. Each `Paragraph` is already one node.
- Tests:
  - A Compose test on `PrivacyContents` asserts the 7 texts in order. It reads them from resources and compares them with
    legal's literal text, so a wording drift fails.
  - A test on Your data asserts `your_data_note` the same way.
- The resource grep is the guard against old phrases. The spec quotes all strings, so the test and the spec cannot drift
  apart unnoticed.
- Why the tests compare against literals and not against `R.string`: a test that reads the same resource it checks
  cannot fail on a wording change.

### 2. One spoken name (D181 (2))

- **Shape.** A clickable (`TextButton`, `Button`, `FilterChip`, `clickable`) gets `semantics { contentDescription = … }`,
  and its child `Text` stays in the merged semantics. TalkBack then reads both.
- **Fix.** Keep the description on the clickable and put `Modifier.clearAndSetSemantics {}` on the child `Text`. Role,
  click action, focus and size stay.
  - Do not use `clearAndSetSemantics` on the button. It would drop the role and the click action.
- **Sweep, task 3.1.**
  - Grep `contentDescription =` in `*/src/main/*.kt` and keep the sites on a clickable with a child `Text`.
  - Include ux's list:
    - `MigrelySnackbar.kt:225`;
    - `AttackModeContents.kt:293, 340`;
    - `DoctorVisitSection.kt:81`;
    - `RemindersScreen.kt:551`.
  - Include ux's "check" list:
    - `AttackPainSection.kt:202`;
    - `AttackMedicationsSection.kt:123`;
    - `AttackTimeSection.kt:208`;
    - `DoctorVisitSection.kt:120`, which already clears its `Text` at line 128;
    - `RemindersScreen.kt:424`;
    - `PaywallTrigger.kt:45`.
  - Add `AttackChoiceSections.kt:242`, which product-manager found and which sets `spokenLabel` on chips.
- **Proof per site.** A Compose test on the merged tree asserts `ContentDescription == [spoken]` and no
  `SemanticsProperties.Text` on that node. On 40536f49 it fails for each site that reads twice. A site whose test passes
  on 40536f49 does not read twice and is left alone.
- **Label in name.** The same test asserts that the spoken label contains the visible label, case-insensitive and in
  order.
  - `reminders_open_settings_description` fails today, which is why it becomes "Open settings for notifications".
  - Every `*_undo_*_description` starts with "Undo" and passes.
- **Maestro.** `skip-onboarding.yaml` taps "Skip this step", the content description, so it keeps working.
  `DoctorVisitScreenTest.kt:217` changes with the string.

### 3. History's invisible stop (D181 (3))

Which node it is was not checked. The two candidates below are ux's guesses:

- (a) the month grid's own `focusGroup` (`MonthSkyCalendar.kt:195-196`) takes focus when `onEnter`'s `requestFocus` on
  the anchor is dropped, for example because the anchor's requester is not attached yet;
- (b) the chosen day takes focus while it is scrolled out of view at 200%.

**Proof first (task 4.1).**

- Setup: emulator-5570, API 36, hardware-keyboard events. Shift+Tab is `adb shell input keycombination
  KEYCODE_SHIFT_LEFT KEYCODE_TAB`.
- After each Shift+Tab, `uiautomator dump` records the node with `focused="true"`: its class, bounds and whether the
  bounds are inside the viewport.
- Cells: 100% and 200%, a day with and without migraines, Period shown and hidden.
- If no node reports focus, the stop is a Compose focus target with no accessibility node. Then log
  `LocalFocusManager`'s focused rect from a debug-only probe, never committed, as `fix-keyboard-follows-cursor` task 1.3
  did.
- The fix follows the proven cause:
  - for (a), a group that can never be a stop (`canFocus = false` on the group, with an `onEnter` that always reaches a
    day);
  - for (b), `bringIntoView` on the chosen day when it gains focus.
- If the proof shows a third node, the rule in the spec stays and the fix targets that node. That is not a new product
  decision.

### 4. Home Today's redirect (D181 (4))

- Today `afterSettings`'s `onExit` (`TodayKeyboardOrder.kt:54-63`) is the only place that sends Shift+Tab to the message.
  A group with no focusable child is never entered or exited, so Shift+Tab from "Migraine started" goes straight to
  Settings.
- The redirect moves to a `previous` link on the **first control after Settings in Tab order**:
  - when a message shows, that control's Shift+Tab goes to the message;
  - with nothing after Settings in its column, that control is the content's next stop, usually "Migraine started", or
    the end column's first control in a short window.
- One way to build it: register a `FocusRequester` on whichever control follows Settings in Tab order and set its
  `focusProperties { previous = messageRequester }` while `messageShows`. The engineer may choose another construction if
  the tests in task 4.4 pass. Their negative control is the same tests on 40536f49.
- The content `onExit` comment (follow-up 4) is rewritten to describe what the code does. code-reviewer confirms that it
  matches the code and the tests.

### 5. Quiet links (D181 (5))

- `QuietLinks` (`TodayContents.kt:293-316`) becomes a measured layout:
  - Measure each label's `minIntrinsicWidth`, the width of its longest word, at the current font scale.
  - Stack when either value plus 24 dp, the `TextButton`'s horizontal padding, is greater than half the row.
- Stacked: a `Column`, each link `fillMaxWidth().heightIn(min = 48.dp)`, "Add a past migraine" first.
- `lastModifier` stays on "Log medication" in both layouts, so the wrap target does not move.
- Test: a fixed 360 dp width at font scales 1.0 and 2.0.
  - At 2.0 the two nodes share x and differ in y.
  - At 1.0 they share y and differ in x.
  - The same test on 40536f49 fails at 2.0.

### 6. Three screens' order (D181 (6))

- Today `selectableGroup` and `selectable(role = RadioButton)` already make each option its own stop. Arrows move focus
  in two dimensions and do not select. The tests pin that down.
- **New behaviour.**
  - **Edit medication, Save problem.** A `FocusRequester` on Name and on Usual dose. When the view state's save problem
    changes to a name problem or `InvalidDose` after a Save, request focus on that field.
    - The request happens only while `LocalInputModeManager.current.inputMode == InputMode.Keyboard`. Compose text
      fields take focus in touch mode too, and a request there would open the soft keyboard over a touch user's error.
    - In touch mode nothing moves, as today. The error stays in the field's supporting text.
    - This reads ux A6 ("keyboard focus moves to Name") literally. The engineer records the mode check in the test.
  - **Edit medication, pending.** If Save loses `enabled` while focused, `focusManager.clearFocus()`.
  - **Log medication, logged.** The tapped medication button leaves composition. Compose would otherwise move focus to a
    neighbour or the root, so clear focus explicitly and move it nowhere.
  - **Add past migraine, "Change the times".** Request focus on Started after the card leaves.
  - **Pickers.** Store the opener's `FocusRequester` and request it on dismiss or confirm, if Compose does not already
    restore it. The test proves which.
- **Tests.** One order test per screen and state, with Tab and Shift+Tab key events on the Compose root. Each test
  carries a negative control: removing one expected stop from the expected list makes it fail. A test that cannot fail
  proves nothing.

## Order of work

0. Merge `develop`, which by then has D180, into this branch. Resolve `docs/BRD.md` by keeping both sides in numeric
   order: D180 then D181, EC-UX-39…44 then EC-UX-45…51, row 48 then row 49.
1. Privacy strings (no dependency).
2. One spoken name.
3. Home and quiet links (both in `features/home/ui`).
4. History, after its proof.
5. The three screens, which sit on D180's edits.

Each item lands in its own commit with its red test first.

## Verification

| Item | Red on 40536f49 | Green after | Device |
|---|---|---|---|
| Privacy | grep finds 2 old phrases; `PrivacyContents` test fails (6 stops, old text) | grep 0; test passes | TalkBack 7 stops; 200% portrait and landscape screenshot |
| Your data note | test fails (old text) | passes | 200% screenshot |
| #217 | onboarding test: merged Skip node has a Text | passes | TalkBack log "Skip this step, Button" on steps 1–4; uiautomator no child text |
| A2b sites | each confirmed site's test fails | passes | TalkBack log on Home attack mode Undo, Details, 15 min; Reminders Remove and Open settings |
| History | Shift+Tab test lands on a non-day node, or a day out of view | lands on the chosen day, in view | ring screenshot at 100% and 200%, portrait and landscape |
| Home | all-empty slots test: Shift+Tab from Migraine started goes to Settings | goes to the message | portrait 100%, short window 200% |
| Quiet links | 2.0 scale test: side by side | stacked | 360 dp 200% portrait and landscape screenshots |
| Three screens | Save-problem focus, Change-the-times focus and logged-clear tests fail | pass | 200% portrait walk on each |

What these checks do not prove:

- Voice Access matching on API 26 and 36.
- Real-phone widths.
- That TalkBack's speech equals the merged node on every TalkBack version. QA logs it on API 36 and 26.
- That legal's wording is legally sufficient. It is a DRAFT; a real lawyer still has to sign.

## Risks

- **The BRD conflict with D180 at merge.** It is mechanical; task 0.1 states the order.
- **The History cause is a guess.** Task 4.1 proves it first, and the fix follows the proof.
- **The Save-problem focus could open the soft keyboard for touch users.** Decision 6 limits it to keyboard input mode.
  A device check at task 7.3 covers both touch and keyboard.
- **Stacked links could hide content at 200% on short portrait phones.** The column scrolls, and nothing becomes
  unreachable (ux A5 cost, accepted in D181 (5)).
- **The old privacy text stays on Play's slide 06 until `aso-seo-specialist` re-captures it after 1.3.0 is live.** This
  is harmless: the slide caption already says "No internet permission".
- **Owner approval of the 5 strings (legal's request).** Task 0.2 gates the merge, not the coding.
