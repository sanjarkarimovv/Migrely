# Tasks

## 1. Page

- [x] 1.1 `COMPARE_APPS`, `COMPARE_REVIEWED` and `compare()` (D1–D3), with `.apps` styles (D4). Add it to `PAGES`.
- [x] 1.2 A link from `/about/`, and the README.

## 2. Verify

- [x] 2.1 Every card fact traced to the research file's "safe to publish" list, and none from "do not publish".
- [x] 2.2 Static checks, headless Chrome (CSP, 320 px in light, dark and 200% text), and a stable rebuild.
- [x] 2.3 `openspec validate add-website-compare --strict`, and the verification record.

## 3. Review and archive

- [x] 3.1 Independent review. Fix the findings and record them.
- [x] 3.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge.

## Verification record

Run on 2026-10-29 at `feature/add-website-compare`, built from `develop` 566c5882.

- **Facts:** every card row was traced to the research file `marketing/research/2026-10-29-compare-facts.md` (outside
  the repo):
  - its "safe to publish" items 1–16 and the confirmed cells of its summary and price tables;
  - nothing from its "do not publish" list: no Exodus counts, no "only app without internet", no Migraine Buddy
    free-tier or export claims, no Migraine Insight account claim, no Dark Logger unlock contents, and no Nagi widget
    price.

  Two phrases with no source were removed before the commit: Bearable's "habits", and Migraine Insight's "among other
  features".
- **Migrely's card:** the prices come from `docs/release/play-products.md`. The free and Premium features match
  `/about/`, and the limits are stated.
- **Build:** the site builds with 11 sitemap pages, and two builds in a row leave `lastmod.json` unchanged.
- **Static checks:** nothing found. The checker now applies the UTM rule only to Migrely's own Play links;
  competitor links stay untagged.
- **Headless Chrome:**
  - 0 CSP violations.
  - No sideways scroll at 320 px in light, dark and 200% text. The cards stack `dt` over `dd` under 520 px.
  - Screenshots 26–27 are in `migrely-work/walks/website/`.
- **OpenSpec:** `openspec validate add-website-compare --strict` passes.
- **Not run:**
  - The live site.
  - A re-read of every competitor source today by a second reader. That is the review's job, below.

## Review

An independent review (`superpowers:code-reviewer`) re-read every competitor fact against its primary source on
2026-10-29, between 14:10 and 14:20. For permissions it used the data behind Play's own "App permissions" dialog. It
confirmed:
- every Data safety headline word for word;
- every account statement;
- the "full network access" claims and Migraine Log's empty permission list;
- the prices of Bearable, Migraine Buddy, Migraine Insight and Dark Logger;
- the download buckets, and every "does well" row.

It found 2 Critical issues, 6 Important and 9 Minor. All were fixed.

**Critical:**
- **C1, Nagi has no monthly plan.** "€4.99 / month" on its site is the €59.90 yearly price shown per month. The App
  Store lists only Annual and Quarterly. The card and the research file were corrected.
- **C2, "Which apps work without the internet?" implied that Nagi and Dark Logger don't**, although both say their
  diaries work offline. The heading became "Which apps have no internet permission?", and the page now credits their
  offline statements. The spec, design and BRD were updated to match.

**Important:**
- **I1:** Dark Logger's card quotes both sides of its policy: logs stored "solely in your device's secure local
  storage", plus the weather and push-token uses.
- **I2:** "Not a subscription" was iOS text. The card now says "Paid 'Pro' unlocks" with the Play price range.
- **I3:** Migraine Insight's Premium is "the pattern finder, and your logs shown on the calendar". Its free tier
  includes automated tracking and the logs as a list.
- **I4:** Migrely's row says "Declared to Google Play: none shared. The Play listing is not public yet".
- **I5:** every app shows only the "shared" headline, Migraine Log included.
- **I6:** every fact has a linked source. The Bearable homepage, browser/desktop and doctor-sharing pages, the
  Migraine Insight and Migraine Buddy homepages were added. Bearable's unlinked "Health Connect" and Migraine Log's
  iOS-only "A4/Letter table view" were dropped.

**Minor:**
- "The most Google Play downloads here (1M+)" replaces "the largest user base".
- Migraine Log reads "None mentioned; it has no network access", and its permission row cites Play and F-Droid.
- The trademark note names the apps and states non-affiliation.
- The AOSP sources are the manifest and the strings; the Play homepage was dropped as a source.
- The spec says "the same set of fields in the same order".
- Migrely gained a "Doctor report" row.
- The constants were reordered, and `NETWORK` became `COMPARE_NETWORK`.
- "Pro" is Dark Logger's own word for its paid tier.

**Re-verified after the fixes:**
- The static checks find nothing, and two builds in a row change nothing.
- 36 overflow measurements (12 pages; light, dark and 200% text) show 0 sideways scroll.
- 0 CSP violations on 5 pages, with a control page caught as before.
- `openspec validate --strict` passes.
