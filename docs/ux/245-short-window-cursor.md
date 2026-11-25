# #245 Short window: the line being typed always shows

- Owner: `ux-designer`. Date: 2026-11-18. Release: 1.3.2 (team-lead decision: does not block 1.3.1).
- Base: `origin/develop` bedd73b4; develop 76c38c38 (v1.3.1) merged in. Revised 2026-11-18 with QA's re-measure
  (#245 comment 6970307531, `migrely-work/qa-245/`), which replaces every earlier number in this file.
- Related records: issue #245; BRD D180 (2), (5), (7), (8), EC-UX-39, EC-UX-40; ux answers U1, U2, U3, U8 (`migrely-work/t63/ux-keyboard.md`); `docs/ux/238-keyboard-room.md` (branch `ux/238-keyboard-room`), whose rule "closing never scrolls" stays.
- Scope: design only. No new string, no new component, nothing announced.

## 1. Decision

**While the keyboard is up, the line being typed is always fully visible. When it does not fit, things give way in this
order: first the 8 dp margins, then the top bar. The line is never clipped while either of them is still there to give.
Closing the keyboard still never scrolls (#238). The fix is in how the page follows while the keyboard is up, not in a pull
on close.**

What 1.3.2 ships, from QA's re-measure:

- **R3 (the top bar steps aside) is the fix for #245.** It is needed on API 36 (Gboard) at 360 dp, 200% landscape, and
  it fits there: without the bar the visible part is 255 px against the 154 px a line and two margins need.
- **R2 ships in a reduced form: the line stays whole when only the margins are short.** The "equal margins" polish is cut.
  The reduced R2 is needed for windows between 360 and 411 dp (section 2.2), not for any cell QA measured.
- **R4 is defined but not built.** No measured window needs it.
- **R5 decides on the line height from layout**, not on a dp constant.

"The line being typed" means the note's cursor line, or a single-line field's input line (Edit medication name and dose,
Log medication name, Delete all data's word), as defined in U1.

This makes one exception to U2 ("the top bar never collapses"). It applies only while a keyboard is up, and only when the
line cannot fit any other way. Section 6 lists the record changes.

## 2. The moment of use and the numbers

Someone on a small phone, landscape, font at 200% because small text hurts, adds a few words to a migraine's note. Today
the page shows the field's two side borders and nothing else (#245). They type blind. A person who needs 200% text is the
last person who can type without seeing it.

### 2.1 Measured (QA re-measure, 2026-11-18)

Source: #245 comment 6970307531, rows `a36dev-*`, `a36v130-*`, `a26dev-*`, `a26v130-*` in `migrely-work/qa-245/rows.jsonl`.
Develop 76c38c38 (its `app/` equals v1.3.1) and v1.3.0 match to the pixel in every cell, so all of this predates 1.3.1.
Density 2.625, font 200%, landscape. No drag or scroll after the keyboard opened. The cursor line was measured at the
end, the start and the middle of a 9-line note.

- **L, the line being typed, is 112 px = 42.7 dp at 200%,** on API 26 and API 36, for the note and for the Log and Edit
  name input lines. The note's L was measured from the step between line tops; Compose's caret rectangle is the whole
  line box. My earlier "about 48 dp" was wrong.
- **M = 8 dp = 21 px.** So **L + 2M = 154 px = 58.7 dp.**
- *H* = the visible part from directly under the top bar to the keyboard's top. The status bar is 63 px (24 dp) in every
  cell.

| API, keyboard | Window | Screen | *H* | Top bar | Line whole today? | Rule needed |
|---|---|---|---|---|---|---|
| 36, Gboard | 411 dp | Attack details | 137 px (52 dp) | 194 px (74 dp) | **Yes**: 4 px from one edge, 21 px from the other (the side depends on the cursor's direction) | None. *H* ≥ L + M, so it holds today (2.2) |
| 36, Gboard | 411 dp | Edit name, Log name | 163 px (62 dp) | 168 px (64 dp) | Yes, 8 dp both sides | None (R1 holds) |
| 36, Gboard | **360 dp** | **Attack details** | **61 px (23 dp)** | 194 px | **No**: 40 of the line's 112 px show; the text cannot be read | **R3**: 61 + 194 = 255 px ≥ 154 px |
| 36, Gboard | **360 dp** | **Edit name, Log name** | **87 px (33 dp)** | 168 px | **No**: 68 of 112 px | **R3**: 87 + 168 = 255 px ≥ 154 px |
| 26, AOSP | 411 dp | Attack details | 239 px (91 dp) | 194 px | Yes | None (R1) |
| 26, AOSP | 411 dp | Edit name, Log name | 265 px | 168 px | Yes | None (R1) |
| 26, AOSP | 360 dp | Attack details | 156 px (59 dp) | 194 px | **Yes**, 8 dp on both sides with 2 px spare | None (R1 holds today) |
| 26, AOSP | 360 dp | Edit name, Log name | 182 px | 168 px | Yes | None (R1) |

**#245 is real only on API 36 with Gboard at 360 dp.** API 26 at 360 dp is fine. QA's earlier "1 px above the keyboard"
figure mixed two coordinate spaces: on API 26, `screencap` gives the physical frame under `wm size`. The earlier text of
this file, which built a 360 dp API 26 case and its control on that figure, is withdrawn.

Not measured: Edit medication's dose field (the harness typed into the name field again), Delete all data's word, other
font scales, other keyboards (Samsung Keyboard, SwiftKey), real phones, and windows between 360 and 411 dp.

### 2.2 Why R2 is still needed between 360 and 411 dp

When the line plus its margins (154 px) is taller than *H*, today's follow asks the page to show that 154 px area, and
Compose aligns the area's near edge to the visible part's edge. At 411 dp the result is exactly that: one margin is
21 px, and the other gets what is left, 137 − 112 − 21 = 4 px. This explains QA's 4 and 21 px.

The same rule clips the line once *H* < L + M = 133 px: the far edge of the line passes the keyboard (typing down) or the
top bar (cursor moving up) by up to 21 px. So for *L* ≤ *H* < *L* + *M*, which is 112 to 132 px, today's code shows a
cut line that the person could see whole. On API 36 with Gboard, if the keyboard's height stays the same, *H* grows by
about 0.56 px per window pixel between QA's two windows. That puts the band at about 1036 to 1073 px (395 to 409 dp) for Attack details and
989 to 1027 px (377 to 391 dp) for the one-line bar screens. Many phones' short
side falls in that range. This is an estimate from two points; SW13 measures it.

The reduced R2 fixes it. The follow keeps the line whole first and takes the margins from whatever is left. It never
moves the line to make the margins equal. Equalizing would shift the line by about 8 px while the person types, for
nothing they can read better. That polish is cut.

Where the space goes in the API 36 360 dp window: status bar 24 dp, top bar 74 dp (details) or 64 dp (one-line bar),
and the rest is Gboard's keyboard. My T63 estimate (80 to 110 dp left) was wrong. The keyboard is two thirds of the window. The keyboard, the status bar
and the 200% text are not ours to shrink. Only our own chrome and our own margins can give way.

## 3. Options compared

| Option | Result in the 360 dp window | Verdict |
|---|---|---|
| **A. Margins first, then the top bar steps aside while the keyboard is up (chosen)** | 61 + 194 = 255 px on details, 87 + 168 = 255 px on the one-line screens: the line and both 8 dp margins (154 px) fit with 101 px spare | **Chosen.** It gives the person what they need most at that moment, their own words. It uses only space that is ours, and it is all undone the moment typing ends |
| B. Margins only (line at the keyboard edge, 0 dp) | 61 px for a 112 px line (87 px on the one-line screens) | Rejected: not enough on its own. Kept as step 1 (reduced R2) for windows where it is enough, 112 to 153 px |
| C. Clip the line (show its lower part) | Under half of the glyphs of a 200% line | Rejected as a solution. Half a line at 200% is unreadable for the person who chose 200%. Kept only as the last resort when nothing else is left (R4) |
| D. Smaller title or date in the bar | Saves about 10 dp | Rejected: not enough, and 200% stays 200% (D181 (5)) |
| E. Android's full-screen editor (landscape "extract" mode) | Room enough | Rejected: Gboard draws it as a large light surface, which is a sudden bright screen in a dark room. Compose text fields also switch it off (F6 in T63) |
| F. Our own one-line "echo" of the typed text above the keyboard | Room enough | Rejected: it is a new component, TalkBack would read the text twice, and it does not help single-line fields' supporting lines |
| G. Content scrolls under a see-through bar | Room enough | Rejected: text over text, and when the bar is back after closing it covers the line |
| H. Pull the line into view when the keyboard closes | Does not help while typing | Rejected: a scroll on close moved TalkBack's focus onto a chip or radio in 6 of 6 control rows (#238). See section 5 |

Why A suits migraine patients:

- **Light.** The top bar is the page's own background colour, so stepping aside only removes its text. No light surface
  appears, the status bar stays, and the screen stays dark during an attack.
- **Motion.** The bar leaves and returns on the keyboard's own movement, in the same frames, with no curve of our own (U3).
  There is one movement, which the person caused by opening or closing the keyboard.
- **Thinking.** The person sees their own words, which is the only thing that matters while typing. When typing ends,
  the screen is as it was: title, date and Back.
- **Exit.** The system Back always works. The first Back closes the keyboard and brings the bar back; the second leaves.
  On-screen Back is again one tap away after closing. This exception exists only in small landscape windows at large
  fonts, and only while typing.

## 4. The rules (all four screens with a text field)

*L* = the height of the line being typed. *M* = 8 dp. *H* = the visible part between the top bar and the keyboard.

| # | Rule |
|---|---|
| R1 | **Normal.** If *H* ≥ *L* + 2*M*, nothing changes: EC-UX-39 and U1 as today, 8 dp below the line and 8 dp above it when the cursor moves up. |
| R2 | **Margins give way first (reduced).** If *L* ≤ *H* < *L* + 2*M*, the line is kept whole and the margins take what is left, down to 0 dp. They need not be equal: the follow moves the line only as far as it must to stay whole, never to even out the margins. The top bar stays. At 411 dp on API 36 (137 px) this already holds today (4 and 21 px). The change matters for 112 ≤ *H* < 133 px (2.2). |
| R3 | **Then the top bar steps aside.** If *H* < *L*, the top bar (Back, title, date or subtitle, any actions) leaves while the keyboard is up. The visible part then starts under the status bar, and R1 or R2 applies again to that larger part. The status bar stays. Nothing else on the screen changes size. |
| R4 | **Last resort, not built.** If the line does not fit even without the bar (*H* plus the bar's height < *L*), the app does nothing more than R3, and the result is recorded as "window too small", never as a pass. No measured window comes near it: the smallest is 255 px for a 112 px line. TalkBack reads the full text in any case. |
| R5 | **When the decision is made, and on what.** The bar steps aside when *H* < *L*. *H* comes from the keyboard's final height (`imeAnimationTarget` on API 30+, the inset on API 26 to 29). *L* is the real height of the line being typed, taken from layout: the caret's line box for the note (`CursorGeometry.lineOf` without its margin), the laid-out input line for a single-line field. It is never a dp or sp constant, because nonlinear font scaling makes 200% text 42.7 dp, not 48. QA's data fits this trigger: 137 ≥ 112 keeps the bar at 411 dp, 61 and 87 < 112 move it at 360 dp. The decision is made once per keyboard opening, the same way D7 decides the Delete row. It never flips while the keyboard stays up: moving the cursor, typing more lines, scrolling, a message, or the keyboard changing height (emoji, symbols) does not bring the bar back or take it away. A new opening, a rotation or a font change decides again. |
| R6 | **How the bar leaves and comes back.** Its height follows the keyboard's own inset animation (the same fraction), with no curve of our own. On API 26 to 29 and with "Remove animations" it is one step. It never fades through another colour. Its content is not shown half-drawn over the page. |
| R7 | **Closing the keyboard** (Back, the hide key, Done, focus leaving, a dialog opening). The bar comes back in the same frames as the keyboard goes. The scroll position does not change (#238 R3): the bar pushes the page down by its own height and the room of #238 fills the rest. So the line being typed stays on screen, lower by the bar's height. No scroll event is sent. |
| R8 | **Back while the bar is aside.** The system Back closes the keyboard (bar back), and the next Back leaves the screen. This is the same count as today, when the keyboard is up. |
| R9 | **No keyboard inset** (hardware keyboard, floating keyboard, split screen with no bottom inset): the bar never steps aside. |
| R10 | **Messages** keep their D180 (4) coordinates to the pixel. The bar stepping aside never moves a message, and a message never brings the bar back. |
| R11 | **Accessibility tree.** While aside, the bar's nodes are removed from the tree, not merely hidden, so TalkBack, Switch Access and Tab never land on something that is not on screen. Nothing is announced when it leaves or returns. TalkBack's focus stays on the field throughout. |
| R12 | **Free and Premium, light and dark, dark room:** the same. In the dark room (Log medication from attack mode) the area the bar leaves is the dark-room background. |

### Case (b), API 26, 200% landscape, about 411 dp tall: not a defect

QA's cause (#245 comment, 2026-11-18): right after typing, the caret line is in view at y 278 to 389 in a visible part of
257 to 496, 107 px clear of the keyboard, on the 1.3.1 candidate, develop bedd73b4 and v1.3.0. The note went under the top
bar only because the test set-up dragged the page to its end. One more typed character brings the caret straight back,
so the D180 (2) follow works on API 26.

What this means for the design:

- While typing: R1 holds there today. Nothing to change.
- After a drag by the person: the page stays where the person put it, also while typing stops, and also on close (#238
  R3 and R4). Their own scroll is respected; the next typed character brings the line back (D180 (2)).
- API 26 at 360 dp: R1 holds today (156 px, 8 dp on both sides, 2 px spare). The earlier "1 px above the keyboard"
  was a measurement error (2.1), so there is no case here and no control. SW4 keeps it as a guard.
- On close: the line, or whatever the person scrolled to, stays where it was. Nothing scrolls.
- My #238 5.3 verdict "NOT OK" for `ux-kr11-api26-details-200-landscape` is withdrawn: the position came from the drag,
  and the room kept the person's own scroll as #238 R4 intends. The verdict for that shot is OK.

**Cause slot (filled by `qa-engineer`, 2026-11-18):**

| Field | Value |
|---|---|
| Cause found on the device | The KR set-up's drag to the page end, not the app |
| Same cause as #245? | No. #245's cause is window capacity: at 200% in a short landscape window, about one line fits between the top bar and the keyboard |
| Issue | None for (b). #245 stays for (a) |
| Builds | The same on the 1.3.1 candidate 47f93991, develop bedd73b4 and v1.3.0 |

## 5. Why the close does not change

I considered "if the line is out of view when the keyboard closes, bring it back". That is a scroll on close. #238
showed that a scroll on close makes TalkBack move its focus to the first item in view, and a double tap there then wrote a
chip or radio into the record in 6 of 6 control rows. A hidden line is a UX cost that one scroll recovers. A wrong entry in
a health diary is a data and accessibility cost that the person may never notice. The close stays still. The line must
already be in view before the keyboard goes, which R1 to R4 guarantee.

## 6. Record changes for `product-manager`

For 1.3.2, a new D-number (read live from `origin/develop` and all open branches first):

1. U2 and D180 (7) "the top bar never collapses" gain one exception: R3, while a keyboard is up and the line being typed
   cannot fit, decided by R5.
2. EC-UX-39 and EC-UX-40: add the 360 dp 200% landscape window. Expected: the line is whole (R1 to R3); the margins may be
   below 8 dp only under R2; the top bar may step aside only under R3.
3. Delete all data: R3 is decided after D7. The row first stays behind the keyboard; if the input line still does not
   fit, the bar steps aside too.
4. R2 in its reduced form (the line whole, margins from what is left, never evened out) and R5's trigger (*H* < *L*, with
   *L* from layout). R4 is defined but not built. Cut: equal margins.
5. #245's scope: the defect is API 36 with Gboard at 360 dp, 200% landscape (Details 61 px, Edit and Log name 87 px).
   API 26 at 360 dp is not a defect.

## 7. Acceptance rows for `qa-engineer`

Builds: candidate = the 1.3.2 fix at the SHA under test. Negative control = 1.3.1 (or v1.3.0, which is the same here).
Mutations as named. Each row restores font scale, rotation, `wm size` and density, animator scales and TalkBack.

| ID | Where | Steps | Expected on candidate | Negative control |
|---|---|---|---|---|
| SW1 | Details, 360 dp (`wm size 945x2100`), 200%, landscape, API 36 Gboard | QA's harness: scroll to Notes without dragging to the end, tap, type 9 lines; cursor at the end, the start and the middle | The cursor line is whole (112 of 112 px) with ≥ 8 dp on both sides; Back and the title are absent from the layout dump; the status bar is unchanged | **1.3.1 (develop 76c38c38) and v1.3.0: 40 of 112 px visible in all three cursor positions** (rows `a36dev-details-360-*`) |
| SW2 | Edit medication name and dose, Log medication name, Delete all data word, each at 360 dp, 200%, landscape, API 36 Gboard | Focus the field, type | The input line is whole; supporting line when it fits (U1 order); the bar is aside only if the input line did not fit with it; Delete row per D7 | Edit and Log name on 1.3.1: *H* 87 px, 68 of 112 px visible. Dose and Delete all data were not measured: QA measures 1.3.1 first, and if the line is whole there, that cell is a guard with SW3's mutation as its control |
| SW3 | Details, 411 dp, 200%, landscape, API 36 | As SW1 | The bar stays (Back in the dump at its usual bounds); the line is whole; margins as today (4 and 21 px, by cursor direction), not evened out | Mutation "bar always aside when the keyboard is up": the bar is missing here, so red. Mutation "equal margins": the line moves by about 8 px, so red |
| SW4 | Guard, API 26 AOSP keyboard: Details and Edit/Log name at 411 dp and 360 dp, 200% landscape | As SW1 | Unchanged from 1.3.1: the bar stays, the line is whole with ≥ 8 dp on both sides (R1); at 360 dp Details the line is at y 280 to 392 against a keyboard top at 413 | Not a fix row: 1.3.1 already passes (rows `a26dev-*`). Its control is a mutation that sets R5's trigger to a constant of 64 dp (168 px > 156 px, so the bar leaves here): red |
| SW5 | SW1 and SW4 set-ups, and once after a drag to the page end while typing | Close by Back, then by the hide key | The bar is back at the same bounds as before typing; the line is still on screen; TalkBack (on, VERBOSE) says nothing but the field or "keyboard hidden" and stays on the field; zero TYPE_VIEW_SCROLLED from the page within 3 s | Mutation that keeps the line's screen position on close by scrolling: a scroll event, so red |
| SW6 | 100% portrait on all four screens, and 200% portrait | Focus, type, close | The bar never leaves; EC-UX-39 and EC-UX-40 as before | Same mutation as SW3: red |
| SW7 | SW1 set-up | While the bar is aside: move the cursor up with arrow keys or the handle, type 10 lines, switch Gboard to symbols and to emoji, scroll the page by hand | The bar does not come back or flicker; the cursor line keeps ≥ 8 dp from the status bar when it moves up | none needed |
| SW8 | SW1 set-up, "Remove animations", and on API 26 | Open and close the keyboard | The bar goes and comes back in one step; no frame shows the bar half-drawn over text, and no lighter frame | A 60 fps recording on API 36 with animations on shows the bar moving in the same frames as the keyboard |
| SW9 | SW1 set-up, TalkBack on | Swipe back from the field while the bar is aside; then close the keyboard and swipe back again | Aside: content above the note and then the top of the page, never Back or the title. After closing: Back is reachable in D178 order | none needed |
| SW10 | SW1 set-up with a message showing (e.g. after an edit with Undo) | Open the keyboard, then close it | The message's bounds are identical with and without the keyboard and the bar (D180 (4)) | Mutation that offsets the message by the bar's height: red |
| SW11 | Log medication from attack mode (dark room), 360 dp, 200%, landscape | Focus Name, type, close | As SW2; the status bar and the area the bar leaves are the dark-room background; no lighter frame | none needed |
| SW12 | SW1 set-up | Rotate with the keyboard up to portrait and back; RTL pseudo-locale; hardware keyboard | Portrait: the bar stays (R1). Back to landscape: decided again. RTL: Back mirrored when it returns. Hardware keyboard: the bar never leaves | an unreachable cell is "not reachable", not a pass |
| SW13 | Reduced R2 band: API 36 Gboard, 200% landscape. Details at a window where QA reads 112 ≤ *H* < 133 px from the dump (estimate `wm size 1053x2100`, about 401 dp); Edit name likewise (estimate `wm size 1007x2100`, about 384 dp) | As SW1; cursor at the end and at the start | The line is whole (112 of 112 px); the bar stays; margins from what is left | 1.3.1 at the same window: the line is cut by (133 − *H*) px at the keyboard edge (end) or the bar edge (start), as 2.2 predicts. **If 1.3.1 shows the line whole there, 2.2 is wrong: R2 is cut and this row is dropped** |

## 8. What I could not judge without a device

1. *L* is now measured (112 px at 200%, 2.1). Still open: the exact window band where the reduced R2 matters on real
   phones, which depends on each keyboard's landscape height. SW13 measures one point; 2.2 is an estimate from two.
2. Whether the bar following the keyboard's inset fraction reads as one calm movement or as two (the keyboard rising
   while the bar leaves). A 60 fps recording in a dark room would settle it.
3. Whether people at 200% in a small landscape window miss the title while typing. I expect not, because they are
   looking at their own words.
4. Real phones with other keyboards (Samsung Keyboard, SwiftKey), whose landscape heights differ from Gboard's.
