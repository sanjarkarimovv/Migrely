# Design

## Context

The paywall shows, in order: the close button, the headline and lead, the plan cards, the buy button, the terms, "What
Premium adds", "Restore purchases". It scrolls, and the cards stay on screen while Google Play's sheet is open.

The person's moment: they tap "Start free trial", a white Google sheet says there is no internet, they tap OK, and they
are back on the same screen. Nothing says what happened or whether their diary is affected. They may be unwell. The app
is sold as working offline, so "no internet" can read as "something is broken here".

## Goals and non-goals

**Goals:** the person understands that buying depends on Google Play and a connection, and that the diary does not. The
buy button and Restore stay easy to reach. The answer holds whichever response code Play returns.

**Non-goals:** see the proposal's out of scope. This change cannot remove the sheet, so it cannot promise that a reviewer
on a device without a connection stops seeing it.

## What Play can answer, and what the person sees

| Play's answer after the sheet | App result | Message | Paywall after it reloads | Changed? |
|---|---|---|---|---|
| `USER_CANCELED` | Cancelled | None | Cards, button and line | Line only |
| No answer in 10 minutes | Cancelled (via TimedOut) | None | Cards, button and line | Line only |
| `BILLING_UNAVAILABLE` or `ITEM_UNAVAILABLE`, products still offered | Unavailable | "Google Play can't be reached. Try again later." | Cards, button and line | **Yes** |
| The same, no products offered | Unavailable | None | "Premium isn't available in your region yet" | No |
| Any other code | Failed | "The purchase didn't go through. Try again." | Cards, button and line | Line only |
| Any answer, and reading the products then fails | As above | As above | "Google Play can't be reached right now", "Check your connection, then try again. Your diary works offline.", "Try again" | Notice text |

The line is present in every row that has a buy button, and the last row says the same about the diary, so no row depends
on knowing the code.

## Decisions

### D1. The copy

| Key | Text |
|---|---|
| `subscription_purchase_needs_play` (new) | `Purchases need Google Play and an internet connection. Your diary works offline.` |
| `subscription_unreachable_text` (changed, was `Check your connection, then try again.`) | `Check your connection, then try again. Your diary works offline.` |

Why this wording, and how it differs from the starting position ("everything else in Migrely works offline"):

- **"Your diary", not "everything else".** "Everything else" would be false. Restore and Manage subscription also need
  Google Play, and a subscription ends after 3 days without Play (`OFFLINE_ALLOWANCE`, EC-BIL-01). "Your diary" is true
  for everyone who sees this screen: logging, history, reminders, export and safety warnings.
- **"Internet connection"** repeats the words in Play's sheet, so the person links the two. The app's other notice says
  "Check your connection", but that one appears only when the connection is already the question.
- **"Google Play" comes first.** The subject of "need" must be the purchase through Play, in an app whose promise is that
  it never uses the network. It does not say that Migrely uses the internet.
- **Unconditional.** It does not say "you are offline", because the app cannot know. It is equally true with a connection.
- **The unreachable notice says the same about the diary.** When Play's sheet closes and the reload fails, the cards leave
  and the line leaves with them. That person has just read "No internet connection" and is the most likely to be offline,
  so the notice carries "Your diary works offline." There is no buy button in this state, so no layout effect on one.
- **No health claim and no promise of an outcome.**

Unchanged, and checked against this line:

| Existing text | Conflict? |
|---|---|
| "Renews automatically. Cancel anytime in Google Play." | None; same name for Play |
| "Cancel before the trial ends and you won't be charged." | None |
| "One payment. Nothing renews." | None |
| "Logging, history, reminders, export and every safety warning stay free." | None. That line is about price, this one about offline |
| "Google Play can't be reached right now", the notice's title. Its text changes, see the table above | None. It replaces the cards, so the line is not on screen with it |
| "The purchase didn't go through. Try again." | None |

### D2. Where the line goes

Inside the block that shows when a plan can be chosen, directly after the terms and before "Manage subscription":

```
[ buy button ]
terms
the line                     <- new
[ Manage subscription ]      only for a subscriber
```

- **After the button**, so the button's position cannot change at any font size. Nothing above it changes.
- **After the terms**, not between them and the button. The terms are the store-policy disclosure and stay against the
  button. Store policy ranks above UX in the handbook's order of risk.
- **Its own paragraph, not appended to `terms()`.** Yearly's terms are already two sentences. Four in one block would bury
  both the disclosure and the line.
- **Style:** `bodyMedium`, `onSurfaceVariant`, as the terms. Spacing is the parent column's 16dp, which `ux-designer`
  kept: tighter would read as more terms.
- **No live region and no heading.** It does not change, so TalkBack reads it once in order, after the terms.
- **No `maxLines`.** It wraps at any size.

It shows wherever a buy button shows: free, pending, or a subscriber moving plan. It does not show for loading, region
unavailable, Play unreachable, or a lifetime owner, because none of these has a buy button.

Estimated size, not measured: `bodyMedium` is 15sp on 22sp. At 100% on a 360dp phone the line takes 2 to 3 lines. At 200%
it takes about 4 to 5. Task 3.3 measures it.

### D3. A person's own cancel shows nothing

Backing out of a payment is a normal choice, not an error. A message after it reads as pressure, which goes against
product principle 7, and adds text and motion for someone who may be in pain. If Play returns the same code after its own
error, the person has just read Play's explanation, and the line is on screen. So nothing is lost by staying silent.

### D4. `Unavailable` after a sheet says something when the cards return

`outcomeOf` treats `Unavailable` as silent, with the comment "The paywall shows these itself once it loads again". That is
true only if the reload also finds no products. If Play refuses the launch or the sheet and then still returns the
products, the cards come back and nothing is said. This is the one path where the app's own code knows Play refused and
leaves the tap with no answer. It is also the same class of defect as the rejection.

This goes beyond "no state, no detection", and the reason is above. It adds no view state and does not detect a
connection. It reads the code Play already returned and the offers the reload already read.

`PaywallViewModel`:

- `onBuyAction` passes to `load` whether the outcome was `Unavailable`.
- In `load`'s result, inside the existing `!working` check: if that flag is set and the offers are
  `PaywallOffersDomainModel.Loaded`, and no `Unlocked` is being delivered, deliver `PlayUnreachable`.
- `outcomeOf` keeps returning `null` for it, and its comment is corrected.

The message appears only after the reload, so the region notice and "can't be reached" never show together. Delivery uses
the existing `undelivered` path, so a rotation does not lose it and it shows once. The snackbar sits at the top of the
screen (EC-UX-16), so scroll position does not hide it, and its duration follows the accessibility timeout.

The `PlayUnreachable` text says "Try again later", which respects review M10 ("an unavailable item is not try again"),
since it does not ask for an immediate retry.

### D5. "Restore purchases" moves above "What Premium adds"

In `PaywallOffer`, swap the `Benefits()` call and the Restore `TextButton`, in every state of the offer.

Maestro's `paywall-and-purchase.yaml` asserts "Restore purchases" visible with no scroll on a 411 x 914dp emulator, which
suggests that Restore may fit the first screen today and that the new line could push it off.

`Benefits` is always taller than the line, since it has a title, four rows and the free-stays sentence against one
80-character sentence. So Restore ends higher than today at every font size and width, with or without measuring.

The cost: "What Premium adds" moves down by about the line plus Restore, and on some phones it starts lower or falls below
the first screen. The buy button was already above it, and the headline and lead already state what Premium is for. The
app has no analytics to measure this (D101), so it stays a judgment call.

In the region and unreachable states Restore also sits above the list. The disabled Restore in the region state is the
least tidy result, and it is accepted so that the order stays the same in every state.

### D6. Every other way to reach a Google Play surface

| Place | What opens | Needs anything? |
|---|---|---|
| Paywall, from onboarding, patterns, doctor report, Insights, Settings: "Start free trial", "Subscribe", "Buy once" | Play's purchase sheet | **This change** |
| Settings, Premium status row: free, pending, subscriber | No sheet; it opens the same paywall | Covered by the paywall |
| Plan switch: a subscriber in the paywall from Settings | Play's sheet, from the same button | Covered. Same `Plans` block, so the line shows |
| "Restore purchases", on the paywall | No sheet. It reads purchases and answers `PlayUnreachable` when Play gives no answer (`restoredOf`) | No. Confirmed, with a test |
| "Restore purchases", in Settings | No sheet. Shows the note "Google Play can't be reached. Try again later." when Play gives no answer (`noteOf`) | No. Confirmed in code |
| "Manage subscription": paywall, its success screen, Settings; "Manage" on the trial banner | Opens Play's subscriptions page outside the app (`ACTION_VIEW`); no billing sheet | No. Play or the browser shows its own state, and the person comes back |
| Play's payment messages at start (`showInAppMessages`) | Play's own message, only when Play has one | No. Nothing was tapped |
| In-app review (D129) | Google's review card | No. It already stays silent when it fails (EC-REV-05) |

### D7. Messages stop covering the top bar (BRD D158, #152)

Decided by `ux-designer` and `team-lead`. The task 4.3 row that asked this question was answered by #152's measure on the
paywall (d60dbb4c): at 100% a tap at Close's centre lands on the message's Dismiss, and at 200% it lands above Dismiss and
nothing happens. A buy button under the message opens no sheet. System Back closes the paywall.

**Where a message sits.** One top offset on `MigrelySnackbarHost`, added to the host's existing insets and 8 dp:

| Screen | Top edge of the message | Why |
|---|---|---|
| Paywall | Below the Close row: 48 dp on the host, so 56 dp under the inset | Close is 48 dp with 4 dp of padding, so it is never covered, moved or guarded |
| Every other screen with a top bar | Below the bar | Back, Insights and Doctor report answer, and a tap cannot hit Undo instead |
| Home Today | Below the Settings row | Settings answers, and a tap cannot hit Undo and reopen the migraine |
| Landscape attack mode | As D52 | The start column already moves below the message |
| Portrait attack mode | Unchanged | Undo over the rain card is accepted: that card is the screen's content, not a top-bar control. Known limit |
| No top bar, such as onboarding | Unchanged | Nothing to clear |

`android-engineer` picks the top-bar value. If the bar is Material's default 64 dp, 56 dp on the host sets the edge at the
bar's bottom. No screen in the repo overrides its height (`git grep expandedHeight` finds none).

**The paywall's guard is 400 ms, not 800 ms,** through one host parameter defaulting to 800. It is longer than the message's
250 ms entry and 200 ms exit. The paywall's message has no Undo, so the longer guard protects less here, and a person
retrying a purchase is the one the guard would block. This is the exception to D114.

**A tap on a message.**

- With no action, a tap anywhere on it dismisses it, by touch only with no new semantics. The Dismiss button stays, and
  TalkBack still reaches that button.
- With Undo, a tap on the body does nothing. A stray tap must not remove the offer to undo a deletion. Data loss ranks above
  this.

**Everything else stays:** duration, top position, dim look, copy, other screens' 800 ms.

**Limits, accepted:**

- A control still under a message needs a tap to dismiss first, then a second tap.
- After that tap, a tap in the message's area does nothing for 400 ms on the paywall and 800 ms elsewhere.
- On the paywall, two X icons are stacked at the right edge. This is to be checked by eye.
- People can dismiss a message unread. That includes "storage full", the one message with no timeout, because it means the
  last action was not saved.
- Not measured on people.

**What reading the code adds, for task 3a.5 to measure. The decision stands; these are where one offset may fall short:**

- **Home's Settings row is not a bar.** `DayHeader` scrolls with the content, and its height follows the date text. If the
  date wraps at 200%, the row is taller and Settings sits lower, so one offset may still cover it. Once scrolled, the message
  lies over whatever is there.
- **Row 1 goes under the message at any font size.** On My medications a message with Undo now lies over the first row, with
  Undo near its Remove. A tap on Remove there hits Undo. This was a 200% case in #152 and becomes a 100% case. It is visible
  and reversible, like the Settings and Doctor report cases it replaces.
- **Landscape Home Today.** The start button sits centred in the end column, and a centred message up to 560 dp wide also
  reaches that column. Moved down, it comes closer to "Migraine started", and at 200% it may overlap it. The spec says a
  message never covers the large controls. If measurement shows an overlap, it goes to `team-lead` before the release is cut.

## Edge-case checklist

| Area | Result |
|---|---|
| Empty, first use, Free, Premium | The onboarding paywall shows the line. A subscriber moving plan sees it. A lifetime owner has no button and no line |
| Loading and failure | No line while there is no button; those states have their own text |
| During an attack | No paywall shows (EC-UX-13); unchanged |
| Offline | Always. The line does not depend on the connection |
| Full storage | No new write |
| Time, zone, backup, restore, undo, deletion | Not touched. The line and message hold no data, and no upgrade step is needed |
| Rotation | The line is static. The message survives through `undelivered` |
| Process death | An undelivered message is lost, as any message is today. The line remains |
| App killed mid-purchase | EC-BIL-13; unchanged |
| Double tap | The `working` guard; unchanged |
| Unordered reads | The new check runs in `load`'s result, inside the `!working` check that already guards it |
| 200% font, small screen, landscape | D2, D5. The line can sit below the fold at 200%, see the risks |
| TalkBack, keyboard | TalkBack reads the line as a stop of its own, after the terms and not merged with them. Keyboard focus skips it, since it is plain text |
| Right-to-left, locale | English only (D148). The text has no number or date |
| Reduced motion, dark, light | No motion. Same colours as the terms |
| Purchase: pending | The "Payment pending" notice and the line show together |
| Purchase: refunded, restored on a new phone | Unchanged |
| Purchase: no products | No button, no line (EC-BIL-11) |

## Risks

- **The reviewer can still reject.** The sheet will still appear on a device without a connection. This change makes the
  screen honest around it. It does not guarantee the outcome. The Play Console review instructions are handled elsewhere.
- **At 200% the line may be below the fold** when the person returns from the sheet, since they scrolled to the button.
  "Always visible" is not possible without moving the button, which is ruled out. It is one short scroll away, and any
  `Failed` or `Unavailable` message appears at the top regardless. Accepted.
- **Restore while offline may say "No purchases were found"** if Play answers `purchases()` from its cache. Not measured,
  not changed here. The real-phone check records it. If it happens, it gets its own issue.
- **`Unavailable` with products still offered is not reachable on the simulated store,** which fails both together. A view
  model test covers it, and no walk can.
- **The paywall's message covered controls** (UX verdict finding, measured in #152). D7 is the answer. Its limits and the
  three places where one offset may fall short are listed there.
- **Emulators cannot run Play's sheet.** So nothing before the real-phone check (task 5.3) shows this on a real sheet.

## Migration

None. No data, no schema, no setting.

## UX verdict (`ux-designer`, task 1.3)

Read from code on 2026-11-06. Nothing here was seen on a device. Where this section differs from a decision above, this
section is the design's position until `product-manager` carries it into D1, the BRD and the spec.

Carried by `product-manager`: the notice text into D1, D153, EC-BIL-14 and the spec; the TalkBack correction into the
edge-case checklist; the finding into the risks and task 4.3.

**One change. Everything else in D1, D2, D3, D4 and D5 stands, including the line's exact text.**

### The change: the unreachable notice also speaks about the diary

| Key | Today | New |
|---|---|---|
| `subscription_unreachable_text` | `Check your connection, then try again.` | `Check your connection, then try again. Your diary works offline.` |

The last row of the table above: Play's sheet closes, the reload fails, and the cards leave, so the line leaves with them.
That person has just read "No internet connection" and is the one most likely to be offline, and the screen no longer
says the diary is fine. The region notice already answers this question ("Everything else in Migrely works as before"),
and this one does not. It is one string, the same words as the line, with no state and no layout effect on a buy button,
since this state has none. This replaces the "unchanged" row for this text in D1.

### Why the rest stands

- **Copy.** Twelve words, and true in every state that shows it. "Purchases" keeps two jobs: it matches the "Restore
  purchases" button under it, which needs Play too. "Buying" would lose that. "Works offline" is shorter and clearer than
  "works without one". Starting with the diary would answer a question the person has not asked yet.
- **Style.** Same as the terms, on purpose. The line shows to everyone, and most have a connection, so it stays quiet. The
  person who needs it is looking for "internet", and those are the words they just read. A card or an icon would look like
  a warning on a phone with nothing wrong, and would compete with the store disclosure. `Notice` also has a live region.
  Contrast on `background`: 7.18:1 dark, 7.32:1 light (computed from `Color.kt`).
- **Spacing.** Keep the column's 16dp. Tighter would read as more terms.
- **Finding it.** At 100% on a phone it should be on screen under the button. At 200% it is not reliable, and in landscape
  at 200% it will usually need a scroll. Accepted: the sheet has already named the cause, so a person who misses the line
  loses reassurance, not the way forward.
- **Restore.** Rejected: Restore in the top bar (it wraps at 200% and moves the button), and the line under Restore or at
  the end (Restore stays put, but the line stops being found). A small gain from the swap: during loading, a tap aimed at
  Restore can only land on a card once the prices arrive, because three cards are always taller than the loading text. It
  can never land on the buy button. Choosing a card spends nothing.
- **TalkBack.** Free user, counted from the code: Close, headline, lead, three cards, button, terms, line, Restore. The line
  is stop 9 and Restore is stop 10. Today Restore is stop 15. No announcement is added when the sheet closes: Play's sheet
  has already been read aloud, and an announcement would also fire on a person's own cancel.

### Corrections to the wording above

- The edge-case row "not focusable, since it is plain text" holds for the keyboard only. For TalkBack the line is a stop of
  its own, not merged with the terms.
- D4 says the top position means scroll cannot hide the message. True, but see the finding below.

### Finding, not part of this change

The paywall's message is drawn over the screen and does not use `onMessageBottomChanged`. From the code it covers Close
while it shows, 8 seconds or longer with an accessibility timeout, with its own Dismiss about where Close is. At 200% it
may also cover whatever is scrolled to the top, which can be the buy button. This applies today to `Failed` and to
Restore's messages, and D4 adds one more path to it. It is not seen on a device yet. `qa-engineer` confirms it and, if
real, files it. It does not block this change.

Lead's decision, which overrides the last two sentences: it is the category Google rejected on, so `android-engineer` tests
it in task 4.3, and a control that does not answer goes to `team-lead` before the release is cut. Measured in #152 and
answered in D7.

### To judge on a device (tasks 3.3, 4.3, 5.3)

- At 200%, portrait and landscape: how much of the line shows when the button has just scrolled into view.
- Whether TalkBack returns to the buy button after Play's sheet, or starts again at Close.
- Whether the line can be told apart from the terms at a glance, in dark and light. If it blends on the real phone, the one
  allowed step is `onSurface` in place of `onSurfaceVariant` for the line: no layout change.
