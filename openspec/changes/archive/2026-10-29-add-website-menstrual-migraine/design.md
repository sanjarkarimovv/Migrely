# Design

## Context

This page follows changes 17 and 18, and reuses `urgent_help()`, `reviewed_block()`, `medical_ld()` and the
`.prose table` style. It draws on these sources, read on 2026-10-29:
- **ICHD-3 A1.1.1 and A1.1.2:** appendix (research) criteria. The window is days −2 to +3, there is no day 0, and
  withdrawal bleeds from combined pills or cyclical HRT count as menstruation. A prospective diary is recommended for
  research but is not mandatory for diagnosis.
- **NICE CG150:** 1.2.5 (predominantly in the window, in 2 of 3 consecutive cycles), 1.2.6 (a diary for at least 2
  cycles), 1.3.30 (combined hormonal contraceptives with aura) and 1.3.31 (specific triptans, which this page does
  not name).
- **The Migraine Trust,** from the launch's checked fact pack: a diary for at least 3 months; fewer than 1 in 100 have
  pure menstrual migraine; as many as two-thirds of women with migraine may have period-related attacks; "your
  doctor can confirm".

The app's own window and wording come from BRD D91: the same 5 days; Insights is Premium; Track periods is optional.

## Decisions

### D1. Page structure

The H1 is "Menstrual migraine: the day −2 to +3 window". The page then runs:

1. **The short answer** (lead).
2. **What is menstrual migraine?** The ICHD-3 description, that these are appendix criteria, the withdrawal-bleed
   note and the Migraine Trust figures.
3. **How do I count the days?** The day-1 rule, then a two-column table with one row per day (D2).
4. **How long should I track it?** NICE's 2 cycles and the Migraine Trust's 3 months. What to record: period days
   and attacks. A link to the free template, which has a period column.
5. **Does contraception matter?** NICE 1.3.30, as general information, then "tell whoever prescribes your
   contraception that you have aura".
6. **How is it treated?** "There are options for predictable period-related migraine that does not respond to usual
   treatment; talk to your doctor." No drug or dose names.
7. **Is my period data private in Migrely?** No internet and no account, so it cannot be sent; where it lives; Track
   periods is optional; period marks are free; Insights (Premium) counts attacks that started in the window and shows
   how much of the time those days are, without drawing a conclusion.
8. **The Play button,** urgent help, Sources and the review block.

### D2. The day table

The table has one header row (Day: −2, −1, 1, 2, 3) and one row of labels (2 days before, the day before, first day
of bleeding, 2nd day, 3rd day). That is six narrow columns, which at 320 px at 200% text could be tight. Instead the
page uses a two-column table with one row per day ("Day −2" | "2 days before your period starts"), which wraps
safely.

## Risks

- **"Started" vs "happen".** The app's Insights counts attacks that *started* in the window. ICHD-3 says attacks
  *occurring* in it. The page describes the app as it is, and does not present its count as the criterion.
- **Sensitive data.** The privacy paragraph uses the same honest edges as `/privacy-check/`: Android's backup and
  exported files.
