# Tasks

- [x] 1. Feasibility with sources (Cloudflare docs read 2026-11-23): zone analytics, Pages Functions metrics and pricing, `_redirects`, Direct Upload. Verify: design.md table.
- [x] 2. BRD: D196, EC-WEB-03 changed, EC-WEB-27 to EC-WEB-29, row 59. Verify: `git show origin/develop:docs/BRD.md`; D-number scan of all branches (highest in range D195, taken by `feature/add-german-localization`).
- [x] 3. `/android-t2/` page, `T2_*` constants, `T2_COUNT`, `check_t2_pages()`. Verify: `python3 website/build.py`.
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
- [ ] 10. **Not merged before deploy day (lead, 2026-11-23).** On deploy day the main session sets the policy's effective date in one commit, merges into `develop` (settling any `feature/policy-word-of-mouth` conflict in number order), archives, and the owner deploys from that merge the same day. Reason: a routine deploy from `develop` in between would carry a policy dated before the counter sentence, or a button without its Function.

## Verification record (aso-t2, 2026-11-23, base origin/develop 82df348f)

- `python3 website/build.py`: exit 0 before and after. `sha256` of all 48 files in `website/public` before and after: the only differences are the new `android-t2/index.html` and `assets/img/qr-ig-ads-test2-v1.svg` (`robots.txt` is unchanged since the `Disallow` was dropped); `android/index.html` is `22fab668...` in both.
- `T2_COUNT = False`: build exit 0, `play.google.com` twice, both buttons the exact link.
- Local `wrangler pages dev` (wrangler 4.158.0), `migrely-work/android-t2/curl-go.txt`: GET (`curl -s -o /dev/null -w '%{http_code} %{redirect_url}'`) gives `302` and a redirect URL byte-equal to the Play link; a GET with Cookie, Referer and a query string gets 302 and no `Set-Cookie` (0 lines); HEAD, POST and PUT all get 302 (no method branch); `/go/other` is 404; the static page has no `Location`.
- QR: decodes (OpenCV, Chrome render at 488 px) to the exact link; the test-1 QR does not (negative control); the generator reproduces the test-1 QR byte for byte.
- Layout (iframe wrapper, headless Chrome): first button bottom at 334, 325 and 325 px for 360x640, 320x568 and 412x915; no horizontal overflow. `/android/`'s own first button is at 345, 362 and 358, so it was above the fold too; its second button (855 px) is the control that fails a "bottom at most 500 px" test.


- `controls.sh` (prints the HEAD it ran against; `controls-final.txt`): 27 PASS, 0 FAIL. It covers: Function URL drift; reviewer mutations (a) a header read on a line with `//`, (b) destructured headers with UA branching, (c) 301 with 302 only in a comment, (d) `request.body`, (e) an import, plus (f) a method branch; flag on with a second Function; flag off with a Function left; flag off with a `/go/` link on the page; `/go/` on `404.html` and on `llms.txt`; new words; words above the button; script; new-tab link; path mismatch; a third `/go/` link; Premium line dropped; footer disclaimer dropped; policy without the counter sentence; policy still saying "analytics"; `SITE_DEPLOY` with the old date, a future date, an unreadable date (clean message) and today's date (accepted).

**Not checked:** a real Instagram in-app browser and the Play app after the 302; Cloudflare's live metrics (nothing is deployed); the production headers of the Function (local only); the words by `medical-reviewer`, `legal-finance-analyst` and `code-reviewer`.
