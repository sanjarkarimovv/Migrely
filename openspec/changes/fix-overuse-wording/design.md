# Design

## Context

The "Last 30 days" section sits in History's month summary. A person opens it to see how the month looks, often tired or
in pain, on one hand. The row they read is a number next to another number, and the word between the two decides what
they think the second number means. "Limit" says it is a line they may go up to. That is wrong: the counts come from the
diagnostic criteria for medication-overuse headache (ICHD-3 8.2), and the NHS's own advice for painkillers is lower than
they are (no more than 2 days a week, quoted on our `/medication-overuse-headache/` page, D141).

The row is built in `OveruseIndicator.kt` from a plural (`indicator_row_*`, with the days and the number) and, when
`row.reached` is true (`days >= limit`), a wrapper (`indicator_at_limit` or `indicator_over_limit`). All this is text only:
the domain, the mapper and the colour stay as they are.

## Goals and non-goals

**Goals:**

- No word near an overuse number reads as a safe, allowed or recommended amount.
- The row still says, in words, when it has reached its number.
- Calm, short, no diagnosis, no order.

**Non-goals:** anything in the proposal's out of scope, such as the numbers, the counting, the colour and `row.limit`.

## Draft copy

**DRAFT.** This text has not been seen by `ux-designer` or `medical-reviewer`. The spec deltas quote the recommended text,
and `product-manager` updates them and D151 if the review changes it (task 1.2).

### Recommended text

| String | Old text | Proposed text |
|---|---|---|
| `indicator_row_simple` one | `Simple painkillers: %1$d day (limit %2$d)` | `Simple painkillers: %1$d day (doctors watch for %2$d+)` |
| `indicator_row_simple` other | `Simple painkillers: %1$d days (limit %2$d)` | `Simple painkillers: %1$d days (doctors watch for %2$d+)` |
| `indicator_row_triptan` one | `Triptans: %1$d day (limit %2$d)` | `Triptans: %1$d day (doctors watch for %2$d+)` |
| `indicator_row_triptan` other | `Triptans: %1$d days (limit %2$d)` | `Triptans: %1$d days (doctors watch for %2$d+)` |
| `indicator_row_other` one | `Combination or other: %1$d day (limit %2$d)` | `Combination or other: %1$d day (doctors watch for %2$d+)` |
| `indicator_row_other` other | `Combination or other: %1$d days (limit %2$d)` | `Combination or other: %1$d days (doctors watch for %2$d+)` |
| `indicator_row_multiple` one | `All attack medication: %1$d day (limit %2$d)` | `All attack medication: %1$d day (doctors watch for %2$d+)` |
| `indicator_row_multiple` other | `All attack medication: %1$d days (limit %2$d)` | `All attack medication: %1$d days (doctors watch for %2$d+)` |
| `indicator_at_limit` | `%1$s, at the limit` | Removed, see `indicator_row_reached` |
| `indicator_over_limit` | `%1$s, over the limit` | Removed, see `indicator_row_reached` |
| `indicator_row_reached` (new) | None | `%1$s, reached` |
| `indicator_reached` | `At this many days, medication can make headaches more frequent. Talk to your doctor about it.` | Unchanged (see "Notes for `medical-reviewer`") |
| `ReportDocumentMapper` label (Kotlin, PDF) | `30-day spans at the medication-overuse limit` | `30-day spans reaching the medication-overuse threshold` |

As the user reads it:

- Below: "Simple painkillers: 6 days (doctors watch for 15+)"
- One day: "Triptans: 1 day (doctors watch for 10+)"
- At the number: "Triptans: 10 days (doctors watch for 10+), reached"
- Past the number: "All attack medication: 12 days (doctors watch for 10+), reached"

### The one alternative for the row form

The row shows no number until it is reached, so it cannot read as room left.

| String | Alternative text |
|---|---|
| `indicator_row_*` one | `Simple painkillers: %1$d day` (and the same for the other three) |
| `indicator_row_*` other | `Simple painkillers: %1$d days` (and the same for the other three) |
| `indicator_row_reached` (new) | `%1$s, doctors watch for %2$d+` |

- Below: "Simple painkillers: 6 days"
- At or past: "Triptans: 12 days, doctors watch for 10+"

It is the strongest answer to "safe amount" and the shortest. It costs the early warning: after "Got it" on the Home card,
someone at 9 triptan days sees "9 days" with nothing to compare it to, and issue #135's expected result and D59 both show
the number. It also changes the requirement's meaning, and `indicator_row_reached` needs `row.limit` passed in. So it is
the fallback if `medical-reviewer` finds that any number shown early reads as room left.

### Why this text

| Constraint | How the recommended text meets it |
|---|---|
| No safe or permitted reading | "Doctors watch for 15+" names the number as something to watch for, not a line to stay behind. The risk that remains is in "Risks" |
| No diagnosis, no order | It says what doctors look for, not what the person has or must do |
| Calm | No "warning", "overuse" or "too many" in the row. Amber and the existing line do the rest |
| Reached is in words | ", reached" is part of the row's text, so it is spoken and it survives colour blindness (WCAG 1.4.1) |
| Singular and plural | Only "%1$d day" and "%1$d days" change with the count. The number is never below 10, so "10+" needs no plural |
| 200% font | The longest row grows from 57 to 63 characters ("All attack medication: 16 days (doctors watch for 10+), reached"). `Text` has no `maxLines`, so it wraps. The walk confirms it |
| TalkBack | One `Text`, read in order. "10+" should read as "ten plus". The walk records what the node holds; real speech needs a listen (task 6.3) |

### Rejected

| Text | Why not |
|---|---|
| "6 of 15 days" | Reads as a quota with 9 days left. This is the worst option |
| "(threshold 15)" in the app | Accurate, but a hard word for someone in pain, and it does not say whose. Fine in the PDF, where a doctor reads it |
| "(guideline 15)", "(recommended 15)" | Reads as advice to go up to 15 |
| "(reference 15)", "(normal under 15)" | Reads like a lab range where below is healthy |
| "(mark 15)", "at the mark" | Neutral but opaque, and an unexplained number gets read as a limit anyway |
| "(worth a doctor's look at 15)" | Implies that below 15 is not worth a look |
| "(overuse from 15)" | "Overuse" next to their own count reads as a verdict on the person |
| Keeping "at" and "over": ", past that" | "16 days" is already inside "15+", so "past" says nothing. ICHD-3 draws no line between them |

## Decisions

### D1. The number stays and says whose it is

The number is useful before it is reached: it gives the person a reason to raise it with a doctor early, even after "Got
it" on the Home card. So the fix changes the word, not the fact. The alternative above stays open for the reviewer.

### D2. One reached string

The old `when` has three arms: not reached, equal, and greater. The criterion is "N or more", and the text no longer tells
equal from greater, so the view becomes:

```kotlin
text = if (row.reached) stringResource(R.string.indicator_row_reached, counted) else counted
```

Keeping two resources with the same text would duplicate them, and keeping the name `indicator_at_limit` for a row past
the number would mislead. `row.limit` stays: the plural still needs it.

### D3. Internal names stay

`row.limit`, `OveruseStatusRowDomainModel.limit` and `reached` are not visible. Renaming them touches three layers and
their tests to no user's benefit. The one comment that quotes visible text ("at the limit" in `OveruseIndicator.kt`) gets
updated so nobody copies it. The new string names carry no "limit".

### D4. The PDF has its own word

A doctor reads the label, as does the patient who carries it. "Threshold" is the clinician's word and the one the spec
already uses. "Reaching" matches what `ReportCalculator` does (`days >= limit`), which "at" did not. The label is a Kotlin
literal in `ReportDocumentMapper`, because the PDF's text is not a resource. The fact row's label column wraps
(`ReportPageLayout.row`), so 54 characters instead of 44 at most adds a line.

### D5. Two scenario names keep the word

"Below the limit" and "At the limit" stay as scenario names, though their text changes. `openspec validate --strict` refuses
a MODIFIED block that drops a scenario the current spec has, and the `# Covers:` lines that `qa/e2e_coverage.py` reads use
these names. They are internal labels, like `row.limit`, and nobody using the app sees them. The new scenarios need
`# Covers:` lines of their own from QA.

## Surface sweep

Searched every `strings.xml`, Kotlin string literals (including the PDF mapper), the notification strings and
`docs/release/listing.md`. `git grep -E` with `\b` found nothing on this Mac, so the search was redone with `-P` and a
control that counts the known rows.

| Surface | Text | Finding | Action |
|---|---|---|---|
| Doctor report PDF | `30-day spans at the medication-overuse limit` | **Same fault, not in the issue** | In this change |
| Home cards | `overuse_title_*`, `overuse_body_soft`, `overuse_body_doctor` | No such word. See the note below | Unchanged |
| PDF | `A month with 15 or more migraine days` | A fact about a count | Unchanged |
| Notifications | `notification_*` in `app` | No overuse text | Unchanged |
| Medication list | `message_limit_reached`, spec's "at the limit" | List cap of 50, not medical | Unchanged |
| Attack tags | `message_tag_limit` | "Your list is full", not medical | Unchanged |
| Onboarding | `onboarding_goal_painkillers`, "Take fewer painkillers" | The person's own goal, with no number | Unchanged |
| Store listing | "the number of days a month linked to medication-overuse headache" | Good wording | Unchanged |
| Website `/about/` (out of scope) | H2 "Where do the medication limits come from?" (`website/build.py:546`) | Same word, in a heading | Note for `aso-seo-specialist` |
| `marketing/social/posts/01-painkiller-days.md:63` (out of scope) | Quotes "All attack medication (limit 10)" | Quotes the old text | Note for `content-creator` |
| Website MOH page | "not safe limits" | Correct use: it denies the reading | Unchanged |

## Notes for `medical-reviewer`

These do not use the word, so they stay in this change unless the review says otherwise:

1. **`indicator_reached`** ends with "Talk to your doctor about it.", an order, where the Home card says "It's worth talking
   to your doctor." BRD §11.9 says the text does not give orders. Should it match the Home card?
2. **`overuse_body_soft`**, "Taking them on %1$d days or more a month can lead to more headaches. Worth keeping an eye on.",
   may imply that fewer days carry no risk. It is the nearest thing left to a safe-amount reading.
3. **"Doctors watch for"** is a claim about clinical practice. ICHD-3 is what doctors use to classify, and the criterion also
   needs 15 or more headache days a month and more than 3 months of overuse. Is the phrase true enough for a row?
4. **The all-medication row** (ICHD-3 8.2.6) carries the same phrase. Is "10+" right there?

## Edge cases checked

| Area | Result |
|---|---|
| States | Empty, first use and past month: no section, unchanged. Offline: always. Free and Premium: the indicator stays free, the PDF stays Premium. During an attack: unchanged |
| Time | The counting does not change, so zone, clock, midnight and overlap are unaffected |
| Data | Plural "1 day" is a scenario. Backup and restore hold no such text. Upgrade from 1.0.0 to 1.0.2: text only, nothing stored. A PDF made before the update keeps its old label, which cannot be helped |
| Lifecycle | No new state. Text is rebuilt on every composition, so rotation and process death are unaffected |
| Access | 200% font and landscape are a scenario. TalkBack is a scenario. Reduced motion does not apply. Right-to-left does not ship (D148), though "10+" would need a check if it ever does |
| Purchase | No change |

## Risks

- **A number shown early can still read as room left.** "Doctors watch for 15+" is better than "limit 15" but does not erase
  that. The alternative does, at a cost. `medical-reviewer` decides.
- **"Doctors watch for" may be untrue or too loose.** See note 3.
- **"Reached" alone may be unclear.** Reached what? It sits right after "10+". `ux-designer` should read it in place.
- **"10+" and regexes.** `+` is a regex character in Maestro's `element:` matchers and `run_flow_env`, so QA must escape it
  as they do the brackets (`\\+`). An unescaped one would fail or match the wrong text.
- **The word is still in stored images.** Screenshots and promo video were not inspected, and they may show "limit" (see
  `tasks.md`).
