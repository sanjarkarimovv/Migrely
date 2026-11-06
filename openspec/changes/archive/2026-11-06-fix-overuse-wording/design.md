# Design

## Context

The "Last 30 days" section sits in History's month summary. A person opens it to see how the month looks, often tired or
in pain, on one hand. The row they read is a count next to a number, and that pairing decides what they think the number
means. "Limit" says it is a line they may go up to. That is wrong: the counts come from the diagnostic criteria for
medication-overuse headache (ICHD-3 8.2), and the NHS's own advice for painkillers is lower than they are (no more than 2
days a week, quoted on our `/medication-overuse-headache/` page, D141).

The row is built in `OveruseIndicator.kt` from a plural (`indicator_row_*`) and, when `row.reached` is true (`days >=
limit`), a wrapper (`indicator_at_limit` or `indicator_over_limit`). `indicator_reached` shows once after the rows when any
row is reached. Home's soft card body is `overuse_body_soft`, which took the number from `thresholdDays`. The domain, the
counting and the colour stay as they are.

## Goals and non-goals

**Goals:**

- No overuse number shows before it is reached.
- No word near an overuse number reads as a safe, allowed or recommended amount.
- A reached row says so in words, not by colour alone.
- Calm, short, no diagnosis, no order.

**Non-goals:** anything in the proposal's out of scope, such as the numbers, the counting and the colour.

## Final copy

`medical-reviewer` gave PASS on exactly this text. Any changed word goes back to it first. `ux-designer` was consulted, and
the disagreement was resolved by the handbook's risk order: safety over UX. A real clinician still has to sign (T31).

| String | Old text | Final text |
|---|---|---|
| `indicator_row_simple` one | `Simple painkillers: %1$d day (limit %2$d)` | `Simple painkillers: %1$d day` |
| `indicator_row_simple` other | `Simple painkillers: %1$d days (limit %2$d)` | `Simple painkillers: %1$d days` |
| `indicator_row_triptan` one | `Triptans: %1$d day (limit %2$d)` | `Triptans: %1$d day` |
| `indicator_row_triptan` other | `Triptans: %1$d days (limit %2$d)` | `Triptans: %1$d days` |
| `indicator_row_other` one | `Combination or other: %1$d day (limit %2$d)` | `Combination or other: %1$d day` |
| `indicator_row_other` other | `Combination or other: %1$d days (limit %2$d)` | `Combination or other: %1$d days` |
| `indicator_row_multiple` one | `All attack medication: %1$d day (limit %2$d)` | `All attack medication: %1$d day` |
| `indicator_row_multiple` other | `All attack medication: %1$d days (limit %2$d)` | `All attack medication: %1$d days` |
| `indicator_at_limit` | `%1$s, at the limit` | Removed, see `indicator_row_reached` |
| `indicator_over_limit` | `%1$s, over the limit` | Removed, see `indicator_row_reached` |
| `indicator_row_reached` (new) | None | `%1$s. Doctors watch for %2$d or more days a month.` |
| `indicator_reached` | `At this many days, medication can make headaches more frequent. Talk to your doctor about it.` | `Taking medication this often, month after month, can make headaches more frequent. It\'s worth talking to your doctor.` |
| `overuse_body_soft` | `Taking them on %1$d days or more a month can lead to more headaches. Worth keeping an eye on.` | `Taking them often can lead to more headaches. Worth keeping an eye on.` |
| `overuse_body_doctor` | `Taking them this often can make headaches more frequent. It\'s worth talking to your doctor.` | Unchanged |
| `chronic_notice` | `15 or more migraine days in %1$s. That\'s a lot. Talk to your doctor about it.` | Unchanged for 1.1.0 |
| `ReportDocumentMapper` label (Kotlin, PDF, line 108) | `30-day spans at the medication-overuse limit` | `30-day spans reaching an ICHD-3 8.2 medication-day threshold` |

As the user reads it:

- Below: "Simple painkillers: 6 days"
- One day: "Triptans: 1 day"
- Reached: "Triptans: 10 days. Doctors watch for 10 or more days a month."
- Under any reached row: "Taking medication this often, month after month, can make headaches more frequent. It's worth
  talking to your doctor."
- Home, soft: "Painkillers on 13 of the last 30 days" and "Taking them often can lead to more headaches. Worth keeping an eye
  on."

### Why this text

| Point | Reason |
|---|---|
| No number before it is reached | A number beside a lower count still reads as room left. 9 days in 30 is already above the NHS "2 days a week" advice |
| "or more", not "+" | TalkBack's speech of "+" is unproven, and the app already says "or more" |
| "days a month" | It gives the unit. "A month" is the sources' unit, while the app counts a rolling 30 days (D45) |
| The reached row and the line together | This pairing is what makes the row pass. The line says what the number is about, and `indicator_reached` shows whenever any row is reached |
| "month after month" | The criterion also needs overuse for more than 3 months. The line says this without a number |
| No diagnosis, no order | "It's worth talking to your doctor" matches the Home card and replaces the order "Talk to your doctor about it" |
| Calm | No "warning", "overuse" or "too many" in the app's rows. Amber and the line do the rest |
| Reached is in words | The second sentence is part of the row's text, so it is spoken and it survives colour blindness (WCAG 1.4.1) |
| Singular and plural | Only "%1$d day" and "%1$d days" change with the count. The number is never below 10, so "days a month" needs no plural |
| 200% font | The longest row grows from 57 to 74 characters ("All attack medication: 16 days. Doctors watch for 10 or more days a month."). `Text` has no `maxLines`, so it wraps. The walk confirms it |
| TalkBack | One `Text`, read in order |

### Rejected

| Text | Why not |
|---|---|
| ", reached" | It says the state, not what was reached, and with no number shown earlier it has nothing to point to |
| "10+" | TalkBack's speech of "+" is unproven |
| "(doctors watch for 15)" before the number is reached | A number beside a lower count reads as room left |
| "Doctors watch for this" | It gives neither the number nor the unit at the moment they apply |
| "6 of 15 days" | Reads as a quota with 9 days left |
| "(threshold 15)" in the app | Hard for someone in pain, and it does not say whose. Fine in the PDF, where a doctor reads it |
| "(guideline 15)", "(recommended 15)" | Reads as advice to go up to 15 |
| "(reference 15)", "(normal under 15)" | Reads like a lab range where below is healthy |
| "(worth a doctor's look at 15)" | Implies that below 15 is not worth a look |
| "(overuse from 15)" | "Overuse" next to their own count reads as a verdict on the person |

## Decisions

### D1. No number until it is reached

Any number beside a lower count reads as room left, whatever word sits between them. So rows below the number show the
count only, and the soft card states no number. This costs an early comparison (see "Risks") and deliberately changes
issue #135's expected result and D59. Safety comes before UX in the handbook's risk order.

### D2. Reached or not reached picks the string

The old `when` had three arms: not reached, equal, and greater. It goes. The criterion is "N or more", and the text no
longer tells equal from greater. `row.limit` is passed only to the reached string:

```kotlin
val counted = pluralStringResource(row.category.indicatorRow(), row.days, row.days)
Text(
    text = if (row.reached) {
        stringResource(R.string.indicator_row_reached, counted, row.limit)
    } else {
        counted
    },
```

The colour and the condition for `indicator_reached` stay as they are.

### D3. Internal names stay, and one dead field goes

`row.limit`, `OveruseStatusRowDomainModel.limit` and `reached` are not visible, and they stay in use. Renaming them touches
three layers and their tests to no user's benefit. The comments that quote or describe the old text
(`OveruseIndicator.kt` lines 29–30 and 42) get updated so nobody copies them. The new string name carries no "limit".

Once the argument at `MedicationPromptCard.kt:265` goes, nothing reads `thresholdDays` on the warning. It would remain as
dead data through two layers and a `@Parcelize` model, so it is removed from `OveruseWarningPresentationModel`,
`MedicationPromptPresentationMapper`, `MedicationPromptUiModel.OveruseWarning` and `MedicationPromptUiMapper`, along with
the preview and tests. The domain's `OveruseCategoryDomainModel.thresholdDays` stays: it decides the levels.

### D4. The PDF has its own wording

A doctor reads the label, as does the patient who carries it. "ICHD-3 8.2" names the source, "medication-day" says what is
counted, and "an" fits because a span can reach any of the several thresholds. "Reaching" matches what `ReportCalculator`
does, which "at" did not. The label is a Kotlin literal in `ReportDocumentMapper`, because the PDF's text is not a resource.
The fact row's label column wraps (`ReportPageLayout.row`), so 60 characters instead of 44 at most adds a line.

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
| Home soft card | `overuse_body_soft` | States the number before it is reached | In this change |
| History line | `indicator_reached` | An order, and no mention of time | In this change |
| Home cards | `overuse_title_*`, `overuse_body_doctor` | The person's own count, no threshold | Unchanged |
| History | `chronic_notice` | Shown only once reached | Unchanged for 1.1.0 |
| PDF | `A month with 15 or more migraine days` | A fact about a count | Unchanged |
| Notifications | `notification_*` in `app` | No overuse text | Unchanged |
| Medication list | `message_limit_reached`, spec's "at the limit" | List cap of 50, not medical | Unchanged |
| Attack tags | `message_tag_limit` | "Your list is full", not medical | Unchanged |
| Onboarding | `onboarding_goal_painkillers`, "Take fewer painkillers" | The person's own goal, with no number | Unchanged |
| Store listing | "the number of days a month linked to medication-overuse headache" | Good wording | Unchanged |
| Website `/about/` (out of scope) | H2 "Where do the medication limits come from?" (`website/build.py:546`) | Same word, in a heading | Note for `aso-seo-specialist` |
| `marketing/social/posts/01-painkiller-days.md:63` (out of scope) | Quotes "All attack medication (limit 10)" | Quotes the old text | Note for `content-creator` |
| Website MOH page | "not safe limits" | Correct use: it denies the reading | Unchanged |

## Edge cases checked

| Area | Result |
|---|---|
| States | Empty, first use and past month: no section, unchanged. Offline: always. Free and Premium: the indicator and the cards stay free, the PDF stays Premium. During an attack: unchanged |
| Time | The counting does not change, so zone, clock, midnight and overlap are unaffected. "A month" in the text against a rolling 30 days is covered in "Risks" |
| Data | Plural "1 day" is a scenario. Backup and restore hold no such text. Upgrade from each shipped version: nothing stored changes. A PDF made before the update keeps its old label, which cannot be helped |
| Lifecycle | No new state. `MedicationPromptUiModel.OveruseWarning` is `@Parcelize` and loses a field. Rotation and process death in one version are unaffected; the walk covers them |
| Access | 200% font and landscape are a scenario. TalkBack is a scenario. Reduced motion and right-to-left do not apply (D148) |
| Purchase | No change |

## Risks

- **The early comparison is gone.** After "Got it" on the soft card, someone at 9 triptan days sees "9 days" with nothing
  beside it until the number is reached. This is accepted: a number there reads as room left, and safety comes first.
- **"A month" against a rolling 30 days.** "A month" is the sources' unit, while the section counts the last 30 days. The
  heading "Last 30 days" sits right above it, and the difference is not one a person acts on.
- **The longest row is 74 characters.** It wraps to more lines at 200% than the old one did. The walk checks it.
- **Removing the dead field touches two more layers.** The compiler catches every reader. The mapper test stops checking
  15, 10, 10, 10 through this path, so task 2.2 confirms that other tests still pin those numbers.
- **The word is still in stored images.** Screenshots and promo video were not inspected, and they may show "limit" (see
  `tasks.md`).
