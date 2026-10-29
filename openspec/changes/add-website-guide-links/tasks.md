# Tasks

## 1. Links

- [ ] 1.1 `GUIDES` (D1, D6), the footer navigation (D3, D4) and the home section (D2).
- [ ] 1.2 `check_orphans()` in `main()` (D5), and the README.

## 2. Verify

- [ ] 2.1 Inbound link counts before and after; the orphan check stops a build with an unlinked page.
- [ ] 2.2 Static checks, headless Chrome (CSP, 320 px in light, dark and 200% text), and a stable rebuild.
- [ ] 2.3 `openspec validate add-website-guide-links --strict`, and the verification record.

## 3. Review and archive

- [ ] 3.1 Independent review. Fix the findings and record them.
- [ ] 3.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge, and a new deploy zip.
