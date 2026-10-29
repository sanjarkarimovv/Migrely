# Design

## Context

The facts come from a research pass on 2026-10-29 that read each app's Play listing, permission list (INTERNET is
shown on Play as "full network access", per AOSP), Data safety page, own website, pricing and privacy policy. It kept a
"safe to publish" list (16 statements) and a "do not publish" list (15). The page uses only the first. The research
corrected two earlier beliefs:
- Migraine Log, which is open source, is on Google Play and asks for no internet permission, so Migrely is not the
  only such diary;
- Bearable requires an account.

Exodus could not be read.

Migrely's own facts: no account, no INTERNET permission, Data safety declares nothing collected or shared, and the
free and Premium features as on `/about/`. The US prices come from `docs/release/play-products.md`: $29.99 a year with
a 7-day trial, $4.99 a month, and $59.99 lifetime. Migrely is Android only, new, in English only, and has no reviews
yet.

## Decisions

### D1. Data, not prose

`COMPARE_APPS` in `build.py` is a list of apps. Each has a name, a maker, and a list of `(field, text)` rows from a
fixed set:
- platforms;
- account;
- internet permission;
- Play Data safety: shared;
- free;
- Premium;
- doctor report;
- does well.

It also has a list of `(label, url)` sources. A row whose fact was not verified is simply absent. Cards render as
`<article>` with an `<h3>` and a `<dl>`. Field names are `<dt>`; the text is escaped `<dd>`, with quotes kept
verbatim.

### D2. Page structure

1. The H1 is "Migraine diary apps for Android, compared on facts".
2. The lead: "We make Migrely, so we're not neutral…" It names the sources, the date, and that prices and features
   change.
3. Two short answers:
   - **"Which apps work without the internet?"**: Migrely and Migraine Log. The others request full network access.
   - **"Which apps need an account?"**:
     - Bearable and Migraine Monitor require one;
     - Migraine Buddy syncs with its server and links data to an account (its help center, quoted);
     - Nagi, Dark Logger, Migraine Log and Migrely need none;
     - Migraine Insight is not named, because it was not verified.
4. "The apps, one by one": Migrely first, then the others in alphabetical order.
5. A trademark note: names belong to their owners, and none of them endorses this page.
6. The Play button, Sources (the general sources: Play, AOSP's permission label) and the review block, with the
   disclaimer "not reviewed by the other apps' makers. Prices and features change; check each source."

### D3. Uniform fairness

Every competitor gets the same fields. Data safety shows only the "shared" headline as Play prints it. Privacy-policy
quotes appear only where the field needs them: Migraine Buddy's account sync, Nagi's offline statement and Dark
Logger's weather and push lookups. These are the same statements those apps make about themselves. Nothing is
called worse or better: the reader judges.

### D4. Styles

`.apps` is a single-column list of cards, reusing the `.features article` look. `dl` rows are a grid with `dt` at
weight 600. At 320 px, `dt` and `dd` stack.

## Risks

- **Stale facts.** Each card shows the date read, and the lead says to check the source. Re-check before
  `COMPARE_REVIEWED` changes.
- **A competitor objects.** Every fact is their own published statement, quoted where it matters, with a link. Remove
  or correct anything proven wrong.
- **Our own bias.** It is stated in the lead, Migrely's limits are on its card, and every competitor's strengths are
  listed.
