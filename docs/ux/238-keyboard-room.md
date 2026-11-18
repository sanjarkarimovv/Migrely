# #238 Keyboard room: closing the keyboard never moves the page

- Owner: `ux-designer`. Date: 2026-11-17. Release: 1.3.1.
- Base: `origin/develop` c49b9e09 (v1.3.0 merged). Fix under review: `bugfix/238-talkback-focus-on-keyboard-close` a1efa2bc (`designsystem/.../input/KeyboardRoom.kt`, Attack details only).
- Related records: BRD D180 (2) and (5), EC-UX-39, EC-UX-40, EC-UX-43; OpenSpec `fix-keyboard-follows-cursor` design D2, D5, D7 and rows K3, K6; ux answers U6, U8, S6 and S13 (`migrely-work/t63/ux-keyboard.md`).
- Scope: design only. No new strings, no new component on screen, nothing announced.

## 1. Decision

**Accepted: the page stays where it is when the keyboard closes. If the page was near its end, the area the keyboard
leaves shows the page's own background until the person scrolls back up. That blank goes only through the person's own
scroll, a reopened keyboard, or leaving the screen. It is never animated away.**

The same rule holds on all four screens with a text field: Attack details (note), Edit medication (name, dose), Log
medication (name, first use) and Delete all data (confirmation word).

This replaces one clause of my earlier answer U6 ("if the page was scrolled to its end, the content settles down by up to
the keyboard's height") and the words "no blank space" in S6, EC-UX-43 and K6. Section 7 lists the record changes for
`product-manager`.

## 2. The moment of use

Someone in the middle of a migraine, or just after one, adds a few words to the note. One hand, a dim room, slow
thinking. They close the keyboard (Back, the keyboard's hide key or Done) and either read what they wrote or leave the
screen. Leaving is the common next step. Scrolling back up to check is the second most common.

Two people need to keep their place:

- **A TalkBack user.** On 1.3.0, closing the keyboard pulls the page down. Compose reports that as a scroll, TalkBack
  treats it as the person's own scroll and moves its focus to the first item in view ("Not selected. Stress"). The person
  believes they are still on the note. A double tap now marks a trigger they did not have. That is a wrong entry in a
  health diary, made without the person noticing.
- **A sighted user with low tolerance for motion.** On 1.3.0 the page slides down by up to the keyboard's height, about
  40% of a phone screen, as the keyboard closes. On Android 8 to 10 it moves in one jump. The line they were reading moves
  away from their eye. The content also slides into the area where their thumb was just typing, so a late or doubled tap
  meant for the hide key can land on a chip, a radio or "Delete migraine".

## 3. Options compared

Risk order from the handbook: safety, privacy, data loss, store policy, money, accessibility, UX, speed.

| Option | TalkBack place | Motion | What the person sees | Verdict |
|---|---|---|---|---|
| **A. Room (chosen).** Blank room below the content, as large as the visible part grew. It shrinks only as the person scrolls up or the keyboard reopens | Kept: no scroll happens, so no scroll event | None that the person did not cause | The page does not move. Near the end, a band of the page's own background where the keyboard was | **Chosen.** It removes the cause, not the symptom. It works the same way for every accessibility service, not only TalkBack. It also removes the API 26 to 29 jump |
| B. Let the page settle (1.3.0), then put TalkBack focus back on the field | Compose has no public way to set accessibility focus. Moving input focus makes TalkBack read the field again ("Editing ..."), which breaks U8. It races TalkBack's own focus move, and that timing differs between TalkBack versions (the bug reproduced headless 3/3 and windowed 0/9) | Up to the keyboard's height; a jump on API 26 to 29 | The page slides down | Rejected: fragile, noisy for a slow listener, and keeps the motion |
| C. Room, then animate it away after the keyboard has closed | Lost again: shrinking the room at the page end is a scroll, so the event fires, only later | A slow slide that nobody asked for, after the keyboard is gone | The page drifts down on its own | Rejected: it brings the bug back a few hundred ms later and adds motion with no visible cause |
| D. Room kept until the screen is left | Kept | None | A blank band that comes back every time the person scrolls to the end | Rejected: a page that ends in empty space looks broken or still loading, for no gain over A |
| E. Room only while TalkBack is on | Kept for TalkBack | Sighted users keep the slide and the API 26 to 29 jump | Two behaviours | Rejected: Switch Access and Voice Access also react to scroll events; two behaviours double the test matrix (KISS) |
| F. Stop padding the scroll's visible part for the keyboard | Kept | None | The typed line goes under the keyboard again (#141) | Rejected: reopens #141 |

Why A suits migraine patients:

- **Light.** The band is the page's own background, never a lighter surface. On a dark page, closing the light system
  keyboard (F7) reveals dark background, so the screen only gets darker. The app stays dark during an attack.
- **Motion.** Nothing moves unless the person moves it. When they scroll up, the band shrinks in step with their finger.
  It never animates, so "Remove animations" changes nothing.
- **Thinking.** The text they were reading stays under their eye. "Delete migraine", or the last control of the page,
  stays right above the band and shows where the page ends.
- **One hand, imprecise taps.** The area where the keyboard was is empty and does nothing, so a late tap meant for the
  keyboard cannot hit content that slid into its place. This is the same rule as "a message that disappears must not let
  a tap fall through".

The cost is accepted: near the end of a page, a band of empty background stays until the person scrolls up. On a short
page that fits the screen without the keyboard (Edit medication and Log medication at 100%), the top of the page stays
scrolled out of view until the person scrolls up. Nothing is lost, nothing is out of reach, and one swipe up brings it
back. This is a UX cost. It ranks below the accessibility and data-integrity risk it removes.

## 4. The rules (all four screens)

Terms: *K* = how much the visible part of the scroll grows when the keyboard closes or gets shorter (keyboard height minus
the navigation bar). *b* = how much of the page lay below the visible part just before that.

| # | Rule |
|---|---|
| R1 | **When room appears.** Only when the keyboard closes or gets shorter while this screen is shown, and only if *b* < *K*. The room is *K* − *b* tall, exactly enough that the scroll position does not change. If *b* ≥ *K*, no room appears and nothing on screen changes except that hidden content is uncovered in place. |
| R2 | **What it looks like.** Background only, of the screen's own colour: the theme background, or the dark room background when the screen is in attack mode. No divider, no shadow, no gradient, no "end" marker, no other colour, at any frame of the keyboard animation. It sits after the last item of the page, below the bottom padding the page already has. |
| R3 | **It never moves the page.** The scroll position in pixels is the same before and after the keyboard closes, on every API level, with or without animations. No frame shows the page lower and then back. |
| R4 | **How it goes.** It shrinks as the person scrolls up (finger, fling, TalkBack's scroll actions, Tab or Shift+Tab bringing a control into view, Page Up, mouse wheel), in step with that scroll, and it never grows back from scrolling. After the person has scrolled up by at least the room's height, it is gone, and scrolling to the end shows the page ending as it normally does. |
| R5 | **Keyboard opens again.** The room shrinks as the visible part shrinks, so the page does not move because of the room. The follow rule of D180 (2) then applies unchanged: the cursor line is at least 8 dp above the keyboard (EC-UX-39), and single-line fields follow U1. |
| R6 | **Keyboard gets taller or shorter while open** (suggestion strip, a different layout, a different keyboard). A shorter keyboard adds room by R1. A taller one uses room first, then D180 (2). |
| R7 | **Content changes while the room is there** (a chip added, an error line appears, a message in card mode). Content that grows uses the room up; the page does not move because of the room. |
| R8 | **Rotation, process death, leaving and coming back** (Back to the previous screen, a forward screen and return, the app sent to the background and the activity recreated). The room is not saved. The screen comes back without room, at its saved scroll position or the nearest position the page allows. TalkBack starts wherever Android and D178 put it on any screen entry. A band with no visible cause would read as a broken layout, and the person's context is reset anyway. |
| R9 | **Background and return without recreation.** The room stays as it was: nothing changed for the person. If the keyboard reopens on return (the field still has focus), R5 applies. |
| R10 | **No bottom keyboard inset** (hardware keyboard, floating keyboard, split screen, the full-screen editor in landscape): no room, ever. The app adds nothing that Android does not report (D180 (5)). |
| R11 | **Not interactive, not in the accessibility tree.** No semantics, not focusable, no Tab stop, not reached by TalkBack swipes. A tap in the room does what a tap on empty page background does anywhere else on that screen: nothing. It never clears focus, opens the keyboard or ripples. |
| R12 | **Nothing is announced.** No new string, no live region, no change to the field's state description. Android's own "keyboard hidden", if the device says it, is enough (U8 (4)). |
| R13 | **Focus.** Input focus stays in the field after the keyboard closes (U6). TalkBack's focus stays on the field. The first Back closes only the keyboard, the second leaves the screen (unchanged). |
| R14 | **Free and Premium are the same.** First use and empty states: no room can exist before the first keyboard close on this visit. |

## 5. Per screen

### 5.1 Attack details (note) — done in a1efa2bc, check against R1 to R14

```
 keyboard open, page near its end        keyboard closed (1.3.1)          after scrolling up a little
+--------------------------------+   +--------------------------------+   +--------------------------------+
| <-  Migraine                   |   | <-  Migraine                   |   | <-  Migraine                   |
|   Impact on your day           |   |   Impact on your day           |   |   ... Triggers ...             |
|   ( ) 3 · Bed rest             |   |   ( ) 3 · Bed rest             |   |   Impact on your day           |
|   Notes                        |   |   Notes                        |   |   ( ) 3 · Bed rest             |
|  [ took a rest in the dark |  ]|   |  [ took a rest in the dark  ]  |   |   Notes                        |
|================================|   |  [                          ]  |   |  [ took a rest in the dark  ]  |
|  keyboard                      |   |       Delete migraine          |   |  [                          ]  |
|                                |   |                                |   |       Delete migraine          |
|                                |   |   (page background, inert)     |   |   (band smaller by the scroll) |
+--------------------------------+   +--------------------------------+   +--------------------------------+
```

- First item in view after a pull on 1.3.0: a trigger chip ("Not selected. Stress"). The negative control targets this.
- In attack mode, if details is opened from the attack card, the band takes whatever background that screen has there.
  It never takes a lighter one.

### 5.2 Edit medication (name, dose)

- Page: Name, Kind (four radio rows), Usual dose, Save. At 100% font it fits the screen without the keyboard, so on
  1.3.0 closing the keyboard from Usual dose pulls the page all the way back to the top. This is the largest pull of
  the four screens, and the first item in view is likely the Name field or a Kind radio ("Not selected. Triptan" or another kind). A
  double tap on a radio would change the kind of medication. Same risk class as #238.
- With the room, Save stays where it was revealed and the Name field stays scrolled out of view until the person
  scrolls up. Accepted (section 3).
- A Save problem moves focus to Name or Usual dose in keyboard mode only (D181 (6)). That is a bring-into-view scroll by
  the app, so it shrinks the room by R4. Unchanged.

### 5.3 Log medication (name, first use; also from attack mode in the dark room)

- The only text field is Name in the first-use form (no medications yet): Taken at, Name, four kinds, Add and log.
- This screen opens from attack mode with the dark room palette (`MigrelyColors.AttackModeBackground`). The band must be
  that background. This is the screen most likely to be used during an attack, so its dark room row is required.
- After "Add and log" the form is replaced by the logged state. The content changes, R7 applies, and the page does not
  jump because of the room.

### 5.4 Delete all data (confirmation word)

- The visible part of the scroll ends at the "Delete everything" row, not at a keyboard inset (D180 (8), D7). Whether
  the row rides above the keyboard or stays behind it, closing the keyboard grows the visible part, and the room applies
  to the body above the row. The row itself moves with the keyboard as D7 already defines. That is unchanged and is the
  only thing that moves.
- D7's promise holds: after Done or Back, the field and "Delete everything" are visible together. The field does not
  move, and the row sits at the bottom.
- The divider above the row ("there is more to read") follows `canScrollForward`. With the room, the page end is in view,
  so the divider is hidden. That is correct: there is nothing more to read.

## 6. Accessibility notes

- **TalkBack.** The field keeps TalkBack focus when the keyboard closes. Swiping forward from the field reaches the next
  control(s) and then the end of the page; the room is never a stop. Swiping back walks up in D178 order, and the scroll
  TalkBack makes shrinks the room (R4). A double tap right after closing acts on the field (reopens the keyboard), never
  on a chip, radio or button.
- **Other services.** Switch Access and Voice Access see no scroll event, so they do not rescan or renumber because of a
  keyboard close.
- **Keyboard and D-pad.** Tab from the field goes to the next control, Shift+Tab to the previous one, as before. Arrow
  and Page keys scroll and shrink the room.
- **200% font, portrait and landscape (411 dp and 360 dp tall windows).** The room can fill more than half of a short
  landscape window. This is accepted: the field, its input line and supporting line stay where they were, fully visible,
  and nothing is cut off. The room is never taller than the keyboard was.
- **Targets and contrast.** Unchanged: the room adds no control and no colour.
- **RTL.** Vertical only; nothing to mirror.
- **Reduced motion.** Nothing to reduce: the room has no animation. With "Remove animations" the keyboard disappears in
  one frame and the page still does not move.
- **Light theme.** The band is the light page background, not white and not a surface tint.

## 7. Record changes for `product-manager` (owner of BRD and OpenSpec)

A new D-number from the D151–D199 range, read live from `origin/develop` right before taking it. Suggested content:

1. D180 (5) and OpenSpec design D5: replace "settles down by at most the keyboard's height if it was scrolled to its end
   (U6)" with the rules R1 to R14 above. Reason: the settle is a scroll, and TalkBack treats it as the person's scroll
   (#238, proven from TalkBack's VERBOSE log).
2. EC-UX-43: "Yopilganda pastda boʻsh joy qolmaydi" becomes, in substance: closing never moves the page; near its end the
   area the keyboard leaves shows the page's own background and disappears as the person scrolls up; after rotation,
   process death or leaving the screen there is no blank.
3. OpenSpec row K6 "no blank space, no jump": change to "no jump; blank only by R1, gone per R4 and R8".
4. My U6 and S6 (`migrely-work/t63/ux-keyboard.md`) are superseded by this file for the closing case. U8 stands, and
   this change is what makes it true again.

## 8. Acceptance rows for `qa-engineer`

Builds: candidate = the 1.3.1 fix branch at the SHA under test; negative control = `v1.3.0`; a mutation build = the
candidate with the room removed from the screen under test. Where 1.3.0 does not reproduce the focus jump on a screen,
that screen's TalkBack row proves only "no jump observed" and must say so; it is not a fix proof.

The bug reproduced headless (3/3) and not windowed (0/9). Run the TalkBack rows headless first, where the control
reproduces, and record windowed runs separately.

**Set-up "near the end":** the field is focused, the keyboard is open, and the page is scrolled so that less than a
keyboard height of content lies below the visible part. Details: Notes with "Delete migraine" above the keyboard, or a
long note typed to its end. Edit medication: Usual dose focused. Log medication: first use, Name focused. Delete all data:
the word typed, both with the row riding above the keyboard (portrait 100%) and left behind it (200% landscape, 360 dp).

| ID | Where | Steps | Expected on candidate | Negative control |
|---|---|---|---|---|
| KR1 TalkBack (U8) | Each of the four screens, API 36, TalkBack VERBOSE, 100% portrait | Set-up near the end, TalkBack on the field, Back once | Within 3 s TalkBack says only the field itself or "keyboard hidden"; no other control's name. Focus stays on the field: one more double tap reopens the keyboard in that field, and the record is unchanged (no chip, radio or kind changed) | v1.3.0: focus moves to the first item in view (details: "Not selected. Stress"); mutation build: same |
| KR2 Close paths | Details and one single-line screen, TalkBack on | As KR1, but close with the keyboard's hide key, then with Done where the field has it | As KR1 | v1.3.0 for the same path |
| KR3 No scroll event | Each screen | Accessibility event log (tool of QA's choice) across Back from set-up near the end | Zero TYPE_VIEW_SCROLLED from the page's scroll container within 3 s | v1.3.0: at least one |
| KR4 Page stays (sighted) | Each screen, API 36 and API 26, 100% portrait | Field bounds from a layout dump before Back and after the keyboard is gone | Bounds identical (±1 px) | v1.3.0: field moves down by more than 0 |
| KR5 Band colour | Details and Log medication, dark theme, light theme, Log medication from attack mode (dark room) | Screenshot after closing; sample the band and the page background at the top of the same screenshot | Every band pixel equals the page background; no pixel brighter than it | Run the same check on the keyboard-open screenshot's keyboard area: it must fail |
| KR6 Nothing to tap | Details | After closing, layout dump at the place the keyboard's hide key was | No clickable or focusable node there; a tap there changes nothing | v1.3.0: a clickable node (content that slid in) at the same place, by dump only, no tap |
| KR7 Room goes by scrolling | Each screen | After closing, swipe up slowly by about a third of the band, then by more than the band, then scroll to the end | The field moves with the finger; the band shrinks by the same amount; after the second swipe no band; at the end the page ends with its normal bottom padding, no band | Mutation: a room that never shrinks shows the band again at the end |
| KR8 TalkBack walk | Details, 200% landscape | After closing, swipe forward from the note, then back several items | Forward: "Delete migraine", then the end of the page; the room is never a stop. Back: D178 order, no jump | none needed |
| KR9 Reopen | Each screen | After closing, tap the field again | Keyboard opens; the page does not jump; cursor line at least 8 dp above the keyboard (EC-UX-39), single-line fields per U1 | none needed |
| KR10 Recreate | Details and Edit medication, band visible | Rotate, rotate back; background then `am kill` and return from recents; leave and reopen the screen | No band after each; note or form text kept; nothing cut | none needed |
| KR11 200% | Each screen, portrait and landscape 411 and 360 dp | Set-up near the end, close | KR4 holds; band not taller than the keyboard was; field, input line and supporting line fully visible | none needed |
| KR12 Remove animations | Details and Delete all data | As KR4 with animator scales 0 | KR4 holds; no frame with the page lower | none needed |
| KR13 No inset | Details | Hardware keyboard on the emulator; floating keyboard and split screen if reachable | No band, no change on close | an unreachable row is "not reachable", not a pass |
| KR14 Messages | Details, band visible | Trigger a message; also close the keyboard while a message shows | The message's position equals its position without a band (D180 (4)); it does not cover Back | none needed |
| KR15 Delete all data, D7 | Delete all data, both row cases | Type the word, Done | The field does not move; the field and "Delete everything" are visible together; no divider when the page end is in view | v1.3.0 for the field bounds (KR4) |
| KR16 Far from the end | Details, long page, note focused mid-page with more than a keyboard height below | Back | No band appears; nothing moves; TalkBack as KR1 | none needed |

Restore every device setting afterwards: font scale, rotation, animator scales, TalkBack, keyboard.

## 9. Engineering notes (not binding on the code)

- Delete all data's visible part is set by the row and `behindKeyboard`, not by `barsAndKeyboard` padding before the
  scroll. The room there needs the growth of the visible part from that layout, not from an inset alone.
- Edit medication and Log medication create their `ScrollState` inline; the room needs the same state.
- The room is a modifier on the scrolling column, not a child. Both medication columns use `spacedBy(12.dp)`, and a
  child would add a 12 dp gap even with zero room.

## 10. Findings outside #238 (to reproduce before anyone files them)

1. A message in card mode that leaves while the page is at its end grows the visible part from the top. The page may
   be pulled the same way, and TalkBack may move its focus. Not reproduced.
2. A split-screen resize that grows the window at the page end: same mechanism, not keyboard-driven, so the room does
   not cover it. Not reproduced.
3. Content that shrinks at the page end (an error line removed) clamps the scroll the same way. Pre-existing for every
   scrolling page. Not reproduced.

## 11. What I could not judge without a device

1. How a band larger than half of a short landscape window reads at 200% font: whether people take it as "the page
   ended" or as "something failed to load". The three screenshots I saw (`migrely-work/238/shots/`) do not show the band
   at all: in each, the content still runs below the visible part. One shot with the page end and the band in view, dark
   and light, is needed before merge.
2. Edit medication at 100% with the Name field scrolled out of view after closing: whether sighted people miss it. One
   swipe brings it back, but a real session would show whether that swipe happens.
3. Whether TalkBack versions other than TalkBack 16, and Samsung's TalkBack build, react to the keyboard close itself
   (not to a scroll) by moving focus. If they do, the room does not help, and that would be a different bug.
4. Whether any frame on API 26 to 29, where the inset changes in one step, draws the page lower for one frame before the
   room arrives. KR4 measures the end state; a screen recording at 60 fps would show a single frame.
