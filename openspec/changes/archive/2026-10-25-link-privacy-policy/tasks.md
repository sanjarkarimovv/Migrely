# Tasks

## 1. Privacy page

- [x] 1.1 Add "Read the full privacy policy" with the written address at the end of the Privacy page
- [x] 1.2 Open https://migrely.health/privacy/ through the platform's URI handler, and ignore a phone with no browser
- [x] 1.3 Bring BRD D119 into the release

## 2. Verify

- [x] 2.1 Gate: `assembleDebug test ktlintCheck detekt`
- [x] 2.2 On emulator-5570: the link is at the end of the Privacy page with the address under it; a tap sends
  `VIEW https://migrely.health/privacy/` to Chrome; the link is one clickable, focusable node 57 dp tall, exposed as a
  button like the app's other buttons
