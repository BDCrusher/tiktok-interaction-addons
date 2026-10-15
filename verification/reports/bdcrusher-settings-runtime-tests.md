# BDCrusher's Settings — 0.3.0-beta.1

Date: 2026-10-08. Bundle SHA-256 `e605211763e9ea56cef031290b3e27fae8fe0a560d0790f0c62db15a27170e78`.

42 JVM/Robolectric tests pass on simulated API 28: 30 Collections, 10 Sound, 2 shared settings hub. NewApi minSDK 23 lint reports no issues for all three runtime modules. Test helpers stand in for HushFeed's public UI contracts; combined patching checks separately resolve the actual external binary. No Android device/emulator, Manager import or pixel validation occurred.

Collections navigation tests now traverse root → BDCrusher's Settings → Collection popup, assert Sound is absent from that test's independent classpath, return through both levels and check the root badge. Existing count dialogs, reset isolation, large-font layout and recreation tests still pass.

Sound navigation opens the same root hub with only Sound selected, asserts Collections is absent, and opens Sound button actions. It no longer takes an INTERFACE section argument. Existing preferences, policy, page and recreation checks pass.

Shared hub tests use explicitly test-only optional adapters to exercise both rows, one idempotent root entry, independent navigation and fresh/restored pages that discover modules without root initialization. Saved counts survive recreation. Fake adapters are under src/test only and absent from the injected payload.

Commands: `:extensions:collections:testDebugUnitTest :extensions:sound:testDebugUnitTest :extensions:settings:testDebugUnitTest`, and `lintDebug` for each module. Device saving, native ViewModel lifecycle, metadata availability, touch dispatch, TalkBack, theme pixels and custom HushFeed options remain unverified.
