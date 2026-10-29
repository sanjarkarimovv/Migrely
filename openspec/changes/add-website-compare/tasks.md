# Tasks

## 1. Page

- [ ] 1.1 `COMPARE_APPS`, `COMPARE_REVIEWED` and `compare()` (D1–D3), with `.apps` styles (D4). Add it to `PAGES`.
- [ ] 1.2 A link from `/about/`, and the README.

## 2. Verify

- [ ] 2.1 Every card fact traced to the research file's "safe to publish" list, and none from "do not publish".
- [ ] 2.2 Static checks, headless Chrome (CSP, 320 px in light, dark and 200% text), and a stable rebuild.
- [ ] 2.3 `openspec validate add-website-compare --strict`, and the verification record.

## 3. Review and archive

- [ ] 3.1 Independent review. Fix the findings and record them.
- [ ] 3.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge.
