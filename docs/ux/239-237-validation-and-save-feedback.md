# #239 and #237: a form problem is heard and seen; a stored log replaces the error

- Owner: `ux-designer`. Date: 2026-11-18. Release: 1.3.2 (tracker T75).
- Base: `origin/develop` 76c38c38 (v1.3.1 merged). Code read only; no device, no emulator.
- Issues: #239 (S3, accessibility: Edit medication, Save with an empty Name is silent and the error is off screen) and
  #237 (S3: Log medication, "Couldn't save. Your phone's storage is full." stays over "Medication taken" after a later
  successful log). Both pre-existing (#237 reproduced on 1.2.1; #239 was not run on 1.2.1).
- Related records: BRD D178 (1), (4), (5); D180 (2) and ux U1; D181 (6) with EC-UX-49, EC-UX-50, EC-UX-51; D189 (#238
  keyboard room); `product-manager`'s planned D190 (#237) and D191 (#239). My earlier notes: F5 in
  `migrely-work/t63/ux-keyboard.md`, A6 in `migrely-work/t66/ux-spec.md`.
- Scope: design only. No new component. Two new strings, only if D190 rule 2 (failed Undo) ships (section 4.4).

## 1. Decisions

1. **#239. After Save (or Add and log) finds a problem in a field, the person hears the problem once, the field and its
   error line come into view, and the soft keyboard stays as it was.** Input focus moves to the field with the problem
   when a keyboard is in use: a hardware keyboard (as today, D181 (6)), or the soft keyboard already open on another
   field. With the soft keyboard closed in touch mode, focus does not move, because a focused field would open it.
2. **One rule for every form with a submit button.** It covers Edit medication (Add and Edit), Log medication's
   first-medication form (normal and dark room) and Add past migraine. Add past migraine already announces its time
   errors. Only its focus and scroll part is new, and I recommend deferring that part (section 3.4).
3. **#237. I agree with `product-manager`'s D190 rules 1 and 2 and add three things:** the message goes in the same frame
   that the logged page appears, with no fade; the person hears only the logged sentence; and a failed Undo says
   "Still logged." instead of "Couldn't save." (section 4.4).
4. **Newest result wins, on these forms too.** A new result of Save or Add and log clears any message from an earlier
   attempt on the same screen. A success does this (D178 (1), D190), and so does a field problem. The field error is
   then the only error on screen, so it is never under a message and never one of two errors at once.

## 2. The moment of use

**#239.** Someone adds or renames a medication: during an attack from Log medication's first-use form in the dark room,
or later from My medications. They cleared the name, or typed one they already have, and tapped Save with the keyboard
still up. Save is at the bottom and Name at the top. With the keyboard open, the page cannot show both. On v1.3.1 the
error appears under Name, off screen. A sighted person sees nothing happen and taps Save again. A TalkBack user hears
nothing at all (#239). Someone who is slow to think and holds the phone in one hand reads "nothing happened" as "the app
is broken" and gives up on logging.

**#237.** Someone logs a dose during an attack. The first tap fails because storage is full. They free space and tap
again. The intake is stored, but the old "Couldn't save." stays on top and covers "Ibuprofen 400 mg logged at 14:20".
Someone who is foggy believes the latest message and logs again. The diary then holds a duplicate dose, and that dose
counts toward the day count linked to medication-overuse headache. This is a data-integrity problem, not a cosmetic one.

## 3. #239: field problems after a submit

### 3.1 What the code does today (develop 76c38c38)

| Screen | Field problems | Spoken | In view | Focus |
|---|---|---|---|---|
| Edit medication | Name: empty or spaces ("Enter a name."), duplicate ("You already have %1$s."), archived name, edit only ("%1$s is in your archive. Add it as a new medication to bring it back."). Dose over 20 characters ("Keep the dose to 20 characters."), which the field's limit makes nearly unreachable | No. The error is the field's supporting text, not a live region. Material also sets the field's error semantics to its default "Invalid input" | No scroll | Name or Usual dose in keyboard mode only (`rememberProblemFocus`). In touch mode, and so with TalkBack, nothing |
| Log medication, first use | Name: empty ("Enter a name."). A duplicate only in a race | No (same as above) | No scroll | None in any mode. Add and log parks focus, and nothing moves it to Name |
| Add past migraine | Started or Ended: "This time is in the future.", "The end must be after the start."; the overlap card | **Yes.** The error line under the row and the card text are polite live regions outside the row | The card takes the place of Add migraine, so it is in view. A row error is not scrolled into view | Keyboard: none for a row error; Started after "Change the times" |
| Custom tag dialog, Delete all data | None: Add is disabled while the name is blank, and Delete everything stays disabled until the word is typed. Delete all data's failure line is a polite live region | n/a | n/a | n/a |

**Only one field problem at a time.** `SaveMedicationUseCase` checks Name first, then the dose, then duplicates, and
returns one result. The screen therefore never shows two field errors. "Several errors" can only mean an older message
plus a new field error, and rule F9 removes that case.

### 3.2 Rules (Edit medication, Log medication first use; Add past migraine per 3.4)

Terms. *Field region*: the field's outline including its floating label, down to the bottom of its supporting (error)
line. *Visible part*: the scroll area below the top bar and above the keyboard, or above the navigation bar when the
keyboard is closed. *Keyboard mode*: Android's input mode after a hardware key press (Tab, arrows, Enter).

| # | Rule |
|---|---|
| F1 | **One result, one field.** The error shows at the first field from the top that has a problem (Name before Usual dose). Its outline and supporting line turn to the error colour and the counter is replaced by the error text, as today. |
| F2 | **Scroll into view.** If the field region is not fully inside the visible part, the page scrolls by the smallest amount that brings it in. Name is above Save, so it ends up at the top of the visible part. If it is already fully visible, nothing moves. If the region is taller than the visible part (200% landscape with the keyboard open), U1's priority applies: input line first, then the error line, then the label. This is the bring-into-view scroll the app already makes when focus moves by Tab (D181 (6), #238 R4). It adds no new kind of motion. With Remove animations on, it is one step. |
| F3 | **Focus.** In keyboard mode, input focus moves to the field with the problem (unchanged from D181 (6)). In touch mode with the soft keyboard open: if the problem field already has focus, nothing changes. If another field has focus (Usual dose), input focus moves to the problem field and the keyboard stays open, so the next letter goes where the fix is. In touch mode with the soft keyboard closed, focus does not move. The text is never selected or changed by the move, and the cursor goes to the end. |
| F4 | **Keyboard.** The app never opens or closes the soft keyboard because of a problem. Open stays open, closed stays closed. A touch user opens it by tapping Name, which F2 has brought into view. |
| F5 | **Announcement, exactly one, caused by the app.** If F3 moved input focus, the field's own reading carries the error (F6), and nothing else announces it. If F3 did not move focus (the common touch and TalkBack case), the error text is announced once as a polite announcement, the same as Add past migraine's time errors and Delete all data's failure line. TalkBack's focus stays where it was (on Save). The app cannot move it. |
| F6 | **The field reads its real error.** When TalkBack, Switch Access or Voice Access reaches the field, it says the label, the value and the error text, in the service's own order, for example "Name, edit box, error: Enter a name." It never says Material's default "Invalid input". The error is one stop with the field: swiping right from Name never reaches a separate stop that says the error again. |
| F7 | **Every failed attempt is heard.** A second Save with the same problem, and nothing edited, announces again (F5). The code already counts arrivals for focus (`problemArrivals`). |
| F8 | **Typing is quiet.** Editing the field clears the error, as today (`onFormChange` → `problem = null`). Neither the error leaving nor the counter coming back is announced. TalkBack's own key echo is the only speech while typing. An announcement for every key would be loud and exhausting during an attack. |
| F9 | **Newest result wins.** When a field problem arrives, any message on the screen (storage full, "Something went wrong", "You can keep up to 50 medications…", "Pick a time up to now.") is cleared in the same frame, silently. It came from an earlier attempt, and Name sits under the message's place at the top, so leaving the message would cover the very error the person must fix (the message is drawn over the content, D178 (4)). If storage is still full, the next Save after the name is fixed shows the storage message again. |
| F10 | **No haptic, no sound, no colour flash.** Field problems have no haptic today, and that stays. Only the outline and the supporting line change colour, in one step, as today. |
| F11 | **Free and Premium, first use, attack mode.** The same everywhere. In the dark room (Log medication opened from attack mode) the error colour is the dark theme's `#E6A4A4`, relative luminance 0.46, which is below the dark room's action text `#BCC8D3` (0.57). Contrast on `#08090C` is about 9.7:1. It is not the brightest thing on screen, so nothing changes. |
| F12 | **Pending.** While Save waits, it is disabled (as today). The result arrives, then F1 to F9 apply in one frame. A press while pending does nothing and announces nothing. |

### 3.3 What each person experiences

**TalkBack, touch (the #239 case).** Keyboard up, Name focused and empty, TalkBack focus on Save, double tap:

1. Nothing is saved. Save is disabled for a moment and enabled again.
2. TalkBack says "Enter a name." (F5, polite). Nothing else is spoken by the app.
3. The page scrolls to Name, above the keyboard (F2). Input focus is still in Name, so the person can type the name at
   once on the soft keyboard. Swiping back reaches Name, which reads its error (F6).
4. On some setups TalkBack moves its own focus to the first item in view after a scroll (the #238 mechanism, seen
   headless 3/3 and windowed 0/9). Here that item is Name, so TalkBack then reads "Name, edit box, error: Enter a name."
   That second reading comes from TalkBack, not from the app. It lands on the field the person has to fix, and a double
   tap there only puts the cursor in Name. Unlike #238, nothing can change by accident. I accept it, and section 5 says
   how QA records it.

**Hardware keyboard (Tab).** Tab to Save, Enter. Focus moves to Name with its focus ring. Name and its error are in
view (F2, via the focus). With TalkBack also on, the reading of the focused field says the error once (F5, F6). No
soft keyboard.

**Sighted touch.** Tap Save. The page scrolls so Name and the red "Enter a name." sit just under the top bar. The
keyboard stays as it was. If it is open and the cursor was in Usual dose, the cursor is now in Name (F3).

**Switch Access and Voice Access.** As touch. The field's name and error follow F6. Voice Access users can say
"Tap Name".

### 3.4 Per screen

**Edit medication (Add and Edit).** All of F1 to F12. With the keyboard open:

```
 v1.3.1 after Save (silent)          1.3.2 after Save
+--------------------------------+   +--------------------------------+
| <-  Edit medication            |   | <-  Edit medication            |
|   ( ) Preventive               |   |  +--------------------------+  |
|   Type                         |   |  | Name                     |  |   red outline
|   (o) Simple painkiller        |   |  |                          |  |
|   ( ) Triptan                  |   |  +--------------------------+  |
|   ...                          |   |   Enter a name.                |   red supporting line
|  [ Usual dose (optional)    ]  |   |   Kind                         |
|  [          Save            ]  |   |   (o) For an attack            |
|================================|   |================================|
|  keyboard                      |   |  keyboard (unchanged)          |
+--------------------------------+   +--------------------------------+
  Name and "Enter a name." are          spoken once: "Enter a name."
  above, off screen
```

The duplicate ("You already have Ibuprofen.") and archived-name problems behave the same. A dose problem goes to Usual
dose. No new string.

**Log medication, first-use form (normal and dark room).** All of F1 to F12. This is new for this screen: v1.3.1 has no
focus, scroll or announcement for its Name error in any mode. Add and log keeps its park-on-press (D181 (6)). In
keyboard mode, the park is followed by the move to Name in the same result frame (F3), so the next Tab does not start
from the top. The 64 dp targets of the dark room are unchanged. The field region is scrolled under the dark room's top
bar, on the dark room background, with no light frame at any point.

**Add past migraine.** It already meets F5 to F8 for its row errors: the error line is a polite live region outside
the clickable row, and the row does not repeat it. F2 (scroll the row into view) and F3 (keyboard focus to the row with
the error) are new for it. **Recommendation: verify only in 1.3.2, and put the F2/F3 part in the backlog.** Reasons: the
pickers prevent most time errors, the rows sit near the top of a short page, the announcement (the S3 part of #239)
already works, and a change here adds risk to a patch release. `product-manager` decides scope.

**Out of the rule by prevention.** The custom tag dialog (Add disabled while the name is blank) and Delete all data
(Delete everything disabled until the word matches; its failure line is already a polite live region). Unchanged.

### 3.5 Options compared (#239)

Risk order from the handbook: safety, privacy, data loss, store policy, money, accessibility, UX, speed.

| Option | Verdict |
|---|---|
| **A. Announce once, scroll into view, keyboard as it was, focus only where a keyboard is in use (chosen)** | Fixes the silence for TalkBack and the "nothing happened" for sighted users. Never covers the error with the soft keyboard and never opens it uninvited (D181 (6)'s reason) |
| B. Move input focus to Name in every mode | With the keyboard closed, touch focus on a field opens the soft keyboard over the page, the cost D181 (6) rejected. In the #239 case Name already has focus, so a focus request does nothing and TalkBack stays silent. It fixes nothing there |
| C. Announce only, no scroll | A TalkBack user hears the problem, but a sighted person still sees nothing happen, and a low-vision TalkBack user cannot find the line |
| D. Disable Save while Name is empty | Prevents only the empty case, not duplicate or archived names. A disabled Save explains nothing ("Save, disabled"), is skipped by Tab, and its dim colour is hard to read in the dark room. Rejected |
| E. Use Android's announce-for-accessibility call | Deprecated on Android 16. A polite live region does the same job and is what the app already uses |
| F. Show the error as a top message | Moves the error away from the field to fix. Messages cover the top of the content (D178 (4)), and a timeless message would need a Dismiss. Rejected |

## 4. #237: a stored log replaces any message (Log medication)

### 4.1 Root cause (agrees with `product-manager`)

A stored intake posts no message. It swaps the page to the logged state (`LoggedContents`, whose "… logged at …" line
is a polite live region). `MedicationMessages` reacts only to `Message` events, so a timeless error stays.
D178 (1) assumed Log medication's success had its own message, and it does not. The message host draws over the
content's top, which is exactly where the logged line sits (QA shot `msg-a5-r2.png`). Compose leaves a fully covered
node out of the accessibility tree, so the line is neither seen nor heard.

### 4.2 Rules

| # | Rule |
|---|---|
| L1 | **What clears.** A stored intake (a medication button, or Add and log on the first-use form) clears any message on the screen: storage full, "Something went wrong. Please try again.", "Pick a time up to now.", "Removed. Nothing was logged.", "You can keep up to 50 medications…". Agrees with D190 rule 1. |
| L2 | **When.** On the Logged result only, never on the tap. A tap whose write fails keeps the old message and replaces it with the new one ("newest message wins", D178). Agrees with D190. |
| L3 | **Same frame, no fade.** The message leaves in the same frame that the logged page replaces the list or the form. There is no 200 ms fade-out for this exit. The page swap is already instant, so one change happens on screen, not two. D178 (5) also does in-place changes without a fade. Reason: a fading message still covers the logged line, and if the line enters the accessibility tree only after its text has changed, its live region may never fire. |
| L4 | **What is heard.** Only the logged sentence, once, polite: "Ibuprofen 400 mg logged at 14:20" (existing `log_logged`). No "Saved.", no "Dismissed", no haptic, no new string. The tapped button is gone and focus is cleared and moves nowhere (D181 (6), EC-UX-50), so nothing interrupts the sentence. |
| L5 | **What is seen.** Title "Medication taken", the logged line, Done, Undo. Nothing over the line. Dark room: the same in the dark room palette. No frame shows the message and the logged line together. |
| L6 | **After that.** Rotation and process death keep the logged page and bring no message back (the message state was cleared and is saveable). Undo works as today. |
| L7 | **Failed Undo** (D190 rule 2, PM's code-read neighbour). If removing the intake fails, the logged page stays with its Undo, and the message says what is true: see 4.4. The list comes back only after a removal that succeeded, with "Removed. Nothing was logged." as today. |

### 4.3 Other screens with "error, then a success that is not a message"

I checked every message host's success path at 76c38c38:

| Screen | Success shows as | Old error after success |
|---|---|---|
| Log medication | Logged page (content) | **Stays: #237.** Fixed by L1 to L5 |
| Edit medication, Add past migraine | Screen closes | Gone with the visit (D178 (2)). QA saw this in the same run |
| Home, History, Reminders, Attack details | "Saved." replaces an error (D178 (1)) | Replaced |
| My medications | Its own message ("Medication removed", "Undone.") | Replaced |
| Your data | Its own message (backup saved, restored, added…) | Replaced |
| Doctor report | Share sheet; the error is cleared silently (D178 (1)) | Cleared |
| Delete all data | App restarts | n/a |
| Home's medication effect card | The card goes; it shows no messages | n/a |

So Log medication is the only screen with this pattern. Rule F9 (3.2) gives the two medication forms the same
"newest result wins" for field problems.

### 4.4 Failed Undo copy (only if D190 rule 2 ships in 1.3.2)

On the logged page, the existing "Couldn't save. Your phone's storage is full." would recreate #237 in reverse. The
message covers the logged line, and "Couldn't save" on a page titled "Medication taken" reads as "the dose was not
logged". A foggy person logs it again. The message for a failed Undo must lead with the state the person needs:

| Key (suggested) | Text | Tone, duration |
|---|---|---|
| `message_intake_kept_storage_full` | Still logged. Your phone's storage is full. | Error, no duration (as storage full today, EC-STO-01) |
| `message_intake_kept_generic` | Still logged. Something went wrong. Please try again. | Error, 8 s with "Time to take action" (as the generic error today) |

These are calm, short, and blame no one. They make no health claim, so `medical-reviewer` is not needed. Length is
close to the existing ones (43 and 53 characters against 44 and 39), so at 200% the message grows by at most
one line. Where these are used, everything else stays as today: assertive for error tone, Dismiss, D182 timing. If
rule 2 is deferred, these strings are deferred with it.

## 5. Acceptance rows for `qa-engineer`

Builds: candidate = the 1.3.2 fix branch at the SHA under test; negative control = `v1.3.1`. API 36 (TalkBack 16,
VERBOSE log) for all rows. Also API 26 for V1, V2, L1. 100% portrait unless the row says otherwise. Storage full set-up
as in #237 (`fallocate`). Restore font scale, rotation, animator scales, TalkBack, keyboard, locale and free space
afterwards.

**Counting speech.** From TalkBack's VERBOSE log, count the utterances in the 5 s after the double tap that contain the
error text. *App-caused* utterances are the live-region announcement and the reading of a field that received focus
from the app (F3). A reading that follows TalkBack's own focus move after a scroll is *TalkBack-caused*. QA records it
separately, and it is not a fail if it is the Name field's reading.

### #239

| ID | Where | Steps | Expected on candidate | Negative control (v1.3.1) |
|---|---|---|---|---|
| V1 Silent Save (the issue) | Edit medication | Issue steps: clear Name, keyboard up, TalkBack on Save, double tap | Within 3 s exactly one app-caused utterance containing "Enter a name". The record is unchanged | 0 utterances in 8 s (QA r2–r4) |
| V2 In view | Edit medication, then Log medication first use | As V1 without TalkBack; layout dump after 1 s | Name's field region and "Enter a name." fully inside the visible part (below the top bar, above the keyboard); keyboard still open; input focus in Name | Name off screen (`tbe83s-r4.png`) |
| V3 Keyboard closed | Edit medication | Clear Name, close the keyboard, scroll to Save, tap Save | Keyboard stays closed; no field focused; region in view (V2); one announcement with TalkBack on | Off screen and silent |
| V4 Cursor in Dose | Edit medication | Clear Name, tap Usual dose (keyboard open), Save, type "a" | Input focus in Name, keyboard never closed, Name reads "a", dose unchanged | Focus stays in dose; "a" goes into the dose |
| V5 Hardware keyboard | Edit medication, then Log medication first use | Hardware keyboard: Tab to Save (or Add and log), Enter; repeat with TalkBack on | Focus ring on Name, region in view. With TalkBack: one app-caused utterance, the field's reading, containing "Enter a name" and never "Invalid input" | Edit: focus moves already (D181 (6)), so record what TalkBack says. Log first use: focus stays parked, silent |
| V6 Twice | Edit medication | V1, then double tap Save again without editing | A second app-caused utterance after the second tap | Silent both times |
| V7 Typing quiet | Edit medication | After V1, type "Ib" in Name | Only TalkBack's key echo; no "1/60", "2/60" or field re-reading; the error leaves silently | Guard. Mutation: a live region on the whole field must make this row fail |
| V8 Swipe walk | Edit medication, after V1 | Swipe from Back to Save, then back | Name is one stop that says label, value and "Enter a name" once; no extra stop repeating it; never "Invalid input" | Record v1.3.1's reading (expected: "Invalid input" and/or the supporting text) |
| V9 Duplicate | Edit medication (Add) | Name = an existing medication, Save | As V1 and V2 with "You already have Ibuprofen." | Silent, off screen if scrolled |
| V10 Message first (F9) | Edit medication | Storage full, Save (storage message shows); free space; clear Name; Save | Storage message gone in the same frame as the Name error appears; Name and error in view and not covered; one announcement | Message stays over Name |
| V11 Dark room | Log medication from attack mode, no medications | Add and log with an empty name, TalkBack on | V1 and V2 hold; no frame brighter than the dark room background outside the field; error pixels not brighter than `#BCC8D3` luminance | Silent, no scroll |
| V12 200% | Edit medication and Log first use, portrait and landscape (411 and 360 dp tall) | As V2 | Region in view; in landscape with the keyboard, the input line and then the error line take priority (U1); nothing cut off | none |
| V13 Remove animations | Edit medication | Animator scales 0; as V2 | Scroll in one step; V2 holds | none |
| V14 Pending | Edit medication | Double tap Save twice fast | One result; one announcement per result | none |
| V15 Add past migraine | Add past migraine | Produce "The end must be after the start." if reachable | Announced once (unchanged); record whether the row is in view at 200% landscape. If it is not, that is evidence for the backlog item, not a 1.3.2 fail | Same as candidate (guard) |
| V16 RTL, light theme | Edit medication, Arabic or Hebrew locale; light theme | As V2 | Error line at the start edge, mirrored; error colour readable on the light background (≥ 4.5:1) | none |

### #237

| ID | Where | Steps | Expected on candidate | Negative control (v1.3.1) |
|---|---|---|---|---|
| L1 The issue | Log medication, list | #237 steps 1–4 | Storage message gone; logged line in the accessibility tree within 1 s; TalkBack says "Ibuprofen … logged at …" once and nothing about storage; `medication_intake` +1 | Message stays at 2 s and 8 s; line not in the tree (QA `msg-a5-r2.png`) |
| L2 Same frame | As L1 | `screenrecord` at 60 fps across the second tap | No frame shows the message together with the logged page; no fade frames | Message persists |
| L3 First use | Log medication, no medications | Storage full, Add and log; free space; Add and log | As L1 | Message stays |
| L4 Info messages | Log medication | (a) Undo a log ("Removed. Nothing was logged."), log again within 8 s; (b) pick a future time ("Pick a time up to now."), fix the time, log within 8 s | Message gone with the logged page, as L1 | Message over the logged line until its 8 s end |
| L5 Failure keeps it | Log medication | Storage full, tap twice | The message stays (replaced in place); no logged page; count unchanged | Same (guard; mutation: clearing on tap fails it) |
| L6 Dark room | Log medication from attack mode | As L1 | As L1, in the dark room palette, no light frame | Message stays |
| L7 Rotate | After L1 | Rotate, rotate back; `am kill` and return | Logged page, no message | none |
| L8 Keyboard focus | After L1 with a hardware keyboard | Tab | Focus cleared after the log; first Tab goes to Back, then Done (EC-UX-50) | Guard |
| L9 Failed Undo (if D190 rule 2 ships) | Log medication | Log; fill storage; Undo | Logged page stays with Undo; "Still logged. Your phone's storage is full."; `medication_intake` unchanged; free space, Undo: list returns with "Removed. Nothing was logged." | List returns with "Couldn't save. Your phone's storage is full." while the intake is still stored |

## 6. Accessibility notes per screen

- **Edit medication.** Order unchanged (EC-UX-49). Targets unchanged (48 dp; Save 56 dp). The error line is part of
  the Name stop (F6). No message can sit over Name when it carries an error (F9).
- **Log medication.** Order unchanged (EC-UX-50). The dark room keeps 64 dp. The logged sentence is the only speech
  after a log (L4). The first-use form's Name error follows F1 to F12.
- **Contrast.** Error colours unchanged: dark `#E6A4A4` on `#08090C` is about 9.7:1 (F11). The light theme's error
  colour is checked by V16 on a device.
- **200% and landscape.** No fixed heights are added. V12 covers it.
- **RTL.** Nothing new to mirror. The supporting line follows the field's start edge.
- **Reduced motion.** The only movement is the existing bring-into-view scroll (F2), which is one step with animations
  removed. #237 removes a fade (L3) and adds none.
- **Messages never cover controls, and no tap falls through.** F9 and L1 remove messages. They never add one. Whatever
  tap protection the host gives a leaving message today stays as it is. Under the message's place there is nothing that
  writes: on the logged page it is the logged line (text), on Edit medication it is the top of Name (a tap there only
  puts the cursor in Name), and on Log medication's first-use form it is "Taken at" (a tap opens the time picker,
  which writes nothing and can be cancelled). A late tap that falls through therefore changes no record. QA confirms this with one layout dump
  per screen in L1 and V10.

## 7. Out of scope

- The message host's place over the top of the content on every screen (D158, D178 (4)), beyond the clears in F9 and
  L1.
- Message wording other than 4.4. Saying "limit" in labels is a separate known item.
- Add past migraine's F2/F3 delta (recommended for the backlog, 3.4).
- Undo on Attack details' own intake rows; the medication effect card.
- Whether Save should be shown as disabled for an empty name (rejected in 3.5 D).
- Write paths that finish while the screen is recreated (a result that arrives during process death).

## 8. Engineering notes (not binding)

- Material 3 1.4.0's `OutlinedTextField` adds `defaultErrorSemantics(isError, "Invalid input")` after the caller's
  modifier, and the supporting text is merged into the field (`mergeDescendants`). So (a) the field's error semantics
  need the real problem text (F6). (b) A `liveRegion` on the supporting `Text` would make the whole merged field a live
  region and announce every keystroke (V7). The F5 announcement needs its own node or mechanism that fires once per
  result arrival (F7), with no repeat stop for swipes (F6).
- `rememberProblemFocus` already runs once per arrival. F2 and F3 belong in the same effect: a `BringIntoViewRequester`
  on the field region for touch, and `requestFocus` under F3's conditions. Whether the soft keyboard is open can be read
  from the IME inset (`WindowInsets.isImeVisible`), which the keyboard room already uses.
- L3: clear `shown` in `MedicationMessages` on `MedicationEventUiModel.Logged`, and on `NotSaved` for F9 (both screens
  share the host), without the host's leave fade for this exit.

## 9. What I could not judge without a device

1. Whether TalkBack 16, and Samsung's TalkBack build, speak the polite announcement before or after their own reading
   of Name when the scroll moves TalkBack's focus (3.3 step 4). The result decides whether the person hears "Enter a
   name" once or twice. V1 records it.
2. Whether TalkBack announces Save's short disabled-then-enabled flip during a fast InvalidName result ("dimmed"). If it
   does, that sound comes before the error, and the engineer may skip the disabled state for results that do not touch
   the database.
3. Whether moving input focus from Usual dose to Name with the soft keyboard open (F3) makes Gboard flicker or switch
   layout. V4 shows it.
4. The failed-Undo message at 200% portrait on a 360 dp wide phone: whether it reaches Done. The logged line under it is
   accepted. Done must not be covered.
5. How the scroll in F2 looks to a person with motion sensitivity at 100%: the page moves up to its top in one smooth
   step, about half a screen. I judge it acceptable because the person caused it and it shows them the fix, but I have
   not watched it.
