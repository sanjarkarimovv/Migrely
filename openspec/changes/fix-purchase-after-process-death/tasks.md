# Tasks

## 1. Test first

- [x] 1.1 `EntitlementRulesTest`: a sheet launched at or after the last answer makes Play due at once; one launched before it waits for the interval
- [x] 1.2 `PremiumCheckerTest`: the start a minute after a check, with a sheet launched since and its purchase completed, asks Play, acknowledges the purchase and shows the trial; with the payment pending it shows pending. The tests fail before the fix.

## 2. Implement

- [x] 2.1 `EntitlementRules.isDue(entitlement, now, launched)`
- [x] 2.2 `PremiumChecker.current()` passes the stored launch
- [x] 2.3 BRD EC-BIL-13

## 3. Verify

- [ ] 3.1 Gate: `assembleDebug test ktlintCheck detekt lint`
- [ ] 3.2 `openspec validate fix-purchase-after-process-death --strict`
- [ ] 3.3 On emulator-5570 with the simulated store (QA #105's steps): a purchase completed while the app was closed shows at the next start within 15 minutes, acknowledged, with the trial end; a pending one shows "Payment pending"

## 4. Review

- [ ] 4.1 Independent review (`superpowers:code-reviewer`)
