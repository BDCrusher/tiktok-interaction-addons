# Local simulated-runtime checks — 2026-10-03

Command: `gradlew.bat :patches:generateMetadata :extensions:collections:testDebugUnitTest :extensions:collections:lintDebug --console=plain`.

Result: **PASS — 23 tests, no failures/errors/skips**, Robolectric API 28 on JDK 17. Android NewApi lint with minSDK 23: **No issues found**. These are not emulator/phone tests.

| Suite | Passed | Coverage |
| --- | ---: | --- |
| PagerTest | 10 | Fill/dedup/order, exhausted lists, error/late response, repeated cursors, timeout, disable/sufficient data, destroyed Activity, exact count/More, equal cached lists with distinct hasMore, initial loader exception. |
| PanelTest | 6 | Height cap and scroll-release cancellation, stock short-list gesture pass-through, first MOVE after hold, pressed overlay reset, large-font/RTL height measurement simulation, clipped hit testing with nonzero window-to-screen offset. |
| RuntimeTest | 7 | Count/More boundaries, preferences/reset, settings-entry idempotence, theme fallback/contrast, pause retaining preferences, custom AMOLED/light preservation/invalid color, settings input validation/save. |

Detailed JUnit XML and HTML are local generated outputs under `extensions/collections/build/test-results/testDebugUnitTest/` and `build/reports/tests/testDebugUnitTest/` respectively. A new test initially needed the Robolectric main looper drained before clicking a dialog button; that test setup was fixed and all tests rerun successfully. Production dialog uses the normal Android OnShow listener.

Limits: placeholder HushFeed helpers and a fake native collection transport are used in these simulations. Actual HushFeed/TikTok references are separately checked by the combined DEX verification. Native save callbacks, account data, TalkBack, rotation, theme switching, ART loading, and accidental-save absence need device validation.
