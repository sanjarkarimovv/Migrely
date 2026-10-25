# Tasks

## 1. Release build

- [x] 1.1 R8 and resource shrinking, `proguard-rules.pro`, and signing from `keystore.properties` with the debug
  fallback.
- [x] 1.2 Release smoke on the emulator; keep rules only for breakages found.

## 2. Icon

- [x] 2.1 The generator and SVG source, the adaptive icon with its monochrome layer, and the 512×512 Play icon; the
  unused template PNGs removed.

## 3. Store documents

- [x] 3.1 Listing, privacy policy (md and html), Data safety, content rating, health apps declaration.

## 4. Verify

- [x] 4.1 Gate, and the release permission check.
- [x] 4.2 Review of the build changes and the documents against the code.

## Verification record

- **Gate:** green. The release APK (4.18 MB, against 18.87 MB for debug) and the AAB both build, and
  `checkReleasePermissions` runs before each. The merged manifest holds only POST_NOTIFICATIONS,
  RECEIVE_BOOT_COMPLETED, WAKE_LOCK and the androidx receiver permission.
- **Signing:** checked both ways. With a throwaway keystore the APK was signed by it with no warning; without one, it
  was signed with the debug key and the warning showed.
- **Release smoke** on emulator-5570, with the app data restored to the baseline afterwards (dump md5 unchanged). Every
  flow worked with no crash and no keep rule needed:
  - cold start;
  - attack mode, details and ending;
  - a past migraine;
  - History;
  - medication, with "Did it help?" from the card and from the notification;
  - reminders;
  - JSON and CSV export, a Replace import, and restoring the copy;
  - the doctor report;
  - Insights;
  - themes and info pages;
  - Delete all data.

## Review (4.2)

An independent `superpowers:code-reviewer` pass checked the build, the icon, and every claim in the documents against
the code. Nothing Critical.

| Finding | Outcome |
|---|---|
| **I1** the bundle for Play skipped the permission check | fixed: it runs before `bundle<Variant>` too |
| **I2** the Play icon was 24-bit | fixed: 32-bit RGBA, written by the generator |
| **I3** Data safety's reasons overstated Play's guidance, and said exports use the share sheet | fixed: reasons rest on no transmission, the user-controlled OS backup (end-to-end from Android 9), and user-chosen exports through the file picker and the share sheet |
| **I4** versionName 1.0.0 was not tracked | moved to 13b, at launch |
| Minor | fixed: the Privacy line now says no unencrypted cloud copy exists on Android 9+; the policy names doses, welcome answers and the automatic copies, and says what uninstalling leaves in Google's backup; listing wording on the shortcut, undo and warning signs; Play skipping questions; design D3 and task text; ADR-09 and D9 marked superseded; `/app/release/` and `*.p12` ignored; a clear error for a missing keystore key; the icon generator writes in place; colour-scheme meta and links in the policy page; account type to confirm in 13b |
