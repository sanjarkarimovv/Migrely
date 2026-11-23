# Tasks

- [x] 1. Feasibility with sources (Cloudflare docs read 2026-11-23): zone analytics, Pages Functions metrics and pricing, `_redirects`, Direct Upload. Verify: design.md table.
- [x] 2. BRD: D196, EC-WEB-03 changed, EC-WEB-27 to EC-WEB-29, row 59. Verify: `git show origin/develop:docs/BRD.md`; D-number scan of all branches (highest in range D195, taken by `feature/add-german-localization`).
- [x] 3. `/android-t2/` page, `T2_*` constants, `T2_COUNT`, `check_t2_pages()`, `robots.txt` rule. Verify: `python3 website/build.py`.
- [x] 4. `website/functions/go/android-t2.js`. Verify: `wrangler pages dev` and `curl -I`.
- [x] 5. QR code `qr-ig-ads-test2-v1.svg`. Verify: decode equals the Play link; the test-1 QR is the negative control.
- [x] 6. `play-links.md` row and `website/README.md` deploy rule.
- [x] 7. Negative controls for every new check (`migrely-work/android-t2/controls.sh`).
- [x] 8a. Legal verdict 2026-11-23 applied: Free/Premium block restored, policy edit (DRAFT wording, verbatim), `Disallow` dropped, README and docstring reworded, policy gate in the build.
- [x] 8b. **Owner's yes, 2026-11-23, in chat, verbatim: "Ha"**, to legal's two questions together: the counter sentence goes into the privacy policy; the counter keeps a number only, ever; if a licensed lawyer later says consent is needed, fall back to the direct Play link (`T2_COUNT = False`). It is not a deploy approval. Commit 95133d14's message says "not yet approved"; the approval came the same day.
- [x] 8c. `medical-reviewer` 2026-11-23: PASS, 33/33 rows (`migrely-work/android-t2/medical-verdict-2026-11-23.md`). Optional risk (c) taken: the footer's "not a medical device" is now required by `check_t2_pages`, with a negative control.
- [x] 8d. Deploy-day date gate: `SITE_DEPLOY=1 python3 website/build.py` stops while the counter is on and the policy's effective date is earlier than 23 November 2026; a normal build is unaffected. Both directions tested.
- [ ] 8. `medical-reviewer` reads the exact page words. `legal-finance-analyst` reads them and gives the verdict on the counter.
- [ ] 9. Independent review (`code-reviewer`), findings fixed.
- [ ] 10. Archive, PR into `develop`, merge. No deploy: the owner's click, then the device test.

## Verification record (aso-t2, 2026-11-23, base origin/develop 82df348f)

- `python3 website/build.py`: exit 0 before and after. `sha256` of all 48 files in `website/public` before and after: the only differences are the new `android-t2/index.html` and `assets/img/qr-ig-ads-test2-v1.svg` (`robots.txt` is unchanged since the `Disallow` was dropped); `android/index.html` is `22fab668...` in both.
- `migrely-work/android-t2/controls.sh`: 14 mutations, each makes the build exit non-zero with the expected message (URL drift, 301, IP read and log, second Function, flag off with a Function left, new words, words above the button, script, new-tab link, path mismatch, third `/go/` link, Premium purchase line dropped, policy without the counter sentence, policy still saying "analytics").
- `T2_COUNT = False`: build exit 0, `play.google.com` twice, both buttons the exact link.
- Local `wrangler pages dev` (wrangler 4.158.0): `GET` and `HEAD` 302, `Location` equals the Play link byte for byte, `POST` 405, `/go/other` 404, a request with `Cookie` and `Referer` gets no `Set-Cookie`. `wrangler pages functions build --output-routes-path`: `include` is only `/go/android-t2`.
- QR: decodes (OpenCV, Chrome render at 488 px) to the exact link; the test-1 QR does not (negative control); the generator reproduces the test-1 QR byte for byte.
- Layout (iframe wrapper, headless Chrome): first button bottom at 334, 325 and 325 px for 360x640, 320x568 and 412x915; no horizontal overflow. `/android/`'s own first button is at 345, 362 and 358, so it was above the fold too; its second button (855 px) is the control that fails a "bottom at most 500 px" test.

- Final controls run: 17 PASS lines, 0 FAIL (`controls-final.txt`): the 14 above plus the footer control and both `SITE_DEPLOY` cases.

**Not checked:** a real Instagram in-app browser and the Play app after the 302; Cloudflare's live metrics (nothing is deployed); the production headers of the Function (local only); the words by `medical-reviewer`, `legal-finance-analyst` and `code-reviewer`.
