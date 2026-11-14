# Tasks

## 1. Wording (done on the branch by aso-seo-specialist, f033ddbf and before)

- [x] 1.1 Site strings, alt texts, JSON-LD and `/llms.txt` per `medical-t65.md` and `legal-t65.md`. Verify: `python3 website/build.py` exits 0.
- [x] 1.2 New image names `01-log-v2`, `02-sky-v3`, `04-insights-v2`, `og-v2`; `images()` strips `-vN`. Verify: `ls website/public/assets/img`.
- [x] 1.3 Listing, privacy policy, data-safety and store art. Verify: `git diff origin/develop...HEAD --stat -- docs/release`.
- [x] 1.4 Privacy policy effective date 14 November 2026, HTML regenerated. Verify: `python3 docs/release/make_policy_html.py` and `grep -c "14 November 2026" docs/release/privacy-policy.html website/public/privacy/index.html`.

## 2. Open before deploy

- [ ] 2.1 `/privacy-check/` (`website/build.py` about line 675) still says "so nothing you enter can leave your phone through Migrely". Change it to "so the app itself cannot send what you enter over the internet" (aso-seo-specialist; scenario "The privacy claim"). Verify: the phrase scan in 3.1 finds no "can leave your phone through".

## 3. Verify

- [ ] 3.1 Phrase scan of the built site text (tags stripped) for "calm", "quiet", "while you rest", "internet access", "nothing leaves", "can leave your phone through", "for good", "migraines share", "spot trigger": the only hits are the promo transcript (v3.3) and "at a calm moment" on `/privacy/` (review prompt timing).
  - 2026-11-14 at fbc31bf7 + this commit: 5 hits; 3 in the promo transcript, 1 "at a calm moment", and 1 open: `/privacy-check/` "can leave your phone through Migrely" (task 2.1).
- [x] 3.2 `openspec validate update-site-wording-truth --strict`.
- [ ] 3.3 `medical-reviewer` re-reads the final diff.

## 4. Later, outside this change

- Promo v3.4 and its transcript (content-creator).
- The Play listing edit and store art upload after the 1.2.1 review result and the owner's go.
- Settings privacy string, Maestro assertion, slide 06 re-capture (1.3.0, T63).
