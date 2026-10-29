# Tasks

## 1. Page

- [ ] 1.1 `reviewed_block(…, disclaimer)` with `MEDICAL_DISCLAIMER` and `VA_DISCLAIMER` (D2). Verify that the
  medical pages' HTML is unchanged.
- [ ] 1.2 `va_migraine_log()` with `VA_REVIEWED`, the DC 8100 table, the notice, the Play button and the `WebPage`
  JSON-LD (D1, D3). Add it to `PAGES`.
- [ ] 1.3 `website/README.md`.

## 2. Verify

- [ ] 2.1 Static checks, headless Chrome (CSP, 320 px in light, dark and 200% text), and a stable rebuild.
- [ ] 2.2 The regulation, DBQ and FAQ quotes compared word for word with the sources. The app sentences compared with
  `AttacksCsvMapper`, the strings and D108.
- [ ] 2.3 `openspec validate add-website-va-log --strict`, and the verification record.

## 3. Review and archive

- [ ] 3.1 Independent review. Fix the findings and record them.
- [ ] 3.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge.
