# QA crash fixture (T83, #280)

Two tiny APKs, built without Gradle by `build.sh` (javac, d8, aapt2, zipalign, apksigner with the debug key):

| APK | Package | Does |
|---|---|---|
| `out/qa-crashview.apk` | `com.migrely.qa.crashview` | handles https/http and `market://` `ACTION_VIEW` and text/plain `ACTION_SEND`; throws in `onCreate` |
| `out/qa-okview.apk` | `com.migrely.qa.okview` | same filters; shows the link (a well-behaved browser with standard launch mode) |

Both log `QAFIX onCreate variant=… task=… data=… flags=0x…` before anything else.

Use: install, then make one the only https handler: `pm disable-user --user 0` the image's browser (and
`com.android.vending` for the Rate fallback) and the other fixture; check with
`cmd package query-activities --brief -a android.intent.action.VIEW -c android.intent.category.BROWSABLE -d https://migrely.health/privacy/`.
Restore: `pm enable` what you disabled, uninstall both fixtures, check the browser role holder (`cmd role get-role-holders android.app.role.BROWSER`).

Keep 130 s between two crashes of the fixture: inside `MIN_CRASH_INTERVAL` AppErrors takes its "crashed too many
times" branch instead of the normal one, and the row proves something else.

First used for T83 change A (`fix-warning-summary-and-outside-apps`, #280): on API 36 develop 476f7a27 lost Migrely's
screen to this crash and e9e0db66 kept it; on API 26 both lose it (EC-SHR-17). `gen-crash/` and `gen-ok/` hold the one
constant that differs between the two variants.
