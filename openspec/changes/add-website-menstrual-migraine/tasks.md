# Tasks

## 1. Page

- [ ] 1.1 `menstrual_migraine()` with `MENSTRUAL_REVIEWED`, the day table, the Play button and the shared helpers
  (D1, D2). Add it to `PAGES`.
- [ ] 1.2 Link from `/headache-diary/`.
- [ ] 1.3 `website/README.md`: the new page in the check list and the medical pages note.

## 2. Verify

- [ ] 2.1 Static checks, headless Chrome (CSP, 320 px in light, dark and 200% text), and a stable rebuild.
- [ ] 2.2 Every medical sentence compared with ICHD-3 A1.1.1/A1.1.2, NICE CG150 and the Migraine Trust. Every app
  sentence compared with D91 and the app's strings.
- [ ] 2.3 `openspec validate add-website-menstrual-migraine --strict`, and the verification record.

## 3. Review and archive

- [ ] 3.1 Independent review. Fix the findings and record them.
- [ ] 3.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge.
