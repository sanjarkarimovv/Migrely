# Tasks

## 1. Privacy page

- [x] 1.1 Add "Read the full privacy policy" with the written address at the end of the Privacy page
- [x] 1.2 Open https://migrely.health/privacy/ through the platform's URI handler, and ignore a phone with no browser
- [x] 1.3 Bring BRD D119 into the release

## 2. Verify

- [x] 2.1 Gate: `assembleDebug test ktlintCheck detekt`
- [ ] 2.2 On emulator-5570: the link is at the end of the Privacy page, a tap opens the browser at the policy, and
  TalkBack reads it as a button
