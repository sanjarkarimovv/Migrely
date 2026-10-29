# Tasks

## 1. Page

- [ ] 1.1 Helpers `urgent_help()`, `reviewed_block()` and `medical_ld()`. Move `/headache-diary/` onto them (D2).
  Verify: `/headache-diary/` text is unchanged apart from the new link.
- [ ] 1.2 `medication_overuse()` with `MOH_REVIEWED`, the table and the Play button. Add it to `PAGES` (D1).
- [ ] 1.3 Links from `/headache-diary/` and `/about/` (D4).
- [ ] 1.4 `.prose table` style (D3).

## 2. Docs

- [ ] 2.1 `website/README.md`: the medical pages and their review dates.

## 3. Verify

- [ ] 3.1 Static checks: titles, descriptions, canonicals, links, campaigns and JSON-LD.
- [ ] 3.2 Headless Chrome behind the CSP: no violations, and no sideways scroll at 320 px in light, dark and 200%
  text.
- [ ] 3.3 Every medical sentence compared with ICHD-3 8.2, NICE CG150 and the NHS.
- [ ] 3.4 `openspec validate add-website-moh-page --strict`.
- [ ] 3.5 Verification record.

## 4. Review and archive

- [ ] 4.1 Independent review. Fix the findings and record them.
- [ ] 4.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge.
